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
using Xunit;

namespace Fop.Svg.Tests;

/// <summary>Tests for <see cref="SvgParser"/>: shapes, paths, transforms, paint and viewBox handling.</summary>
public sealed class SvgParserTests
{
    private static SvgGraphic Parse(string body, string attrs = "width=\"100\" height=\"100\" viewBox=\"0 0 100 100\"")
        => SvgParser.Parse($"<svg xmlns=\"http://www.w3.org/2000/svg\" {attrs}>{body}</svg>");

    [Fact]
    public void ReadsViewBoxAndIntrinsicSize()
    {
        SvgGraphic g = Parse(string.Empty, "width=\"120\" height=\"60\" viewBox=\"0 0 200 100\"");
        Assert.Equal(120, g.IntrinsicWidth, 3);
        Assert.Equal(60, g.IntrinsicHeight, 3);
        Assert.Equal(200, g.ViewBoxWidth, 3);
        Assert.Equal(100, g.ViewBoxHeight, 3);
    }

    [Fact]
    public void IntrinsicSizeFallsBackToViewBox()
    {
        SvgGraphic g = Parse(string.Empty, "viewBox=\"0 0 40 30\"");
        Assert.Equal(40, g.IntrinsicWidth, 3);
        Assert.Equal(30, g.IntrinsicHeight, 3);
    }

    [Fact]
    public void RootWidthWithUnitsConvertsToPoints()
    {
        SvgGraphic g = Parse(string.Empty, "width=\"1in\" height=\"1in\" viewBox=\"0 0 10 10\"");
        Assert.Equal(72, g.IntrinsicWidth, 3);
        Assert.Equal(72, g.IntrinsicHeight, 3);
    }

