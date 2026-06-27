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

        foreach (PageArea pageArea in tree.Pages)
        {
            PdfPage page = document.AddPage();
            page.Width = XUnit.FromPoint(pageArea.WidthMpt / MptPerPoint);
            page.Height = XUnit.FromPoint(pageArea.HeightMpt / MptPerPoint);

            using XGraphics gfx = XGraphics.FromPdfPage(page);

            foreach (RectFill rect in pageArea.RectFills)
            {
                gfx.DrawRectangle(
                    new XSolidBrush(ToXColor(rect.Color)),
                    rect.XMpt / MptPerPoint,
                    rect.YMpt / MptPerPoint,
                    rect.WidthMpt / MptPerPoint,
                    rect.HeightMpt / MptPerPoint);
            }

            foreach (ImageRun image in pageArea.Images)
            {
                DrawImage(gfx, image);
            }

            foreach (TextRun run in pageArea.TextRuns)
            {
                if (run.Text.Length == 0)
                {
                    continue;
                }

                XFont font = measurer.GetXFont(run.Font);
                var brush = new XSolidBrush(ToXColor(run.Color));
                gfx.DrawString(
                    run.Text,
                    font,
                    brush,
                    run.XMpt / MptPerPoint,
                    run.BaselineYMpt / MptPerPoint,
                    baseline);
            }

            // Link annotations are emitted after the visible content so the clickable region overlays
            // the text/rule it wraps. They do not paint anything themselves.
            double pageHeightPt = pageArea.HeightMpt / MptPerPoint;
            foreach (LinkArea link in pageArea.Links)
            {
                AddLink(page, link, pageHeightPt);
            }
        }

        document.Save(output);
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
