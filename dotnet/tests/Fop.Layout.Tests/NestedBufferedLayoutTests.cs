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
/// Layout tests for tables and lists nested inside buffered (non-paginating) contexts -- a table cell,
/// a list-item body -- exercising the unified target-driven block/table/list walk. Uses the
/// deterministic <see cref="FakeFontMeasurer"/> (glyph 500u@1000mpt, ascender 800, descender 200): a
/// single 10pt character advances 5000 mpt, a 10pt line is 12000 mpt tall and its baseline sits 9000
/// mpt below the line top.
/// </summary>
public sealed class NestedBufferedLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string body, double pageWidthPt = 400, double pageHeightPt = 1000) => $"""
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

    // ----- table nested inside a table cell ----------------------------------------------

    [Fact]
    public void TableInsideTableCell_RendersInnerCellText()
    {
        // Outer table: single 300pt column. The outer cell has 10pt padding (inset 10000) so the inner
        // table's content origin is offset by the outer cell inset. Inner table: single 100pt column,
        // one cell containing "z" (no insets) => inner "z" content origin local (0,0). Flushed to the
        // outer cell content origin (x = 0 + 10000, y = 0 + 10000), then baseline 9000 below top.
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="300pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell padding="10pt">
                    <fo:table width="100pt">
                      <fo:table-column column-width="100pt"/>
                      <fo:table-body>
                        <fo:table-row><fo:table-cell><fo:block>z</fo:block></fo:table-cell></fo:table-row>
                      </fo:table-body>
                    </fo:table>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun z = page.TextRuns.Single(r => r.Text == "z");
        Assert.Equal(10_000, z.XMpt, 3);               // outer cell left padding
        Assert.Equal(19_000, z.BaselineYMpt, 3);       // 10000 top padding + 9000 baseline
    }

    [Fact]
    public void TableInsideTableCell_InnerHeightGrowsOuterRow()
    {
        // The inner table has two rows (two 12000-tall lines = 24000). The outer cell therefore measures
        // 24000 tall, so a second outer row's content starts at y = 24000 (baseline 24000 + 9000).
        string body = """
            <fo:table width="200pt">
              <fo:table-column column-width="200pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:table width="100pt">
                      <fo:table-column column-width="100pt"/>
                      <fo:table-body>
                        <fo:table-row><fo:table-cell><fo:block>a</fo:block></fo:table-cell></fo:table-row>
                        <fo:table-row><fo:table-cell><fo:block>b</fo:block></fo:table-cell></fo:table-row>
                      </fo:table-body>
                    </fo:table>
                  </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                  <fo:table-cell><fo:block>outer2</fo:block></fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun outer2 = page.TextRuns.Single(r => r.Text == "outer2");
        Assert.Equal(33_000, outer2.BaselineYMpt, 3); // outer row 1 height 24000 + baseline 9000
    }

    // ----- list nested inside a table cell -----------------------------------------------

    [Fact]
    public void ListInsideTableCell_LaysOutLabelAndBody()
    {
        // A list inside a cell. Default provisional distance 24pt => body origin offset 24000 from the
        // list start edge. The cell has no insets, so the list start edge is the cell content origin (0).
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="300pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:list-block>
                      <fo:list-item>
                        <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                        <fo:list-item-body><fo:block>item</fo:block></fo:list-item-body>
                      </fo:list-item>
                    </fo:list-block>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun label = page.TextRuns.Single(r => r.Text == "*");
        TextRun item = page.TextRuns.Single(r => r.Text == "item");
        Assert.Equal(0, label.XMpt, 3);
        Assert.Equal(24_000, item.XMpt, 3);
        Assert.Equal(9_000, label.BaselineYMpt, 3);
        Assert.Equal(9_000, item.BaselineYMpt, 3);
    }

    // ----- table nested inside a list-item body ------------------------------------------

    [Fact]
    public void TableInsideListItemBody_LaysOut()
    {
        // A table directly inside a list-item body. The body column starts at the provisional distance
        // (24000), so the inner table's single cell text "t" begins at the body origin x = 24000.
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body>
                  <fo:table width="100pt">
                    <fo:table-column column-width="100pt"/>
                    <fo:table-body>
                      <fo:table-row><fo:table-cell><fo:block>t</fo:block></fo:table-cell></fo:table-row>
                    </fo:table-body>
                  </fo:table>
                </fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun t = page.TextRuns.Single(r => r.Text == "t");
        Assert.Equal(24_000, t.XMpt, 3);
        Assert.Equal(9_000, t.BaselineYMpt, 3);
    }

    [Fact]
    public void TableInsideListItemBody_HeightContributesToItem()
    {
        // The body's table has two rows (24000 tall), taller than the one-line label. The item height is
        // therefore 24000, so a second item starts at y = 24000 (baseline 24000 + 9000 = 33000).
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body>
                  <fo:table width="100pt">
                    <fo:table-column column-width="100pt"/>
                    <fo:table-body>
                      <fo:table-row><fo:table-cell><fo:block>a</fo:block></fo:table-cell></fo:table-row>
                      <fo:table-row><fo:table-cell><fo:block>b</fo:block></fo:table-cell></fo:table-row>
                    </fo:table-body>
                  </fo:table>
                </fo:list-item-body>
              </fo:list-item>
              <fo:list-item>
                <fo:list-item-label><fo:block>+</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>second</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun secondLabel = page.TextRuns.Single(r => r.Text == "+");
        Assert.Equal(33_000, secondLabel.BaselineYMpt, 3);
    }

    // ----- list nested inside a list-item body (regression for the unified walk) ----------

    [Fact]
    public void ListInsideListItemBody_StillNests()
    {
        // A list nested directly in a list-item body: outer body origin 24000, inner list body a further
        // 24000 in => inner body content origin at 48000 (matches the pre-existing nested-list test).
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body>
                  <fo:list-block>
                    <fo:list-item>
                      <fo:list-item-label><fo:block>-</fo:block></fo:list-item-label>
                      <fo:list-item-body><fo:block>inner</fo:block></fo:list-item-body>
                    </fo:list-item>
                  </fo:list-block>
                </fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun innerBody = page.TextRuns.Single(r => r.Text == "inner");
        Assert.Equal(48_000, innerBody.XMpt, 3);
    }

    // ----- nested table box emission ------------------------------------------------------

    [Fact]
    public void TableInsideTableCell_InnerCellBoxEmitsRectFills()
    {
        // The inner cell carries a background; its RectFill must appear in the page output offset to the
        // outer cell content origin (proving buffered box emission flows through for nested tables).
        string body = """
            <fo:table width="300pt">
              <fo:table-column column-width="300pt"/>
              <fo:table-body>
                <fo:table-row>
                  <fo:table-cell>
                    <fo:table width="100pt">
                      <fo:table-column column-width="100pt"/>
                      <fo:table-body>
                        <fo:table-row>
                          <fo:table-cell background-color="#00ff00"><fo:block>z</fo:block></fo:table-cell>
                        </fo:table-row>
                      </fo:table-body>
                    </fo:table>
                  </fo:table-cell>
                </fo:table-row>
              </fo:table-body>
            </fo:table>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        RectFill bg = page.RectFills.Single(r => r.Color.Green == 255 && r.Color.Red == 0 && r.Color.Blue == 0);
        Assert.Equal(0, bg.XMpt, 3);
        Assert.Equal(100_000, bg.WidthMpt, 3); // inner column width
    }
}