    [Fact]
    public void RectProducesFilledClosedPath()
    {
        SvgGraphic g = Parse("<rect x=\"10\" y=\"20\" width=\"30\" height=\"40\" fill=\"red\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.NotNull(shape.Fill);
        Assert.Equal(255, shape.Fill!.Red);
        Assert.Equal(SvgVerb.MoveTo, shape.Segments[0].Verb);
        Assert.Equal(SvgVerb.Close, shape.Segments[^1].Verb);
        // First point is the rect's top-left corner.
        Assert.Equal(10, shape.Segments[0].X0, 3);
        Assert.Equal(20, shape.Segments[0].Y0, 3);
    }

    [Fact]
    public void NoFillNoStrokeShapeIsDropped()
    {
        // Default fill is black, so an explicit fill:none with no stroke yields nothing visible.
        SvgGraphic g = Parse("<rect x=\"0\" y=\"0\" width=\"5\" height=\"5\" fill=\"none\"/>");
        Assert.Empty(g.Shapes);
    }

    [Fact]
    public void LineIsStrokeOnly()
    {
        SvgGraphic g = Parse("<line x1=\"0\" y1=\"0\" x2=\"10\" y2=\"10\" stroke=\"black\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Null(shape.Fill);
        Assert.NotNull(shape.Stroke);
        Assert.True(shape.StrokeWidth > 0);
    }

    [Fact]
    public void CircleBecomesFourCubicBeziers()
    {
        SvgGraphic g = Parse("<circle cx=\"50\" cy=\"50\" r=\"40\" fill=\"blue\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(4, shape.Segments.Count(s => s.Verb == SvgVerb.CubicTo));
    }

    [Fact]
    public void PolygonClosesPolyline()
    {
        SvgGraphic g = Parse("<polygon points=\"0,0 10,0 10,10\" fill=\"green\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(SvgVerb.Close, shape.Segments[^1].Verb);
        Assert.Equal(2, shape.Segments.Count(s => s.Verb == SvgVerb.LineTo));
    }

    [Fact]
    public void PathCommandsParse()
    {
        SvgGraphic g = Parse("<path d=\"M10 10 H 90 V 90 H 10 Z\" fill=\"black\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(SvgVerb.MoveTo, shape.Segments[0].Verb);
        Assert.Equal(3, shape.Segments.Count(s => s.Verb == SvgVerb.LineTo)); // H, V, H
        Assert.Equal(SvgVerb.Close, shape.Segments[^1].Verb);
    }

    [Fact]
    public void RelativePathIsResolvedToAbsolute()
    {
        SvgGraphic g = Parse("<path d=\"M10 10 l 20 0\" stroke=\"black\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        SvgPathSegment line = shape.Segments[1];
        Assert.Equal(SvgVerb.LineTo, line.Verb);
        Assert.Equal(30, line.X0, 3);
        Assert.Equal(10, line.Y0, 3);
    }

    [Fact]
    public void TranslateTransformShiftsCoordinates()
    {
        SvgGraphic g = Parse("<g transform=\"translate(5,7)\"><rect x=\"0\" y=\"0\" width=\"2\" height=\"2\" fill=\"black\"/></g>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(5, shape.Segments[0].X0, 3);
        Assert.Equal(7, shape.Segments[0].Y0, 3);
    }

    [Fact]
    public void ScaleTransformScalesStrokeWidth()
    {
        SvgGraphic g = Parse("<g transform=\"scale(2)\"><line x1=\"0\" y1=\"0\" x2=\"1\" y2=\"0\" stroke=\"black\" stroke-width=\"3\"/></g>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(6, shape.StrokeWidth, 3);
        Assert.Equal(2, shape.Segments[1].X0, 3); // x2=1 scaled by 2
    }

    [Fact]
    public void StyleAttributeOverridesPresentationAttribute()
    {
        SvgGraphic g = Parse("<rect x=\"0\" y=\"0\" width=\"4\" height=\"4\" fill=\"red\" style=\"fill:lime\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(0, shape.Fill!.Red);
        Assert.Equal(255, shape.Fill!.Green);
    }

    [Fact]
    public void FillOpacityProducesAlpha()
    {
        SvgGraphic g = Parse("<rect x=\"0\" y=\"0\" width=\"4\" height=\"4\" fill=\"black\" fill-opacity=\"0.5\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.InRange(shape.Fill!.Alpha, 126, 129);
    }

    [Fact]
    public void FillInheritsFromGroup()
    {
        SvgGraphic g = Parse("<g fill=\"red\"><rect x=\"0\" y=\"0\" width=\"4\" height=\"4\"/></g>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Equal(255, shape.Fill!.Red);
    }

    [Fact]
    public void TextIsCaptured()
    {
        SvgGraphic g = Parse("<text x=\"10\" y=\"20\" font-size=\"14\" text-anchor=\"middle\" fill=\"black\">Hi</text>");
        SvgTextItem t = Assert.Single(g.Texts);
        Assert.Equal("Hi", t.Text);
        Assert.Equal(10, t.X, 3);
        Assert.Equal(20, t.Y, 3);
        Assert.Equal(14, t.FontSize, 3);
        Assert.Equal(SvgTextAnchor.Middle, t.Anchor);
    }

    [Fact]
    public void DefsContentIsNotRendered()
    {
        SvgGraphic g = Parse("<defs><rect x=\"0\" y=\"0\" width=\"5\" height=\"5\" fill=\"black\"/></defs>");
        Assert.Empty(g.Shapes);
    }

    [Fact]
    public void ArcConvertsToCurves()
    {
        SvgGraphic g = Parse("<path d=\"M0 0 A 10 10 0 0 1 10 10\" stroke=\"black\"/>");
        SvgShape shape = Assert.Single(g.Shapes);
        Assert.Contains(shape.Segments, s => s.Verb == SvgVerb.CubicTo);
    }

    [Fact]
    public void MalformedSvgThrows()
    {
        Assert.ThrowsAny<System.Exception>(() => SvgParser.Parse("not xml at all <"));
    }
}
