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
using Fop.Layout;
using PdfSharp.Drawing;
using PdfSharp.Drawing.Layout;
using PdfSharp.Pdf;

namespace Fop.Render.Pdf;

/// <summary>
/// Renders a laid-out <see cref="AreaTree"/> to a PDF document using PdfSharp.
/// </summary>
public sealed class PdfRenderer
{
    private const double MptPerPoint = 1000.0;

    private readonly PdfSharpFontMeasurer measurer;

    /// <summary>Creates a renderer using the given measurer (which owns the font resolution).</summary>
    public PdfRenderer(PdfSharpFontMeasurer measurer)
    {
        this.measurer = measurer ?? throw new ArgumentNullException(nameof(measurer));
    }

    /// <summary>Renders the area tree and writes the resulting PDF to <paramref name="output"/>.</summary>
    public void Render(AreaTree tree, Stream output)
    {
        ArgumentNullException.ThrowIfNull(tree);
        ArgumentNullException.ThrowIfNull(output);

        using var document = new PdfDocument();
        var baseline = new XStringFormat { Alignment = XStringAlignment.Near, LineAlignment = XLineAlignment.BaseLine };

        // The PdfPage of each area-tree page, by 0-based index, so outline entries can target pages.
        var pdfPages = new List<PdfPage>(tree.Pages.Count);

        foreach (PageArea pageArea in tree.Pages)
        {
            PdfPage page = document.AddPage();
            pdfPages.Add(page);
            page.Width = XUnit.FromPoint(pageArea.WidthMpt / MptPerPoint);
            page.Height = XUnit.FromPoint(pageArea.HeightMpt / MptPerPoint);

            using XGraphics gfx = XGraphics.FromPdfPage(page);

            DrawRects(gfx, pageArea.RectFills);

            foreach (ImageRun image in pageArea.Images)
            {
                DrawImage(gfx, image);
            }

            DrawVectors(gfx, pageArea.Vectors);

            DrawRuns(gfx, pageArea.TextRuns, baseline);

            // Transformed groups (e.g. rotated block-containers) paint after the page's flat content,
            // each under its own translate+rotate transform applied around the group's local origin.
            foreach (AreaGroup group in pageArea.Groups)
            {
                DrawGroup(gfx, group, baseline);
            }

            // Link annotations are emitted after the visible content so the clickable region overlays
            // the text/rule it wraps. They do not paint anything themselves.
            double pageHeightPt = pageArea.HeightMpt / MptPerPoint;
            foreach (LinkArea link in pageArea.Links)
            {
                AddLink(page, link, pageHeightPt);
            }
        }

        // Emit the document outline (PDF bookmarks) after the pages exist, so each entry can point at
        // its target PdfPage. Guarded so we never touch document.Outlines when there is nothing to add:
        // the getter materializes an /Outlines catalog entry, which we avoid for bookmark-free documents.
        if (tree.Outline.Count > 0 && pdfPages.Count > 0)
        {
            AddOutline(document.Outlines, tree.Outline, pdfPages);
        }

        document.Save(output);
    }

    /// <summary>
    /// Adds <paramref name="entries"/> to <paramref name="collection"/> (the document's top-level
    /// outline collection, or a parent entry's child collection) as PdfSharp outline nodes, recursing
    /// into each entry's children. Each entry targets the <see cref="PdfPage"/> at its
    /// <see cref="OutlineEntry.TargetPageIndex"/>; an entry whose target is missing falls back to the
    /// first page so it stays clickable. <see cref="PdfOutline.Outlines"/> holds an entry's children.
    /// </summary>
    private static void AddOutline(PdfOutlineCollection collection, IReadOnlyList<OutlineEntry> entries,
        IReadOnlyList<PdfPage> pdfPages)
    {
        if (entries.Count == 0 || pdfPages.Count == 0)
        {
            return;
        }

        foreach (OutlineEntry entry in entries)
        {
            // Map the 0-based target page index to its PdfPage. PdfSharp outlines are page-targeted and
            // require a destination page, so we clamp to a valid page and default to the first page when
            // the entry has no resolved target (e.g. an unresolved ref-id or a URI-only bookmark).
            // TODO: external-destination bookmarks cannot be expressed as a web link in a PdfSharp
            // outline node (outlines are page destinations), so a URI-only bookmark navigates to a page
            // rather than opening the URI.
            int index = entry.TargetPageIndex is int i && i >= 0 && i < pdfPages.Count ? i : 0;
            PdfPage destination = pdfPages[index];

            PdfOutline node = collection.Add(entry.Title, destination, entry.Open);
            AddOutline(node.Outlines, entry.Children, pdfPages);
        }
    }

