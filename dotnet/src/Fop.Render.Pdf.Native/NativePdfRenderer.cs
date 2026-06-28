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

using System.Globalization;
using System.IO.Compression;
using System.Text;
using Fop.Colors;
using Fop.Fo;
using Fop.Imaging;
using Fop.Layout;

namespace Fop.Render.Pdf.Native;

/// <summary>
/// Renders a laid-out <see cref="AreaTree"/> to a PDF document by writing the file directly on the
/// <c>Fop.Pdf</c> object model -- no PdfSharp. Text is drawn with the 14 standard PDF fonts
/// (WinAnsi-encoded, no embedding), vector graphics as path operators, rules/backgrounds as filled
/// rectangles, plus link annotations and a document outline (bookmarks). Raster images are drawn as a
/// placeholder box for now (native image embedding is future work).
/// <para>
/// An <see cref="IFontMeasurer"/> is used only to position text decorations (descender/cap-height);
/// glyph positions come straight from the area tree. The renderer itself has no PdfSharp dependency.
/// </para>
/// </summary>
public sealed class NativePdfRenderer
{
    private const double MptPerPoint = 1000.0;

    private readonly IFontMeasurer measurer;

    /// <summary>Creates a native renderer using <paramref name="measurer"/> for decoration metrics.</summary>
    public NativePdfRenderer(IFontMeasurer measurer)
    {
        this.measurer = measurer ?? throw new ArgumentNullException(nameof(measurer));
    }

    /// <summary>Renders <paramref name="tree"/> and writes the PDF bytes to <paramref name="output"/>.</summary>
    public void Render(AreaTree tree, Stream output)
    {
        ArgumentNullException.ThrowIfNull(tree);
        ArgumentNullException.ThrowIfNull(output);

        var doc = new PdfFile();
        int catalog = doc.Reserve();
        int pagesTree = doc.Reserve();

        // Shared font resources (one object per distinct Base-14 face actually used).
        var fontObjects = new Dictionary<string, int>(StringComparer.Ordinal);
        int FontObject(string baseFont)
        {
            if (!fontObjects.TryGetValue(baseFont, out int num))
            {
                num = doc.Reserve();
                fontObjects[baseFont] = num;
            }

            return num;
        }

        var pageObjectNumbers = new List<int>(tree.Pages.Count);
        foreach (PageArea _ in tree.Pages)
        {
            pageObjectNumbers.Add(doc.Reserve());
        }

        // Image XObjects are shared across pages, deduped by source path.
        var imageCache = new Dictionary<string, int>(StringComparer.Ordinal);

        // Build each page's content stream, font resource map and annotations.
        for (int i = 0; i < tree.Pages.Count; i++)
        {
            PageArea page = tree.Pages[i];
            var fontsOnPage = new Dictionary<string, string>(StringComparer.Ordinal); // resourceKey -> baseFont
            var xobjectsOnPage = new Dictionary<string, int>(StringComparer.Ordinal); // resourceKey -> object
            string content = BuildPageContent(page, fontsOnPage, doc, imageCache, xobjectsOnPage);

            int contentObj = doc.Reserve();
            doc.Write(contentObj, Stream(content));

            // Font resource dictionary entries (resourceKey -> indirect font object).
            var fontRes = new StringBuilder();
            foreach ((string key, string baseFont) in fontsOnPage)
            {
                fontRes.Append('/').Append(key).Append(' ').Append(FontObject(baseFont)).Append(" 0 R ");
            }

            var xobjectRes = new StringBuilder();
            foreach ((string key, int num) in xobjectsOnPage)
            {
                xobjectRes.Append('/').Append(key).Append(' ').Append(num).Append(" 0 R ");
            }

            // Link annotations.
            var annotRefs = new List<int>();
            foreach (LinkArea link in page.Links)
            {
                int? annot = BuildLink(doc, link, page.HeightMpt, pageObjectNumbers);
                if (annot is int a)
                {
                    annotRefs.Add(a);
                }
            }

            var pageDict = new StringBuilder();
            pageDict.Append("<< /Type /Page /Parent ").Append(pagesTree).Append(" 0 R");
            pageDict.Append(" /MediaBox [0 0 ")
                .Append(F(page.WidthMpt / MptPerPoint)).Append(' ').Append(F(page.HeightMpt / MptPerPoint)).Append(']');
            pageDict.Append(" /Resources << /Font << ").Append(fontRes).Append(">>");
            if (xobjectRes.Length > 0)
            {
                pageDict.Append(" /XObject << ").Append(xobjectRes).Append(">>");
            }

            pageDict.Append(" >>");
            pageDict.Append(" /Contents ").Append(contentObj).Append(" 0 R");
            if (annotRefs.Count > 0)
            {
                pageDict.Append(" /Annots [");
                foreach (int a in annotRefs)
                {
                    pageDict.Append(a).Append(" 0 R ");
                }

                pageDict.Append(']');
            }

            pageDict.Append(" >>");
            doc.Write(pageObjectNumbers[i], pageDict.ToString());
        }

        // Font objects.
        foreach ((string baseFont, int num) in fontObjects)
        {
            doc.Write(num,
                $"<< /Type /Font /Subtype /Type1 /BaseFont /{baseFont} /Encoding /WinAnsiEncoding >>");
        }

        // Pages tree.
        var kids = new StringBuilder("[");
        foreach (int p in pageObjectNumbers)
        {
            kids.Append(p).Append(" 0 R ");
        }

        kids.Append(']');
        doc.Write(pagesTree,
            $"<< /Type /Pages /Kids {kids} /Count {pageObjectNumbers.Count} >>");

        // Optional document outline (bookmarks).
        int? outlineRoot = tree.Outline.Count > 0 && pageObjectNumbers.Count > 0
            ? BuildOutline(doc, tree.Outline, pageObjectNumbers)
            : null;

        // Catalog.
        string outlineEntry = outlineRoot is int r ? $" /Outlines {r} 0 R" : string.Empty;
        doc.Write(catalog, $"<< /Type /Catalog /Pages {pagesTree} 0 R{outlineEntry} >>");

        doc.WriteTo(output);
    }

