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

using System.Text;
using Fop.Render.Pdf;
using Xunit;

namespace Fop.Rendering.Tests;

public class EndToEndPdfTests
{
    private const string SimpleFo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block font-size="24pt" font-weight="bold" space-after="12pt" color="#1a3c7b">FOP for .NET</fo:block>
              <fo:block space-after="6pt" text-align="justify">This PDF was produced by the C# port of Apache FOP: the FO document was parsed into a formatting-object tree, laid out into an area tree, and rendered with PdfSharp.</fo:block>
              <fo:block font-style="italic">A second paragraph, in italics, demonstrating inherited font properties and inline styling.</fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void ProducesValidPdf()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(SimpleFo);

        Assert.True(pdf.Length > 1000, $"PDF unexpectedly small: {pdf.Length} bytes");
        string header = Encoding.ASCII.GetString(pdf, 0, 5);
        Assert.Equal("%PDF-", header);
        // PDF files end with the %%EOF marker.
        string tail = Encoding.ASCII.GetString(pdf, Math.Max(0, pdf.Length - 8), Math.Min(8, pdf.Length));
        Assert.Contains("EOF", tail);
    }

    [Fact]
    public void SingleShortDocumentIsOnePage()
    {
        var processor = new FopProcessor();
        var root = Fo.FoTreeBuilder.ParseString(SimpleFo);
        var tree = processor.LayOut(root);
        Assert.Single(tree.Pages);
        // The first block emitted at least one positioned text run.
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text.Contains("FOP"));
    }

    [Fact]
    public void LongDocumentPaginates()
    {
        var sb = new StringBuilder();
        sb.Append("""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                    margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">
            """);
        for (int i = 0; i < 120; i++)
        {
            sb.Append($"<fo:block space-after=\"6pt\">Paragraph number {i}: the quick brown fox jumps over the lazy dog, several times, to fill the page with enough text to force pagination across multiple pages.</fo:block>");
        }

        sb.Append("</fo:flow></fo:page-sequence></fo:root>");

        var processor = new FopProcessor();
        var root = Fo.FoTreeBuilder.ParseString(sb.ToString());
        var tree = processor.LayOut(root);
        Assert.True(tree.Pages.Count > 1, $"Expected multiple pages, got {tree.Pages.Count}");
    }
}
