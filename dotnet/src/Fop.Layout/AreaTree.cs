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
using Fop.Fo;

namespace Fop.Layout;

/// <summary>
/// The laid-out document: an ordered list of pages, each carrying positioned primitives ready for a
/// renderer to paint. This is a pragmatic, flat "intermediate format" -- richer nested area trees
/// (block/line areas) can layer on later, but positioned runs are sufficient to render text.
/// <para>All coordinates are in millipoints, measured from the top-left of the page.</para>
/// </summary>
public sealed class AreaTree
{
    private readonly List<PageArea> pages = new();

    /// <summary>The pages, in order.</summary>
    public IReadOnlyList<PageArea> Pages => pages;

    /// <summary>Adds a page.</summary>
    public void AddPage(PageArea page)
    {
        ArgumentNullException.ThrowIfNull(page);
        pages.Add(page);
    }

    /// <summary>
    /// The document outline (PDF bookmarks): the top-level <see cref="OutlineEntry"/> roots built from
    /// the FO tree's <c>fo:bookmark-tree</c>, or an empty list when the document has no bookmark tree.
    /// Each entry may carry nested children. Renderers emit this as the PDF navigation tree.
    /// </summary>
    public IReadOnlyList<OutlineEntry> Outline { get; internal set; } = [];
}

/// <summary>
/// One entry in the document outline (a PDF bookmark). Targets a page by 0-based
/// <see cref="TargetPageIndex"/> (an index into <see cref="AreaTree.Pages"/>) or, when it only has an
/// external <see cref="Uri"/>, carries that instead. <see cref="Open"/> reflects the bookmark's
/// <c>starting-state</c> (expanded when <c>show</c>). <see cref="Children"/> holds nested entries.
/// </summary>
/// <param name="Title">The bookmark's label text.</param>
/// <param name="TargetPageIndex">
/// The 0-based target page index, or <c>null</c> when the bookmark's internal-destination did not
/// resolve to a page (an unknown/missing ref-id) and it has no usable page target.
/// </param>
/// <param name="Uri">The external destination URI for a URI-only bookmark, or <c>null</c> otherwise.</param>
/// <param name="Open">Whether the entry opens expanded (its children visible).</param>
/// <param name="Children">The nested child entries, in document order.</param>
public sealed record OutlineEntry(
    string Title,
    int? TargetPageIndex,
    string? Uri,
    bool Open,
    IReadOnlyList<OutlineEntry> Children);

/// <summary>A single laid-out page and its positioned content.</summary>
public sealed class PageArea
{
    private readonly List<TextRun> textRuns = new();
    private readonly List<RectFill> rectFills = new();
    private readonly List<ImageRun> images = new();
    private readonly List<VectorPath> vectors = new();
    private readonly List<LinkArea> links = new();
    private readonly List<AreaGroup> groups = new();

    /// <summary>Creates a page of the given size (millipoints).</summary>
    public PageArea(double widthMpt, double heightMpt)
    {
        WidthMpt = widthMpt;
        HeightMpt = heightMpt;
    }

    /// <summary>Page width in millipoints.</summary>
    public double WidthMpt { get; }

    /// <summary>Page height in millipoints.</summary>
    public double HeightMpt { get; }

    /// <summary>Positioned text runs on this page.</summary>
    public IReadOnlyList<TextRun> TextRuns => textRuns;

    /// <summary>Filled rectangles (e.g. backgrounds, rules) on this page.</summary>
    public IReadOnlyList<RectFill> RectFills => rectFills;

    /// <summary>Positioned images on this page.</summary>
    public IReadOnlyList<ImageRun> Images => images;

    /// <summary>
    /// Vector graphic paths on this page (e.g. an embedded SVG flattened into filled/stroked paths,
    /// in page coordinates). They paint after images and before text.
    /// </summary>
    public IReadOnlyList<VectorPath> Vectors => vectors;

    /// <summary>Clickable link rectangles (internal or external) on this page.</summary>
    public IReadOnlyList<LinkArea> Links => links;

