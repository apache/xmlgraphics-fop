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
/// Layout tests for <see cref="FoBlockContainer"/> over the deterministic <see cref="FakeFontMeasurer"/>
/// (500 units/glyph at 1000mpt, ascender 800, descender 200, scaled by font size).
/// </summary>
public sealed class BlockContainerLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

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

    // ----- absolute positioning ----------------------------------------------------------

    [Fact]
    public void AbsoluteContainer_PlacesContentAtLeftTop_AndDoesNotAdvanceFlow()
    {
        // An absolute container at (left=30pt, top=40pt) holding "ab", followed by a normal block.
        // The following block must start at the content top (0) as if the container were absent.
        string body =
            "<fo:block-container absolute-position=\"absolute\" left=\"30pt\" top=\"40pt\" width=\"100pt\">" +
            "<fo:block font-size=\"10pt\">ab</fo:block></fo:block-container>" +
            "<fo:block font-size=\"10pt\">flow</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200, pageHeightPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        // Two runs: the absolutely placed "ab" and the in-flow "flow".
        TextRun ab = Assert.Single(page.TextRuns, r => r.Text == "ab");
        TextRun flow = Assert.Single(page.TextRuns, r => r.Text == "flow");

        // "ab" content starts at left=30pt. Its baseline = top(40000) + leading/2(1000) + ascender(8000).
        Assert.Equal(30_000, ab.XMpt, 3);
        Assert.Equal(40_000 + 1_000 + 8_000, ab.BaselineYMpt, 3);

        // The following flow block is NOT pushed down by the container: it starts at content top 0.
        Assert.Equal(0, flow.XMpt, 3);
        Assert.Equal(0 + 1_000 + 8_000, flow.BaselineYMpt, 3);
    }

    [Fact]
    public void FixedContainer_PlacesRelativeToPage()
    {
        // A page with margins so the body content area differs from the page origin. A fixed container
        // at left=0,top=0 places content at the page top-left (not the body content area top-left).
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt"
                    margin-left="20pt" margin-top="30pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block-container absolute-position="fixed" left="0pt" top="0pt" width="100pt">
                    <fo:block font-size="10pt">fx</fo:block>
                  </fo:block-container>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        TextRun fx = Assert.Single(page.TextRuns);
        // Fixed left=0 maps to page x=0 (NOT the body content-left of 20pt).
        Assert.Equal(0, fx.XMpt, 3);
        Assert.Equal(0 + 1_000 + 8_000, fx.BaselineYMpt, 3);
    }

    [Fact]
    public void AbsoluteContainer_RelativeToContentArea()
    {
        // With body margins, an absolute container's offset is relative to the body content area.
        const string fo = """
            <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
              <fo:layout-master-set>
                <fo:simple-page-master master-name="p" page-width="200pt" page-height="200pt"
                    margin-left="20pt" margin-top="30pt">
                  <fo:region-body/>
                </fo:simple-page-master>
              </fo:layout-master-set>
              <fo:page-sequence master-reference="p">
                <fo:flow flow-name="xsl-region-body">
                  <fo:block-container absolute-position="absolute" left="0pt" top="0pt" width="100pt">
                    <fo:block font-size="10pt">ab</fo:block>
                  </fo:block-container>
                </fo:flow>
              </fo:page-sequence>
            </fo:root>
            """;
        AreaTree tree = LayOut(fo);
        PageArea page = Assert.Single(tree.Pages);
        TextRun ab = Assert.Single(page.TextRuns);
        // absolute left=0 maps to the content-left of 20pt; top=0 to content-top 30pt.
        Assert.Equal(20_000, ab.XMpt, 3);
        Assert.Equal(30_000 + 1_000 + 8_000, ab.BaselineYMpt, 3);
    }

    // ----- normal-flow (auto) container --------------------------------------------------

    [Fact]
    public void AutoContainer_FlowsLikeBlock_AndAdvancesCursor()
    {
        string body =
            "<fo:block-container><fo:block font-size=\"10pt\">first</fo:block></fo:block-container>" +
            "<fo:block font-size=\"10pt\">second</fo:block>";
        AreaTree tree = LayOut(Document(body, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        TextRun first = Assert.Single(page.TextRuns, r => r.Text == "first");
        TextRun second = Assert.Single(page.TextRuns, r => r.Text == "second");

        // Container line top = 0; following block top = line-height (12000).
        Assert.Equal(1_000 + 8_000, first.BaselineYMpt, 3);
        Assert.Equal(12_000 + 1_000 + 8_000, second.BaselineYMpt, 3);
    }

    [Fact]
    public void AutoContainer_WidthShrinksLineWrapping()
    {
        // A container with width=20pt forces the inner block to wrap one word per line (each word "ab"
        // is 10000mpt; a second needs +space 5000 +10000 = 25000 > 20000).
        string body =
            "<fo:block-container width=\"20pt\">" +
            "<fo:block font-size=\"10pt\">ab ab ab</fo:block></fo:block-container>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 500, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        // Three lines, one "ab" each.
        Assert.Equal(3, page.TextRuns.Count);
        Assert.All(page.TextRuns, r => Assert.Equal("ab", r.Text));
    }

    // ----- reference-orientation (rotation) ----------------------------------------------

    [Fact]
    public void RotatedContainer_ProducesTransformGroup()
    {
        string body =
            "<fo:block-container absolute-position=\"absolute\" left=\"50pt\" top=\"60pt\" width=\"100pt\" " +
            "reference-orientation=\"90\">" +
            "<fo:block font-size=\"10pt\">rot</fo:block></fo:block-container>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 300, pageHeightPt: 300));
        PageArea page = Assert.Single(tree.Pages);

        // The rotated content is in a group, not a flat run.
        Assert.Empty(page.TextRuns);
        AreaGroup group = Assert.Single(page.Groups);

        Assert.Equal(90, group.RotationDegrees);
        Assert.Equal(50_000, group.TranslateXMpt, 3);
        Assert.Equal(60_000, group.TranslateYMpt, 3);

        // The group's primitives are in LOCAL coordinates: the content starts at the group origin (0,0),
        // so the text run sits at local x=0 and baseline = leading/2 + ascender.
        TextRun run = Assert.Single(group.TextRuns);
        Assert.Equal("rot", run.Text);
        Assert.Equal(0, run.XMpt, 3);
        Assert.Equal(1_000 + 8_000, run.BaselineYMpt, 3);
    }

    [Fact]
    public void RotatedAutoContainer_AdvancesByWidthFor90Degrees()
    {
        // A 90-degree rotated in-flow container occupies its (pre-rotation) WIDTH vertically. width=40pt
        // => the following block starts at 40000mpt.
        string body =
            "<fo:block-container width=\"40pt\" height=\"15pt\" reference-orientation=\"90\">" +
            "<fo:block font-size=\"10pt\">x</fo:block></fo:block-container>" +
            "<fo:block font-size=\"10pt\">after</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 500, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);

        // The rotated container is a group; the following block is a flat run.
        Assert.Single(page.Groups);
        TextRun after = Assert.Single(page.TextRuns, r => r.Text == "after");

        // Advance = pre-rotation width 40000 => after baseline = 40000 + leading/2 + ascender.
        Assert.Equal(40_000 + 1_000 + 8_000, after.BaselineYMpt, 3);
    }

    [Fact]
    public void NoRotation_StaysFlat()
    {
        string body =
            "<fo:block-container reference-orientation=\"0\">" +
            "<fo:block font-size=\"10pt\">flat</fo:block></fo:block-container>";
        AreaTree tree = LayOut(Document(body, pageHeightPt: 1000));
        PageArea page = Assert.Single(tree.Pages);
        Assert.Empty(page.Groups);
        Assert.Single(page.TextRuns);
    }
}
