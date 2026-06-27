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
using System.Xml;
using System.Xml.Linq;
using Fop.Colors;

namespace Fop.Svg;

/// <summary>
/// A pragmatic SVG parser that flattens a (static) SVG document into renderer-neutral
/// <see cref="SvgGraphic"/> primitives. It supports the common static-graphics subset: the basic
/// shapes (<c>rect</c>, <c>circle</c>, <c>ellipse</c>, <c>line</c>, <c>polyline</c>, <c>polygon</c>),
/// <c>path</c> (all commands, arcs converted to Beziers), grouping (<c>g</c>/<c>svg</c>/<c>a</c>) with
/// <c>transform</c>, presentation attributes (and the <c>style</c> attribute) for fill/stroke/opacity,
/// and simple <c>text</c>. Animation, scripting, filters, gradients, patterns, clipping and
/// <c>&lt;use&gt;</c> references are not modelled (their elements are skipped or painted with their
/// solid presentation colour).
/// </summary>
public static class SvgParser
{
    /// <summary>The SVG namespace URI.</summary>
    public const string SvgNamespace = "http://www.w3.org/2000/svg";

    /// <summary>Parses an SVG document from its XML text.</summary>
    public static SvgGraphic Parse(string svgXml)
    {
        ArgumentNullException.ThrowIfNull(svgXml);
        var settings = new XmlReaderSettings
        {
            DtdProcessing = DtdProcessing.Ignore,
            IgnoreComments = true,
            IgnoreProcessingInstructions = true,
            MaxCharactersFromEntities = 1024,
        };
        using var reader = XmlReader.Create(new StringReader(svgXml), settings);
        XDocument doc = XDocument.Load(reader);
        return Parse(doc.Root ?? throw new InvalidOperationException("SVG document has no root element."));
    }

    /// <summary>Parses an SVG document from its already-parsed root <c>&lt;svg&gt;</c> element.</summary>
    public static SvgGraphic Parse(XElement svg)
    {
        ArgumentNullException.ThrowIfNull(svg);

        // viewBox: "min-x min-y width height".
        double vbX = 0, vbY = 0, vbW = 0, vbH = 0;
        bool hasViewBox = false;
        string? vb = Attr(svg, "viewBox");
        if (vb is not null)
        {
            double[] nums = ParseNumberList(vb);
            if (nums.Length == 4)
            {
                vbX = nums[0]; vbY = nums[1]; vbW = nums[2]; vbH = nums[3];
                hasViewBox = true;
            }
        }

        // Intrinsic size from width/height (points), falling back to the viewBox extent.
        double? wAttr = ParseRootLength(Attr(svg, "width"));
        double? hAttr = ParseRootLength(Attr(svg, "height"));
        double intrinsicW = wAttr ?? (hasViewBox ? vbW : 0);
        double intrinsicH = hAttr ?? (hasViewBox ? vbH : 0);

        if (!hasViewBox)
        {
            // No viewBox: the user space IS the intrinsic pixel/point space.
            vbX = 0; vbY = 0;
            vbW = intrinsicW > 0 ? intrinsicW : 100;
            vbH = intrinsicH > 0 ? intrinsicH : 100;
        }

        if (intrinsicW <= 0) { intrinsicW = vbW; }
        if (intrinsicH <= 0) { intrinsicH = vbH; }

        var shapes = new List<SvgShape>();
        var texts = new List<SvgTextItem>();
        Walk(svg, SvgMatrix.Identity, Style.Initial, shapes, texts);

        return new SvgGraphic(intrinsicW, intrinsicH, vbX, vbY, vbW, vbH, shapes, texts);
    }

    private static void Walk(XElement el, SvgMatrix ctm, Style style,
        List<SvgShape> shapes, List<SvgTextItem> texts)
    {
        SvgMatrix local = ctm;
        string? transform = Attr(el, "transform");
        if (transform is not null)
        {
            local = ctm.Multiply(SvgTransform.Parse(transform));
        }

        Style s = style.With(el);

        switch (el.Name.LocalName)
        {
            case "g":
            case "svg":
            case "a":
            case "switch":
                foreach (XElement child in el.Elements())
                {
                    Walk(child, local, s, shapes, texts);
                }

                break;

            case "rect":
                EmitRect(el, local, s, shapes);
                break;

            case "circle":
                EmitEllipse(Num(el, "cx"), Num(el, "cy"), Num(el, "r"), Num(el, "r"), local, s, shapes);
                break;

            case "ellipse":
                EmitEllipse(Num(el, "cx"), Num(el, "cy"), Num(el, "rx"), Num(el, "ry"), local, s, shapes);
                break;

            case "line":
                EmitLine(el, local, s, shapes);
                break;

            case "polyline":
                EmitPoly(el, local, s, shapes, close: false);
                break;

            case "polygon":
                EmitPoly(el, local, s, shapes, close: true);
                break;

            case "path":
                EmitPath(el, local, s, shapes);
                break;

            case "text":
                EmitText(el, local, s, texts);
                break;

            // defs/symbol/clipPath/mask/title/desc/metadata/style/use and unknown: not rendered here.
            default:
                break;
        }
    }