    // ----- Content stream -------------------------------------------------------------------

    private string BuildPageContent(PageArea page, Dictionary<string, string> fontsOnPage,
        PdfFile doc, Dictionary<string, int> imageCache, Dictionary<string, int> xobjectsOnPage)
    {
        double pageHeightPt = page.HeightMpt / MptPerPoint;
        var sb = new StringBuilder();

        // Paint order matches the PdfSharp renderer: rects, images, vectors, then text.
        foreach (RectFill rect in page.RectFills)
        {
            EmitRect(sb, rect, pageHeightPt);
        }

        foreach (ImageRun image in page.Images)
        {
            EmitImage(sb, image, pageHeightPt, doc, imageCache, xobjectsOnPage);
        }

        foreach (VectorPath path in page.Vectors)
        {
            EmitVector(sb, path, pageHeightPt);
        }

        foreach (TextRun run in page.TextRuns)
        {
            EmitText(sb, run, pageHeightPt, fontsOnPage);
        }

        return sb.ToString();
    }

    private static void EmitRect(StringBuilder sb, RectFill rect, double pageHeightPt)
    {
        if (rect.WidthMpt <= 0 || rect.HeightMpt <= 0)
        {
            return;
        }

        double x = rect.XMpt / MptPerPoint;
        double y = pageHeightPt - (rect.YMpt + rect.HeightMpt) / MptPerPoint;
        double w = rect.WidthMpt / MptPerPoint;
        double h = rect.HeightMpt / MptPerPoint;
        SetFill(sb, rect.Color);
        sb.Append(F(x)).Append(' ').Append(F(y)).Append(' ').Append(F(w)).Append(' ').Append(F(h))
            .Append(" re f\n");
    }

