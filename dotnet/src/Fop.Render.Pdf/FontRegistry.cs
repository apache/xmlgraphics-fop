// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

using System.Buffers.Binary;
using System.Collections.Concurrent;
using System.Text;

namespace Fop.Render.Pdf;

/// <summary>
/// A registry of caller-supplied TTF/OTF fonts, keyed by normalized family name + (bold, italic)
/// style. It is consulted (before the built-in Liberation fallback) by <see cref="FopFontResolver"/>
/// for embedding and, transitively, by <see cref="PdfSharpFontMeasurer"/> for metrics: because both
/// resolve the same family through the same resolver, the measured and embedded faces agree.
/// <para>
/// Each registration is assigned a unique, deterministic <em>face name</em> (e.g.
/// <c>FopFont_dejavuserif_b0_i0</c>). PdfSharp identifies embedded fonts by that face name, so the
/// registry doubles as the face-name -&gt; bytes store that <see cref="FopFontResolver.GetFont"/>
/// reads from.
/// </para>
/// <para>
/// The registry is thread-safe. Note the PdfSharp global-resolver caveat documented on
/// <see cref="FopFontResolver"/>: register fonts <em>before</em> the first <c>XFont</c> for a family
/// is created, because PdfSharp caches resolved faces process-wide.
/// </para>
/// </summary>
public sealed class FontRegistry
{
    private const string FacePrefix = "FopFont_";

    private readonly ConcurrentDictionary<FaceKey, RegisteredFace> facesByKey = new();
    private readonly ConcurrentDictionary<string, RegisteredFace> facesByName = new(StringComparer.Ordinal);

    /// <summary>The (normalized family, bold, italic) key under which a face is registered.</summary>
    private readonly record struct FaceKey(string Family, bool Bold, bool Italic);

    private sealed record RegisteredFace(string FaceName, byte[] Data, string Family, bool Bold, bool Italic);

    /// <summary>True if no custom fonts have been registered (so callers fall back to Liberation).</summary>
    public bool IsEmpty => facesByKey.IsEmpty;

    /// <summary>The face names of all registered fonts (mainly for diagnostics/testing).</summary>
    public IReadOnlyCollection<string> RegisteredFaceNames => facesByName.Keys.ToArray();

    /// <summary>Registers a font from its in-memory bytes under the given family + style.</summary>
    /// <returns>The unique face name assigned to this registration.</returns>
    public string RegisterFont(byte[] data, string family, bool bold = false, bool italic = false)
    {
        ArgumentNullException.ThrowIfNull(data);
        ArgumentException.ThrowIfNullOrWhiteSpace(family);
        if (data.Length == 0)
        {
            throw new ArgumentException("Font data is empty.", nameof(data));
        }

        string normalized = Normalize(family);
        var key = new FaceKey(normalized, bold, italic);
        string faceName = MakeFaceName(normalized, bold, italic);
        // Copy so later mutation of the caller's array cannot corrupt embedded bytes.
        var copy = (byte[])data.Clone();
        var face = new RegisteredFace(faceName, copy, normalized, bold, italic);

        facesByKey[key] = face;
        facesByName[faceName] = face;
        return faceName;
    }

    /// <summary>Registers a font from a file on disk under the given family + style.</summary>
    /// <returns>The unique face name assigned to this registration.</returns>
    public string RegisterFont(string path, string family, bool bold = false, bool italic = false)
    {
        ArgumentException.ThrowIfNullOrEmpty(path);
        return RegisterFont(File.ReadAllBytes(path), family, bold, italic);
    }

    /// <summary>
    /// Scans <paramref name="dir"/> for <c>*.ttf</c>/<c>*.otf</c> files and registers each, deriving
    /// the family name and bold/italic flags from the OpenType <c>name</c> table where possible and
    /// from the file name otherwise (see <see cref="DeriveFromFontFile"/>).
    /// </summary>
    /// <returns>The face names assigned to the fonts that were registered.</returns>
    public IReadOnlyList<string> RegisterFontsDirectory(string dir)
    {
        ArgumentException.ThrowIfNullOrEmpty(dir);
        if (!Directory.Exists(dir))
        {
            throw new DirectoryNotFoundException($"Font directory not found: {dir}");
        }

        var registered = new List<string>();
        IEnumerable<string> files = Directory
            .EnumerateFiles(dir, "*.*", SearchOption.TopDirectoryOnly)
            .Where(f => f.EndsWith(".ttf", StringComparison.OrdinalIgnoreCase)
                     || f.EndsWith(".otf", StringComparison.OrdinalIgnoreCase));

        foreach (string file in files)
        {
            byte[] data;
            try
            {
                data = File.ReadAllBytes(file);
            }
            catch (IOException)
            {
                // Best effort: skip unreadable files rather than abort the whole scan.
                continue;
            }

            (string family, bool bold, bool italic) = DeriveFromFontFile(data, file);
            registered.Add(RegisterFont(data, family, bold, italic));
        }

        return registered;
    }

