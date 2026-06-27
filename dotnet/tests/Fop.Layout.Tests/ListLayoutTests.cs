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
/// Layout tests for <c>fo:list-block</c> over the deterministic <see cref="FakeFontMeasurer"/> (500mpt
/// glyph at 1000mpt size, ascender 800, descender 200). A single 10pt line is 12000 mpt tall and its
/// baseline sits 9000 mpt below the line top ((12000-10000)/2 leading + 8000 ascender).
/// </summary>
public sealed class ListLayoutTests
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

    // ----- label column width + body offset from provisional distances -------------------

    [Fact]
    public void LabelAtStart_BodyOffsetByProvisionalDistance()
    {
        // Default provisional-distance-between-starts = 24pt = 24000. Body content origin is at 24000.
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun label = page.TextRuns.Single(r => r.Text == "*");
        TextRun bodyRun = page.TextRuns.Single(r => r.Text == "x");
        Assert.Equal(0, label.XMpt, 3);
        Assert.Equal(24_000, bodyRun.XMpt, 3);

        // Both sit on the same line (item top 0): baseline 9000.
        Assert.Equal(9_000, label.BaselineYMpt, 3);
        Assert.Equal(9_000, bodyRun.BaselineYMpt, 3);
    }

    [Fact]
    public void OverriddenProvisionalDistance_MovesBody()
    {
        // provisional-distance-between-starts = 40pt => body origin at 40000.
        string body = """
            <fo:list-block provisional-distance-between-starts="40pt" provisional-label-separation="10pt">
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);
        TextRun bodyRun = page.TextRuns.Single(r => r.Text == "x");
        Assert.Equal(40_000, bodyRun.XMpt, 3);
    }

    [Fact]
    public void LabelColumnWidth_WrapsLabelContent()
    {
        // Default label column width = 24pt - 6pt = 18pt = 18000. A 4-char word at 10pt = 20000 > 18000,
        // so two 4-char words wrap to two lines within the label column.
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>aaaa bbbb</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        var labelRuns = page.TextRuns.Where(r => r.Text == "aaaa" || r.Text == "bbbb").ToList();
        Assert.Equal(2, labelRuns.Count);
        Assert.NotEqual(labelRuns[0].BaselineYMpt, labelRuns[1].BaselineYMpt);
    }

    // ----- item height = max(label, body) ------------------------------------------------

    [Fact]
    public void ItemHeight_IsMaxOfLabelAndBody()
    {
        // Item 1: label one line, body two blocks (two lines, 24000 tall) => item height 24000.
        // Item 2's label/body therefore start at y = 24000 => baseline 24000 + 9000 = 33000.
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>a</fo:block><fo:block>b</fo:block></fo:list-item-body>
              </fo:list-item>
              <fo:list-item>
                <fo:list-item-label><fo:block>+</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>c</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        TextRun secondLabel = page.TextRuns.Single(r => r.Text == "+");
        Assert.Equal(33_000, secondLabel.BaselineYMpt, 3);
    }

    // ----- item stacking with space ------------------------------------------------------

    [Fact]
    public void Items_StackWithSpaceBeforeAndAfter()
    {
        // Item 1 is one line (12000). Item 2 has space-before 10pt => its top is 12000 + 10000 = 22000,
        // baseline 22000 + 9000 = 31000.
        string body = """
            <fo:list-block>
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>a</fo:block></fo:list-item-body>
              </fo:list-item>
              <fo:list-item space-before="10pt">
                <fo:list-item-label><fo:block>+</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>b</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);
        TextRun secondBody = page.TextRuns.Single(r => r.Text == "b");
        Assert.Equal(31_000, secondBody.BaselineYMpt, 3);
    }

    [Fact]
    public void List_FollowsPrecedingBlockAndHonoursSpaceBefore()
    {
        // A 12000-tall block, then a list with space-before 5pt. First item baseline = 12000 + 5000 + 9000.
        string body = """
            <fo:block>top</fo:block>
            <fo:list-block space-before="5pt">
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>a</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);
        TextRun first = page.TextRuns.Single(r => r.Text == "a");
        Assert.Equal(26_000, first.BaselineYMpt, 3);
    }

    // ----- nested list indent ------------------------------------------------------------

    [Fact]
    public void NestedList_IndentsBodyContent()
    {
        // Outer list: body origin at 24000. The nested list inside the body inherits that as its left
        // edge, so its own body origin is at 24000 + 24000 = 48000.
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

        TextRun innerLabel = page.TextRuns.Single(r => r.Text == "-");
        TextRun innerBody = page.TextRuns.Single(r => r.Text == "inner");
        Assert.Equal(24_000, innerLabel.XMpt, 3);
        Assert.Equal(48_000, innerBody.XMpt, 3);
    }

    [Fact]
    public void StartIndent_ShiftsList()
    {
        // start-indent 20pt shifts the whole list start edge: label at 20000, body at 20000 + 24000.
        string body = """
            <fo:list-block start-indent="20pt">
              <fo:list-item>
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);
        TextRun label = page.TextRuns.Single(r => r.Text == "*");
        TextRun bodyRun = page.TextRuns.Single(r => r.Text == "x");
        Assert.Equal(20_000, label.XMpt, 3);
        Assert.Equal(44_000, bodyRun.XMpt, 3);
    }

    // ----- pagination across items -------------------------------------------------------

    [Fact]
    public void Items_PaginateWhenTheyOverflow()
    {
        // Page content height = 50pt = 50000. Each item is one line tall (12000). 5 items = 60000 > 50000
        // so the list spills onto a second page (4 items fit, 1 spills).
        string items = string.Concat(Enumerable.Range(0, 5).Select(i => $"""
            <fo:list-item>
              <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
              <fo:list-item-body><fo:block>i{i}</fo:block></fo:list-item-body>
            </fo:list-item>
            """));
        string body = $"<fo:list-block>{items}</fo:list-block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300, pageHeightPt: 50));

        Assert.Equal(2, tree.Pages.Count);
        // Page 1: 4 items => 4 labels + 4 bodies = 8 runs. Page 2: 1 item => 2 runs.
        Assert.Equal(8, tree.Pages[0].TextRuns.Count);
        Assert.Equal(2, tree.Pages[1].TextRuns.Count);
    }

    // ----- box emission ------------------------------------------------------------------

    [Fact]
    public void ListAndItemBox_EmitRectFills()
    {
        // The list has a background+border, and the single item has a background+border.
        string body = """
            <fo:list-block background-color="#00ff00" border="1pt solid #000000">
              <fo:list-item background-color="#0000ff" border="1pt solid #000000">
                <fo:list-item-label><fo:block>*</fo:block></fo:list-item-label>
                <fo:list-item-body><fo:block>x</fo:block></fo:list-item-body>
              </fo:list-item>
            </fo:list-block>
            """;
        AreaTree tree = LayOut(Document(body));
        PageArea page = Assert.Single(tree.Pages);

        // Each box = 1 background + 4 borders = 5 RectFills; two boxes => 10.
        Assert.Equal(10, page.RectFills.Count);
        Assert.Contains(page.RectFills, r => r.Color.Green == 255 && r.Color.Blue == 0);
        Assert.Contains(page.RectFills, r => r.Color.Blue == 255 && r.Color.Green == 0);
    }
}