    private static void EmitImage(StringBuilder sb, ImageRun image, double pageHeightPt,
        PdfFile doc, Dictionary<string, int> imageCache, Dictionary<string, int> xobjectsOnPage)
    {
        double x = image.XMpt / MptPerPoint;
        double y = pageHeightPt - (image.YMpt + image.HeightMpt) / MptPerPoint;
        double w = image.WidthMpt / MptPerPoint;
        double h = image.HeightMpt / MptPerPoint;
        if (w <= 0 || h <= 0)
        {
            return;
        }

        int? xobject = ResolveImageObject(image, doc, imageCache);
        if (xobject is int num)
        {
            // Register a per-page resource key for the (possibly shared) XObject and draw it: the image
            // fills the unit square, scaled and translated into place by the CTM.
            string key = "Im" + num;
            xobjectsOnPage[key] = num;
            sb.Append("q ").Append(F(w)).Append(" 0 0 ").Append(F(h)).Append(' ')
                .Append(F(x)).Append(' ').Append(F(y)).Append(" cm /").Append(key).Append(" Do Q\n");
            return;
        }

        // Decode failed (missing/unsupported): reserve the area with a light placeholder box.
        sb.Append("0.9 0.9 0.9 rg ").Append(F(x)).Append(' ').Append(F(y)).Append(' ')
            .Append(F(w)).Append(' ').Append(F(h)).Append(" re f\n");
        sb.Append("0.63 0.63 0.63 RG 0.5 w ").Append(F(x)).Append(' ').Append(F(y)).Append(' ')
            .Append(F(w)).Append(' ').Append(F(h)).Append(" re S\n");
    }

    /// <summary>
    /// Returns the image XObject object number for <paramref name="image"/>, decoding and creating it
    /// (and a soft-mask XObject for any alpha channel) on first use, or <c>null</c> when the image
    /// cannot be decoded. Path-sourced images are shared across pages via <paramref name="imageCache"/>.
    /// </summary>
    private static int? ResolveImageObject(ImageRun image, PdfFile doc, Dictionary<string, int> imageCache)
    {
        string? cacheKey = image.SourcePath;
        if (cacheKey is not null && imageCache.TryGetValue(cacheKey, out int cached))
        {
            return cached;
        }

        EmbeddableImage? decoded = RasterImage.Load(image.SourcePath, image.SourceBytes);
        if (decoded is null)
        {
            return null;
        }

        int obj = CreateImageObject(doc, decoded);
        if (cacheKey is not null)
        {
            imageCache[cacheKey] = obj;
        }

        return obj;
    }

    private static int CreateImageObject(PdfFile doc, EmbeddableImage image)
    {
        string common = $"/Type /XObject /Subtype /Image /Width {image.Width} /Height {image.Height} " +
                        "/BitsPerComponent 8";

        if (image.Encoding == ImageEncoding.Jpeg)
        {
            string colorSpace = image.Components switch
            {
                1 => "/DeviceGray",
                4 => "/DeviceCMYK",
                _ => "/DeviceRGB",
            };
            int jpeg = doc.Reserve();
            doc.Write(jpeg, PdfFile.StreamObject($"{common} /ColorSpace {colorSpace} /Filter /DCTDecode", image.Data));
            return jpeg;
        }

        // Decoded RGB, optionally with an 8-bit grayscale soft mask for alpha.
        string smaskEntry = string.Empty;
        if (image.Alpha is { } alpha)
        {
            int smask = doc.Reserve();
            doc.Write(smask, PdfFile.StreamObject(
                $"/Type /XObject /Subtype /Image /Width {image.Width} /Height {image.Height} " +
                "/BitsPerComponent 8 /ColorSpace /DeviceGray /Filter /FlateDecode",
                Deflate(alpha)));
            smaskEntry = $" /SMask {smask} 0 R";
        }

        int obj = doc.Reserve();
        doc.Write(obj, PdfFile.StreamObject(
            $"{common} /ColorSpace /DeviceRGB /Filter /FlateDecode{smaskEntry}", Deflate(image.Data)));
        return obj;
    }

