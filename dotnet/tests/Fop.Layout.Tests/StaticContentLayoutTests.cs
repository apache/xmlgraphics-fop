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

using Fop.Fo;
using Fop.Layout;

using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Layout tests for running headers/footers (fo:static-content into region-before/after) and
/// fo:page-number, over the deterministic <see cref="FakeFontMeasurer"/> (500 units/glyph at 1000mpt,
/// ascender 800, descender 200, scaled by font size).
/// </summary>
public sealed class StaticContentLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>
    /// A page 200pt tall with a region-before band of 20pt at the top, a region-after band of 20pt at
    /// the bottom, and a body inset by matching region-body margins so it does not overlap the bands.
    /// The body holds <paramref name="bodyBlocks"/> blocks tall enough to span <paramref name="pages"/>
    /// pages (each block ~ one line at 10pt).
    /// </summary>
    private static string Document(string header, string footer, string body,
        string? initialPageNumber = null)
    {
        string ipn = initialPageNumber is null ? string.Empty : $" initial-page-number=\"{initialPageNumber}\"";
        return $"""
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body margin-top="20pt" margin-bottom="20pt"/>
                  <fo:region-before extent="20pt"/>
                  <fo:region-after extent="20pt"/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p"{ipn}>
                <fo:static-content flow-name="xsl-region-before">{header}</fo:static-content>
                <fo:static-content flow-name="xsl-region-after">{footer}</fo:static-content>
                <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
    }

    /// <summary>N blocks each holding a single short word (one 10pt line, 12pt high).</summary>
    private static string Blocks(int n) =>
        string.Concat(Enumerable.Range(0, n).Select(i => $"<fo:block>B{i}</fo:block>"));

    [Fact]
    public void HeaderAppearsInTopBandOnSinglePage()
    {
        string fo = Document(
            header: "<fo:block>HDR</fo:block>",
            footer: "<fo:block>FTR</fo:block>",
            body: Blocks(1));

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);

        TextRun header = Assert.Single(page.TextRuns, r => r.Text == "HDR");
        TextRun footer = Assert.Single(page.TextRuns, r => r.Text == "FTR");

        // Region-before band starts at the page top margin (0 here). Its baseline is within [0, 20pt].
        Assert.True(header.BaselineYMpt > 0 && header.BaselineYMpt <= 20_000,
            $"header baseline {header.BaselineYMpt} not in top band");

        // Region-after band is the bottom 20pt (180pt..200pt).
        Assert.True(footer.BaselineYMpt > 180_000 && footer.BaselineYMpt <= 200_000,
            $"footer baseline {footer.BaselineYMpt} not in bottom band");
    }

    [Fact]
    public void HeaderRepeatsOnEveryPage()
    {
        // The body region is 160pt tall (200 - 20 - 20). Each block is one 12pt line; 20 blocks
        // overflow onto multiple pages.
        string fo = Document(
            header: "<fo:block>HDR</fo:block>",
            footer: "<fo:block>FTR</fo:block>",
            body: Blocks(40));

        AreaTree tree = LayOut(fo);
        Assert.True(tree.Pages.Count >= 2, $"expected multiple pages, got {tree.Pages.Count}");

        foreach (PageArea page in tree.Pages)
        {
            Assert.Contains(page.TextRuns, r => r.Text == "HDR");
            Assert.Contains(page.TextRuns, r => r.Text == "FTR");
        }
    }

    [Fact]
    public void PageNumberInFooterIncrementsAcrossPages()
    {
        string fo = Document(
            header: "<fo:block>HDR</fo:block>",
            footer: "<fo:block><fo:page-number/></fo:block>",
            body: Blocks(40));

        AreaTree tree = LayOut(fo);
        Assert.True(tree.Pages.Count >= 2);

        for (int i = 0; i < tree.Pages.Count; i++)
        {
            string expected = (i + 1).ToString();
            PageArea page = tree.Pages[i];
            Assert.Contains(page.TextRuns,
                r => r.Text == expected && r.BaselineYMpt > 180_000 && r.BaselineYMpt <= 200_000);
        }
    }

    [Fact]
    public void PageNumberHonoursInitialPageNumber()
    {
        string fo = Document(
            header: "<fo:block>HDR</fo:block>",
            footer: "<fo:block><fo:page-number/></fo:block>",
            body: Blocks(40),
            initialPageNumber: "5");

        AreaTree tree = LayOut(fo);

        for (int i = 0; i < tree.Pages.Count; i++)
        {
            string expected = (i + 5).ToString();
            Assert.Contains(tree.Pages[i].TextRuns, r => r.Text == expected);
        }
    }

    [Fact]
    public void BodyPageNumberResolvesToItsPage()
    {
        // A page-number in the body flow resolves to the page on which its block begins.
        string fo = Document(
            header: "<fo:block>HDR</fo:block>",
            footer: "<fo:block>FTR</fo:block>",
            body: Blocks(40) + "<fo:block>last <fo:page-number/></fo:block>");

        AreaTree tree = LayOut(fo);
        int lastPageNumber = tree.Pages.Count;

        // The "last" block sits on the final page; its page-number reads that page's number. The word
        // and the page number coalesce into one styled run ("last <n>") because they share a style.
        PageArea finalPage = tree.Pages[^1];
        Assert.Contains(finalPage.TextRuns,
            r => r.Text == $"last {lastPageNumber}");
    }

    [Fact]
    public void HeaderTextUsesItsOwnContentNotBodyText()
    {
        string fo = Document(
            header: "<fo:block>HEADERWORD</fo:block>",
            footer: "<fo:block>FOOTERWORD</fo:block>",
            body: "<fo:block>BODYWORD</fo:block>");

        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);

        Assert.Contains(page.TextRuns, r => r.Text == "HEADERWORD");
        Assert.Contains(page.TextRuns, r => r.Text == "FOOTERWORD");
        Assert.Contains(page.TextRuns, r => r.Text == "BODYWORD");

        // Body word lands inside the body content rectangle (top band ends at 20pt; body starts at 20pt).
        TextRun body = page.TextRuns.Single(r => r.Text == "BODYWORD");
        Assert.True(body.BaselineYMpt > 20_000, $"body baseline {body.BaselineYMpt} should be below the top band");
    }
}
