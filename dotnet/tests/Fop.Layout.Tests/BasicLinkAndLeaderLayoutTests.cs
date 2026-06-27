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

using System.Linq;

using Fop.Fo;
using Fop.Layout;

using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Layout tests for <c>fo:basic-link</c> (LinkArea generation + target resolution) and
/// <c>fo:leader</c> (expansion to fill the line, dot/rule rendering) over the deterministic
/// <see cref="FakeFontMeasurer"/> (500 units/glyph at 1000mpt; the default font is 12pt, so a glyph
/// and a word-space are each 6000mpt wide).
/// </summary>
public sealed class BasicLinkAndLeaderLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>A single page-sequence on a page sized exactly to its content box (zero margins).</summary>
    private static string Document(string body, double pageWidthPt = 200, double pageHeightPt = 200) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="{pageWidthPt}pt" page-height="{pageHeightPt}pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              {body}
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    // ----- basic-link -------------------------------------------------------------------

    [Fact]
    public void ExternalLinkProducesLinkAreaCoveringTheText()
    {
        // "Hi" = 2 glyphs * 6000 = 12000 mpt wide, starting at content-left = 0.
        AreaTree tree = LayOut(Document(
            "<fo:block><fo:basic-link external-destination=\"url(https://example.com/)\">Hi</fo:basic-link></fo:block>"));

        PageArea page = Assert.Single(tree.Pages);
        LinkArea link = Assert.Single(page.Links);

        Assert.Null(link.TargetPageIndex);
        Assert.Equal("https://example.com/", link.Uri);
        Assert.Equal(0, link.XMpt, 3);
        Assert.Equal(12_000, link.WidthMpt, 3);
        Assert.True(link.HeightMpt > 0);

        // The link text is still emitted as a visible run.
        Assert.Contains(page.TextRuns, r => r.Text == "Hi");
    }

    [Fact]
    public void InternalLinkResolvesToTargetPageIndex()
    {
        // Page 1 carries the link; a forced page break puts the target block (id="dest") on page 2
        // (tree index 1). The internal link must resolve to that 0-based page index.
        AreaTree tree = LayOut(Document("""
            <fo:block><fo:basic-link internal-destination="dest">go</fo:basic-link></fo:block>
            <fo:block break-before="page" id="dest">target</fo:block>
            """));

        Assert.Equal(2, tree.Pages.Count);
        LinkArea link = Assert.Single(tree.Pages[0].Links);
        Assert.Equal(1, link.TargetPageIndex);
        Assert.Null(link.Uri);
    }

    [Fact]
    public void UnresolvedInternalDestinationProducesNoLinkArea()
    {
        AreaTree tree = LayOut(Document(
            "<fo:block><fo:basic-link internal-destination=\"missing\">x</fo:basic-link></fo:block>"));

        PageArea page = Assert.Single(tree.Pages);
        Assert.Empty(page.Links);
        // The text still renders.
        Assert.Contains(page.TextRuns, r => r.Text == "x");
    }

    [Fact]
    public void LinkSpanningTwoLinesProducesOneLinkAreaPerLine()
    {
        // Page content width = 24pt = 24000mpt -> at most 4 glyphs (4*6000) before a wrap.
        // Two 4-letter words inside the link wrap onto two lines, one LinkArea each.
        AreaTree tree = LayOut(Document(
            "<fo:block><fo:basic-link external-destination=\"u\">aaaa bbbb</fo:basic-link></fo:block>",
            pageWidthPt: 24));

        PageArea page = Assert.Single(tree.Pages);
        Assert.Equal(2, page.Links.Count);
        Assert.All(page.Links, l => Assert.Equal("u", l.Uri));
    }

    // ----- leader -----------------------------------------------------------------------

    [Fact]
    public void DotsLeaderFillsSlackSoTrailingTextEndsAtEndEdge()
    {
        // Content width 100pt = 100000mpt. "A" (6000) + leader + "42" (12000). The leader expands to
        // 100000 - 6000 - 12000 = 82000mpt, so "42" ends exactly at the end edge (x = 100000).
        AreaTree tree = LayOut(Document(
            "<fo:block>A<fo:leader leader-pattern=\"dots\"/>42</fo:block>",
            pageWidthPt: 100));

        PageArea page = Assert.Single(tree.Pages);

        TextRun trailing = page.TextRuns.Single(r => r.Text == "42");
        // "42" is 2 glyphs * 6000 = 12000 wide; its right edge is the line end edge (100000mpt).
        Assert.Equal(100_000, trailing.XMpt + 12_000, 1);

        // The dot run sits between "A" (ends at 6000) and "42" (starts at 88000), filling 82000mpt.
        TextRun dots = page.TextRuns.Single(r => r.Text.Length > 0 && r.Text[0] == '\u00B7');
        Assert.Equal(6_000, dots.XMpt, 1);
        // 82000 / 6000 = 13 whole dots.
        Assert.Equal(13, dots.Text.Length);
    }

    [Fact]
    public void RuleLeaderEmitsRectFillAcrossTheSlack()
    {
        // "A" (6000) + rule leader filling the rest of a 100pt (100000mpt) line.
        AreaTree tree = LayOut(Document(
            "<fo:block>A<fo:leader leader-pattern=\"rule\" rule-thickness=\"2pt\"/>B</fo:block>",
            pageWidthPt: 100));

        PageArea page = Assert.Single(tree.Pages);

        // The rule is a thin RectFill: width = 100000 - 6000 (A) - 6000 (B) = 88000mpt, height 2000mpt.
        RectFill rule = Assert.Single(page.RectFills);
        Assert.Equal(6_000, rule.XMpt, 1);
        Assert.Equal(88_000, rule.WidthMpt, 1);
        Assert.Equal(2_000, rule.HeightMpt, 1);

        // Trailing "B" ends at the line end edge.
        TextRun b = page.TextRuns.Single(r => r.Text == "B");
        Assert.Equal(100_000, b.XMpt + 6_000, 1);
    }

    [Fact]
    public void FixedLengthLeaderConsumesOnlyItsLength()
    {
        // Leader fixed at 30pt = 30000mpt. "A" at 0..6000, leader 6000..36000, "B" at 36000..42000.
        AreaTree tree = LayOut(Document(
            "<fo:block>A<fo:leader leader-pattern=\"dots\" leader-length=\"30pt\"/>B</fo:block>",
            pageWidthPt: 200));

        PageArea page = Assert.Single(tree.Pages);
        TextRun b = page.TextRuns.Single(r => r.Text == "B");
        Assert.Equal(36_000, b.XMpt, 1);
    }

    [Fact]
    public void SpaceLeaderPaintsNothingButStillPushesTrailingContent()
    {
        AreaTree tree = LayOut(Document(
            "<fo:block>A<fo:leader/>B</fo:block>",
            pageWidthPt: 100));

        PageArea page = Assert.Single(tree.Pages);
        // No rule, no dots.
        Assert.Empty(page.RectFills);
        Assert.DoesNotContain(page.TextRuns, r => r.Text.Length > 0 && r.Text[0] == '\u00B7');

        // Trailing "B" is still pushed to the end edge by the (invisible) leader.
        TextRun b = page.TextRuns.Single(r => r.Text == "B");
        Assert.Equal(100_000, b.XMpt + 6_000, 1);
    }
}
