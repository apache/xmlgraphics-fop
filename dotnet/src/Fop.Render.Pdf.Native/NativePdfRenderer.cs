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
    private readonly INativeFontProvider? fontProvider;

    /// <summary>Creates a native renderer using <paramref name="measurer"/> for decoration metrics.</summary>
    public NativePdfRenderer(IFontMeasurer measurer)
        : this(measurer, fontProvider: null)
    {
    }

    /// <summary>
    /// Creates a native renderer with <paramref name="measurer"/> for decoration metrics and an
    /// optional <paramref name="fontProvider"/>. When the provider supplies a font program for a used
    /// font, that TrueType/OpenType face is embedded (so the output is self-contained and not limited
    /// to the standard-14 fonts); otherwise the renderer falls back to a metric-compatible standard-14
    /// font.
    /// </summary>
    public NativePdfRenderer(IFontMeasurer measurer, INativeFontProvider? fontProvider)
    {
        this.measurer = measurer ?? throw new ArgumentNullException(nameof(measurer));
        this.fontProvider = fontProvider;
    }

    /// <summary>Renders <paramref name="tree"/> and writes the PDF bytes to <paramref name="output"/>.</summary>
    public void Render(AreaTree tree, Stream output) => Render(tree, output, encryption: null);

    /// <summary>
    /// Renders <paramref name="tree"/> to <paramref name="output"/>, optionally encrypting the document
    /// with the standard security handler when <paramref name="encryption"/> is supplied.
    /// </summary>
    public void Render(AreaTree tree, Stream output, PdfEncryptionOptions? encryption)
    {
        ArgumentNullException.ThrowIfNull(tree);
        ArgumentNullException.ThrowIfNull(output);

        var doc = new PdfFile();
        int catalog = doc.Reserve();
        int pagesTree = doc.Reserve();

        // Encryption: derive the handler from a fresh file id, publish the /Encrypt dict (its own
        // strings are never encrypted) and the trailer /ID. When off, a no-op encryptor is used.
        IObjectEncryptor enc = NoEncryption.Instance;
        if (encryption is not null)
        {
            byte[] fileId = Guid.NewGuid().ToByteArray();
            var handler = new StandardSecurityHandler(encryption, fileId);
            int encryptObj = doc.Reserve();
            doc.Write(encryptObj, $"<< {handler.EncryptDictionary()} >>");
            doc.EncryptReference = encryptObj;
            doc.FileIdHex = Convert.ToHexString(fileId);
            enc = handler;
        }

        // Shared font resources: embedded TrueType faces (when a provider supplies them) and/or the
        // standard-14 fallback, each created once and reused across pages. The per-font set of used
        // code points (collected up front) drives glyph subsetting.
        var fonts = new FontResources(doc, fontProvider, CollectFontUsage(tree), enc);

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
            var fontsOnPage = new Dictionary<string, int>(StringComparer.Ordinal); // resourceKey -> font object
            var xobjectsOnPage = new Dictionary<string, int>(StringComparer.Ordinal); // resourceKey -> object
            string content = BuildPageContent(page, fonts, fontsOnPage, doc, imageCache, xobjectsOnPage, enc);

            int contentObj = doc.Reserve();
            doc.Write(contentObj, PdfFile.StreamObject("/Filter /FlateDecode",
                enc.EncryptStream(contentObj, Deflate(Encoding.Latin1.GetBytes(content)))));

            // Font resource dictionary entries (resourceKey -> indirect font object).
            var fontRes = new StringBuilder();
            foreach ((string key, int num) in fontsOnPage)
            {
                fontRes.Append('/').Append(key).Append(' ').Append(num).Append(" 0 R ");
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
                int? annot = BuildLink(doc, link, page.HeightMpt, pageObjectNumbers, enc);
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
            ? BuildOutline(doc, tree.Outline, pageObjectNumbers, enc)
            : null;

        // Catalog.
        string outlineEntry = outlineRoot is int r ? $" /Outlines {r} 0 R" : string.Empty;
        doc.Write(catalog, $"<< /Type /Catalog /Pages {pagesTree} 0 R{outlineEntry} >>");

        doc.WriteTo(output);
    }

    /// <summary>
    /// Collects, per font, the set of code points used in the document's text (page runs and the runs
    /// inside transformed groups), so each embedded face can be subset to just those glyphs.
    /// </summary>
    private static Dictionary<FontKey, HashSet<int>> CollectFontUsage(AreaTree tree)
    {
        var usage = new Dictionary<FontKey, HashSet<int>>();

        void Add(TextRun run)
        {
            if (!usage.TryGetValue(run.Font, out HashSet<int>? set))
            {
                set = new HashSet<int>();
                usage[run.Font] = set;
            }

            foreach (char c in run.Text)
            {
                set.Add(c);
            }
        }

        foreach (PageArea page in tree.Pages)
        {
            foreach (TextRun run in page.TextRuns)
            {
                Add(run);
            }

            foreach (AreaGroup group in page.Groups)
            {
                foreach (TextRun run in group.TextRuns)
                {
                    Add(run);
                }
            }
        }

        return usage;
    }

    // ----- Content stream -------------------------------------------------------------------

    private string BuildPageContent(PageArea page, FontResources fonts, Dictionary<string, int> fontsOnPage,
        PdfFile doc, Dictionary<string, int> imageCache, Dictionary<string, int> xobjectsOnPage,
        IObjectEncryptor enc)
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
            EmitImage(sb, image, pageHeightPt, doc, imageCache, xobjectsOnPage, enc);
        }

        foreach (VectorPath path in page.Vectors)
        {
            EmitVector(sb, path, pageHeightPt);
        }

        foreach (TextRun run in page.TextRuns)
        {
            EmitText(sb, run, pageHeightPt, fonts, fontsOnPage);
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
        PdfFile doc, Dictionary<string, int> imageCache, Dictionary<string, int> xobjectsOnPage,
        IObjectEncryptor enc)
    {
        double x = image.XMpt / MptPerPoint;
        double y = pageHeightPt - (image.YMpt + image.HeightMpt) / MptPerPoint;
        double w = image.WidthMpt / MptPerPoint;
        double h = image.HeightMpt / MptPerPoint;
        if (w <= 0 || h <= 0)
        {
            return;
        }

        int? xobject = ResolveImageObject(image, doc, imageCache, enc);
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
    private static int? ResolveImageObject(ImageRun image, PdfFile doc, Dictionary<string, int> imageCache,
        IObjectEncryptor enc)
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

        int obj = CreateImageObject(doc, decoded, enc);
        if (cacheKey is not null)
        {
            imageCache[cacheKey] = obj;
        }

        return obj;
    }

    private static int CreateImageObject(PdfFile doc, EmbeddableImage image, IObjectEncryptor enc)
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
            doc.Write(jpeg, PdfFile.StreamObject($"{common} /ColorSpace {colorSpace} /Filter /DCTDecode",
                enc.EncryptStream(jpeg, image.Data)));
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
                enc.EncryptStream(smask, Deflate(alpha))));
            smaskEntry = $" /SMask {smask} 0 R";
        }

        int obj = doc.Reserve();
        doc.Write(obj, PdfFile.StreamObject(
            $"{common} /ColorSpace /DeviceRGB /Filter /FlateDecode{smaskEntry}",
            enc.EncryptStream(obj, Deflate(image.Data))));
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

    private void EmitText(StringBuilder sb, TextRun run, double pageHeightPt, FontResources fonts,
        Dictionary<string, int> fontsOnPage)
    {
        if (run.Text.Length == 0)
        {
            return;
        }

        UsedFont used = fonts.Use(run.Font, fontsOnPage);

        double sizePt = run.Font.SizeMpt / MptPerPoint;
        double x = run.XMpt / MptPerPoint;
        double y = pageHeightPt - run.BaselineYMpt / MptPerPoint;

        sb.Append("BT\n");
        sb.Append('/').Append(used.ResourceKey).Append(' ').Append(F(sizePt)).Append(" Tf\n");
        SetFill(sb, run.Color);
        if (run.LetterSpacingMpt != 0)
        {
            sb.Append(F(run.LetterSpacingMpt / MptPerPoint)).Append(" Tc\n");
        }

        sb.Append(F(x)).Append(' ').Append(F(y)).Append(" Td\n");

        // An embedded Identity-H font is addressed by 2-byte glyph ids; a standard-14 font by WinAnsi
        // single-byte codes.
        sb.Append(used.Embedded is { } ttf ? GlyphHex(run.Text, ttf) : EscapeWinAnsi(run.Text)).Append(" Tj\n");
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
        IReadOnlyList<int> pageObjectNumbers, IObjectEncryptor enc)
    {
        double w = link.WidthMpt / MptPerPoint;
        double h = link.HeightMpt / MptPerPoint;
        if (w <= 0 || h <= 0)
        {
            return null;
        }

        bool internalLink = link.TargetPageIndex is int t && t >= 0 && t < pageObjectNumbers.Count;
        if (!internalLink && string.IsNullOrEmpty(link.Uri))
        {
            return null;
        }

        double pageHeightPt = pageHeightMpt / MptPerPoint;
        double x = link.XMpt / MptPerPoint;
        double lowerY = pageHeightPt - (link.YMpt + link.HeightMpt) / MptPerPoint;
        string rect = $"[{F(x)} {F(lowerY)} {F(x + w)} {F(lowerY + h)}]";

        // Reserve the object first so the URI string can be encrypted under its key.
        int obj = doc.Reserve();
        string action = internalLink
            ? $" /Dest [{pageObjectNumbers[link.TargetPageIndex!.Value]} 0 R /Fit]"
            : $" /A << /S /URI /URI {enc.LiteralString(obj, link.Uri!)} >>";

        doc.Write(obj, $"<< /Type /Annot /Subtype /Link /Rect {rect} /Border [0 0 0]{action} >>");
        return obj;
    }

    /// <summary>Builds the outline tree and returns the outlines-root object number.</summary>
    private static int BuildOutline(PdfFile doc, IReadOnlyList<OutlineEntry> roots,
        IReadOnlyList<int> pageObjectNumbers, IObjectEncryptor enc)
    {
        int rootObj = doc.Reserve();
        (int first, int last, int count) = BuildOutlineItems(doc, roots, rootObj, pageObjectNumbers, enc);
        doc.Write(rootObj, $"<< /Type /Outlines /First {first} 0 R /Last {last} 0 R /Count {count} >>");
        return rootObj;
    }

    /// <summary>
    /// Writes the outline items for <paramref name="entries"/> under <paramref name="parent"/>, wiring
    /// the sibling First/Last/Prev/Next links, and returns (firstObj, lastObj, openCount).
    /// </summary>
    private static (int First, int Last, int Count) BuildOutlineItems(PdfFile doc,
        IReadOnlyList<OutlineEntry> entries, int parent, IReadOnlyList<int> pageObjectNumbers,
        IObjectEncryptor enc)
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
            sb.Append("<< /Title ").Append(enc.LiteralString(nums[i], e.Title));
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
                (int cf, int cl, int cc) = BuildOutlineItems(doc, e.Children, nums[i], pageObjectNumbers, enc);
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

    /// <summary>
    /// Owns the document's font objects: it embeds a TrueType/OpenType face (via the optional provider)
    /// the first time a <see cref="FontKey"/> is used, or creates a standard-14 fallback font,
    /// reusing each across pages. <see cref="Use"/> registers the font in a page's resource map and
    /// returns the resource key the content stream references.
    /// </summary>
    /// <summary>
    /// How a run's font is realized in the content stream: the resource key to select with <c>Tf</c>,
    /// and -- for an embedded Identity-H font -- the parsed face used to encode text as 2-byte glyph
    /// ids. <see cref="Embedded"/> is <c>null</c> for a standard-14 fallback (WinAnsi single bytes).
    /// </summary>
    private readonly record struct UsedFont(string ResourceKey, TrueTypeFont? Embedded);

    private sealed class FontResources(
        PdfFile doc, INativeFontProvider? provider, IReadOnlyDictionary<FontKey, HashSet<int>> usage,
        IObjectEncryptor enc)
    {
        private readonly Dictionary<FontKey, (string Key, int Obj, TrueTypeFont Ttf)> embedded = new();
        private readonly Dictionary<string, (string Key, int Obj)> standard = new(StringComparer.Ordinal);
        private int embeddedCounter;

        public UsedFont Use(FontKey font, Dictionary<string, int> fontsOnPage)
        {
            if (provider is not null)
            {
                if (embedded.TryGetValue(font, out var e))
                {
                    fontsOnPage[e.Key] = e.Obj;
                    return new UsedFont(e.Key, e.Ttf);
                }

                if (TryEmbed(font) is (string ek, int eo, TrueTypeFont ettf))
                {
                    embedded[font] = (ek, eo, ettf);
                    fontsOnPage[ek] = eo;
                    return new UsedFont(ek, ettf);
                }
            }

            string baseFont = Base14Fonts.BaseFontName(font);
            if (!standard.TryGetValue(baseFont, out var s))
            {
                int obj = doc.Reserve();
                doc.Write(obj,
                    $"<< /Type /Font /Subtype /Type1 /BaseFont /{baseFont} /Encoding /WinAnsiEncoding >>");
                s = ("F" + baseFont.Replace("-", string.Empty), obj);
                standard[baseFont] = s;
            }

            fontsOnPage[s.Key] = s.Obj;
            return new UsedFont(s.Key, Embedded: null);
        }

        /// <summary>
        /// Builds an embedded, subsetted font as a Type0 (composite) font with Identity-H encoding and
        /// a CIDFontType2 descendant. Identity-H means the content stream addresses glyphs by their
        /// 2-byte glyph id directly, so any Unicode character the face covers can be shown (not just
        /// WinAnsi). Returns the Type0 font object and the parsed face for glyph-id encoding.
        /// </summary>
        private (string Key, int Obj, TrueTypeFont Ttf)? TryEmbed(FontKey font)
        {
            byte[]? program = provider!.GetFontProgram(font);
            if (program is null)
            {
                return null;
            }

            TrueTypeFont? ttf = TrueTypeFont.Parse(program);
            if (ttf is null)
            {
                return null;
            }

            // The glyphs actually used (always include .notdef). Subset to them; on failure embed full.
            usage.TryGetValue(font, out HashSet<int>? codePoints);
            var usedGlyphs = new SortedSet<int> { 0 };
            foreach (int cp in codePoints ?? [])
            {
                usedGlyphs.Add(ttf.GlyphIndex(cp));
            }

            byte[] embedProgram = program;
            string prefix = string.Empty;
            byte[]? subset = TrueTypeSubsetter.Subset(program, usedGlyphs);
            if (subset is not null)
            {
                embedProgram = subset;
                prefix = SubsetPrefix(font) + "+";
            }

            string name = prefix + EmbeddedFontName(font, embeddedCounter);

            // FontFile2: the (deflated) font program, with /Length1 = the uncompressed length.
            int fontFile = doc.Reserve();
            doc.Write(fontFile, PdfFile.StreamObject(
                $"/Length1 {embedProgram.Length} /Filter /FlateDecode",
                enc.EncryptStream(fontFile, Deflate(embedProgram))));

            int flags = 32 | (ttf.Italic || font.IsItalic ? 64 : 0); // Nonsymbolic [+ Italic]
            int stemV = font.IsBold || ttf.Bold ? 120 : 80;
            int descriptor = doc.Reserve();
            doc.Write(descriptor,
                $"<< /Type /FontDescriptor /FontName /{name} /Flags {flags} " +
                $"/FontBBox [{ttf.FontBBox[0]} {ttf.FontBBox[1]} {ttf.FontBBox[2]} {ttf.FontBBox[3]}] " +
                $"/ItalicAngle {F(ttf.ItalicAngle)} /Ascent {ttf.Ascent} /Descent {ttf.Descent} " +
                $"/CapHeight {ttf.CapHeight} /StemV {stemV} /FontFile2 {fontFile} 0 R >>");

            // /W: per-CID (== glyph id, Identity map) advance widths, only for the used glyphs.
            var w = new StringBuilder();
            foreach (int gid in usedGlyphs)
            {
                w.Append(gid).Append(" [").Append(ttf.AdvanceWidthByGlyph(gid)).Append("] ");
            }

            int cidFont = doc.Reserve();
            doc.Write(cidFont,
                $"<< /Type /Font /Subtype /CIDFontType2 /BaseFont /{name} " +
                $"/CIDSystemInfo << /Registry {enc.LiteralString(cidFont, "Adobe")} " +
                $"/Ordering {enc.LiteralString(cidFont, "Identity")} /Supplement 0 >> " +
                $"/FontDescriptor {descriptor} 0 R /CIDToGIDMap /Identity /DW 0 /W [{w.ToString().TrimEnd()}] >>");

            int toUnicode = doc.Reserve();
            doc.Write(toUnicode, PdfFile.StreamObject("/Filter /FlateDecode",
                enc.EncryptStream(toUnicode, Deflate(Encoding.Latin1.GetBytes(ToUnicodeCMap(ttf, codePoints))))));

            int fontObj = doc.Reserve();
            doc.Write(fontObj,
                $"<< /Type /Font /Subtype /Type0 /BaseFont /{name} /Encoding /Identity-H " +
                $"/DescendantFonts [{cidFont} 0 R] /ToUnicode {toUnicode} 0 R >>");

            string key = "FE" + embeddedCounter++;
            return (key, fontObj, ttf);
        }

        /// <summary>
        /// Builds a ToUnicode CMap mapping each used glyph id (CID, Identity) back to its Unicode code
        /// point, so text in the embedded Identity-H font remains searchable / copyable.
        /// </summary>
        private static string ToUnicodeCMap(TrueTypeFont ttf, HashSet<int>? codePoints)
        {
            var mappings = new SortedDictionary<int, int>(); // glyph id -> code point
            foreach (int cp in codePoints ?? [])
            {
                int gid = ttf.GlyphIndex(cp);
                if (gid > 0 && !mappings.ContainsKey(gid))
                {
                    mappings[gid] = cp;
                }
            }

            var sb = new StringBuilder();
            sb.Append("/CIDInit /ProcSet findresource begin\n12 dict begin\nbegincmap\n");
            sb.Append("/CIDSystemInfo << /Registry (Adobe) /Ordering (UCS) /Supplement 0 >> def\n");
            sb.Append("/CMapName /Adobe-Identity-UCS def\n/CMapType 2 def\n");
            sb.Append("1 begincodespacerange\n<0000> <FFFF>\nendcodespacerange\n");
            if (mappings.Count > 0)
            {
                sb.Append(mappings.Count).Append(" beginbfchar\n");
                foreach ((int gid, int cp) in mappings)
                {
                    sb.Append('<').Append(gid.ToString("X4")).Append("> <")
                        .Append(cp.ToString("X4")).Append(">\n");
                }

                sb.Append("endbfchar\n");
            }

            sb.Append("endcmap\nCMapName currentdict /CMap defineresource pop\nend\nend\n");
            return sb.ToString();
        }

        /// <summary>
        /// A deterministic 6-uppercase-letter subset tag for a font (PDF requires a <c>XXXXXX+</c>
        /// prefix on a subset font's name), derived from the font's identity.
        /// </summary>
        private static string SubsetPrefix(FontKey font)
        {
            // A small FNV-1a hash over the font identity, rendered as six letters A-Z.
            uint hash = 2166136261u;
            foreach (char c in $"{font.Family}|{font.Weight}|{font.Style}|{font.SizeMpt}")
            {
                hash = (hash ^ c) * 16777619u;
            }

            var letters = new char[6];
            for (int i = 0; i < 6; i++)
            {
                letters[i] = (char)('A' + (int)(hash % 26));
                hash /= 26;
            }

            return new string(letters);
        }

        /// <summary>A unique PostScript-style font name for an embedded face (without the subset prefix).</summary>
        private static string EmbeddedFontName(FontKey font, int index)
        {
            string family = new string(font.Family.Where(char.IsLetterOrDigit).ToArray());
            if (family.Length == 0)
            {
                family = "Font";
            }

            string style = (font.IsBold, font.IsItalic) switch
            {
                (true, true) => "-BoldItalic",
                (true, false) => "-Bold",
                (false, true) => "-Italic",
                _ => string.Empty,
            };
            return $"{family}{style}-{index}";
        }
    }

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
    /// <summary>
    /// Encodes text as a hex string of 2-byte glyph ids for an embedded Identity-H font, mapping each
    /// character through the font's cmap. Surrogate pairs are combined into a single code point.
    /// </summary>
    private static string GlyphHex(string text, TrueTypeFont ttf)
    {
        var sb = new StringBuilder(text.Length * 4 + 2);
        sb.Append('<');
        for (int i = 0; i < text.Length; i++)
        {
            int codePoint = text[i];
            if (char.IsHighSurrogate(text[i]) && i + 1 < text.Length && char.IsLowSurrogate(text[i + 1]))
            {
                codePoint = char.ConvertToUtf32(text[i], text[i + 1]);
                i++;
            }

            int glyph = ttf.GlyphIndex(codePoint) & 0xFFFF;
            sb.Append(glyph.ToString("X4"));
        }

        sb.Append('>');
        return sb.ToString();
    }

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

}
