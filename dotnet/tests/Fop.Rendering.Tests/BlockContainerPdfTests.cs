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

using Fop.Layout;
using Fop.Render.Pdf;

using PdfSharp.Pdf.IO;

using Xunit;

namespace Fop.Rendering.Tests;

public class BlockContainerPdfTests
{
    private const string FoXml = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block>Normal flow content.</fo:block>
              <fo:block-container absolute-position="absolute" left="20mm" top="120mm"
                  width="60mm" border="1pt solid #1a3c7b" background-color="#eef2ff">
                <fo:block>Absolutely positioned box.</fo:block>
              </fo:block-container>
              <fo:block-container absolute-position="absolute" left="120mm" top="120mm"
                  width="80mm" reference-orientation="90" border="0.5pt solid #800000">
                <fo:block>Rotated ninety degrees.</fo:block>
              </fo:block-container>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void AbsoluteAndRotatedContainersConvertToValidPdf()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(FoXml);

        Assert.True(pdf.Length > 1000, $"PDF unexpectedly small: {pdf.Length} bytes");
        Assert.Equal("%PDF-", Encoding.ASCII.GetString(pdf, 0, 5));
        string tail = Encoding.ASCII.GetString(pdf, Math.Max(0, pdf.Length - 8), Math.Min(8, pdf.Length));
        Assert.Contains("EOF", tail);
    }

    [Fact]
    public void RotatedContainerEmitsAGroupOnTheLaidOutTree()
    {
        var processor = new FopProcessor();
        var tree = processor.LayOut(Fo.FoTreeBuilder.ParseString(FoXml));

        // The rotated container becomes a transform group with the expected angle; the unrotated
        // absolute container stays as flat primitives.
        var groups = tree.Pages.SelectMany(p => p.Groups).ToList();
        Assert.Contains(groups, g => g.RotationDegrees == 90);
        Assert.Single(tree.Pages);
    }

    private const string LinkInRotatedContainerFo = """
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-family="Helvetica" font-size="12pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="A4" page-width="210mm" page-height="297mm"
                margin-top="20mm" margin-bottom="20mm" margin-left="25mm" margin-right="25mm">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="A4">
            <fo:flow flow-name="xsl-region-body">
              <fo:block-container absolute-position="absolute" left="120mm" top="120mm"
                  width="80mm" reference-orientation="90">
                <fo:block><fo:basic-link external-destination="url(https://example.com/)">Rotated link</fo:basic-link></fo:block>
              </fo:block-container>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    [Fact]
    public void LinkInsideRotatedContainerIsAGroupLocalLink()
    {
        var processor = new FopProcessor();
        var tree = processor.LayOut(Fo.FoTreeBuilder.ParseString(LinkInRotatedContainerFo));

        // The link lives inside the rotated group (group-local coordinates), not on the flat page, so it
        // can later be mapped through the group's transform into a page-space annotation.
        AreaGroup group = Assert.Single(tree.Pages.SelectMany(p => p.Groups), g => g.RotationDegrees == 90);
        Assert.Contains(group.Links, l => l.Uri == "https://example.com/");
        Assert.DoesNotContain(tree.Pages.SelectMany(p => p.Links), l => l.Uri == "https://example.com/");
    }

    [Fact]
    public void LinkInsideRotatedContainerBecomesAPageAnnotation()
    {
        var processor = new FopProcessor();
        byte[] pdf = processor.Convert(LinkInRotatedContainerFo);

        // Re-open the rendered PDF: the group-local link must surface as a link annotation on the page
        // (its rect is the axis-aligned bounding box of the rotated link region).
        using var stream = new MemoryStream(pdf);
        using var doc = PdfReader.Open(stream, PdfDocumentOpenMode.Import);
        var page = doc.Pages[0];
        Assert.True(page.Annotations.Count >= 1, "Expected a link annotation from the rotated container.");
    }
}
