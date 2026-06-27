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

using System.Collections.Concurrent;
using System.Reflection;
using PdfSharp.Fonts;

namespace Fop.Render.Pdf;

/// <summary>
/// A PdfSharp <see cref="IFontResolver"/> backed by the embedded Liberation fonts (metric-compatible
/// with Arial / Times New Roman / Courier New). PdfSharp 6+ ships no core fonts, so a resolver that
/// supplies real font data is mandatory; this one keeps the renderer self-contained and portable.
/// <para>Families are mapped: serif/Times -&gt; LiberationSerif, monospace/Courier -&gt; LiberationMono,
/// everything else -&gt; LiberationSans (sans-serif). Applications can register their own resolver
/// via <see cref="GlobalFontSettings.FontResolver"/> instead.</para>
/// </summary>
public sealed class LiberationFontResolver : IFontResolver
{
    private static readonly LiberationFontResolver Shared = new();
    private static readonly Lock InstallLock = new();
    private static bool installed;

    private readonly ConcurrentDictionary<string, byte[]> fontData = new(StringComparer.Ordinal);

    /// <summary>Ensures this resolver is installed as the global PdfSharp font resolver (idempotent).</summary>
    public static void EnsureInstalled()
    {
        if (installed)
        {
            return;
        }

        lock (InstallLock)
        {
            if (!installed)
            {
                GlobalFontSettings.FontResolver = Shared;
                installed = true;
            }
        }
    }

    /// <inheritdoc/>
    public byte[]? GetFont(string faceName)
        => fontData.GetOrAdd(faceName, LoadEmbeddedFont);

    /// <inheritdoc/>
    public FontResolverInfo? ResolveTypeface(string familyName, bool isBold, bool isItalic)
    {
        string family = familyName.Trim().ToLowerInvariant();
        string baseName = family switch
        {
            "times" or "times new roman" or "times roman" or "serif" or "liberationserif" => "LiberationSerif",
            "courier" or "courier new" or "monospace" or "mono" or "liberationmono" => "LiberationMono",
            _ => "LiberationSans",
        };

        string suffix = (isBold, isItalic) switch
        {
            (true, true) => "-BoldItalic",
            (true, false) => "-Bold",
            (false, true) => "-Italic",
            (false, false) => "-Regular",
        };

        return new FontResolverInfo(baseName + suffix);
    }

    private static byte[] LoadEmbeddedFont(string faceName)
    {
        Assembly assembly = typeof(LiberationFontResolver).Assembly;
        // Embedded resource names look like "Fop.Render.Pdf.Fonts.LiberationSans-Regular.ttf".
        string suffix = $"Fonts.{faceName}.ttf";
        string? resourceName = assembly.GetManifestResourceNames()
            .FirstOrDefault(n => n.EndsWith(suffix, StringComparison.Ordinal));

        if (resourceName is null)
        {
            throw new InvalidOperationException($"Embedded font not found for face '{faceName}'.");
        }

        using Stream stream = assembly.GetManifestResourceStream(resourceName)
            ?? throw new InvalidOperationException($"Could not open embedded font resource '{resourceName}'.");
        using var buffer = new MemoryStream();
        stream.CopyTo(buffer);
        return buffer.ToArray();
    }
}
