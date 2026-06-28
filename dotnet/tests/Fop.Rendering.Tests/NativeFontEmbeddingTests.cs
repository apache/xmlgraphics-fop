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
/// Tests for embedding real TrueType faces in the native renderer: the <see cref="TrueTypeFont"/>
/// parser on a bundled Liberation face, and the end-to-end embedded-font output via
/// <see cref="FopProcessor.ConvertNative(string)"/>.
/// </summary>
public class NativeFontEmbeddingTests
{
    private const string Doc = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="300pt" page-height="200pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              <fo:block>Embedded TrueType text.</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    private static byte[] LiberationBytes()
    {
        // Ensure the resolver is installed, then fetch the real font program the renderer would embed.
        _ = new PdfSharpFontMeasurer();
        var provider = new ResolverFontProvider(FopFontResolver.Shared);
        byte[]? bytes = provider.GetFontProgram(new FontKey("Helvetica", 12_000, 400, FontStyle.Normal));
        Assert.NotNull(bytes);
        return bytes!;
    }

    [Fact]
    public void TrueTypeParserReadsSaneMetricsAndWidths()
    {
        TrueTypeFont? font = TrueTypeFont.Parse(LiberationBytes());
        Assert.NotNull(font);

        // Metrics are in 1000-em space: positive ascent/cap-height, negative descent, sensible bbox.
        Assert.True(font!.Ascent > 0);
        Assert.True(font.Descent < 0);
        Assert.True(font.CapHeight > 0);
        Assert.Equal(4, font.FontBBox.Length);

        // Proportional widths: a space and letters have positive advance; 'i' is narrower than 'm'.
        Assert.True(font.AdvanceWidth(' ') > 0);
        Assert.True(font.AdvanceWidth('m') > font.AdvanceWidth('i'));
        Assert.True(font.AdvanceWidth('A') is > 0 and < 2000);
    }

    [Fact]
    public void ConvertNativeEmbedsType0CidFont()
    {
        byte[] pdf = new FopProcessor().ConvertNative(Doc);
        string text = Encoding.Latin1.GetString(pdf);
        // Embedded faces use a Type0/Identity-H composite font with a CIDFontType2 descendant.
        Assert.Contains("/Subtype /Type0", text);
        Assert.Contains("/Encoding /Identity-H", text);
        Assert.Contains("/Subtype /CIDFontType2", text);
        Assert.Contains("/CIDToGIDMap /Identity", text);
        Assert.Contains("/FontFile2", text);
        Assert.Contains("/ToUnicode", text);
        Assert.DoesNotContain("/Subtype /Type1", text); // no standard-14 fallback needed here
    }

    [Fact]
    public void NonWinAnsiCharacterMapsToGlyphAndEmbeds()
    {
        // 'ł' (U+0142, Latin Extended-A) is outside WinAnsi; the simple-font path would have lost it.
        // The Identity-H CID path maps it through the font cmap to a real glyph.
        TrueTypeFont font = TrueTypeFont.Parse(LiberationBytes())!;
        Assert.True(font.GlyphIndex('ł') > 0, "Liberation should cover U+0142");

        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="100pt">
                  <fo:region-body/></fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p"><fo:flow flow-name="xsl-region-body">
                <fo:block>Wrocław αβγ</fo:block>
              </fo:flow></fo:page-sequence>
            </fo:root>
            """;
        byte[] pdf = new FopProcessor().ConvertNative(fo);
        string text = Encoding.Latin1.GetString(pdf);
        Assert.Contains("/Subtype /Type0", text);
        Assert.Contains("/ToUnicode", text);

        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.Equal(1, doc.PageCount);
    }

    [Fact]
    public void EmbeddedFontPdfReopensInPdfSharp()
    {
        byte[] pdf = new FopProcessor().ConvertNative(Doc);
        using var input = new MemoryStream(pdf);
        using var doc = PdfReader.Open(input, PdfDocumentOpenMode.Import);
        Assert.Equal(1, doc.PageCount);
    }

    [Fact]
    public void FallsBackToStandardFontWhenNoProgram()
    {
        // A renderer with no font provider keeps the standard-14 path.
        var tree = new AreaTree();
        var page = new PageArea(200_000, 100_000);
        page.Add(new TextRun(10_000, 50_000, "Hi", new FontKey("Helvetica", 12_000, 400, FontStyle.Normal),
            Fop.Colors.FopColor.FromRgb(0, 0, 0)));
        tree.AddPage(page);

        using var output = new MemoryStream();
        new NativePdfRenderer(new PdfSharpFontMeasurer()).Render(tree, output);
        string text = Encoding.Latin1.GetString(output.ToArray());
        Assert.Contains("/Subtype /Type1", text);
        Assert.Contains("/BaseFont /Helvetica", text);
    }
}
