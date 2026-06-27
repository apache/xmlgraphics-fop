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
/// Layout tests for markers (fo:marker/fo:retrieve-marker) and the side regions
/// (fo:region-start/fo:region-end), over the deterministic <see cref="FakeFontMeasurer"/>.
/// </summary>
public sealed class MarkerAndSideRegionLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>N filler blocks each holding a single short word (one 10pt line, 12pt high).</summary>
    private static string Blocks(int n) =>
        string.Concat(Enumerable.Range(0, n).Select(i => $"<fo:block>F{i}</fo:block>"));

    /// <summary>A block whose text is <paramref name="text"/> carrying a "chapter" marker of <paramref name="marker"/>.</summary>
    private static string MarkerBlock(string text, string marker) =>
        $"<fo:block>{text}<fo:marker marker-class-name=\"chapter\">{marker}</fo:marker></fo:block>";

    /// <summary>
    /// A page 200pt tall with a region-before band of 20pt holding a chapter retrieve-marker header, a
    /// body inset by 20pt top/bottom margins (160pt tall, ~13 12pt lines per page).
    /// </summary>
    private static string Document(string body, string retrievePosition = "first-starting-within-page") => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
              <fo:region-body margin-top="20pt" margin-bottom="20pt"/>
              <fo:region-before extent="20pt"/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:static-content flow-name="xsl-region-before">
              <fo:block><fo:retrieve-marker retrieve-class-name="chapter" retrieve-position="{retrievePosition}"/></fo:block>
            </fo:static-content>
            <fo:flow flow-name="xsl-region-body">{body}</fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    private static bool HeaderHas(PageArea page, string text) =>
        page.TextRuns.Any(r => r.Text == text && r.BaselineYMpt > 0 && r.BaselineYMpt <= 20_000);

    [Fact]
    public void MarkerSetOnPageTwoIsRetrievedByHeaderOnPageTwo()
    {
        // Page 1 carries CHAP1; the 20 filler blocks push CHAP2 onto page 2.
        string body = MarkerBlock("Start", "CHAP1") + Blocks(20) + MarkerBlock("Next", "CHAP2");
        AreaTree tree = LayOut(Document(body));

        Assert.True(tree.Pages.Count >= 2, $"expected >= 2 pages, got {tree.Pages.Count}");

        Assert.True(HeaderHas(tree.Pages[0], "CHAP1"), "page 1 header should show CHAP1");
        Assert.True(HeaderHas(tree.Pages[1], "CHAP2"), "page 2 header should show CHAP2");
    }

    [Fact]
    public void FirstStartingWithinPagePicksTheFirstMarkerOnThePage()
    {
        // Two markers both start on page 1; first-starting-within-page picks the first (CHAP1).
        string body = MarkerBlock("A", "CHAP1") + MarkerBlock("B", "CHAP2") + Blocks(3);
        AreaTree tree = LayOut(Document(body, "first-starting-within-page"));

        PageArea page1 = tree.Pages[0];
        Assert.True(HeaderHas(page1, "CHAP1"), "should pick the first marker on the page");
        Assert.False(HeaderHas(page1, "CHAP2"), "should not pick the second marker for first-starting");
    }

    [Fact]
    public void LastStartingWithinPagePicksTheLastMarkerOnThePage()
    {
        string body = MarkerBlock("A", "CHAP1") + MarkerBlock("B", "CHAP2") + Blocks(3);
        AreaTree tree = LayOut(Document(body, "last-starting-within-page"));

        PageArea page1 = tree.Pages[0];
        Assert.True(HeaderHas(page1, "CHAP2"), "last-starting should pick the last marker on the page");
        Assert.False(HeaderHas(page1, "CHAP1"));
    }

    [Fact]
    public void CarryoverSuppliesLastMarkerFromEarlierPageWhenCurrentPageHasNone()
    {
        // Only page 1 has a marker (CHAP1). Page 2 has no marker of the class; with
        // first-including-carryover its header falls back to the carried-over CHAP1.
        string body = MarkerBlock("Start", "CHAP1") + Blocks(30);
        AreaTree tree = LayOut(Document(body, "first-including-carryover"));

        Assert.True(tree.Pages.Count >= 2, $"expected >= 2 pages, got {tree.Pages.Count}");
        Assert.True(HeaderHas(tree.Pages[1], "CHAP1"),
            "page 2 header should carry over CHAP1 from page 1");
    }

    [Fact]
    public void FirstStartingWithinPageDoesNotCarryOver()
    {
        // Only page 1 has CHAP1. Page 2 has no marker; first-starting-within-page renders nothing.
        string body = MarkerBlock("Start", "CHAP1") + Blocks(30);
        AreaTree tree = LayOut(Document(body, "first-starting-within-page"));

        Assert.True(tree.Pages.Count >= 2);
        Assert.False(HeaderHas(tree.Pages[1], "CHAP1"),
            "first-starting-within-page must not fall back to a carried-over marker");
    }

    [Fact]
    public void RegionStartStaticContentRendersInTheLeftBand()
    {
        // A region-start band of 30pt down the left edge; the body is inset by a 30pt left margin so
        // it does not overlap the band. The side static content renders within [0, 30pt] horizontally.
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body margin-left="30pt"/>
                  <fo:region-start extent="30pt"/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:static-content flow-name="xsl-region-start">
                  <fo:block>SIDE</fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body"><fo:block>BODY</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);

        TextRun side = Assert.Single(page.TextRuns, r => r.Text == "SIDE");
        // Left band spans x in [0, 30pt]; the side word begins at the band left edge (x = 0).
        Assert.True(side.XMpt >= 0 && side.XMpt < 30_000,
            $"side content x {side.XMpt} not in the left band [0, 30000)");

        // Body content is inset by the 30pt left margin, so it starts at x >= 30pt.
        TextRun bodyRun = Assert.Single(page.TextRuns, r => r.Text == "BODY");
        Assert.True(bodyRun.XMpt >= 30_000, $"body x {bodyRun.XMpt} should be right of the left band");
    }

    [Fact]
    public void RegionEndStaticContentRendersInTheRightBand()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" font-size="10pt">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body margin-right="30pt"/>
                  <fo:region-end extent="30pt"/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:static-content flow-name="xsl-region-end">
                  <fo:block>EDGE</fo:block>
                </fo:static-content>
                <fo:flow flow-name="xsl-region-body"><fo:block>BODY</fo:block></fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);

        // The right band starts at x = 200pt - 30pt = 170pt.
        TextRun edge = Assert.Single(page.TextRuns, r => r.Text == "EDGE");
        Assert.True(edge.XMpt >= 170_000, $"end content x {edge.XMpt} not in the right band [170000, 200000]");
    }
}