    private static void EmitRect(XElement el, SvgMatrix ctm, Style s, List<SvgShape> shapes)
    {
        double x = Num(el, "x"), y = Num(el, "y");
        double w = Num(el, "width"), h = Num(el, "height");
        if (w <= 0 || h <= 0)
        {
            return;
        }

        double rx = NumOpt(el, "rx") ?? NumOpt(el, "ry") ?? 0;
        double ry = NumOpt(el, "ry") ?? NumOpt(el, "rx") ?? 0;
        rx = Math.Min(rx, w / 2);
        ry = Math.Min(ry, h / 2);

        var local = new List<SvgPathSegment>();
        if (rx > 0 && ry > 0)
        {
            // Rounded rectangle: corners as quarter-ellipse cubic Beziers (k = 0.5523).
            double kx = rx * 0.5522847498, ky = ry * 0.5522847498;
            local.Add(new SvgPathSegment(SvgVerb.MoveTo, x + rx, y));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x + w - rx, y));
            local.Add(new SvgPathSegment(SvgVerb.CubicTo, x + w - rx + kx, y, x + w, y + ry - ky, x + w, y + ry));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x + w, y + h - ry));
            local.Add(new SvgPathSegment(SvgVerb.CubicTo, x + w, y + h - ry + ky, x + w - rx + kx, y + h, x + w - rx, y + h));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x + rx, y + h));
            local.Add(new SvgPathSegment(SvgVerb.CubicTo, x + rx - kx, y + h, x, y + h - ry + ky, x, y + h - ry));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x, y + ry));
            local.Add(new SvgPathSegment(SvgVerb.CubicTo, x, y + ry - ky, x + rx - kx, y, x + rx, y));
            local.Add(new SvgPathSegment(SvgVerb.Close));
        }
        else
        {
            local.Add(new SvgPathSegment(SvgVerb.MoveTo, x, y));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x + w, y));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x + w, y + h));
            local.Add(new SvgPathSegment(SvgVerb.LineTo, x, y + h));
            local.Add(new SvgPathSegment(SvgVerb.Close));
        }

        AddShape(shapes, local, ctm, s, fillable: true);
    }

    private static void EmitEllipse(double cx, double cy, double rx, double ry,
        SvgMatrix ctm, Style s, List<SvgShape> shapes)
    {
        if (rx <= 0 || ry <= 0)
        {
            return;
        }

        double kx = rx * 0.5522847498, ky = ry * 0.5522847498;
        var local = new List<SvgPathSegment>
        {
            new(SvgVerb.MoveTo, cx + rx, cy),
            new(SvgVerb.CubicTo, cx + rx, cy + ky, cx + kx, cy + ry, cx, cy + ry),
            new(SvgVerb.CubicTo, cx - kx, cy + ry, cx - rx, cy + ky, cx - rx, cy),
            new(SvgVerb.CubicTo, cx - rx, cy - ky, cx - kx, cy - ry, cx, cy - ry),
            new(SvgVerb.CubicTo, cx + kx, cy - ry, cx + rx, cy - ky, cx + rx, cy),
            new(SvgVerb.Close),
        };
        AddShape(shapes, local, ctm, s, fillable: true);
    }

    private static void EmitLine(XElement el, SvgMatrix ctm, Style s, List<SvgShape> shapes)
    {
        var local = new List<SvgPathSegment>
        {
            new(SvgVerb.MoveTo, Num(el, "x1"), Num(el, "y1")),
            new(SvgVerb.LineTo, Num(el, "x2"), Num(el, "y2")),
        };
        AddShape(shapes, local, ctm, s, fillable: false);
    }

    private static void EmitPoly(XElement el, SvgMatrix ctm, Style s, List<SvgShape> shapes, bool close)
    {
        double[] pts = ParseNumberList(Attr(el, "points") ?? string.Empty);
        if (pts.Length < 4)
        {
            return;
        }

        var local = new List<SvgPathSegment> { new(SvgVerb.MoveTo, pts[0], pts[1]) };
        for (int i = 2; i + 1 < pts.Length; i += 2)
        {
            local.Add(new SvgPathSegment(SvgVerb.LineTo, pts[i], pts[i + 1]));
        }

        if (close)
        {
            local.Add(new SvgPathSegment(SvgVerb.Close));
        }

        AddShape(shapes, local, ctm, s, fillable: true);
    }

    private static void EmitPath(XElement el, SvgMatrix ctm, Style s, List<SvgShape> shapes)
    {
        string? d = Attr(el, "d");
        if (string.IsNullOrWhiteSpace(d))
        {
            return;
        }

        List<SvgPathSegment> local = SvgPathData.Parse(d);
        if (local.Count > 0)
        {
            AddShape(shapes, local, ctm, s, fillable: true);
        }
    }

    private static void EmitText(XElement el, SvgMatrix ctm, Style s, List<SvgTextItem> texts)
    {
        // Concatenate the element's text and any tspan text (positions on tspan are not modelled).
        string text = string.Concat(el.DescendantNodes().OfType<XText>().Select(t => t.Value));
        text = CollapseWhitespace(text);
        if (text.Length == 0)
        {
            return;
        }

        var (x, y) = ctm.Apply(Num(el, "x"), Num(el, "y"));
        FopColor fill = s.Fill ?? FopColor.FromRgb(0, 0, 0);
        double fontSize = s.FontSize * ctm.MeanScale;
        texts.Add(new SvgTextItem(x, y, text, s.FontFamily, fontSize, fill, s.Bold, s.Italic, s.TextAnchor));
    }

    private static void AddShape(List<SvgShape> shapes, List<SvgPathSegment> local, SvgMatrix ctm,
        Style s, bool fillable)
    {
        var transformed = new List<SvgPathSegment>(local.Count);
        foreach (SvgPathSegment seg in local)
        {
            transformed.Add(TransformSegment(seg, ctm));
        }

        FopColor? fill = fillable ? s.Fill : null;
        FopColor? stroke = s.Stroke;
        if (fill is null && stroke is null)
        {
            return; // invisible
        }

        double strokeWidth = stroke is not null ? s.StrokeWidth * ctm.MeanScale : 0;
        shapes.Add(new SvgShape(transformed, fill, stroke, strokeWidth));
    }

    private static SvgPathSegment TransformSegment(SvgPathSegment seg, SvgMatrix m)
    {
        switch (seg.Verb)
        {
            case SvgVerb.MoveTo:
            case SvgVerb.LineTo:
            {
                var (x, y) = m.Apply(seg.X0, seg.Y0);
                return seg with { X0 = x, Y0 = y };
            }

            case SvgVerb.QuadTo:
            {
                var (cx, cy) = m.Apply(seg.X0, seg.Y0);
                var (x, y) = m.Apply(seg.X1, seg.Y1);
                return seg with { X0 = cx, Y0 = cy, X1 = x, Y1 = y };
            }

            case SvgVerb.CubicTo:
            {
                var (c1x, c1y) = m.Apply(seg.X0, seg.Y0);
                var (c2x, c2y) = m.Apply(seg.X1, seg.Y1);
                var (x, y) = m.Apply(seg.X2, seg.Y2);
                return seg with { X0 = c1x, Y0 = c1y, X1 = c2x, Y1 = c2y, X2 = x, Y2 = y };
            }

            default:
                return seg;
        }
    }

    // ---- Attribute / value helpers -----------------------------------------------------------

    /// <summary>
    /// Returns the value of a presentation attribute, preferring an inline <c>style</c> declaration
    /// (which has higher priority in SVG) over a like-named XML attribute.
    /// </summary>
    internal static string? Attr(XElement el, string name)
    {
        string? styleVal = StyleLookup(el, name);
        if (styleVal is not null)
        {
            return styleVal;
        }

        return el.Attribute(name)?.Value;
    }

    private static string? StyleLookup(XElement el, string name)
    {
        string? style = el.Attribute("style")?.Value;
        if (string.IsNullOrEmpty(style))
        {
            return null;
        }

        foreach (string decl in style.Split(';', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries))
        {
            int colon = decl.IndexOf(':');
            if (colon > 0)
            {
                string prop = decl[..colon].Trim();
                if (prop.Equals(name, StringComparison.OrdinalIgnoreCase))
                {
                    return decl[(colon + 1)..].Trim();
                }
            }
        }

        return null;
    }

    private static double Num(XElement el, string name) => NumOpt(el, name) ?? 0;

    private static double? NumOpt(XElement el, string name) => ParseLength(Attr(el, name));

    /// <summary>Parses a coordinate/length value, ignoring any unit suffix (treated as user units).</summary>
    internal static double? ParseLength(string? value)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return null;
        }

        string t = value.Trim();
        int i = 0;
        if (i < t.Length && (t[i] == '+' || t[i] == '-'))
        {
            i++;
        }

        bool dot = false;
        while (i < t.Length && (char.IsDigit(t[i]) || (t[i] == '.' && !dot)))
        {
            if (t[i] == '.')
            {
                dot = true;
            }

            i++;
        }

        if (i < t.Length && (t[i] == 'e' || t[i] == 'E'))
        {
            i++;
            if (i < t.Length && (t[i] == '+' || t[i] == '-'))
            {
                i++;
            }

            while (i < t.Length && char.IsDigit(t[i]))
            {
                i++;
            }
        }

        return double.TryParse(t.AsSpan(0, i), NumberStyles.Float, CultureInfo.InvariantCulture, out double v) ? v : null;
    }

    /// <summary>Parses a root <c>width</c>/<c>height</c> value with units into points.</summary>
    private static double? ParseRootLength(string? value)
    {
        if (string.IsNullOrWhiteSpace(value) || value.Contains('%'))
        {
            return null;
        }

        double? n = ParseLength(value);
        if (n is null)
        {
            return null;
        }

        string unit = new string(value.Trim().Where(char.IsLetter).ToArray()).ToLowerInvariant();
        return unit switch
        {
            "in" => n * 72,
            "cm" => n * 72 / 2.54,
            "mm" => n * 72 / 25.4,
            "pc" => n * 12,
            "pt" => n,
            _ => n, // px / unitless: treat as points (1px == 1pt at 72dpi, matching FoLength)
        };
    }

    internal static double[] ParseNumberList(string s)
    {
        var list = new List<double>();
        int i = 0;
        while (i < s.Length)
        {
            while (i < s.Length && (char.IsWhiteSpace(s[i]) || s[i] == ','))
            {
                i++;
            }

            int start = i;
            if (i < s.Length && (s[i] == '+' || s[i] == '-'))
            {
                i++;
            }

            bool dot = false;
            while (i < s.Length && (char.IsDigit(s[i]) || (s[i] == '.' && !dot)))
            {
                if (s[i] == '.')
                {
                    dot = true;
                }

                i++;
            }

            if (i < s.Length && (s[i] == 'e' || s[i] == 'E'))
            {
                i++;
                if (i < s.Length && (s[i] == '+' || s[i] == '-'))
                {
                    i++;
                }

                while (i < s.Length && char.IsDigit(s[i]))
                {
                    i++;
                }
            }

            if (i > start && double.TryParse(s.AsSpan(start, i - start), NumberStyles.Float,
                    CultureInfo.InvariantCulture, out double v))
            {
                list.Add(v);
            }
            else
            {
                i++; // skip a stray character
            }
        }

        return list.ToArray();
    }

    private static string CollapseWhitespace(string text)
    {
        var sb = new System.Text.StringBuilder(text.Length);
        bool lastSpace = false;
        foreach (char c in text)
        {
            bool space = c is ' ' or '\t' or '\r' or '\n';
            if (space)
            {
                if (!lastSpace && sb.Length > 0)
                {
                    sb.Append(' ');
                }

                lastSpace = true;
            }
            else
            {
                sb.Append(c);
                lastSpace = false;
            }
        }

        return sb.ToString().TrimEnd();
    }

    /// <summary>
    /// The resolved, inheritable presentation state for an element: fill/stroke paint, stroke width,
    /// opacities and font attributes. <see cref="With"/> returns a copy overridden by an element's own
    /// presentation attributes/style.
    /// </summary>
    private sealed record Style
    {
        public FopColor? Fill { get; init; }
        public FopColor? Stroke { get; init; }
        public double StrokeWidth { get; init; }
        public double FillOpacity { get; init; }
        public double StrokeOpacity { get; init; }
        public double Opacity { get; init; }
        public FopColor CurrentColor { get; init; } = FopColor.FromRgb(0, 0, 0);
        public string FontFamily { get; init; } = "sans-serif";
        public double FontSize { get; init; }
        public bool Bold { get; init; }
        public bool Italic { get; init; }
        public SvgTextAnchor TextAnchor { get; init; }

        // The raw paint keywords are tracked so opacity changes on a descendant recompose the colour.
        private string FillPaint { get; init; } = "black";
        private string StrokePaint { get; init; } = "none";

        public static readonly Style Initial = new()
        {
            Fill = FopColor.FromRgb(0, 0, 0),
            Stroke = null,
            StrokeWidth = 1,
            FillOpacity = 1,
            StrokeOpacity = 1,
            Opacity = 1,
            CurrentColor = FopColor.FromRgb(0, 0, 0),
            FontFamily = "sans-serif",
            FontSize = 16,
            Bold = false,
            Italic = false,
            TextAnchor = SvgTextAnchor.Start,
            FillPaint = "black",
            StrokePaint = "none",
        };

        public Style With(XElement el)
        {
            Style s = this;

            string? color = Attr(el, "color");
            if (color is not null)
            {
                FopColor? c = ParsePaint(color, s.CurrentColor);
                if (c is not null)
                {
                    s = s with { CurrentColor = c };
                }
            }

            string? fillOpacity = Attr(el, "fill-opacity");
            if (fillOpacity is not null && ParseLength(fillOpacity) is double fo)
            {
                s = s with { FillOpacity = Math.Clamp(fo, 0, 1) };
            }

            string? strokeOpacity = Attr(el, "stroke-opacity");
            if (strokeOpacity is not null && ParseLength(strokeOpacity) is double so)
            {
                s = s with { StrokeOpacity = Math.Clamp(so, 0, 1) };
            }

            string? opacity = Attr(el, "opacity");
            if (opacity is not null && ParseLength(opacity) is double op)
            {
                s = s with { Opacity = Math.Clamp(op, 0, 1) };
            }

            string? fillPaint = Attr(el, "fill");
            if (fillPaint is not null)
            {
                s = s with { FillPaint = fillPaint };
            }

            string? strokePaint = Attr(el, "stroke");
            if (strokePaint is not null)
            {
                s = s with { StrokePaint = strokePaint };
            }

            string? sw = Attr(el, "stroke-width");
            if (sw is not null && ParseLength(sw) is double w)
            {
                s = s with { StrokeWidth = w };
            }

            string? ff = Attr(el, "font-family");
            if (ff is not null)
            {
                s = s with { FontFamily = ff.Split(',')[0].Trim().Trim('\'', '"') };
            }

            string? fs = Attr(el, "font-size");
            if (fs is not null && ParseLength(fs) is double size)
            {
                s = s with { FontSize = size };
            }

            string? fw = Attr(el, "font-weight");
            if (fw is not null)
            {
                s = s with { Bold = fw.Trim() is "bold" or "bolder" or "700" or "800" or "900" };
            }

            string? fst = Attr(el, "font-style");
            if (fst is not null)
            {
                s = s with { Italic = fst.Trim() is "italic" or "oblique" };
            }

            string? anchor = Attr(el, "text-anchor");
            if (anchor is not null)
            {
                s = s with
                {
                    TextAnchor = anchor.Trim().ToLowerInvariant() switch
                    {
                        "middle" => SvgTextAnchor.Middle,
                        "end" => SvgTextAnchor.End,
                        _ => SvgTextAnchor.Start,
                    },
                };
            }

            // Recompose the effective fill/stroke colours from paint keywords + opacities each time.
            s = s with
            {
                Fill = Compose(s.FillPaint, s.CurrentColor, s.FillOpacity * s.Opacity),
                Stroke = Compose(s.StrokePaint, s.CurrentColor, s.StrokeOpacity * s.Opacity),
            };

            return s;
        }

        private static FopColor? Compose(string paint, FopColor currentColor, double opacity)
        {
            FopColor? c = ParsePaint(paint, currentColor);
            if (c is null)
            {
                return null;
            }

            int alpha = (int)Math.Round(Math.Clamp(opacity, 0, 1) * c.Alpha);
            return FopColor.FromArgb(c.Red, c.Green, c.Blue, alpha);
        }

        private static FopColor? ParsePaint(string paint, FopColor currentColor)
        {
            string p = paint.Trim();
            if (p.Length == 0 || p.Equals("none", StringComparison.OrdinalIgnoreCase)
                || p.Equals("transparent", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            if (p.Equals("currentColor", StringComparison.OrdinalIgnoreCase))
            {
                return currentColor;
            }

            // url(#...) paint servers (gradients/patterns) are not supported: skip the paint.
            if (p.StartsWith("url(", StringComparison.OrdinalIgnoreCase))
            {
                return null;
            }

            try
            {
                return ColorUtil.ParseColorString(null, p);
            }
            catch (Exception)
            {
                return null;
            }
        }
    }
}
