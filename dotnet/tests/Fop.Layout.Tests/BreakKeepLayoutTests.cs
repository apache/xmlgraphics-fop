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
/// Layout tests for <c>break-before</c>/<c>break-after</c> and <c>keep-together</c> over the
/// deterministic <see cref="FakeFontMeasurer"/> (glyph 500u@1000mpt, ascender 800, descender 200).
/// A 10pt line is 12000mpt tall with a baseline 9000mpt below its top.
/// </summary>
public sealed class BreakKeepLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    /// <summary>Page sized exactly to the content box (zero margins) at 10pt default font size.</summary>
    private static string Document(string body, double pageWidthPt = 200, double pageHeightPt = 200) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format" color="#000000" font-size="10pt">
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

    // ----- break-before ------------------------------------------------------------------

    [Fact]
    public void BreakBeforePage_StartsNewPage()
    {
        string body = """
            <fo:block>first</fo:block>
            <fo:block break-before="page">second</fo:block>
            """;
        AreaTree tree = LayOut(Document(body));
        Assert.Equal(2, tree.Pages.Count);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text == "first");
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text == "second");
    }

    [Fact]
    public void BreakBeforePage_IsNoOpAtPageTop()
    {
        // The very first block carries break-before=page; the page is fresh so no blank page appears.
        string body = """
            <fo:block break-before="page">only</fo:block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "only");
    }

    // ----- break-after -------------------------------------------------------------------

    [Fact]
    public void BreakAfterPage_PushesNextObjectToNewPage()
    {
        string body = """
            <fo:block break-after="page">first</fo:block>
            <fo:block>second</fo:block>
            """;
        AreaTree tree = LayOut(Document(body));
        Assert.Equal(2, tree.Pages.Count);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text == "first");
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text == "second");
    }

    // ----- even/odd page -----------------------------------------------------------------

    [Fact]
    public void BreakBeforeEvenPage_InsertsBlankPageToReachEvenNumber()
    {
        // first lands on page 1 (odd). break-before=even-page must move "second" to page 2.
        string body = """
            <fo:block>first</fo:block>
            <fo:block break-before="even-page">second</fo:block>
            """;
        AreaTree tree = LayOut(Document(body));
        Assert.Equal(2, tree.Pages.Count);
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text == "second");
    }

    [Fact]
    public void BreakBeforeOddPage_InsertsBlankPageToReachOddNumber()
    {
        // first on page 1; a plain page break would put "second" on page 2 (even). odd-page must skip
        // to page 3, leaving page 2 blank.
        string body = """
            <fo:block>first</fo:block>
            <fo:block break-before="odd-page">second</fo:block>
            """;
        AreaTree tree = LayOut(Document(body));
        Assert.Equal(3, tree.Pages.Count);
        Assert.Empty(tree.Pages[1].TextRuns); // blank filler page
        Assert.Contains(tree.Pages[2].TextRuns, r => r.Text == "second");
    }

    // ----- keep-together -----------------------------------------------------------------

    [Fact]
    public void KeepTogether_MovesBlockWholeToNextPage_WhenItDoesNotFit()
    {
        // Page content height 50pt = 50000, width 45pt = 45000. Each 2-char word is 10000mpt, a space
        // 5000mpt, so a line holds 3 words (40000). Leading block of 7 words = 3 lines = 36000, leaving
        // 14000. The keep block of 6 words = 2 lines = 24000, which does NOT fit in 14000 but fits on an
        // empty page, so it must move whole to page 2 (both lines together), not split.
        string body = """
            <fo:block>l1 l2 l3 l4 l5 l6 l7</fo:block>
            <fo:block keep-together="always">k1 k2 k3 k4 k5 k6</fo:block>
            """;
        string fo = Document(body, pageWidthPt: 45, pageHeightPt: 50);
        AreaTree tree = LayOut(fo);

        Assert.Equal(2, tree.Pages.Count);

        // The keep block's words must all be on the SAME page (page 2) -- not split across pages.
        bool[] hasKeepWord = new bool[2];
        for (int p = 0; p < 2; p++)
        {
            hasKeepWord[p] = tree.Pages[p].TextRuns.Any(r => r.Text.Contains('k'));
        }

        Assert.False(hasKeepWord[0]); // nothing of the keep block on page 1
        Assert.True(hasKeepWord[1]);  // the whole keep block on page 2
    }

    [Fact]
    public void KeepTogether_StaysOnSamePage_WhenItFits()
    {
        // A short keep block that fits with the leading content stays on page 1.
        string body = """
            <fo:block>top</fo:block>
            <fo:block keep-together="always">keep</fo:block>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200, pageHeightPt: 200));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Contains(page.TextRuns, r => r.Text == "top");
        Assert.Contains(page.TextRuns, r => r.Text == "keep");
    }

    [Fact]
    public void KeepTogether_TallerThanPage_FallsBackToLineSplitting()
    {
        // Page content height 50pt = 50000 => 4 lines (48000) fit per page. A keep-together block of 8
        // lines (96000) is taller than a full page, so the keep cannot be honoured without overflow:
        // it must degrade to the normal line-by-line walk and split across pages rather than overflow
        // a single page.
        string lines = string.Join(' ', Enumerable.Range(1, 8).Select(i => $"k{i}"));
        string body = $"""
            <fo:block keep-together="always">{lines}</fo:block>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 20, pageHeightPt: 50));

        // The block paginates instead of overflowing: its lines appear on more than one page.
        Assert.True(tree.Pages.Count >= 2);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text.Contains('k'));
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text.Contains('k'));

        // No page is over-filled: every line top stays within the content box (no overflow).
        foreach (PageArea p in tree.Pages)
        {
            foreach (TextRun run in p.TextRuns)
            {
                // baseline 9000 below a line top; the line top must be < content bottom (50000).
                Assert.True(run.BaselineYMpt - 9_000 < 50_000 + 0.001);
            }
        }
    }

    [Fact]
    public void NonKeepBlock_StillSplitsLineByLine()
    {
        // Without keep-together, a block taller than the remaining space splits across pages.
        // Page height 50000. Leading 3 lines (36000) leaves 14000 = room for one more line. The second
        // block's two lines therefore split: line 1 on page 1, line 2 on page 2.
        // widows/orphans disabled so the split is purely greedy line-by-line (tested separately).
        string body = """
            <fo:block widows="1" orphans="1">l1 l2 l3 l4 l5 l6 l7</fo:block>
            <fo:block widows="1" orphans="1">k1 k2 k3 k4 k5 k6</fo:block>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 45, pageHeightPt: 50));
        // The second block is allowed to split, so its lines appear across both pages.
        Assert.Equal(2, tree.Pages.Count);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text.Contains('k'));
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text.Contains('k'));
    }
}
