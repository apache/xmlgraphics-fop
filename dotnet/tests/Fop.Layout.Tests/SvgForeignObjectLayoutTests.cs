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
using Fop.Colors;
using Fop.Fo;
using Xunit;

namespace Fop.Layout.Tests;

/// <summary>
/// Layout tests for <c>fo:instream-foreign-object</c> carrying SVG: the embedded graphic is parsed,
/// scaled to the content box and emitted as <see cref="VectorPath"/>s (and text) in page coordinates.
/// </summary>
public sealed class SvgForeignObjectLayoutTests
{
    private static readonly FakeFontMeasurer Measurer = new();

    private static AreaTree LayOut(string fo) =>
        new LayoutEngine(Measurer).LayOut(FoTreeBuilder.ParseString(fo));

    private static string Document(string svg, string ifoAttrs) => $"""
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
          <fo:layout-master-set>
            <fo:simple-page-master master-name="p" page-width="300pt" page-height="300pt">
              <fo:region-body/>
            </fo:simple-page-master>
          </fo:layout-master-set>
          <fo:page-sequence master-reference="p">
            <fo:flow flow-name="xsl-region-body">
              <fo:block>
                <fo:instream-foreign-object {ifoAttrs}>{svg}</fo:instream-foreign-object>
              </fo:block>
            </fo:flow>
          </fo:page-sequence>
        </fo:root>
        """;

    private const string SimpleSvg =
        "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"100\" height=\"100\" viewBox=\"0 0 100 100\">" +
        "<rect x=\"0\" y=\"0\" width=\"100\" height=\"100\" fill=\"red\"/>" +
        "<text x=\"0\" y=\"50\" font-size=\"10\" fill=\"black\">Hi</text></svg>";

    [Fact]
    public void EmitsVectorPathsForSvg()
    {
        AreaTree tree = LayOut(Document(SimpleSvg, "content-width=\"100pt\" content-height=\"100pt\""));
        PageArea page = tree.Pages[0];
        VectorPath path = Assert.Single(page.Vectors);
        Assert.NotNull(path.FillColor);
        Assert.Equal(255, path.FillColor!.Red);
    }

    [Fact]
    public void ScalesViewBoxOntoContentBox()
    {
        // viewBox is 100x100; content box is 200pt -> a 2x scale. The rect spans the full box.
        AreaTree tree = LayOut(Document(SimpleSvg, "content-width=\"200pt\" content-height=\"200pt\""));
        VectorPath path = Assert.Single(tree.Pages[0].Vectors);
        double maxX = path.Segments.Max(s => System.Math.Max(s.X0, System.Math.Max(s.X1, s.X2)));
        // 100 user units * 2 scale = 200pt = 200000mpt, measured from the page/box origin (0,0).
        Assert.Equal(200_000, maxX, 1);
    }

    [Fact]
    public void EmitsTextRunFromSvgText()
    {
        AreaTree tree = LayOut(Document(SimpleSvg, "content-width=\"100pt\" content-height=\"100pt\""));
        TextRun run = Assert.Single(tree.Pages[0].TextRuns, r => r.Text == "Hi");
        Assert.NotNull(run);
    }

    [Fact]
    public void UnparseableForeignContentStillReservesBox()
    {
        // No SVG: a border makes the reserved box observable even though there is no graphic.
        string fo = Document("<bogus>not svg</bogus>",
            "content-width=\"50pt\" content-height=\"50pt\" border=\"1pt solid black\"");
        AreaTree tree = LayOut(fo);
        Assert.Empty(tree.Pages[0].Vectors);
        Assert.NotEmpty(tree.Pages[0].RectFills); // the border box
    }

    [Fact]
    public void IntrinsicSizeUsedWhenNoContentSize()
    {
        // No content-width/height: the SVG's 100x100 intrinsic size (points) drives the box.
        AreaTree tree = LayOut(Document(SimpleSvg, string.Empty));
        VectorPath path = Assert.Single(tree.Pages[0].Vectors);
        double maxX = path.Segments.Max(s => s.X0);
        Assert.Equal(100_000, maxX, 1); // 100 user units == 100pt == 100000mpt
    }
}
