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

namespace Fop.Render.Pdf.Native;

/// <summary>
/// A minimal TrueType/OpenType font reader: it parses just the tables needed to embed a font as a
/// simple PDF font -- glyph advance widths (via <c>cmap</c> + <c>hmtx</c>), the descriptor metrics
/// (<c>head</c>/<c>hhea</c>/<c>OS/2</c>/<c>post</c>) and the glyph count. All metrics are reported in
/// 1000-unit em space (the PDF glyph-space convention). Returns <c>null</c> for input that is not a
/// recognisable sfnt font. Only the common subtable formats (cmap 4/6/0) are supported, which covers
/// the bundled Liberation faces and typical Latin text fonts.
/// </summary>
internal sealed class TrueTypeFont
{
    private readonly int unitsPerEm;
    private readonly ushort[] advanceWidths; // per glyph, in font units
    private readonly Func<int, int> cmapLookup;

    private TrueTypeFont(int unitsPerEm, ushort[] advanceWidths, Func<int, int> cmapLookup,
        int ascent, int descent, int capHeight, int[] fontBBox, double italicAngle, bool italic, bool bold)
    {
        this.unitsPerEm = unitsPerEm;
        this.advanceWidths = advanceWidths;
        this.cmapLookup = cmapLookup;
        Ascent = Scale(ascent);
        Descent = Scale(descent);
        CapHeight = Scale(capHeight);
        FontBBox = [Scale(fontBBox[0]), Scale(fontBBox[1]), Scale(fontBBox[2]), Scale(fontBBox[3])];
        ItalicAngle = italicAngle;
        Italic = italic;
        Bold = bold;
    }

    public int Ascent { get; }

    public int Descent { get; }

    public int CapHeight { get; }

    public int[] FontBBox { get; }

    public double ItalicAngle { get; }

    public bool Italic { get; }

    public bool Bold { get; }

    /// <summary>The number of glyphs in the font.</summary>
    public int NumGlyphs => advanceWidths.Length;

    /// <summary>The glyph index <paramref name="codePoint"/> maps to via the font's cmap (0 = .notdef).</summary>
    public int GlyphIndex(int codePoint) => cmapLookup(codePoint);

    /// <summary>The advance width (1000-em units) of the glyph mapped from <paramref name="codePoint"/>.</summary>
    public int AdvanceWidth(int codePoint)
    {
        int glyph = cmapLookup(codePoint);
        if (glyph < 0 || glyph >= advanceWidths.Length)
        {
            glyph = 0;
        }

        return advanceWidths.Length == 0 ? 0 : Scale(advanceWidths[glyph]);
    }

    private int Scale(int fontUnits) => unitsPerEm == 0 ? 0 : (int)Math.Round(fontUnits * 1000.0 / unitsPerEm);

    public static TrueTypeFont? Parse(byte[] data)
    {
        try
        {
            return ParseCore(data);
        }
        catch (Exception)
        {
            return null;
        }
    }

    private static TrueTypeFont? ParseCore(byte[] d)
    {
        if (d.Length < 12)
        {
            return null;
        }

        uint version = ReadU32(d, 0);
        // 0x00010000 (TrueType), 'OTTO' (CFF OpenType), 'true'/'typ1'. CFF (OTTO) has no glyf but still
        // has hmtx/cmap/head, which is all we need for widths + descriptor.
        if (version != 0x00010000 && version != 0x4F54544F && version != 0x74727565)
        {
            return null;
        }

        ushort numTables = ReadU16(d, 4);
        var tables = new Dictionary<string, (int Offset, int Length)>(StringComparer.Ordinal);
        for (int i = 0; i < numTables; i++)
        {
            int rec = 12 + i * 16;
            if (rec + 16 > d.Length)
            {
                break;
            }

            string tag = System.Text.Encoding.ASCII.GetString(d, rec, 4);
            int offset = (int)ReadU32(d, rec + 8);
            int length = (int)ReadU32(d, rec + 12);
            tables[tag] = (offset, length);
        }

        if (!tables.TryGetValue("head", out var head) || !tables.TryGetValue("hhea", out var hhea)
            || !tables.TryGetValue("hmtx", out var hmtx) || !tables.TryGetValue("maxp", out var maxp)
            || !tables.TryGetValue("cmap", out var cmap))
        {
            return null;
        }

        int unitsPerEm = ReadU16(d, head.Offset + 18);
        int[] bbox = [ReadI16(d, head.Offset + 36), ReadI16(d, head.Offset + 38),
            ReadI16(d, head.Offset + 40), ReadI16(d, head.Offset + 42)];

        int ascent = ReadI16(d, hhea.Offset + 4);
        int descent = ReadI16(d, hhea.Offset + 6);
        int numberOfHMetrics = ReadU16(d, hhea.Offset + 34);
        int numGlyphs = ReadU16(d, maxp.Offset + 4);

        var widths = new ushort[Math.Max(1, numGlyphs)];
        ushort last = 0;
        for (int g = 0; g < widths.Length; g++)
        {
            if (g < numberOfHMetrics)
            {
                int at = hmtx.Offset + g * 4;
                last = at + 2 <= d.Length ? ReadU16(d, at) : last;
            }

            widths[g] = last;
        }

        Func<int, int> lookup = ParseCmap(d, cmap.Offset) ?? (_ => 0);

        // Descriptor extras: cap height from OS/2 (sCapHeight, v2+), italic angle from post, style flags.
        int capHeight = (int)(ascent * 0.7);
        bool italic = false;
        bool bold = false;
        if (tables.TryGetValue("OS/2", out var os2) && os2.Length >= 90)
        {
            ushort os2Version = ReadU16(d, os2.Offset);
            ushort fsSelection = ReadU16(d, os2.Offset + 62);
            italic = (fsSelection & 0x01) != 0;
            bold = (fsSelection & 0x20) != 0;
            if (os2Version >= 2 && os2.Length >= 90)
            {
                int sCapHeight = ReadI16(d, os2.Offset + 88);
                if (sCapHeight > 0)
                {
                    capHeight = sCapHeight;
                }
            }
        }

        double italicAngle = 0;
        if (tables.TryGetValue("post", out var post) && post.Length >= 8)
        {
            italicAngle = ReadI32(d, post.Offset + 4) / 65536.0;
        }

        return new TrueTypeFont(unitsPerEm, widths, lookup, ascent, descent, capHeight, bbox,
            italicAngle, italic, bold);
    }

