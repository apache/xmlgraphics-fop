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
using System.Linq;

using Fop.Fo;
using Fop.Layout;

using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Tests that prove the line breaker is Knuth-Plass total-fit (optimal over the whole paragraph) and
/// not greedy first-fit. All run over the deterministic <see cref="FakeFontMeasurer"/> (500 units per
/// glyph at 1000mpt, so a 10pt glyph and the inter-word space are each 5000mpt).
/// </summary>
public sealed class TotalFitLineBreakingTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string body, double pageWidthPt, double pageHeightPt = 1000) => $"""
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

    /// <summary>The text of every run on the first page, in order (one entry per coalesced line run).</summary>
    private static List<string> RunsOf(AreaTree tree) =>
        tree.Pages[0].TextRuns.Select(r => r.Text).ToList();

    // ----- total-fit balances lines greedy would not ------------------------------------

    [Fact]
    public void TotalFit_BalancesLines_WhereGreedyWouldLeaveALooseLine()
    {
        // Words (10pt, glyph/space 5000mpt): "aaaaaaa"=35000, "aaaa"=20000, "aaaa"=20000, "a"=5000,
        // "aaaaaa"=30000. Content width 60pt = 60000mpt.
        //
        // GREEDY first-fit packs line 1 as full as possible:
        //   line1 "aaaaaaa aaaa" = 35000 + 5000 + 20000 = 60000 (exactly full)
        //   line2 "aaaa a"       = 20000 + 5000 + 5000  = 30000 (half empty -- a loose line)
        //   line3 "aaaaaa"       = 30000 (last)
        // TOTAL-FIT instead spreads the slack so no single line is badly loose:
        //   line1 "aaaaaaa"      = 35000
        //   line2 "aaaa aaaa a"  = 20000 + 5000 + 20000 + 5000 + 5000 = 55000 (nearly full)
        //   line3 "aaaaaa"       = 30000 (last)
        // Total-fit's middle line (55000) is far less ragged than greedy's (30000); the optimiser trades
        // a little looseness on line 1 to avoid a very loose line 2, minimising total demerits.
        string body = "<fo:block font-size=\"10pt\">aaaaaaa aaaa aaaa a aaaaaa</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 60));

        Assert.Equal(["aaaaaaa", "aaaa aaaa a", "aaaaaa"], RunsOf(tree));

        // Sanity: this is NOT the greedy split (which would start line 1 with "aaaaaaa aaaa").
        Assert.NotEqual("aaaaaaa aaaa", tree.Pages[0].TextRuns[0].Text);
    }

    [Fact]
    public void TotalFit_AvoidsGreedysVeryLooseMiddleLine_ThreeLineParagraph()
    {
        // Words: aaaaa=25000, aa=10000, aaaaaa=30000, aa=10000, aaaaaaa=35000, aaaaa=25000, aaaaaaa=35000.
        // Width 100pt = 100000mpt.
        //   GREEDY:  "aaaaa aa aaaaaa aa" (25000+5000+10000+5000+30000+5000+10000 = 90000, full)
        //            "aaaaaaa aaaaa"      (35000+5000+25000 = 65000  -- very loose, 35000 slack)
        //            "aaaaaaa"            (last)
        //   TOTAL-FIT: "aaaaa aa aaaaaa"  (25000+5000+10000+5000+30000 = 75000)
        //              "aa aaaaaaa aaaaa" (10000+5000+35000+5000+25000 = 80000)
        //              "aaaaaaa"          (last)
        // Total-fit moves the small "aa" down a line, turning one near-full + one very-loose line into
        // two evenly-filled lines -- a strictly lower total demerit, which greedy cannot find.
        string body = "<fo:block font-size=\"10pt\">aaaaa aa aaaaaa aa aaaaaaa aaaaa aaaaaaa</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));

        Assert.Equal(["aaaaa aa aaaaaa", "aa aaaaaaa aaaaa", "aaaaaaa"], RunsOf(tree));
    }

    // ----- emergency break: an over-long unbreakable word still lays out -----------------

    [Fact]
    public void OverLongWord_GetsItsOwnLine_AndNeighboursWrapAroundIt()
    {
        // The middle word is 20 glyphs = 100000mpt, far wider than the 40pt = 40000mpt line. No feasible
        // breaking exists, so the breaker falls back to its infinite-tolerance emergency pass: the
        // over-long word is placed on its own (overflowing) line and the short neighbours wrap cleanly
        // around it rather than being crammed onto the overfull line.
        string body = "<fo:block font-size=\"10pt\">aa bbbbbbbbbbbbbbbbbbbb cc</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 40));

        Assert.Equal(["aa", "bbbbbbbbbbbbbbbbbbbb", "cc"], RunsOf(tree));
    }

    [Fact]
    public void EmergencyBreak_PrefersLeavingAGap_OverOverflowingTheEdge()
    {
        // "ab" then a word too wide to share its line. With no feasible break, the emergency pass prefers
        // an underfull line (a ragged gap) to an overfull one (overprint): "ab" sits alone and the wide
        // word takes the next line, rather than both being forced onto one overfull line.
        string body = "<fo:block font-size=\"10pt\">ab cccccccc</fo:block>"; // cccccccc = 8 glyphs = 40000
        AreaTree tree = LayOut(Document(body, pageWidthPt: 30)); // 30000mpt; "ab cccccccc" = 55000 overfull

        Assert.Equal(["ab", "cccccccc"], RunsOf(tree));
    }

    // ----- justification distributes the computed glue stretch ---------------------------

    [Fact]
    public void Justify_DistributesStretchSoAStyledRunMovesToItsJustifiedPosition()
    {
        // A justified, non-last line "aa bb cc dd" (aa + bold bb + cc + dd) is 55000mpt wide on a 60pt =
        // 60000mpt line, so 5000mpt of slack is spread over its 3 inter-word gaps = 1666.67mpt extra per
        // gap. The bold "bb" run (a separate run, since its style differs) must therefore start at
        // 10000 (aa) + 5000 (space) + 1666.67 (distributed stretch) = 16666.67mpt -- proving the breaker
        // chose a non-last line that the existing justification path then stretches to the edge.
        string body =
            "<fo:block font-size=\"10pt\" text-align=\"justify\">aa " +
            "<fo:inline font-weight=\"bold\">bb</fo:inline> cc dd ee ff gg hh</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 60));
        PageArea page = tree.Pages[0];

        double firstY = page.TextRuns[0].BaselineYMpt;
        List<TextRun> line1 = page.TextRuns.Where(r => r.BaselineYMpt == firstY).ToList();

        TextRun bold = line1.Single(r => r.Font.IsBold);
        Assert.Equal("bb", bold.Text);
        Assert.Equal(10_000 + 5_000 + (5_000.0 / 3.0), bold.XMpt, 2);

        // The first run sits at the content-left edge (no leading slack on a justified line).
        Assert.Equal(0, line1[0].XMpt, 3);
    }

    // ----- hyphenation is used only when it improves the paragraph -----------------------

    /// <summary>A deterministic stub that hyphenates any word at the given fixed offsets.</summary>
    private sealed class StubHyphenator(params int[] points) : ILineHyphenator
    {
        public int[]? Hyphenate(string? language, string? country, string word, int remain, int push)
        {
            int[] valid = points.Where(p => p > 0 && p < word.Length).ToArray();
            return valid.Length > 0 ? valid : null;
        }
    }

    [Fact]
    public void Hyphenation_IsUsed_WhenItProducesABetterLine()
    {
        // "ab longword" on a 30pt = 30000mpt line. Adding the whole word to line 1 ("ab longword" =
        // 55000) overflows, so total-fit takes a flagged hyphen penalty. Of the two offsets (2, 4) it
        // chooses the one that does not overflow: "ab lo-" = 10000 + 5000 + "lo"(10000) + hyphen(5000) =
        // 30000 fills line 1 exactly, and "ngword" falls to line 2. (Offset 4 would give "ab long-" =
        // 40000, overfull, so the optimiser prefers offset 2.) The hyphen is worth its penalty here
        // because it avoids an overfull line -- exactly the trade-off Knuth-Plass weighs.
        var engine = new LayoutEngine(Measurer) { Hyphenator = new StubHyphenator(2, 4) };
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = engine.LayOut(FoTreeBuilder.ParseString(Document(body, pageWidthPt: 30)));

        Assert.Equal(["ab lo-", "ngword"], RunsOf(tree));
    }

    [Fact]
    public void Hyphenation_IsAvoided_WhenTheWholeWordFitsOnItsOwnLine()
    {
        // Same content and hyphenation points, but a wider 70pt = 70000mpt line on which the whole word
        // "longword" (40000) fits comfortably on line 2. Total-fit (unlike greedy, which would have
        // committed "ab" to line 1 and then split the word) keeps the word whole, because inserting a
        // needless hyphen only adds demerits. This is a defining total-fit behaviour: a hyphen is used
        // only when it genuinely improves the paragraph.
        var engine = new LayoutEngine(Measurer) { Hyphenator = new StubHyphenator(2, 4) };
        string body = "<fo:block font-size=\"10pt\" hyphenate=\"true\" language=\"en\">ab longword</fo:block>";
        AreaTree tree = engine.LayOut(FoTreeBuilder.ParseString(Document(body, pageWidthPt: 70)));

        Assert.Equal(["ab longword"], RunsOf(tree));
    }
}