    private static void DrawRects(XGraphics gfx, IReadOnlyList<RectFill> rects)
    {
        foreach (RectFill rect in rects)
        {
            gfx.DrawRectangle(
                new XSolidBrush(ToXColor(rect.Color)),
                rect.XMpt / MptPerPoint,
                rect.YMpt / MptPerPoint,
                rect.WidthMpt / MptPerPoint,
                rect.HeightMpt / MptPerPoint);
        }
    }

    private void DrawRuns(XGraphics gfx, IReadOnlyList<TextRun> runs, XStringFormat baseline)
    {
        foreach (TextRun run in runs)
        {
            if (run.Text.Length == 0)
            {
                continue;
            }

            XFont font = measurer.GetXFont(run.Font);
            var brush = new XSolidBrush(ToXColor(run.Color));
            double y = run.BaselineYMpt / MptPerPoint;

            if (run.LetterSpacingMpt != 0)
            {
                // Letter-spacing: draw each glyph at its own x, advancing by the glyph width plus the
                // tracking between glyphs (no trailing space after the last glyph), matching the layout
                // engine's MeasuredAdvance and FOP's per-glyph positioning.
                double x = run.XMpt;
                for (int i = 0; i < run.Text.Length; i++)
                {
                    string s = run.Text[i].ToString();
                    gfx.DrawString(s, font, brush, x / MptPerPoint, y, baseline);
                    x += measurer.MeasureWidthMpt(s, run.Font);
                    if (i < run.Text.Length - 1)
                    {
                        x += run.LetterSpacingMpt;
                    }
                }
            }
            else
            {
                gfx.DrawString(run.Text, font, brush, run.XMpt / MptPerPoint, y, baseline);
            }

            // Text-decoration is painted after the glyphs so a line-through overlays them.
            if (!run.Decoration.IsNone)
            {
                DrawTextDecoration(gfx, run);
            }
        }
    }

    /// <summary>
    /// Paints the run's <c>text-decoration</c> lines over its glyphs. The line positions and thickness
    /// follow FOP's <c>AbstractPathOrientedRenderer.renderTextDecoration</c>: the underline sits at
    /// <c>baseline + descender/2</c>, the overline at <c>baseline - 1.1*capHeight</c>, the line-through
    /// at <c>baseline - 0.45*capHeight</c>, and each line is <c>descender/8</c> thick.
    /// </summary>
    private void DrawTextDecoration(XGraphics gfx, TextRun run)
    {
        double widthMpt = RunAdvanceMpt(run);
        if (widthMpt <= 0)
        {
            return;
        }

        double descenderMpt = measurer.DescenderMpt(run.Font);
        double capHeightMpt = measurer.CapHeightMpt(run.Font);

        foreach (RectFill line in BuildDecorationLines(
            run.Decoration, run.XMpt, widthMpt, run.BaselineYMpt, descenderMpt, capHeightMpt))
        {
            gfx.DrawRectangle(new XSolidBrush(ToXColor(line.Color)),
                line.XMpt / MptPerPoint, line.YMpt / MptPerPoint,
                line.WidthMpt / MptPerPoint, line.HeightMpt / MptPerPoint);
        }
    }

    /// <summary>The inline advance of a run in millipoints (matching the layout engine's measurement).</summary>
    private double RunAdvanceMpt(TextRun run)
    {
        if (run.LetterSpacingMpt == 0)
        {
            return measurer.MeasureWidthMpt(run.Text, run.Font);
        }

        double width = 0;
        foreach (char c in run.Text)
        {
            width += measurer.MeasureWidthMpt(c.ToString(), run.Font);
        }

        return width + run.LetterSpacingMpt * Math.Max(0, run.Text.Length - 1);
    }

    /// <summary>
    /// Builds the decoration line rectangles (in millipoints) for <paramref name="decoration"/> over a
    /// run that starts at <paramref name="runXMpt"/>, is <paramref name="widthMpt"/> wide and sits on
    /// <paramref name="baselineMpt"/>, given the font's positive <paramref name="descenderMpt"/> and
    /// <paramref name="capHeightMpt"/>. Each line is a thin rectangle centred on its target y and uses
    /// the colour the decoration carries (the colour of the FO that turned the line on). The formulas
    /// mirror FOP's <c>renderTextDecoration</c> (with FOP's negative descender folded into the positive
    /// value used here). Exposed internally so the geometry can be unit-tested.
    /// </summary>
    internal static IEnumerable<RectFill> BuildDecorationLines(Fop.Fo.TextDecorationTraits decoration,
        double runXMpt, double widthMpt, double baselineMpt, double descenderMpt, double capHeightMpt)
    {
        // FOP: halfLineWidth = (descender / -8) / 2 with a negative descender, so the full line
        // thickness is |descender|/8.
        double thickness = descenderMpt / 8.0;
        var lines = new List<RectFill>(3);

        void Add(double centreYMpt, FopColor color) =>
            lines.Add(new RectFill(runXMpt, centreYMpt - thickness / 2, widthMpt, thickness, color));

        // Order matches FOP: underline, overline, line-through. Each line uses its own recorded colour.
        if (decoration.UnderlineColor is { } underline)
        {
            Add(baselineMpt + descenderMpt / 2.0, underline);
        }

        if (decoration.OverlineColor is { } overline)
        {
            Add(baselineMpt - 1.1 * capHeightMpt, overline);
        }

        if (decoration.LineThroughColor is { } lineThrough)
        {
            Add(baselineMpt - 0.45 * capHeightMpt, lineThrough);
        }

        return lines;
    }