    /// <summary>
    /// Resolves a requested family + style to a registered face name, or <c>null</c> if no custom
    /// font is registered for that family (so the caller falls back to Liberation). Matching is by
    /// exact family first; within a matched family, the exact (bold, italic) variant is preferred,
    /// then the nearest available style.
    /// </summary>
    public string? Resolve(string family, bool bold, bool italic)
    {
        if (facesByKey.IsEmpty || string.IsNullOrWhiteSpace(family))
        {
            return null;
        }

        string normalized = Normalize(family);

        // Exact (family, bold, italic).
        if (facesByKey.TryGetValue(new FaceKey(normalized, bold, italic), out RegisteredFace? exact))
        {
            return exact.FaceName;
        }

        // Same family, nearest style: try preserving one axis before falling to the base face.
        ReadOnlySpan<FaceKey> candidates =
        [
            new(normalized, bold, !italic),
            new(normalized, !bold, italic),
            new(normalized, false, false),
            new(normalized, !bold, !italic),
        ];

        foreach (FaceKey candidate in candidates)
        {
            if (facesByKey.TryGetValue(candidate, out RegisteredFace? near))
            {
                return near.FaceName;
            }
        }

        return null;
    }

    /// <summary>Returns the embedded bytes for a registry face name, or <c>null</c> if unknown.</summary>
    public byte[]? GetFaceBytes(string faceName)
        => facesByName.TryGetValue(faceName, out RegisteredFace? face) ? face.Data : null;

    /// <summary>True if <paramref name="faceName"/> denotes a registered (custom) face.</summary>
    public bool IsRegistryFace(string faceName)
        => faceName.StartsWith(FacePrefix, StringComparison.Ordinal) && facesByName.ContainsKey(faceName);

    /// <summary>Normalizes a family name for case- and whitespace-insensitive matching.</summary>
    internal static string Normalize(string family)
        => family.Trim().ToLowerInvariant();

    private static string MakeFaceName(string normalizedFamily, bool bold, bool italic)
    {
        // Keep only characters that are safe inside a PdfSharp face name; the (bold, italic) suffix
        // makes the name unique per style of the same family.
        var sb = new StringBuilder(FacePrefix);
        foreach (char c in normalizedFamily)
        {
            sb.Append(char.IsLetterOrDigit(c) ? c : '_');
        }

        sb.Append("_b").Append(bold ? '1' : '0');
        sb.Append("_i").Append(italic ? '1' : '0');
        return sb.ToString();
    }

    /// <summary>
    /// Derives (family, bold, italic) for an unattended directory scan. Parses the OpenType
    /// <c>name</c> table (name IDs 16/17 typographic family/subfamily, falling back to 1/2 legacy
    /// family/subfamily) for the family and to detect Bold/Italic from the subfamily. If the table
    /// cannot be parsed, falls back to the file name.
    /// </summary>
    internal static (string Family, bool Bold, bool Italic) DeriveFromFontFile(byte[] data, string path)
    {
        if (TryReadNameTable(data, out string? family, out string? subfamily) && family is not null)
        {
            string sub = (subfamily ?? string.Empty).ToLowerInvariant();
            bool bold = sub.Contains("bold");
            bool italic = sub.Contains("italic") || sub.Contains("oblique");
            return (family, bold, italic);
        }

        // Fall back to the file name (e.g. "DejaVuSans-BoldItalic.ttf").
        string stem = Path.GetFileNameWithoutExtension(path);
        string lower = stem.ToLowerInvariant();
        bool fileBold = lower.Contains("bold");
        bool fileItalic = lower.Contains("italic") || lower.Contains("oblique");

        // Strip common style words / separators to recover a family name from the file name.
        string nameFamily = stem;
        foreach (string word in new[] { "BoldItalic", "Bold", "Italic", "Oblique", "Regular" })
        {
            nameFamily = nameFamily.Replace(word, string.Empty, StringComparison.OrdinalIgnoreCase);
        }

        nameFamily = nameFamily.Replace('-', ' ').Replace('_', ' ').Trim();
        if (nameFamily.Length == 0)
        {
            nameFamily = stem;
        }

        return (nameFamily, fileBold, fileItalic);
    }

