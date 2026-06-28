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
using Fop.Colors;
using Fop.Fo;
using Fop.Layout;
using Fop.Render.Pdf;
using Xunit;

namespace Fop.Rendering.Tests;

/// <summary>
/// Verifies the <c>text-decoration</c> line geometry matches FOP's
/// <c>AbstractPathOrientedRenderer.renderTextDecoration</c>: thickness <c>descender/8</c>, underline at
/// <c>baseline + descender/2</c>, overline at <c>baseline - 1.1*capHeight</c>, line-through at
/// <c>baseline - 0.45*capHeight</c>, all spanning the run width.
/// </summary>
public class TextDecorationGeometryTests
{
    private const double RunX = 1_000;
    private const double Width = 50_000;
    private const double Baseline = 100_000;
    private const double Descender = 2_400; // positive (|descender|)
    private const double CapHeight = 8_400;

    private static readonly FopColor Col = FopColor.FromRgb(10, 20, 30);

    private static TextDecorationTraits Traits(bool u = false, bool o = false, bool l = false) =>
        new(u ? Col : null, o ? Col : null, l ? Col : null);

    private static List<RectFill> Lines(TextDecorationTraits deco) =>
        PdfRenderer.BuildDecorationLines(deco, RunX, Width, Baseline, Descender, CapHeight).ToList();

    [Fact]
    public void UnderlineSitsBelowBaselineWithCorrectThickness()
    {
        RectFill line = Assert.Single(Lines(Traits(u: true)));
        double thickness = Descender / 8.0;          // 300
        double centre = Baseline + Descender / 2.0;   // 101200
        Assert.Equal(thickness, line.HeightMpt, 3);
        Assert.Equal(centre - thickness / 2, line.YMpt, 3);
        Assert.Equal(RunX, line.XMpt, 3);
        Assert.Equal(Width, line.WidthMpt, 3);
        Assert.True(line.YMpt > Baseline, "underline below baseline");
    }

    [Fact]
    public void OverlineSitsAboveBaseline()
    {
        RectFill line = Assert.Single(Lines(Traits(o: true)));
        double centre = Baseline - 1.1 * CapHeight; // 90760
        Assert.Equal(centre - (Descender / 8.0) / 2, line.YMpt, 3);
        Assert.True(line.YMpt < Baseline, "overline above baseline");
    }

    [Fact]
    public void LineThroughSitsNearMidCap()
    {
        RectFill line = Assert.Single(Lines(Traits(l: true)));
        double centre = Baseline - 0.45 * CapHeight; // 96220
        Assert.Equal(centre - (Descender / 8.0) / 2, line.YMpt, 3);
    }

    [Fact]
    public void CombinedDecorationsEmitInFopOrder()
    {
        List<RectFill> lines = Lines(Traits(u: true, o: true, l: true));
        Assert.Equal(3, lines.Count);
        // FOP order: underline (lowest on the page), then overline (highest), then line-through.
        Assert.True(lines[0].YMpt > Baseline);                  // underline
        Assert.True(lines[1].YMpt < lines[2].YMpt);             // overline above line-through
    }

    [Fact]
    public void DecorationCarriesPerLineColor()
    {
        RectFill line = Assert.Single(Lines(Traits(u: true)));
        Assert.Equal(10, line.Color.Red);
        Assert.Equal(20, line.Color.Green);
        Assert.Equal(30, line.Color.Blue);
    }

    [Fact]
    public void NoneEmitsNothing()
    {
        Assert.Empty(Lines(Traits()));
    }
}