    /// <summary>
    /// Draws the page's vector paths. Each <see cref="VectorPath"/> is rebuilt as an
    /// <see cref="XGraphicsPath"/> (quadratics promoted to cubics) and filled (nonzero winding) and/or
    /// stroked. Coordinates are converted from millipoints to points.
    /// </summary>
    private static void DrawVectors(XGraphics gfx, IReadOnlyList<VectorPath> vectors)
    {
        foreach (VectorPath vector in vectors)
        {
            XGraphicsPath? path = BuildPath(vector.Segments);
            if (path is null)
            {
                continue;
            }

            XBrush? brush = vector.FillColor is { } fill ? new XSolidBrush(ToXColor(fill)) : null;
            XPen? pen = vector.StrokeColor is { } stroke && vector.StrokeWidthMpt > 0
                ? new XPen(ToXColor(stroke), Math.Max(0.1, vector.StrokeWidthMpt / MptPerPoint))
                : null;

            if (brush is not null && pen is not null)
            {
                gfx.DrawPath(pen, brush, path);
            }
            else if (brush is not null)
            {
                gfx.DrawPath(brush, path);
            }
            else if (pen is not null)
            {
                gfx.DrawPath(pen, path);
            }
        }
    }

    /// <summary>
    /// Builds an <see cref="XGraphicsPath"/> from area-tree path segments, tracking the current point
    /// to feed PdfSharp's start-point-based segment API and converting quadratic Beziers to cubics.
    /// Returns <c>null</c> when the path has no drawable segments.
    /// </summary>
    private static XGraphicsPath? BuildPath(IReadOnlyList<PathSegment> segments)
    {
        var path = new XGraphicsPath { FillMode = XFillMode.Winding };
        double curX = 0, curY = 0, startX = 0, startY = 0;
        bool any = false;

        foreach (PathSegment seg in segments)
        {
            switch (seg.Verb)
            {
                case PathVerb.MoveTo:
                    path.StartFigure();
                    curX = startX = seg.X0 / MptPerPoint;
                    curY = startY = seg.Y0 / MptPerPoint;
                    break;

                case PathVerb.LineTo:
                {
                    double x = seg.X0 / MptPerPoint, y = seg.Y0 / MptPerPoint;
                    path.AddLine(curX, curY, x, y);
                    curX = x; curY = y;
                    any = true;
                    break;
                }

                case PathVerb.CubicTo:
                {
                    double c1x = seg.X0 / MptPerPoint, c1y = seg.Y0 / MptPerPoint;
                    double c2x = seg.X1 / MptPerPoint, c2y = seg.Y1 / MptPerPoint;
                    double x = seg.X2 / MptPerPoint, y = seg.Y2 / MptPerPoint;
                    path.AddBezier(curX, curY, c1x, c1y, c2x, c2y, x, y);
                    curX = x; curY = y;
                    any = true;
                    break;
                }

                case PathVerb.QuadTo:
                {
                    double cx = seg.X0 / MptPerPoint, cy = seg.Y0 / MptPerPoint;
                    double x = seg.X1 / MptPerPoint, y = seg.Y1 / MptPerPoint;
                    // Promote the quadratic to a cubic about the current point.
                    double c1x = curX + 2.0 / 3.0 * (cx - curX);
                    double c1y = curY + 2.0 / 3.0 * (cy - curY);
                    double c2x = x + 2.0 / 3.0 * (cx - x);
                    double c2y = y + 2.0 / 3.0 * (cy - y);
                    path.AddBezier(curX, curY, c1x, c1y, c2x, c2y, x, y);
                    curX = x; curY = y;
                    any = true;
                    break;
                }

                case PathVerb.Close:
                    path.CloseFigure();
                    curX = startX; curY = startY;
                    break;
            }
        }

        return any ? path : null;
    }

