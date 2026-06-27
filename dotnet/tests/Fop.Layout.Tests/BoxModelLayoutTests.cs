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
/// Layout tests for the box model (borders, padding, backgrounds) and images, using the
/// deterministic <see cref="FakeFontMeasurer"/> (500mpt glyph, 800 ascender, 200 descender).
/// </summary>
public sealed class BoxModelLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string body, double pageWidthPt = 200, double pageHeightPt = 1000) => $"""
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

    [Fact]
    public void BorderAndPadding_InsetContentAndAdvanceCursor()
    {
        // border 2pt + padding 3pt = 5pt inset on each edge.
        // Content "ab" at 10pt: glyph 5000 each -> natural width 10000.
        // content-left = 0 + 5000 = 5000.
        // content top inset = 5000; line-height 12000 => leading (12000-10000)=2000; ascender 8000.
        // baseline = boxTop(0) + topInset(5000) + leading/2(1000) + ascender(8000) = 14000.
        string body = "<fo:block font-size=\"10pt\" border=\"2pt solid #000000\" padding=\"3pt\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);

        TextRun run = Assert.Single(page.TextRuns);
        Assert.Equal(5_000, run.XMpt, 3);
        Assert.Equal(14_000, run.BaselineYMpt, 3);
    }

    [Fact]
    public void BorderAndPadding_ReduceAvailableWidthForJustifyAndEnd()
    {
        // page width 100pt = 100000. inset 5pt each side => content width 90000.
        // end-aligned "ab" (10000) => X = contentLeft(5000) + slack(90000-10000=80000) = 85000.
        string body = "<fo:block font-size=\"10pt\" text-align=\"end\" border=\"2pt solid #000000\" "
            + "padding=\"3pt\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        TextRun run = Assert.Single(Assert.Single(tree.Pages).TextRuns);
        Assert.Equal(85_000, run.XMpt, 3);
    }

    [Fact]
    public void Background_EmitsRectCoveringBorderBox()
    {
        // border box: left=0, top=0, width=page(100000), height = topInset+content+bottomInset.
        // content one line: advance = line-height 12000. insets 0 (no border) + padding 4pt = 4000.
        // box height = 4000 + 12000 + 4000 = 20000.
        string body = "<fo:block font-size=\"10pt\" background-color=\"#00ff00\" padding=\"4pt\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);

        RectFill bg = Assert.Single(page.RectFills);
        Assert.Equal(0, bg.XMpt, 3);
        Assert.Equal(0, bg.YMpt, 3);
        Assert.Equal(100_000, bg.WidthMpt, 3);
        Assert.Equal(20_000, bg.HeightMpt, 3);
        Assert.Equal(0, bg.Color.Red);
        Assert.Equal(255, bg.Color.Green);
    }

    [Fact]
    public void Borders_EmitFourEdgeRects_WithCorrectGeometry()
    {
        // border 2pt all round, no padding. one line content (12000 high).
        // box: left 0, top 0, width 100000, height = 2000 + 12000 + 2000 = 16000.
        string body = "<fo:block font-size=\"10pt\" border=\"2pt solid #000000\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);

        // No background => exactly four border rects.
        Assert.Equal(4, page.RectFills.Count);

        double boxW = 100_000;
        double boxH = 16_000;

        RectFill top = page.RectFills[0];
        Assert.Equal(0, top.XMpt, 3);
        Assert.Equal(0, top.YMpt, 3);
        Assert.Equal(boxW, top.WidthMpt, 3);
        Assert.Equal(2_000, top.HeightMpt, 3);

        RectFill bottom = page.RectFills[1];
        Assert.Equal(boxH - 2_000, bottom.YMpt, 3);
        Assert.Equal(2_000, bottom.HeightMpt, 3);
        Assert.Equal(boxW, bottom.WidthMpt, 3);

        RectFill left = page.RectFills[2];
        Assert.Equal(0, left.XMpt, 3);
        Assert.Equal(2_000, left.WidthMpt, 3);
        Assert.Equal(boxH, left.HeightMpt, 3);

        RectFill right = page.RectFills[3];
        Assert.Equal(boxW - 2_000, right.XMpt, 3);
        Assert.Equal(2_000, right.WidthMpt, 3);
        Assert.Equal(boxH, right.HeightMpt, 3);
    }

    [Fact]
    public void BackgroundAndBorder_BackgroundEmittedBeforeBorders()
    {
        string body = "<fo:block font-size=\"10pt\" background-color=\"#0000ff\" "
            + "border=\"1pt solid #000000\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);

        // 1 background + 4 borders.
        Assert.Equal(5, page.RectFills.Count);
        // First rect is the background (full box), so it paints behind the border edges.
        Assert.Equal(255, page.RectFills[0].Color.Blue);
        Assert.Equal(100_000, page.RectFills[0].WidthMpt, 3);
    }

    [Fact]
    public void OnlyVisibleEdgesPaint()
    {
        // Only a top solid border; others unset (style none) => single edge rect.
        string body = "<fo:block font-size=\"10pt\" border-top=\"3pt solid #000000\">ab</fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 100));
        PageArea page = Assert.Single(tree.Pages);
        RectFill edge = Assert.Single(page.RectFills);
        Assert.Equal(0, edge.YMpt, 3);
        Assert.Equal(3_000, edge.HeightMpt, 3);
    }

    [Fact]
    public void ExternalGraphic_EmitsImageRunOfSpecifiedSize()
    {
        string body = "<fo:block><fo:external-graphic src=\"pic.png\" content-width=\"40pt\" "
            + "content-height=\"30pt\"/></fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        ImageRun image = Assert.Single(page.Images);
        Assert.Equal(40_000, image.WidthMpt, 3);
        Assert.Equal(30_000, image.HeightMpt, 3);
        Assert.Equal("pic.png", image.SourcePath);
        // Block has no insets => image at content origin.
        Assert.Equal(0, image.XMpt, 3);
        Assert.Equal(0, image.YMpt, 3);
    }

    [Fact]
    public void ExternalGraphic_WithPaddingOffsetsImage()
    {
        string body = "<fo:block><fo:external-graphic src=\"pic.png\" content-width=\"40pt\" "
            + "content-height=\"30pt\" padding=\"5pt\" border=\"1pt solid #000000\"/></fo:block>";
        AreaTree tree = LayOut(Document(body, pageWidthPt: 200));
        PageArea page = Assert.Single(tree.Pages);

        ImageRun image = Assert.Single(page.Images);
        // inset = border 1 + padding 5 = 6pt.
        Assert.Equal(6_000, image.XMpt, 3);
        Assert.Equal(6_000, image.YMpt, 3);
        // border box around the image = 4 border edges (no background).
        Assert.Equal(4, page.RectFills.Count);
    }
}
