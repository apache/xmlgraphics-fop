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

using Fop.Colors;

namespace Fop.Svg;

/// <summary>The drawing verb of an <see cref="SvgPathSegment"/>.</summary>
public enum SvgVerb
{
    /// <summary>Start a new subpath at (X0,Y0).</summary>
    MoveTo,

    /// <summary>Straight line to (X0,Y0).</summary>
    LineTo,

    /// <summary>Quadratic Bezier: control (X0,Y0), end (X1,Y1).</summary>
    QuadTo,

    /// <summary>Cubic Bezier: controls (X0,Y0),(X1,Y1), end (X2,Y2).</summary>
    CubicTo,

    /// <summary>Close the current subpath.</summary>
    Close,
}

/// <summary>
/// One segment of a parsed SVG path, in the document's user coordinate space (all element transforms
/// already applied). Which point fields matter depends on <see cref="Verb"/>.
/// </summary>
public readonly record struct SvgPathSegment(
    SvgVerb Verb, double X0 = 0, double Y0 = 0, double X1 = 0, double Y1 = 0, double X2 = 0, double Y2 = 0);

/// <summary>
/// A single filled and/or stroked shape from an SVG document, reduced to a flat path in user
/// coordinates. <see cref="Fill"/>/<see cref="Stroke"/> are <c>null</c> when that paint is absent
/// (<c>none</c>). <see cref="StrokeWidth"/> is in user units (with any element-transform scale already
/// folded in).
/// </summary>
/// <param name="Segments">The path segments, in user coordinates.</param>
/// <param name="Fill">The resolved fill colour (alpha-composited with opacity), or <c>null</c>.</param>
/// <param name="Stroke">The resolved stroke colour (alpha-composited with opacity), or <c>null</c>.</param>
/// <param name="StrokeWidth">The stroke width in user units.</param>
public sealed record SvgShape(
    IReadOnlyList<SvgPathSegment> Segments,
    FopColor? Fill,
    FopColor? Stroke,
    double StrokeWidth);

/// <summary>
/// A run of SVG <c>&lt;text&gt;</c> content positioned at its anchor point in user coordinates. Only
/// simple, axis-aligned text is modelled (no rotation/skew); the anchor is the text baseline origin.
/// </summary>
/// <param name="X">Baseline-origin x in user coordinates.</param>
/// <param name="Y">Baseline-origin y in user coordinates.</param>
/// <param name="Text">The text content.</param>
/// <param name="FontFamily">The resolved font family.</param>
/// <param name="FontSize">The font size in user units.</param>
/// <param name="Fill">The fill colour (defaulting to black).</param>
/// <param name="Bold">Whether the font weight is bold (>= 700).</param>
/// <param name="Italic">Whether the font style is italic/oblique.</param>
/// <param name="Anchor">The text-anchor (start/middle/end) governing horizontal alignment to (X,Y).</param>
public sealed record SvgTextItem(
    double X,
    double Y,
    string Text,
    string FontFamily,
    double FontSize,
    FopColor Fill,
    bool Bold,
    bool Italic,
    SvgTextAnchor Anchor);

/// <summary>The SVG <c>text-anchor</c> values: where the anchor point sits relative to the text.</summary>
public enum SvgTextAnchor
{
    /// <summary>The anchor is the start of the text (the default).</summary>
    Start,

    /// <summary>The anchor is the horizontal centre of the text.</summary>
    Middle,

    /// <summary>The anchor is the end of the text.</summary>
    End,
}

/// <summary>
/// A parsed SVG document flattened into renderer-neutral primitives. Geometry is expressed in the
/// document's user (viewBox) coordinate space; a consumer scales the view-box rectangle
/// onto its target viewport. <see cref="IntrinsicWidth"/>/<see cref="IntrinsicHeight"/> are the
/// document's natural size (from the <c>width</c>/<c>height</c> attributes, falling back to the
/// view-box extent) used when the embedding context does not specify a size.
/// </summary>
/// <param name="IntrinsicWidth">Natural width in points (user units treated as points).</param>
/// <param name="IntrinsicHeight">Natural height in points.</param>
/// <param name="ViewBoxX">viewBox min-x.</param>
/// <param name="ViewBoxY">viewBox min-y.</param>
/// <param name="ViewBoxWidth">viewBox width.</param>
/// <param name="ViewBoxHeight">viewBox height.</param>
/// <param name="Shapes">The filled/stroked shapes, in document order (painted before text).</param>
/// <param name="Texts">The text runs, in document order (painted after shapes).</param>
public sealed record SvgGraphic(
    double IntrinsicWidth,
    double IntrinsicHeight,
    double ViewBoxX,
    double ViewBoxY,
    double ViewBoxWidth,
    double ViewBoxHeight,
    IReadOnlyList<SvgShape> Shapes,
    IReadOnlyList<SvgTextItem> Texts);