    /// <summary>
    /// Transformed groups on this page (e.g. a rotated <c>fo:block-container</c>): each carries its own
    /// primitives in group-local top-left coordinates plus an affine transform (translation + rotation)
    /// that maps them into page coordinates. A renderer applies the transform, draws the group's
    /// primitives, then restores. Groups paint after the page's flat primitives.
    /// </summary>
    public IReadOnlyList<AreaGroup> Groups => groups;

    /// <summary>Adds a text run.</summary>
    public void Add(TextRun run)
    {
        ArgumentNullException.ThrowIfNull(run);
        textRuns.Add(run);
    }

    /// <summary>Adds a filled rectangle.</summary>
    public void Add(RectFill rect) => rectFills.Add(rect);

    /// <summary>Adds a positioned image.</summary>
    public void Add(ImageRun image)
    {
        ArgumentNullException.ThrowIfNull(image);
        images.Add(image);
    }

    /// <summary>Adds a vector graphic path.</summary>
    public void Add(VectorPath vector)
    {
        ArgumentNullException.ThrowIfNull(vector);
        vectors.Add(vector);
    }

    /// <summary>Adds a clickable link rectangle.</summary>
    public void Add(LinkArea link)
    {
        ArgumentNullException.ThrowIfNull(link);
        links.Add(link);
    }

    /// <summary>Adds a transformed group.</summary>
    public void Add(AreaGroup group)
    {
        ArgumentNullException.ThrowIfNull(group);
        groups.Add(group);
    }
}

/// <summary>
/// A transformed group of primitives: its own text runs, rectangles, images and links expressed in
/// group-LOCAL top-left coordinates (millipoints), plus an affine transform that maps that local space
/// into page coordinates. The transform is a translation to (<see cref="TranslateXMpt"/>,
/// <see cref="TranslateYMpt"/>) followed by a clockwise rotation of <see cref="RotationDegrees"/>
/// (one of 0/90/180/270) about that translated origin. Used to render a rotated
/// <c>fo:block-container</c>: laying its content out at origin (0,0) and emitting it as a group lets a
/// renderer rotate both the glyph positions and the glyphs themselves.
/// </summary>
public sealed class AreaGroup
{
    private readonly List<TextRun> textRuns = new();
    private readonly List<RectFill> rectFills = new();
    private readonly List<ImageRun> images = new();
    private readonly List<VectorPath> vectors = new();
    private readonly List<LinkArea> links = new();

    /// <summary>Creates a group whose local origin maps to (<paramref name="translateXMpt"/>,
    /// <paramref name="translateYMpt"/>) on the page, rotated clockwise by
    /// <paramref name="rotationDegrees"/> (0/90/180/270) about that point.</summary>
    public AreaGroup(double translateXMpt, double translateYMpt, int rotationDegrees)
    {
        TranslateXMpt = translateXMpt;
        TranslateYMpt = translateYMpt;
        RotationDegrees = rotationDegrees;
    }

    /// <summary>The page-space x of the group's local origin (millipoints from the page left).</summary>
    public double TranslateXMpt { get; }

    /// <summary>The page-space y of the group's local origin (millipoints from the page top).</summary>
    public double TranslateYMpt { get; }

    /// <summary>The clockwise rotation about the local origin, in degrees (0/90/180/270).</summary>
    public int RotationDegrees { get; }

    /// <summary>Text runs in group-local coordinates.</summary>
    public IReadOnlyList<TextRun> TextRuns => textRuns;

    /// <summary>Filled rectangles in group-local coordinates.</summary>
    public IReadOnlyList<RectFill> RectFills => rectFills;

    /// <summary>Images in group-local coordinates.</summary>
    public IReadOnlyList<ImageRun> Images => images;

    /// <summary>Vector graphic paths in group-local coordinates.</summary>
    public IReadOnlyList<VectorPath> Vectors => vectors;

    /// <summary>Link rectangles in group-local coordinates.</summary>
    public IReadOnlyList<LinkArea> Links => links;

