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
/// Layout tests for <c>fo:table</c> over the deterministic <see cref="FakeFontMeasurer"/> (500mpt
/// glyph at 1000mpt size, ascender 800, descender 200). All geometry is therefore predictable: a
/// single character at 10pt advances 5000 mpt and a default line is 12000 mpt tall.
/// </summary>
public sealed class TableLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string body, double pageWidthPt = 300, double pageHeightPt = 1000) => $"""
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

    // ----- column width distribution -----------------------------------------------------

    [Fact]
    public void EqualDistribution_WhenNoColumnWidths()
    {
        // page width 300pt = 300000; three cells, no fo:table-column => each column 100000 wide.
        // Cells contain a single char "x" => TextRun X at the column's content origin.
        string body = """
            <fo:table>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>c</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        var xs = page.TextRuns.OrderBy(r => r.XMpt).Select(r => r.XMpt).ToList();
        Assert.Equal(3, xs.Count);
        Assert.Equal(0, xs[0], 3);
        Assert.Equal(100_000, xs[1], 3);
        Assert.Equal(200_000, xs[2], 3);
    }

    [Fact]
    public void AbsoluteColumnWidths_PositionCells()
    {
        // columns 60pt and 240pt => second column content origin at 60000.
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="60pt"/>
              <fo:table-column column-width="240pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        var xs = page.TextRuns.OrderBy(r => r.XMpt).Select(r => r.XMpt).ToList();
        Assert.Equal(0, xs[0], 3);
        Assert.Equal(60_000, xs[1], 3);
    }

    [Fact]
    public void PercentAndProportionalColumnWidths()
    {
        // table width 300pt. col1 = 20% = 60000. col2/col3 proportional 1 and 3 share remaining 240000
        // => col2 = 60000 (starts at 60000), col3 = 180000 (starts at 120000).
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="20%"/>
              <fo:table-column column-width="proportional-column-width(1)"/>
              <fo:table-column column-width="proportional-column-width(3)"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>c</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        var xs = page.TextRuns.OrderBy(r => r.XMpt).Select(r => r.XMpt).ToList();
        Assert.Equal(0, xs[0], 3);
        Assert.Equal(60_000, xs[1], 3);
        Assert.Equal(120_000, xs[2], 3);
    }

    // ----- cell content laid out within cell width ---------------------------------------

    [Fact]
    public void CellContent_WrapsWithinCellWidth()
    {
        // Each column 100000 wide. Cell text "aaaa aaaa aaaa aaaa" at 10pt: each word 4 chars = 20000.
        // 100000 fits ~ 4 words but with spaces (5000 each) -> "aaaa aaaa aaaa" = 20000+5000+20000+5000+20000 = 70000,
        // adding a 4th would be +5000+20000=95000 still fits... use narrower to force wrap.
        string body = """
            <fo:table width="100pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>aaaaaaaaaaaaaaaaaaaa bbbbbbbbbbbbbbbbbbbb</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        // each 20-char word = 100000 wide == column width; so two words wrap to two lines.
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Equal(2, page.TextRuns.Count);
        // The two lines have different baselines (stacked).
        Assert.NotEqual(page.TextRuns[0].BaselineYMpt, page.TextRuns[1].BaselineYMpt);
    }

    // ----- row height = tallest cell -----------------------------------------------------

    [Fact]
    public void RowHeight_IsTallestCell()
    {
        // Cell 1: one line (12000). Cell 2: two blocks (two lines, 24000). Row height = 24000.
        // The second row's cell content should start at y = 24000.
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block><fo:block>c</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>d</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>e</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        // baseline of a single 10pt line: leading (12000-10000)/2 = 1000, ascender 8000 => 9000 from top.
        // Row 1 cells baseline at 9000. Row 2 starts at row height 24000 => baseline 33000.
        TextRun rowTwoCell = page.TextRuns.Single(r => r.Text == "d");
        Assert.Equal(33_000, rowTwoCell.BaselineYMpt, 3);
    }

    [Fact]
    public void RowHeight_RespectsMinimumHeight()
    {
        // Row height attribute 50pt = 50000 > content (12000). Second row baseline = 50000 + 9000.
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row height="50pt">
                  <fo:table-cell><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>d</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>e</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);
        TextRun rowTwoCell = page.TextRuns.Single(r => r.Text == "d");
        Assert.Equal(59_000, rowTwoCell.BaselineYMpt, 3);
    }

    // ----- grid x/y positions ------------------------------------------------------------

    [Fact]
    public void GridPositions_CellContentOffsetByInsets()
    {
        // Cell with border 1pt + padding 2pt = 3pt inset = 3000 mpt.
        // Column 0 content origin: x = 0 + 3000 = 3000. y = rowTop(0) + topInset(3000) + baseline(9000).
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell border="1pt solid #000000" padding="2pt"><fo:block>a</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);
        TextRun run = page.TextRuns.Single(r => r.Text == "a");
        Assert.Equal(3_000, run.XMpt, 3);
        Assert.Equal(12_000, run.BaselineYMpt, 3); // 3000 + 9000
    }

    // ----- cell border / background RectFills --------------------------------------------

    [Fact]
    public void CellBackgroundAndBorder_EmitRectFills()
    {
        string body = """
            <fo:table width="100pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell background-color="#00ff00" border="1pt solid #000000"><fo:block>a</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);

        // 1 background + 4 borders for the single cell.
        Assert.Equal(5, page.RectFills.Count);
        RectFill bg = page.RectFills[0];
        Assert.Equal(0, bg.XMpt, 3);
        Assert.Equal(0, bg.YMpt, 3);
        Assert.Equal(100_000, bg.WidthMpt, 3);
        Assert.Equal(255, bg.Color.Green);
    }

    // ----- column spanning ---------------------------------------------------------------

    [Fact]
    public void ColumnSpanning_WidensCell()
    {
        // Three 100pt columns. First cell spans 2 columns => width 200000, content origin x=0.
        // Second cell sits in column 2 => content origin x = 200000.
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell number-columns-spanned="2" background-color="#ff0000"><fo:block>a</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>b</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        TextRun a = page.TextRuns.Single(r => r.Text == "a");
        TextRun b = page.TextRuns.Single(r => r.Text == "b");
        Assert.Equal(0, a.XMpt, 3);
        Assert.Equal(200_000, b.XMpt, 3);

        // The spanned cell background covers two columns = 200000 wide.
        RectFill bg = page.RectFills.Single(r => r.Color.Red == 255 && r.Color.Green == 0);
        Assert.Equal(200_000, bg.WidthMpt, 3);
    }

    // ----- pagination --------------------------------------------------------------------

    [Fact]
    public void MultiRow_PaginatesWhenRowsOverflow()
    {
        // Page content height = 50pt = 50000. Each row is one line tall (12000). 5 rows = 60000 > 50000,
        // so the table spills onto a second page.
        string rows = string.Concat(Enumerable.Range(0, 5).Select(i =>
            $"<fo:table-row><fo:table-cell><fo:block>r{i}</fo:block></fo:table-cell></fo:table-row>"));
        string body = $"""
            <fo:table width="100pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                {rows}
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 50));
        Assert.Equal(2, tree.Pages.Count);

        // First page holds the rows that fit (50000/12000 = 4 rows), second page the rest.
        Assert.Equal(4, tree.Pages[0].TextRuns.Count);
        Assert.Single(tree.Pages[1].TextRuns);
    }

    [Fact]
    public void Header_RepeatsOnEachPage()
    {
        // Header row + body rows overflowing onto a 2nd page. Header should appear on both pages.
        string rows = string.Concat(Enumerable.Range(0, 5).Select(i =>
            $"<fo:table-row><fo:table-cell><fo:block>r{i}</fo:block></fo:table-cell></fo:table-row>"));
        string body = $"""
            <fo:table width="100pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-header>
                <fo:table-row><fo:table-cell><fo:block>H</fo:block></fo:table-cell></fo:table-row>
              </fo:table-header>
              <fo:table-body>
                {rows}
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 50));
        Assert.Equal(2, tree.Pages.Count);
        Assert.Contains(tree.Pages[0].TextRuns, r => r.Text == "H");
        Assert.Contains(tree.Pages[1].TextRuns, r => r.Text == "H");
    }

    // ----- table participates in block flow ----------------------------------------------

    [Fact]
    public void Table_FollowsPrecedingBlockInFlow()
    {
        // A block (one 12000-tall line) then a table row. The table row's cell content starts below
        // the block: baseline = 12000 (block) + 9000 (cell line) = 21000.
        string body = """
            <fo:block>top</fo:block>
            <fo:table width="100pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row><fo:table-cell><fo:block>x</fo:block></fo:table-cell></fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);
        TextRun cell = page.TextRuns.Single(r => r.Text == "x");
        Assert.Equal(21_000, cell.BaselineYMpt, 3);
    }

    [Fact]
    public void Table_SpaceBeforeApplies()
    {
        string body = """
            <fo:block>top</fo:block>
            <fo:table width="100pt" space-before="10pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row><fo:table-cell><fo:block>x</fo:block></fo:table-cell></fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);
        TextRun cell = page.TextRuns.Single(r => r.Text == "x");
        // 12000 (block) + 10000 (space-before) + 9000 = 31000.
        Assert.Equal(31_000, cell.BaselineYMpt, 3);
    }

    // ----- intra-row splitting (a row taller than a full page) ----------------------------

    [Fact]
    public void OverTallRowSplitsAcrossPagesAtLineBoundaries()
    {
        // A single-cell row of six lines is 72000mpt tall; a zero-margin 40pt page holds 40000mpt, so
        // the row is taller than a whole page and must split rather than overflow. Three 12000mpt lines
        // (36000) fit per page, with the cut taken on the line boundary at 34000 (line 3's bottom).
        string body = """
            <fo:table>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:block>l1</fo:block><fo:block>l2</fo:block><fo:block>l3</fo:block>
                    <fo:block>l4</fo:block><fo:block>l5</fo:block><fo:block>l6</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 40));

        Assert.Equal(2, tree.Pages.Count);

        // Every line appears exactly once and none was lost in the split.
        var all = tree.Pages.SelectMany(p => p.TextRuns).Select(r => r.Text).OrderBy(t => t).ToList();
        Assert.Equal(new[] { "l1", "l2", "l3", "l4", "l5", "l6" }, all);

        // The first three lines stay on page 1, the last three move to page 2.
        Assert.Equal(new[] { "l1", "l2", "l3" },
            tree.Pages[0].TextRuns.OrderBy(r => r.BaselineYMpt).Select(r => r.Text).ToArray());
        Assert.Equal(new[] { "l4", "l5", "l6" },
            tree.Pages[1].TextRuns.OrderBy(r => r.BaselineYMpt).Select(r => r.Text).ToArray());

        // No line overflows the 40000mpt content region on its page (baseline + 2000 descender).
        foreach (PageArea p in tree.Pages)
        {
            foreach (TextRun run in p.TextRuns)
            {
                Assert.True(run.BaselineYMpt + 2_000 <= 40_000 + 0.5,
                    $"line {run.Text} at {run.BaselineYMpt} overflows the page");
            }
        }
    }

    [Fact]
    public void OverTallRowPaintsCellBackgroundOnEverySlice()
    {
        // The cell carries a background; when the row splits, each page must show a background slice
        // (a split box paints its background on every segment).
        string body = """
            <fo:table>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell background-color="#ddeeff">
                    <fo:block>l1</fo:block><fo:block>l2</fo:block><fo:block>l3</fo:block>
                    <fo:block>l4</fo:block><fo:block>l5</fo:block><fo:block>l6</fo:block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 40));

        Assert.Equal(2, tree.Pages.Count);
        foreach (PageArea p in tree.Pages)
        {
            Assert.Contains(p.RectFills, r => r.Color.Red == 0xDD && r.Color.Green == 0xEE && r.Color.Blue == 0xFF);
        }
    }
}
