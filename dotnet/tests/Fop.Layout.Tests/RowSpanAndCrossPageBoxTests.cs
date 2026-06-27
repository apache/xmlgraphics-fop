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
/// Layout tests for table row-spanning and for box painting across a page break, over the
/// deterministic <see cref="FakeFontMeasurer"/> (glyph 500u@1000mpt, ascender 800, descender 200).
/// </summary>
public sealed class RowSpanAndCrossPageBoxTests
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

    // ----- row spanning ------------------------------------------------------------------

    [Fact]
    public void RowSpanningCell_FollowingRowSkipsOccupiedColumn()
    {
        // 3 columns x 100pt. Row 1: A spans 2 rows in column 0, then B, C. Row 2 has two cells, which
        // must fall in columns 1 and 2 (column 0 still occupied by A), at x = 100000 and 200000.
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell number-rows-spanned="2"><fo:block>A</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>B</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>C</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>D</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>E</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        TextRun a = page.TextRuns.Single(r => r.Text == "A");
        TextRun d = page.TextRuns.Single(r => r.Text == "D");
        TextRun e = page.TextRuns.Single(r => r.Text == "E");

        // A in column 0; the row-2 cells skip column 0 and land in columns 1 and 2.
        Assert.Equal(0, a.XMpt, 3);
        Assert.Equal(100_000, d.XMpt, 3);
        Assert.Equal(200_000, e.XMpt, 3);

        // Row 2's content baseline is below row 1 (rowTop 12000 + 9000 = 21000).
        Assert.Equal(21_000, d.BaselineYMpt, 3);
    }

    [Fact]
    public void RowSpanningCell_BoxCoversSpannedRows()
    {
        // A spanning cell with a background covers both rows: its background height is the sum of the
        // two row heights = 24000 (each row a single 12000-tall line).
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell number-rows-spanned="2" background-color="#ff0000"><fo:block>A</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>B</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>D</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        RectFill spanBg = page.RectFills.Single(r => r.Color.Red == 255 && r.Color.Green == 0);
        Assert.Equal(0, spanBg.YMpt, 3);
        Assert.Equal(24_000, spanBg.HeightMpt, 3); // two rows combined
        Assert.Equal(100_000, spanBg.WidthMpt, 3); // single column
    }

    [Fact]
    public void RowSpanningCell_GrowsRowsWhenTallerThanContent()
    {
        // The spanning cell has two lines (24000) but each ordinary row would only be 12000 tall.
        // The covered rows must grow so their combined height is at least 24000; the second row's
        // content therefore starts no earlier than the first row's natural height.
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="100pt"/>
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell number-rows-spanned="2"><fo:block>P</fo:block><fo:block>Q</fo:block></fo:table-cell>
                  <fo:table-cell><fo:block>B</fo:block></fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>D</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        // Row 1 = 12000 (single line "B"); the spanning cell needs 24000, so row 2 grows to 12000.
        // Row 2 starts at 12000 => "D" baseline = 12000 + 9000 = 21000.
        TextRun d = page.TextRuns.Single(r => r.Text == "D");
        Assert.Equal(21_000, d.BaselineYMpt, 3);
    }

    // ----- cross-page box painting -------------------------------------------------------

    [Fact]
    public void BorderedBlockSpanningPageBreak_PaintsBoxSegmentOnEachPage()
    {
        // Page content height 50pt = 50000. A bordered block of 6 lines (72000) overflows onto a second
        // page, so its border box must be painted on BOTH pages.
        string lines = string.Concat(Enumerable.Range(0, 6).Select(i => $"<fo:block>w{i}</fo:block>"));
        string body = $"""
            <fo:block border="2pt solid #0000ff" background-color="#00ff00">
              {lines}
            </fo:block>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 50));
        Assert.Equal(2, tree.Pages.Count);

        // Each page carries a green background segment for the block.
        Assert.Contains(tree.Pages[0].RectFills, r => r.Color.Green == 255 && r.Color.Blue == 0);
        Assert.Contains(tree.Pages[1].RectFills, r => r.Color.Green == 255 && r.Color.Blue == 0);

        // The top border (a thin full-width blue rect at the box top) appears only on the first page.
        static bool IsTopBorder(RectFill r) =>
            r.Color.Blue == 255 && r.Color.Red == 0 && r.HeightMpt is > 0 and <= 2_000 && r.WidthMpt > 50_000;

        int topBordersPage0 = tree.Pages[0].RectFills.Count(IsTopBorder);
        int topBordersPage1 = tree.Pages[1].RectFills.Count(IsTopBorder);

        // Page 0 has a top border at y=0; page 1 has a bottom border near its content bottom but no top
        // border at y=0.
        Assert.Contains(tree.Pages[0].RectFills, r => IsTopBorder(r) && r.YMpt == 0);
        Assert.DoesNotContain(tree.Pages[1].RectFills, r => IsTopBorder(r) && r.YMpt == 0);
        Assert.True(topBordersPage0 >= 1);
        _ = topBordersPage1;

        // Side borders (tall thin blue rects) appear on both pages.
        static bool IsSideBorder(RectFill r) => r.Color.Blue == 255 && r.WidthMpt is > 0 and <= 2_000 && r.HeightMpt > 2_000;
        Assert.Contains(tree.Pages[0].RectFills, IsSideBorder);
        Assert.Contains(tree.Pages[1].RectFills, IsSideBorder);
    }

    [Fact]
    public void TableSpanningPageBreak_PaintsOwnBoxOnEachPage()
    {
        // A bordered table whose rows overflow onto a second page must paint its own border box on each
        // page it covers.
        string rows = string.Concat(Enumerable.Range(0, 6).Select(i =>
            $"<fo:table-row><fo:table-cell><fo:block>r{i}</fo:block></fo:table-cell></fo:table-row>"));
        string body = $"""
            <fo:table width="100pt" border="2pt solid #0000ff" background-color="#00ff00">
              <fo:table-column column-width="100pt"/>
              <fo:table-body>
                {rows}
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100, pageHeightPt: 50));
        Assert.Equal(2, tree.Pages.Count);

        // Table background segment on each page.
        Assert.Contains(tree.Pages[0].RectFills, r => r.Color.Green == 255 && r.Color.Blue == 0);
        Assert.Contains(tree.Pages[1].RectFills, r => r.Color.Green == 255 && r.Color.Blue == 0);
    }
}