    /// <summary>Adds a text run (group-local coordinates).</summary>
    public void Add(TextRun run)
    {
        ArgumentNullException.ThrowIfNull(run);
        textRuns.Add(run);
    }

    /// <summary>Adds a filled rectangle (group-local coordinates).</summary>
    public void Add(RectFill rect) => rectFills.Add(rect);

    /// <summary>Adds an image (group-local coordinates).</summary>
    public void Add(ImageRun image)
    {
        ArgumentNullException.ThrowIfNull(image);
        images.Add(image);
    }

    /// <summary>Adds a vector graphic path (group-local coordinates).</summary>
    public void Add(VectorPath vector)
    {
        ArgumentNullException.ThrowIfNull(vector);
        vectors.Add(vector);
    }

    /// <summary>Adds a link rectangle (group-local coordinates).</summary>
    public void Add(LinkArea link)
    {
        ArgumentNullException.ThrowIfNull(link);
        links.Add(link);
    }
}

/// <summary>
/// A run of text positioned on a page. <see cref="XMpt"/> is the left edge of the run and
/// <see cref="BaselineYMpt"/> is the text baseline, both in millipoints from the page top-left.
/// </summary>
/// <param name="XMpt">Left edge of the run, in millipoints from the page left.</param>
/// <param name="BaselineYMpt">Text baseline, in millipoints from the page top.</param>
/// <param name="Text">The run's text.</param>
/// <param name="Font">The font to render with.</param>
/// <param name="Color">The fill colour.</param>
/// <param name="LetterSpacingMpt">
/// Extra space inserted between glyphs, in millipoints (the resolved <c>letter-spacing</c>). Zero
/// (the default) means natural spacing and lets a renderer draw the run as one string; a non-zero
/// value asks the renderer to advance glyph-by-glyph.
/// </param>
/// <param name="Decoration">
/// The active <c>text-decoration</c> lines (and their colours) for this run. The renderer paints them
/// <em>after</em> the glyphs (so a line-through overlays the text), positioned from the font's
/// descender/cap-height, mirroring FOP's <c>renderTextDecoration</c>.
/// </param>
public sealed record TextRun(
    double XMpt, double BaselineYMpt, string Text, FontKey Font, FopColor Color,
    double LetterSpacingMpt = 0, TextDecorationTraits Decoration = default);

/// <summary>The kind of a <see cref="PathSegment"/> -- which drawing verb it represents.</summary>
public enum PathVerb
{
    /// <summary>Start a new subpath at the segment's first point.</summary>
    MoveTo,

    /// <summary>A straight line to the segment's first point.</summary>
    LineTo,

    /// <summary>A quadratic Bezier with control point (X0,Y0) ending at (X1,Y1).</summary>
    QuadTo,

    /// <summary>A cubic Bezier with controls (X0,Y0),(X1,Y1) ending at (X2,Y2).</summary>
    CubicTo,

    /// <summary>Close the current subpath back to its start point.</summary>
    Close,
}

/// <summary>
/// One segment of a <see cref="VectorPath"/>. Coordinates are in page (or group-local) millipoints
/// from the top-left, already transformed from the source graphic's coordinate space. Which of the
/// point fields are meaningful depends on <see cref="Verb"/> (see <see cref="PathVerb"/>).
/// </summary>
/// <param name="Verb">The drawing verb.</param>
/// <param name="X0">First point x (the target point for move/line, first control for curves).</param>
/// <param name="Y0">First point y.</param>
/// <param name="X1">Second point x (end for quad, second control for cubic).</param>
/// <param name="Y1">Second point y.</param>
/// <param name="X2">Third point x (end for cubic).</param>
/// <param name="Y2">Third point y.</param>
public readonly record struct PathSegment(
    PathVerb Verb, double X0 = 0, double Y0 = 0, double X1 = 0, double Y1 = 0, double X2 = 0, double Y2 = 0)
{
    /// <summary>A move-to (X,Y).</summary>
    public static PathSegment Move(double x, double y) => new(PathVerb.MoveTo, x, y);

    /// <summary>A line-to (X,Y).</summary>
    public static PathSegment Line(double x, double y) => new(PathVerb.LineTo, x, y);

    /// <summary>A quadratic Bezier: control (cx,cy), end (x,y).</summary>
    public static PathSegment Quad(double cx, double cy, double x, double y) => new(PathVerb.QuadTo, cx, cy, x, y);

    /// <summary>A cubic Bezier: controls (c1x,c1y),(c2x,c2y), end (x,y).</summary>
    public static PathSegment Cubic(double c1x, double c1y, double c2x, double c2y, double x, double y)
        => new(PathVerb.CubicTo, c1x, c1y, c2x, c2y, x, y);

    /// <summary>A close-subpath.</summary>
    public static PathSegment ClosePath() => new(PathVerb.Close);
}