    /// <summary>Compresses bytes with zlib (the PDF <c>FlateDecode</c> filter format).</summary>
    private static byte[] Deflate(byte[] data)
    {
        using var output = new MemoryStream();
        using (var zlib = new ZLibStream(output, CompressionLevel.Optimal, leaveOpen: true))
        {
            zlib.Write(data);
        }

        return output.ToArray();
    }

    private static void EmitVector(StringBuilder sb, VectorPath path, double pageHeightPt)
    {
        if (path.Segments.Count == 0 || (path.FillColor is null && path.StrokeColor is null))
        {
            return;
        }

        bool any = false;
        foreach (PathSegment seg in path.Segments)
        {
            switch (seg.Verb)
            {
                case PathVerb.MoveTo:
                    sb.Append(F(seg.X0 / MptPerPoint)).Append(' ').Append(Y(seg.Y0, pageHeightPt)).Append(" m\n");
                    break;
                case PathVerb.LineTo:
                    sb.Append(F(seg.X0 / MptPerPoint)).Append(' ').Append(Y(seg.Y0, pageHeightPt)).Append(" l\n");
                    any = true;
                    break;
                case PathVerb.CubicTo:
                    sb.Append(F(seg.X0 / MptPerPoint)).Append(' ').Append(Y(seg.Y0, pageHeightPt)).Append(' ')
                        .Append(F(seg.X1 / MptPerPoint)).Append(' ').Append(Y(seg.Y1, pageHeightPt)).Append(' ')
                        .Append(F(seg.X2 / MptPerPoint)).Append(' ').Append(Y(seg.Y2, pageHeightPt)).Append(" c\n");
                    any = true;
                    break;
                case PathVerb.QuadTo:
                    // Promote quadratic to cubic about the previous point is non-trivial without the
                    // current point here; approximate the control as both cubic controls.
                    sb.Append(F(seg.X0 / MptPerPoint)).Append(' ').Append(Y(seg.Y0, pageHeightPt)).Append(' ')
                        .Append(F(seg.X0 / MptPerPoint)).Append(' ').Append(Y(seg.Y0, pageHeightPt)).Append(' ')
                        .Append(F(seg.X1 / MptPerPoint)).Append(' ').Append(Y(seg.Y1, pageHeightPt)).Append(" c\n");
                    any = true;
                    break;
                case PathVerb.Close:
                    sb.Append("h\n");
                    break;
            }
        }

        if (!any)
        {
            return;
        }

        bool fill = path.FillColor is not null;
        bool stroke = path.StrokeColor is not null && path.StrokeWidthMpt > 0;
        if (fill)
        {
            SetFill(sb, path.FillColor!);
        }

        if (stroke)
        {
            SetStroke(sb, path.StrokeColor!);
            sb.Append(F(Math.Max(0.1, path.StrokeWidthMpt / MptPerPoint))).Append(" w\n");
        }

        sb.Append(fill && stroke ? "B\n" : fill ? "f\n" : "S\n");
    }

    private void EmitText(StringBuilder sb, TextRun run, double pageHeightPt,
        Dictionary<string, string> fontsOnPage)
    {
        if (run.Text.Length == 0)
        {
            return;
        }

        string baseFont = Base14Fonts.BaseFontName(run.Font);
        string resourceKey = ResourceKey(baseFont);
        fontsOnPage[resourceKey] = baseFont;

        double sizePt = run.Font.SizeMpt / MptPerPoint;
        double x = run.XMpt / MptPerPoint;
        double y = pageHeightPt - run.BaselineYMpt / MptPerPoint;

        sb.Append("BT\n");
        sb.Append('/').Append(resourceKey).Append(' ').Append(F(sizePt)).Append(" Tf\n");
        SetFill(sb, run.Color);
        if (run.LetterSpacingMpt != 0)
        {
            sb.Append(F(run.LetterSpacingMpt / MptPerPoint)).Append(" Tc\n");
        }

        sb.Append(F(x)).Append(' ').Append(F(y)).Append(" Td\n");
        sb.Append(EscapeWinAnsi(run.Text)).Append(" Tj\n");
        sb.Append("ET\n");

        if (!run.Decoration.IsNone)
        {
            EmitDecoration(sb, run, pageHeightPt);
        }
    }

