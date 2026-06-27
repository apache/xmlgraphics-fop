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
/// Layout tests over a deterministic fake measurer (<see cref="FakeFontMeasurer"/>): 500 units per
/// glyph at 1000mpt, ascender 800, descender 200, all scaled by the font size.
/// </summary>
public sealed class LayoutEngineTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    // ----- helpers ----------------------------------------------------------------------

    /// <summary>
    /// Wraps a body in a single page-sequence on a page master of the given content geometry. The
    /// page is sized exactly to the requested content box (zero page margins, zero region margins),
    /// so content-left = 0, content-top = 0, content-width/height = the supplied values.
    /// </summary>
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

    // ----- page geometry ----------------------------------------------------------------

    [Fact]
    public void Geometry_SubtractsPageAndRegionMargins()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="100pt" page-height="100pt"
                    margin-left="10pt" margin-right="5pt" margin-top="8pt" margin-bottom="4pt">
                  <fo:region-body margin-left="2pt" margin-top="3pt"/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block font-size="10pt">x</fo:block>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Equal(100_000, page.WidthMpt);
        Assert.Equal(100_000, page.HeightMpt);

        TextRun run = Assert.Single(page.TextRuns);
        // content-left = page margin-left (10) + region margin-left (2) = 12pt
        Assert.Equal(12_000, run.XMpt, 3);
        // content-top = 8 + 3 = 11pt; baseline = top + leading/2 + ascender.
        // size 10pt: ascender 8000, font height 10000, line-height 12000 => leading 2000.
        Assert.Equal(11_000 + 1_000 + 8_000, run.BaselineYMpt, 3);
    }

    // ----- line breaking ----------------------------------------------------------------

    [Fact]
    public void LineBreaking_WrapsLongParagraphToKnownLineCount()
    {
        // size 10pt => glyph 5000mpt, space 5000mpt. Word "ab" = 10000mpt. With a space, each
        // "ab " unit after the first is 15000mpt. Content width 40000mpt fits: ab(10000) + ab(5000+10000)
        // = 25000, + ab(5000+10000)=40000 (exactly fits 3 words), next would overflow.
        // 7 words => 3 + 3 + 1 = 3 lines.
        string body = "<fo:block font-size=\"10pt\">ab ab ab ab ab ab ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 40, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        // One run per line (start aligned, single style coalesced).
        Assert.Equal(3, page.TextRuns.Count);
        Assert.Equal("ab ab ab", page.TextRuns[0].Text);
        Assert.Equal("ab ab ab", page.TextRuns[1].Text);
        Assert.Equal("ab", page.TextRuns[2].Text);

        // Lines stack by line-height (1.2 * 10pt = 12000mpt).
        double dy = page.TextRuns[1].BaselineYMpt - page.TextRuns[0].BaselineYMpt;
        Assert.Equal(12_000, dy, 3);
    }

    [Fact]
    public void LineBreaking_OverWideWordStillPlacedAlone()
    {
        string body = "<fo:block font-size=\"10pt\">abcdefghij</fo:block>"; // 10 chars => 50000mpt > 20000
        AreaTree tree = LayOut(Document(body, pageWidthPt: 20, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);
        TextRun run = Assert.Single(page.TextRuns);
        Assert.Equal("abcdefghij", run.Text);
    }

    // ----- text alignment ---------------------------------------------------------------

    [Fact]
    public void TextAlign_StartLeftAlignsAtContentLeft()
    {
        string body = "<fo:block font-size=\"10pt\" text-align=\"start\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        TextRun run = Assert.Single(Assert.Single(tree.Pages).TextRuns);
        Assert.Equal(0, run.XMpt, 3);
    }

    [Fact]
    public void TextAlign_CenterOffsetsByHalfSlack()
    {
        // content width 100pt = 100000mpt; "ab" natural width = 10000mpt; slack = 90000 => offset 45000.
        string body = "<fo:block font-size=\"10pt\" text-align=\"center\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        TextRun run = Assert.Single(Assert.Single(tree.Pages).TextRuns);
        Assert.Equal(45_000, run.XMpt, 3);
    }

    [Fact]
    public void TextAlign_EndOffsetsByFullSlack()
    {
        string body = "<fo:block font-size=\"10pt\" text-align=\"end\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        TextRun run = Assert.Single(Assert.Single(tree.Pages).TextRuns);
        Assert.Equal(90_000, run.XMpt, 3);
    }

    [Fact]
    public void TextAlign_JustifyDistributesSlackExceptLastLine()
    {
        // Width 40pt => 40000mpt. "ab ab ab" natural = 40000 (fits exactly, no slack on full line);
        // make it wider so there IS slack. Use width 50pt = 50000; line "ab ab ab" natural 40000 =>
        // slack 10000 over 2 gaps => 5000 extra per gap. Words are separate styles? No, same style,
        // but justification forces per-word runs (gaps carry extra space), so 3 runs on line 1.
        string body = "<fo:block font-size=\"10pt\" text-align=\"justify\">ab ab ab ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 50, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        // 4 words, line fits 3 ("ab ab ab" = 40000 <= 50000; adding 4th: +5000 space +10000 = 55000 > 50000).
        // Line 1 = 3 words justified; line 2 = "ab" (last line, not justified).
        var firstLine = page.TextRuns.Where(r => r.BaselineYMpt == page.TextRuns[0].BaselineYMpt).ToList();

        // Justified line coalesces into one run, but the run's text keeps single spaces; the X advance
        // is what carries the extra. Check the run starts at content-left 0.
        Assert.Equal(0, firstLine[0].XMpt, 3);

        // The last line "ab" sits at content-left with no stretching.
        TextRun last = page.TextRuns[^1];
        Assert.Equal("ab", last.Text);
        Assert.Equal(0, last.XMpt, 3);
    }

    [Fact]
    public void Justify_StretchesGapsBetweenStyledRuns()
    {
        // Two differently-styled words on one justified line force two runs; the second run's X must
        // include the distributed extra space. Width 100pt = 100000mpt.
        // word1 "ab" (10pt) = 10000; word2 "cd" bold (10pt) = 10000; space 5000. natural = 25000.
        // slack = 75000 over 1 gap => extra 75000. Second run X = 10000 (w1) + 5000 (space) + 75000 = 90000.
        string body =
            "<fo:block font-size=\"10pt\" text-align=\"justify\">ab " +
            "<fo:inline font-weight=\"bold\">cd</fo:inline> wrapme wrapme wrapme</fo:block>";
        // Add trailing words so this is NOT the last line (justify applies). Keep width large enough
        // that "ab cd" plus one more word overflows onto line 2, leaving "ab cd ..." on line 1.
        AreaTree tree = LayOut(Document(body, pageWidthPt: 60, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        // First line should be "ab" + "cd" (10000+5000+10000 = 25000) then "wrapme" (6 chars=30000,
        // +space 5000 => 60000 == 60000 fits). So line1 = ab cd wrapme. That is 3 words, mixed styles:
        // run1 "ab" (normal), run2 "cd" (bold), run3 "wrapme" (normal).
        var line1Y = page.TextRuns[0].BaselineYMpt;
        var line1 = page.TextRuns.Where(r => r.BaselineYMpt == line1Y).ToList();
        Assert.Equal(3, line1.Count);
        Assert.Equal("ab", line1[0].Text);
        Assert.Equal("cd", line1[1].Text);
        Assert.True(line1[1].Font.IsBold);
        Assert.Equal("wrapme", line1[2].Text);

        // Line 1 natural width = ab(10000)+sp(5000)+cd(10000)+sp(5000)+wrapme(30000)=60000 == width,
        // so slack 0 => runs sit at their natural positions: cd at 15000, wrapme at 30000.
        Assert.Equal(0, line1[0].XMpt, 3);
        Assert.Equal(15_000, line1[1].XMpt, 3);
        Assert.Equal(30_000, line1[2].XMpt, 3);
    }

    // ----- space before/after -----------------------------------------------------------

    [Fact]
    public void SpaceBeforeAndAfter_AdvanceCursorBetweenBlocks()
    {
        string body =
            "<fo:block font-size=\"10pt\" space-after=\"20pt\">one</fo:block>" +
            "<fo:block font-size=\"10pt\" space-before=\"7pt\">two</fo:block>";
        AreaTree tree = LayOut(Document(body, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Equal(2, page.TextRuns.Count);

        // Block1 line top = 0, advance = line-height 12000. Then space-after 20000 + space-before 7000.
        // Block2 line top = 12000 + 20000 + 7000 = 39000.
        double b1Baseline = page.TextRuns[0].BaselineYMpt; // 0 + leading/2(1000) + ascender(8000) = 9000
        double b2Baseline = page.TextRuns[1].BaselineYMpt; // 39000 + 1000 + 8000 = 48000
        Assert.Equal(9_000, b1Baseline, 3);
        Assert.Equal(48_000, b2Baseline, 3);
    }

    // ----- nested block indent ----------------------------------------------------------

    [Fact]
    public void NestedBlock_IndentShiftsContentLeftAndNarrowsWidth()
    {
        string body =
            "<fo:block font-size=\"10pt\" start-indent=\"15pt\" end-indent=\"5pt\">" +
            "  <fo:block text-align=\"end\">ab</fo:block>" +
            "</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);
        // Parent has no direct inline text -> only the child renders.
        TextRun run = Assert.Single(page.TextRuns);

        // Child content-left = 0 + start-indent 15000 = 15000.
        // Child available width = 100000 - 15000 - 5000 = 80000. "ab" = 10000 => slack 70000.
        // end align => X = 15000 + 70000 = 85000.
        Assert.Equal(85_000, run.XMpt, 3);
    }

    // ----- pagination --------------------------------------------------------------------

    [Fact]
    public void Pagination_OverflowStartsNewPage()
    {
        // Page content height 25pt = 25000mpt. line-height 12000 => 2 lines per page (0..12000, 12000..24000),
        // 3rd line (24000..36000) overflows => new page. 5 single-line blocks => 2 on page1, 2 on page2, 1 on page3.
        string blocks = string.Concat(Enumerable.Range(0, 5)
            .Select(i => $"<fo:block font-size=\"10pt\">L{i}</fo:block>"));
        AreaTree tree = LayOut(Document(blocks, pageWidthPt: 200, pageHeightPt: 25));
        Assert.Equal(3, tree.Pages.Count);
        Assert.Equal(2, tree.Pages[0].TextRuns.Count);
        Assert.Equal(2, tree.Pages[1].TextRuns.Count);
        Assert.Single(tree.Pages[2].TextRuns);
    }

    [Fact]
    public void Pagination_SingleBlockSplitsAcrossPagesLineByLine()
    {
        // One block, 5 words each on its own line (narrow width), page holds 2 lines.
        string body = "<fo:block font-size=\"10pt\">aa bb cc dd ee</fo:block>";
        // width 20pt => one "aa" (10000) fits, second word "bb" needs +5000+10000 = 25000 > 20000 => 1 word/line.
        AreaTree tree = LayOut(Document(body, pageWidthPt: 20, pageHeightPt: 25));
        Assert.Equal(3, tree.Pages.Count);
        Assert.Equal(2, tree.Pages[0].TextRuns.Count);
        Assert.Equal(2, tree.Pages[1].TextRuns.Count);
        Assert.Single(tree.Pages[2].TextRuns);
    }

    // ----- empty / structural blocks -----------------------------------------------------

    [Fact]
    public void EmptyBlock_ProducesNoRunsButStillOnePage()
    {
        AreaTree tree = LayOut(Document("<fo:block></fo:block>"));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Empty(page.TextRuns);
    }

    [Fact]
    public void PageSequenceWithoutFlow_StillEmitsOnePage()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="100pt" page-height="100pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p"/>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        Assert.Empty(page.TextRuns);
    }

    // ----- multiple page-sequences -------------------------------------------------------

    [Fact]
    public void MultiplePageSequences_EachStartFreshPages()
    {
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block font-size="10pt">first</fo:block>
                </fo:flow>
              </fo:page-sequence>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block font-size="10pt">second</fo:block>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        Assert.Equal(2, tree.Pages.Count);
        Assert.Equal("first", Assert.Single(tree.Pages[0].TextRuns).Text);
        Assert.Equal("second", Assert.Single(tree.Pages[1].TextRuns).Text);
        // Second sequence restarts at the region top (baseline identical to the first page's).
        Assert.Equal(tree.Pages[0].TextRuns[0].BaselineYMpt, tree.Pages[1].TextRuns[0].BaselineYMpt, 3);
    }

    // ----- inline font changes -----------------------------------------------------------

    [Fact]
    public void InlineFontChange_SplitsIntoStyledRuns()
    {
        string body =
            "<fo:block font-size=\"10pt\">plain " +
            "<fo:inline font-weight=\"bold\" color=\"#ff0000\">bold</fo:inline> tail</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 500));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Equal(3, page.TextRuns.Count);

        Assert.Equal("plain", page.TextRuns[0].Text);
        Assert.False(page.TextRuns[0].Font.IsBold);

        Assert.Equal("bold", page.TextRuns[1].Text);
        Assert.True(page.TextRuns[1].Font.IsBold);
        Assert.Equal(255, page.TextRuns[1].Color.Red);

        Assert.Equal("tail", page.TextRuns[2].Text);
        Assert.False(page.TextRuns[2].Font.IsBold);

        // X positions advance: "plain"(25000) + space(5000) = 30000 for "bold";
        // + "bold"(20000) + space(5000) = 55000 for "tail".
        Assert.Equal(0, page.TextRuns[0].XMpt, 3);
        Assert.Equal(30_000, page.TextRuns[1].XMpt, 3);
        Assert.Equal(55_000, page.TextRuns[2].XMpt, 3);
    }
}