    /// <summary>
    /// Renders an <see cref="AreaGroup"/> under its affine transform. PdfSharp's <see cref="XGraphics"/>
    /// (from <c>FromPdfPage</c>) uses a top-left origin with y growing downward -- the same convention as
    /// our area tree -- so the group's translate (page-space mpt converted to points) maps directly and a
    /// positive <c>RotateTransform</c> angle rotates clockwise about the current origin. The transforms
    /// are applied as translate-then-rotate (so the rotation pivots about the group's local origin), the
    /// group's primitives are drawn in their local coordinates, then the graphics state is restored.
    /// Link annotations inside a group are not emitted (PDF link rectangles are axis-aligned page-space
    /// boxes and cannot follow a rotation); rotated containers are visual, not interactive, here.
    /// </summary>
    private void DrawGroup(XGraphics gfx, AreaGroup group, XStringFormat baseline)
    {
        XGraphicsState state = gfx.Save();
        try
        {
            gfx.TranslateTransform(group.TranslateXMpt / MptPerPoint, group.TranslateYMpt / MptPerPoint);
            if (group.RotationDegrees != 0)
            {
                gfx.RotateTransform(group.RotationDegrees);
            }

            DrawRects(gfx, group.RectFills);
            foreach (ImageRun image in group.Images)
            {
                DrawImage(gfx, image);
            }

            DrawVectors(gfx, group.Vectors);

            DrawRuns(gfx, group.TextRuns, baseline);
        }
        finally
        {
            gfx.Restore(state);
        }
    }

    private static void DrawImage(XGraphics gfx, ImageRun image)
    {
        double x = image.XMpt / MptPerPoint;
        double y = image.YMpt / MptPerPoint;
        double w = image.WidthMpt / MptPerPoint;
        double h = image.HeightMpt / MptPerPoint;

        XImage? loaded = TryLoadImage(image);
        if (loaded is not null)
        {
            using (loaded)
            {
                gfx.DrawImage(loaded, x, y, w, h);
            }

            return;
        }

        // TODO: PdfSharp image decoding can fail (missing file, unsupported codec, or PdfSharp's
        // platform image support being unavailable on this OS). As a graceful fallback we paint a
        // light-grey placeholder box with a thin border so the image's reserved area is still visible.
        gfx.DrawRectangle(new XSolidBrush(XColor.FromArgb(255, 230, 230, 230)), x, y, w, h);
        gfx.DrawRectangle(new XPen(XColor.FromArgb(255, 160, 160, 160), 0.5), x, y, w, h);
    }

    private static XImage? TryLoadImage(ImageRun image)
    {
        try
        {
            if (image.SourceBytes is { Length: > 0 } bytes)
            {
                var stream = new MemoryStream(bytes, writable: false);
                return XImage.FromStream(stream);
            }

            if (!string.IsNullOrEmpty(image.SourcePath) && File.Exists(image.SourcePath))
            {
                return XImage.FromFile(image.SourcePath);
            }
        }
        catch (Exception)
        {
            // Fall through to the placeholder. Image loading is best-effort.
        }

        return null;
    }

    /// <summary>
    /// Adds one PdfSharp link annotation for <paramref name="link"/> on <paramref name="page"/>. Our
    /// link rectangle is top-left origin in millipoints; PdfSharp's <see cref="PdfRectangle"/> is PDF
    /// user space (points, bottom-left origin), so the lower-left corner is
    /// (x, pageHeight - (yTop + h)) and the upper-right is (x + w, pageHeight - yTop). An internal link
    /// targets a 0-based page index via <c>AddDocumentLink</c>; an external link uses <c>AddWebLink</c>.
    /// </summary>
    private static void AddLink(PdfPage page, LinkArea link, double pageHeightPt)
    {
        double x = link.XMpt / MptPerPoint;
        double w = link.WidthMpt / MptPerPoint;
        double yTop = link.YMpt / MptPerPoint;
        double h = link.HeightMpt / MptPerPoint;
        if (w <= 0 || h <= 0)
        {
            return;
        }

        double lowerLeftY = pageHeightPt - (yTop + h);
        double upperRightY = pageHeightPt - yTop;
        var rect = new PdfRectangle(new XPoint(x, lowerLeftY), new XPoint(x + w, upperRightY));

        if (link.TargetPageIndex is int targetPage)
        {
            // Internal link: jump to the target page (no explicit destination point -> page-level link).
            page.AddDocumentLink(rect, targetPage, point: null);
        }
        else if (!string.IsNullOrEmpty(link.Uri))
        {
            page.AddWebLink(rect, link.Uri);
        }
    }

    private static XColor ToXColor(FopColor color)
        => XColor.FromArgb(color.Alpha, color.Red, color.Green, color.Blue);
}
