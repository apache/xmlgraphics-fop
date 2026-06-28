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

using System.Collections.Generic;
using System.IO;
using System.Text;
using Fop.Fo;
using Fop.Layout;
using Fop.Render.Pdf;
using Fop.Render.Pdf.Native;
using PdfSharp.Pdf.IO;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Tests for TrueType glyph subsetting in the native renderer: the subset must be much smaller, keep
/// glyph ids/metrics intact (so encoding and widths stay valid), and produce a re-openable PDF.
/// </summary>
public class NativeFontSubsetTests
{
    private static byte[] LiberationBytes()
    {
        _ = new PdfSharpFontMeasurer();
        byte[]? bytes = new ResolverFontProvider(FopFontResolver.Shared)
            .GetFontProgram(new FontKey("Helvetica", 12_000, 400, FontStyle.Normal));
        Assert.NotNull(bytes);
        return bytes!;
    }

    [Fact]
    public void SubsetIsSmallerAndPreservesGlyphIdsAndWidths()
    {
        byte[] full = LiberationBytes();
        TrueTypeFont original = TrueTypeFont.Parse(full)!;

        // The glyphs behind "Hello, World".
        var used = new HashSet<int>();
        foreach (char c in "Hello, World")
        {
            used.Add(original.GlyphIndex(c));
        }

        byte[]? subset = TrueTypeSubsetter.Subset(full, used);
        Assert.NotNull(subset);
        Assert.True(subset!.Length < full.Length / 2, $"subset {subset.Length} not << full {full.Length}");

        // Re-parse: glyph count is preserved (ids unchanged) and advance widths are intact (hmtx kept).
        TrueTypeFont reparsed = TrueTypeFont.Parse(subset)!;
        Assert.Equal(original.NumGlyphs, reparsed.NumGlyphs);
        foreach (char c in "HeloW,rd ")
        {
            Assert.Equal(original.AdvanceWidth(c), reparsed.AdvanceWidth(c));
        }
    }

    [Fact]
    public void SubsettedNativePdfIsSmallAndReopens()
    {
        // A document using only a handful of glyphs should embed a small subset, not the whole face.
        string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="300pt" page-height="200pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body"><fo:block>Hi</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;

        byte[] pdf = new FopProcessor().ConvertNative(fo);
        string text = Encoding.Latin1.GetString(pdf);

        Assert.Matches("/BaseFont /[A-Z]{6}\\+", text); // subset tag present
        Assert.True(pdf.Length < 60_000, $"subsetted single-glyph PDF unexpectedly large: {pdf.Length}");

        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.Equal(1, doc.PageCount);
    }

    [Fact]
    public void SubsetFailureFallsBackToFullEmbed()
    {
        // Non-sfnt bytes cannot be subset; the subsetter returns null (caller embeds the full program).
        Assert.Null(TrueTypeSubsetter.Subset([1, 2, 3, 4], new HashSet<int> { 0 }));
    }
}