    /// <summary>Selects the best Unicode cmap subtable and returns a codepoint-to-glyph lookup.</summary>
    private static Func<int, int>? ParseCmap(byte[] d, int cmapOffset)
    {
        ushort numTables = ReadU16(d, cmapOffset + 2);
        int best = -1;
        int bestScore = -1;
        for (int i = 0; i < numTables; i++)
        {
            int rec = cmapOffset + 4 + i * 8;
            ushort platform = ReadU16(d, rec);
            ushort encoding = ReadU16(d, rec + 2);
            int subOffset = (int)ReadU32(d, rec + 4);
            int score = (platform, encoding) switch
            {
                (3, 1) => 4, // Windows Unicode BMP
                (0, _) => 3, // Unicode
                (3, 0) => 2, // Windows Symbol
                _ => 1,
            };
            if (score > bestScore)
            {
                bestScore = score;
                best = cmapOffset + subOffset;
            }
        }

        if (best < 0)
        {
            return null;
        }

        ushort format = ReadU16(d, best);
        return format switch
        {
            4 => ParseCmapFormat4(d, best),
            6 => ParseCmapFormat6(d, best),
            0 => ParseCmapFormat0(d, best),
            _ => null,
        };
    }

    private static Func<int, int> ParseCmapFormat0(byte[] d, int at)
    {
        var glyphs = new byte[256];
        Array.Copy(d, at + 6, glyphs, 0, Math.Min(256, d.Length - (at + 6)));
        return cp => cp is >= 0 and < 256 ? glyphs[cp] : 0;
    }

    private static Func<int, int> ParseCmapFormat6(byte[] d, int at)
    {
        int first = ReadU16(d, at + 6);
        int count = ReadU16(d, at + 8);
        var glyphs = new ushort[count];
        for (int i = 0; i < count; i++)
        {
            glyphs[i] = ReadU16(d, at + 10 + i * 2);
        }

        return cp =>
        {
            int idx = cp - first;
            return idx >= 0 && idx < count ? glyphs[idx] : 0;
        };
    }

    private static Func<int, int> ParseCmapFormat4(byte[] d, int at)
    {
        int segCount = ReadU16(d, at + 6) / 2;
        int endCodes = at + 14;
        int startCodes = endCodes + segCount * 2 + 2; // +2 for reservedPad
        int idDeltas = startCodes + segCount * 2;
        int idRangeOffsets = idDeltas + segCount * 2;

        return cp =>
        {
            if (cp is < 0 or > 0xFFFF)
            {
                return 0;
            }

            for (int s = 0; s < segCount; s++)
            {
                int end = ReadU16(d, endCodes + s * 2);
                if (cp > end)
                {
                    continue;
                }

                int start = ReadU16(d, startCodes + s * 2);
                if (cp < start)
                {
                    return 0;
                }

                int idDelta = ReadI16(d, idDeltas + s * 2);
                int idRangeOffset = ReadU16(d, idRangeOffsets + s * 2);
                if (idRangeOffset == 0)
                {
                    return (cp + idDelta) & 0xFFFF;
                }

                // The glyph index is read from the glyphIdArray that follows the idRangeOffset array.
                int glyphIndexAddr = idRangeOffsets + s * 2 + idRangeOffset + (cp - start) * 2;
                if (glyphIndexAddr + 2 > d.Length)
                {
                    return 0;
                }

                int glyph = ReadU16(d, glyphIndexAddr);
                return glyph == 0 ? 0 : (glyph + idDelta) & 0xFFFF;
            }

            return 0;
        };
    }

    private static ushort ReadU16(byte[] d, int offset) => BinaryPrimitives.ReadUInt16BigEndian(d.AsSpan(offset, 2));

    private static short ReadI16(byte[] d, int offset) => BinaryPrimitives.ReadInt16BigEndian(d.AsSpan(offset, 2));

    private static uint ReadU32(byte[] d, int offset) => BinaryPrimitives.ReadUInt32BigEndian(d.AsSpan(offset, 4));

    private static int ReadI32(byte[] d, int offset) => BinaryPrimitives.ReadInt32BigEndian(d.AsSpan(offset, 4));
}
