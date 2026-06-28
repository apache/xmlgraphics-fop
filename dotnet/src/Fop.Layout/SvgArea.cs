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
using Fop.Svg;

namespace Fop.Layout;

/// <summary>
/// The result of placing a parsed SVG graphic into a positioned viewport: the flattened vector paths
/// and text runs in page (millipoint) coordinates, ready to add to a page or buffer.
/// </summary>
/// <param name="Paths">The filled/stroked paths, in page coordinates.</param>
/// <param name="Texts">The text runs, in page coordinates.</param>
internal readonly record struct PlacedSvg(IReadOnlyList<VectorPath> Paths, IReadOnlyList<TextRun> Texts);

/// <summary>
/// Bridges <c>Fop.Svg</c> to the area tree: parses embedded SVG markup and maps its user-space
/// geometry onto a positioned viewport rectangle (in millipoints), producing area-tree
/// <see cref="VectorPath"/>s and <see cref="TextRun"/>s. The SVG viewBox is scaled (non-uniformly,
/// matching the <c>preserveAspectRatio="none"</c>-style fit the caller already chose by computing the
/// viewport size) onto the target rectangle; stroke widths and font sizes scale by the geometric-mean
/// of the axis scales.
/// </summary>
internal static class SvgArea
{
    private const double MptPerPoint = 1000.0;

    /// <summary>
    /// Parses <paramref name="svgXml"/> and returns its natural size in millipoints (intrinsic
    /// width/height, treating SVG user units/points as points), or <c>null</c> when the markup cannot
    /// be parsed or has no usable size.
    /// </summary>
    public static (double WidthMpt, double HeightMpt)? IntrinsicSizeMpt(string svgXml)
    {
        SvgGraphic? graphic = TryParse(svgXml);
        if (graphic is null || graphic.IntrinsicWidth <= 0 || graphic.IntrinsicHeight <= 0)
        {
            return null;
        }

        return (graphic.IntrinsicWidth * MptPerPoint, graphic.IntrinsicHeight * MptPerPoint);
    }

    /// <summary>Parses SVG markup, returning <c>null</c> on any failure (rendering is best-effort).</summary>
    public static SvgGraphic? TryParse(string svgXml)
    {
        if (string.IsNullOrWhiteSpace(svgXml))
        {
            return null;
        }

        try
        {
            return SvgParser.Parse(svgXml);
        }
        catch (Exception)
        {
            return null;
        }
    }

    /// <summary>
    /// Maps a parsed <paramref name="graphic"/> onto the viewport rectangle whose top-left is
    /// (<paramref name="leftMpt"/>, <paramref name="topMpt"/>) and whose size is
    /// (<paramref name="widthMpt"/>, <paramref name="heightMpt"/>), all in millipoints.
    /// </summary>
    public static PlacedSvg Place(SvgGraphic graphic, double leftMpt, double topMpt,
        double widthMpt, double heightMpt, IFontMeasurer? measurer = null)
    {
        double vbW = graphic.ViewBoxWidth > 0 ? graphic.ViewBoxWidth : 1;
        double vbH = graphic.ViewBoxHeight > 0 ? graphic.ViewBoxHeight : 1;
        double scaleX = widthMpt / vbW;
        double scaleY = heightMpt / vbH;

        // preserveAspectRatio: unless "none" (stretch), scale uniformly (meet = fit, slice = cover)
        // and align the scaled viewBox within the viewport, mirroring the SVG viewport algorithm Batik
        // applies in the original.
        SvgAspectRatio aspect = graphic.AspectRatio;
        if (aspect.Align != SvgAspectAlign.None)
        {
            double s = aspect.Slice ? Math.Max(scaleX, scaleY) : Math.Min(scaleX, scaleY);
            double extraX = widthMpt - vbW * s;
            double extraY = heightMpt - vbH * s;
            leftMpt += AlignFactorX(aspect.Align) * extraX;
            topMpt += AlignFactorY(aspect.Align) * extraY;
            scaleX = s;
            scaleY = s;
        }

        double scaleMean = Math.Sqrt(Math.Abs(scaleX * scaleY));

        double MapX(double ux) => leftMpt + (ux - graphic.ViewBoxX) * scaleX;
        double MapY(double uy) => topMpt + (uy - graphic.ViewBoxY) * scaleY;

        var paths = new List<VectorPath>(graphic.Shapes.Count);
        foreach (SvgShape shape in graphic.Shapes)
        {
            var segs = new List<PathSegment>(shape.Segments.Count);
            foreach (SvgPathSegment s in shape.Segments)
            {
                segs.Add(s.Verb switch
                {
                    SvgVerb.MoveTo => PathSegment.Move(MapX(s.X0), MapY(s.Y0)),
                    SvgVerb.LineTo => PathSegment.Line(MapX(s.X0), MapY(s.Y0)),
                    SvgVerb.QuadTo => PathSegment.Quad(MapX(s.X0), MapY(s.Y0), MapX(s.X1), MapY(s.Y1)),
                    SvgVerb.CubicTo => PathSegment.Cubic(
                        MapX(s.X0), MapY(s.Y0), MapX(s.X1), MapY(s.Y1), MapX(s.X2), MapY(s.Y2)),
                    _ => PathSegment.ClosePath(),
                });
            }

            paths.Add(new VectorPath(segs, shape.Fill, shape.Stroke, shape.StrokeWidth * scaleMean * MptPerPoint));
        }

        var texts = new List<TextRun>(graphic.Texts.Count);
        foreach (SvgTextItem t in graphic.Texts)
        {
            double fontMpt = t.FontSize * scaleMean * MptPerPoint;
            int weight = t.Bold ? 700 : 400;
            FontStyle style = t.Italic ? FontStyle.Italic : FontStyle.Normal;
            var font = new FontKey(t.FontFamily, fontMpt, weight, style);

            // text-anchor shifts the run left of the anchor point for middle/end alignment. The width
            // scales with the x-axis scale (the run is laid horizontally in user space).
            double x = MapX(t.X);
            if (measurer is not null && t.Anchor != SvgTextAnchor.Start)
            {
                double widthScaled = measurer.MeasureWidthMpt(t.Text, font);
                x -= t.Anchor == SvgTextAnchor.Middle ? widthScaled / 2 : widthScaled;
            }

            texts.Add(new TextRun(x, MapY(t.Y), t.Text, font, t.Fill));
        }

        return new PlacedSvg(paths, texts);
    }

    /// <summary>The horizontal alignment fraction (0=min, 0.5=mid, 1=max) of an aspect alignment.</summary>
    private static double AlignFactorX(SvgAspectAlign a) => a switch
    {
        SvgAspectAlign.XMinYMin or SvgAspectAlign.XMinYMid or SvgAspectAlign.XMinYMax => 0.0,
        SvgAspectAlign.XMaxYMin or SvgAspectAlign.XMaxYMid or SvgAspectAlign.XMaxYMax => 1.0,
        _ => 0.5,
    };

    /// <summary>The vertical alignment fraction (0=min, 0.5=mid, 1=max) of an aspect alignment.</summary>
    private static double AlignFactorY(SvgAspectAlign a) => a switch
    {
        SvgAspectAlign.XMinYMin or SvgAspectAlign.XMidYMin or SvgAspectAlign.XMaxYMin => 0.0,
        SvgAspectAlign.XMinYMax or SvgAspectAlign.XMidYMax or SvgAspectAlign.XMaxYMax => 1.0,
        _ => 0.5,
    };
}