    private void EmitDecoration(StringBuilder sb, TextRun run, double pageHeightPt)
    {
        double widthMpt = RunAdvanceMpt(run);
        if (widthMpt <= 0)
        {
            return;
        }

        double descenderMpt = measurer.DescenderMpt(run.Font);
        double capHeightMpt = measurer.CapHeightMpt(run.Font);
        double thickness = descenderMpt / 8.0;

        void Line(double centreYMpt, FopColor color)
        {
            double x = run.XMpt / MptPerPoint;
            double w = widthMpt / MptPerPoint;
            double top = centreYMpt - thickness / 2;
            double y = pageHeightPt - (top + thickness) / MptPerPoint;
            double h = thickness / MptPerPoint;
            SetFill(sb, color);
            sb.Append(F(x)).Append(' ').Append(F(y)).Append(' ').Append(F(w)).Append(' ').Append(F(h))
                .Append(" re f\n");
        }

        if (run.Decoration.UnderlineColor is { } u)
        {
            Line(run.BaselineYMpt + descenderMpt / 2.0, u);
        }

        if (run.Decoration.OverlineColor is { } o)
        {
            Line(run.BaselineYMpt - 1.1 * capHeightMpt, o);
        }

        if (run.Decoration.LineThroughColor is { } l)
        {
            Line(run.BaselineYMpt - 0.45 * capHeightMpt, l);
        }
    }

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

    // ----- Links & outline ------------------------------------------------------------------

    private static int? BuildLink(PdfFile doc, LinkArea link, double pageHeightMpt,
        IReadOnlyList<int> pageObjectNumbers)
    {
        double w = link.WidthMpt / MptPerPoint;
        double h = link.HeightMpt / MptPerPoint;
        if (w <= 0 || h <= 0)
        {
            return null;
        }

        double pageHeightPt = pageHeightMpt / MptPerPoint;
        double x = link.XMpt / MptPerPoint;
        double lowerY = pageHeightPt - (link.YMpt + link.HeightMpt) / MptPerPoint;
        string rect = $"[{F(x)} {F(lowerY)} {F(x + w)} {F(lowerY + h)}]";

        string action;
        if (link.TargetPageIndex is int target && target >= 0 && target < pageObjectNumbers.Count)
        {
            action = $" /Dest [{pageObjectNumbers[target]} 0 R /Fit]";
        }
        else if (!string.IsNullOrEmpty(link.Uri))
        {
            action = $" /A << /S /URI /URI {EscapeWinAnsi(link.Uri)} >>";
        }
        else
        {
            return null;
        }

        int obj = doc.Reserve();
        doc.Write(obj, $"<< /Type /Annot /Subtype /Link /Rect {rect} /Border [0 0 0]{action} >>");
        return obj;
    }

    /// <summary>Builds the outline tree and returns the outlines-root object number.</summary>
    private static int BuildOutline(PdfFile doc, IReadOnlyList<OutlineEntry> roots,
        IReadOnlyList<int> pageObjectNumbers)
    {
        int rootObj = doc.Reserve();
        (int first, int last, int count) = BuildOutlineItems(doc, roots, rootObj, pageObjectNumbers);
        doc.Write(rootObj, $"<< /Type /Outlines /First {first} 0 R /Last {last} 0 R /Count {count} >>");
        return rootObj;
    }