    /// <summary>
    /// Minimal big-endian reader for the OpenType <c>name</c> table. Returns the best family and
    /// subfamily strings found (preferring the Windows platform, then any). Returns <c>false</c> on
    /// any malformed/short data rather than throwing.
    /// </summary>
    private static bool TryReadNameTable(byte[] data, out string? family, out string? subfamily)
    {
        family = null;
        subfamily = null;
        try
        {
            // Offset table: sfntVersion(4), numTables(2), then table records. We skip the version and
            // search the directory for the 'name' table.
            if (data.Length < 12)
            {
                return false;
            }

            ushort numTables = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(4, 2));
            int nameOffset = -1;
            for (int i = 0; i < numTables; i++)
            {
                int rec = 12 + (i * 16);
                if (rec + 16 > data.Length)
                {
                    return false;
                }

                // Tag(4), checkSum(4), offset(4), length(4).
                if (data[rec] == (byte)'n' && data[rec + 1] == (byte)'a'
                    && data[rec + 2] == (byte)'m' && data[rec + 3] == (byte)'e')
                {
                    nameOffset = (int)BinaryPrimitives.ReadUInt32BigEndian(data.AsSpan(rec + 8, 4));
                    break;
                }
            }

            if (nameOffset < 0 || nameOffset + 6 > data.Length)
            {
                return false;
            }

            // name table header: format(2), count(2), stringOffset(2).
            ushort count = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(nameOffset + 2, 2));
            ushort stringOffset = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(nameOffset + 4, 2));
            int storageBase = nameOffset + stringOffset;
            int recordsBase = nameOffset + 6;

            // Name IDs: 1 = family, 2 = subfamily, 16 = typographic family, 17 = typographic subfamily.
            // Prefer the typographic (16/17) values; track best by platform (Windows = 3 preferred).
            string? family1 = null, family16 = null, subfamily2 = null, subfamily17 = null;

            for (int i = 0; i < count; i++)
            {
                int rec = recordsBase + (i * 12);
                if (rec + 12 > data.Length)
                {
                    break;
                }

                ushort platformId = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(rec, 2));
                ushort nameId = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(rec + 6, 2));
                ushort length = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(rec + 8, 2));
                ushort offset = BinaryPrimitives.ReadUInt16BigEndian(data.AsSpan(rec + 10, 2));

                if (nameId is not (1 or 2 or 16 or 17))
                {
                    continue;
                }

                int start = storageBase + offset;
                if (start + length > data.Length || length == 0)
                {
                    continue;
                }

                string? value = DecodeNameValue(data.AsSpan(start, length), platformId);
                if (string.IsNullOrEmpty(value))
                {
                    continue;
                }

                switch (nameId)
                {
                    case 1: family1 ??= value; break;
                    case 16: family16 ??= value; break;
                    case 2: subfamily2 ??= value; break;
                    case 17: subfamily17 ??= value; break;
                }
            }

            family = family16 ?? family1;
            subfamily = subfamily17 ?? subfamily2;
            return family is not null;
        }
        catch (Exception)
        {
            // TODO: a richer parser could honour language IDs and encoding records more precisely;
            // for auto-registration the family/style heuristic above is sufficient (best effort).
            return false;
        }
    }

    /// <summary>
    /// Decodes a name-record string. Windows (platform 3) and Unicode (platform 0) records are
    /// UTF-16BE; Macintosh (platform 1) records are (close enough to) ASCII for Latin family names.
    /// </summary>
    private static string? DecodeNameValue(ReadOnlySpan<byte> bytes, ushort platformId)
    {
        if (platformId is 3 or 0)
        {
            return Encoding.BigEndianUnicode.GetString(bytes).Trim('\0').Trim();
        }

        // Macintosh Roman is ASCII-compatible for the Latin range used by family names.
        return Encoding.ASCII.GetString(bytes).Trim('\0').Trim();
    }
}