/// <summary>
/// A vector graphic path on a page (or in a group), in millipoints with a top-left origin. The path is
/// described by an ordered list of <see cref="PathSegment"/>s and painted with an optional fill and/or
/// stroke. A renderer fills first (when <see cref="FillColor"/> is set) then strokes (when
/// <see cref="StrokeColor"/> is set and <see cref="StrokeWidthMpt"/> is positive), using the nonzero
/// winding rule (the SVG/PostScript default).
/// </summary>
/// <param name="Segments">The ordered path segments.</param>
/// <param name="FillColor">The fill colour, or <c>null</c> for no fill.</param>
/// <param name="StrokeColor">The stroke colour, or <c>null</c> for no stroke.</param>
/// <param name="StrokeWidthMpt">The stroke width in millipoints.</param>
public sealed record VectorPath(
    IReadOnlyList<PathSegment> Segments,
    FopColor? FillColor,
    FopColor? StrokeColor,
    double StrokeWidthMpt);

/// <summary>A filled rectangle on a page (top-left origin, millipoints).</summary>
/// <param name="XMpt">Left edge.</param>
/// <param name="YMpt">Top edge.</param>
/// <param name="WidthMpt">Width.</param>
/// <param name="HeightMpt">Height.</param>
/// <param name="Color">Fill colour.</param>
public sealed record RectFill(double XMpt, double YMpt, double WidthMpt, double HeightMpt, FopColor Color);

/// <summary>
/// A positioned image on a page (top-left origin, millipoints). The source is resolved either as a
/// filesystem path (<see cref="SourcePath"/>) or as already-loaded bytes (<see cref="SourceBytes"/>);
/// at least one is set.
/// </summary>
/// <param name="XMpt">Left edge.</param>
/// <param name="YMpt">Top edge.</param>
/// <param name="WidthMpt">Drawn width.</param>
/// <param name="HeightMpt">Drawn height.</param>
/// <param name="SourcePath">The resolved filesystem path of the image, or <c>null</c> when bytes are supplied.</param>
/// <param name="SourceBytes">The image bytes, or <c>null</c> when a path is supplied.</param>
public sealed record ImageRun(
    double XMpt,
    double YMpt,
    double WidthMpt,
    double HeightMpt,
    string? SourcePath,
    byte[]? SourceBytes);

/// <summary>
/// A clickable link rectangle on a page (top-left origin, millipoints). A link targets either an
/// internal page (<see cref="TargetPageIndex"/>, a 0-based index into <see cref="AreaTree.Pages"/>)
/// or an external <see cref="Uri"/>; exactly one is set. A link whose content spans several lines is
/// recorded as one <see cref="LinkArea"/> per line, all sharing the same target.
/// </summary>
/// <param name="XMpt">Left edge.</param>
/// <param name="YMpt">Top edge.</param>
/// <param name="WidthMpt">Width.</param>
/// <param name="HeightMpt">Height.</param>
/// <param name="TargetPageIndex">The 0-based target page index for an internal link, or <c>null</c> for an external link.</param>
/// <param name="Uri">The destination URI for an external link, or <c>null</c> for an internal link.</param>
public sealed record LinkArea(
    double XMpt,
    double YMpt,
    double WidthMpt,
    double HeightMpt,
    int? TargetPageIndex,
    string? Uri);