    /// <summary>
    /// Writes the outline items for <paramref name="entries"/> under <paramref name="parent"/>, wiring
    /// the sibling First/Last/Prev/Next links, and returns (firstObj, lastObj, openCount).
    /// </summary>
    private static (int First, int Last, int Count) BuildOutlineItems(PdfFile doc,
        IReadOnlyList<OutlineEntry> entries, int parent, IReadOnlyList<int> pageObjectNumbers)
    {
        // Reserve a number for each sibling so Prev/Next can reference neighbours.
        var nums = new int[entries.Count];
        for (int i = 0; i < entries.Count; i++)
        {
            nums[i] = doc.Reserve();
        }

        int visible = entries.Count;
        for (int i = 0; i < entries.Count; i++)
        {
            OutlineEntry e = entries[i];
            var sb = new StringBuilder();
            sb.Append("<< /Title ").Append(EscapeWinAnsi(e.Title));
            sb.Append(" /Parent ").Append(parent).Append(" 0 R");
            if (i > 0)
            {
                sb.Append(" /Prev ").Append(nums[i - 1]).Append(" 0 R");
            }

            if (i < entries.Count - 1)
            {
                sb.Append(" /Next ").Append(nums[i + 1]).Append(" 0 R");
            }

            int targetIndex = e.TargetPageIndex is int t && t >= 0 && t < pageObjectNumbers.Count ? t : 0;
            sb.Append(" /Dest [").Append(pageObjectNumbers[targetIndex]).Append(" 0 R /Fit]");

            if (e.Children.Count > 0)
            {
                (int cf, int cl, int cc) = BuildOutlineItems(doc, e.Children, nums[i], pageObjectNumbers);
                sb.Append(" /First ").Append(cf).Append(" 0 R /Last ").Append(cl).Append(" 0 R");
                // A closed entry hides its descendants and reports a negative count (PDF convention).
                sb.Append(" /Count ").Append(e.Open ? cc : -cc);
                if (e.Open)
                {
                    visible += cc;
                }
            }

            doc.Write(nums[i], sb.ToString());
        }

        return (nums[0], nums[^1], visible);
    }

    // ----- Helpers --------------------------------------------------------------------------

    private static void SetFill(StringBuilder sb, FopColor c) =>
        sb.Append(F(c.Red / 255.0)).Append(' ').Append(F(c.Green / 255.0)).Append(' ')
            .Append(F(c.Blue / 255.0)).Append(" rg\n");

    private static void SetStroke(StringBuilder sb, FopColor c) =>
        sb.Append(F(c.Red / 255.0)).Append(' ').Append(F(c.Green / 255.0)).Append(' ')
            .Append(F(c.Blue / 255.0)).Append(" RG\n");

    private static string Y(double yMpt, double pageHeightPt) => F(pageHeightPt - yMpt / MptPerPoint);

    private static string ResourceKey(string baseFont) => "F" + baseFont.Replace("-", string.Empty);

    /// <summary>Formats a number for a content stream: invariant, up to 4 decimals, no trailing zeros.</summary>
    private static string F(double value)
    {
        if (Math.Abs(value) < 1e-6)
        {
            return "0";
        }

        return value.ToString("0.####", CultureInfo.InvariantCulture);
    }

    /// <summary>
    /// Encodes a string as a PDF literal string in (approximately) WinAnsi: code points 32..255 are
    /// emitted as single bytes (escaping the special characters), and anything outside that range is
    /// replaced with '?'. Suitable for the WinAnsi-encoded standard fonts.
    /// </summary>
    private static string EscapeWinAnsi(string text)
    {
        var sb = new StringBuilder(text.Length + 2);
        sb.Append('(');
        foreach (char ch in text)
        {
            switch (ch)
            {
                case '(':
                    sb.Append("\\(");
                    break;
                case ')':
                    sb.Append("\\)");
                    break;
                case '\\':
                    sb.Append("\\\\");
                    break;
                case '\r':
                    sb.Append("\\r");
                    break;
                case '\n':
                    sb.Append("\\n");
                    break;
                case '\t':
                    sb.Append("\\t");
                    break;
                default:
                    if (ch is >= ' ' and <= (char)255)
                    {
                        sb.Append(ch);
                    }
                    else
                    {
                        sb.Append('?');
                    }

                    break;
            }
        }

        sb.Append(')');
        return sb.ToString();
    }

    private static string Stream(string content)
    {
        byte[] bytes = Encoding.Latin1.GetBytes(content);
        return $"<< /Length {bytes.Length} >>\nstream\n{content}\nendstream";
    }
}
