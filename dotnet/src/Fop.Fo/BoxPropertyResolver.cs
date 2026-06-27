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
using Fop.Traits;

namespace Fop.Fo;

/// <summary>
/// Resolves the box-model properties (background, borders, padding) of a single formatting object
/// from its <see cref="PropertyList"/>.
/// <para>
/// Border and padding can be expressed at several levels of specificity. The resolver applies them
/// from least to most specific so the most specific declaration wins, matching CSS/XSL-FO cascade
/// behaviour for shorthands:
/// </para>
/// <list type="number">
/// <item>the <c>border</c> shorthand (one value applied to all four edges);</item>
/// <item>the <c>border-width</c>/<c>border-color</c>/<c>border-style</c> shorthands (1-4 space
/// separated values in CSS top/right/bottom/left order);</item>
/// <item>the per-edge shorthands <c>border-{top|right|bottom|left}</c> and
/// <c>border-{before|after|start|end}</c>;</item>
/// <item>the per-edge/per-component longhands
/// <c>border-{edge}-{width|color|style}</c>.</item>
/// </list>
/// <para>
/// Writing-mode-relative edges (before/after/start/end) are mapped to physical edges assuming the
/// default lr-tb writing mode: before=top, after=bottom, start=left, end=right.
/// </para>
/// </summary>
internal static class BoxPropertyResolver
{
    // The four physical edges, in CSS top/right/bottom/left order.
    private enum Edge
    {
        Top = 0,
        Right = 1,
        Bottom = 2,
        Left = 3,
    }

    public static BoxProperties Resolve(PropertyList properties)
    {
        FopColor defaultColor = properties.GetColor();

        var width = new FoLength[4];
        var color = new FopColor[4];
        var style = new BorderStyle[4];
        for (int i = 0; i < 4; i++)
        {
            color[i] = defaultColor;
            style[i] = BorderStyle.None;
        }

        // 1. The 'border' shorthand applies one set of values to all edges.
        ApplyBorderShorthand(properties.GetRaw("border"), [Edge.Top, Edge.Right, Edge.Bottom, Edge.Left],
            width, color, style, defaultColor);

        // 2. The component shorthands (1-4 values, CSS top/right/bottom/left expansion).
        ApplyComponentShorthand(properties.GetRaw("border-width"), width,
            (raw, fontSize) => ParseBorderWidth(raw, fontSize));
        ApplyComponentShorthand(properties.GetRaw("border-color"), color,
            (raw, _) => ColorUtil.ParseColorString(null, raw) ?? defaultColor);
        ApplyComponentShorthand(properties.GetRaw("border-style"), style,
            (raw, _) => FoEnumParsing.ParseBorderStyle(raw));

        // 3. Per-edge shorthands, both CSS-ish (top/right/bottom/left) and FO-relative.
        ApplyEdgeShorthand(properties, "border-top", Edge.Top, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-right", Edge.Right, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-bottom", Edge.Bottom, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-left", Edge.Left, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-before", Edge.Top, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-after", Edge.Bottom, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-start", Edge.Left, width, color, style, defaultColor);
        ApplyEdgeShorthand(properties, "border-end", Edge.Right, width, color, style, defaultColor);

        // 4. Per-edge/per-component longhands (most specific).
        ApplyEdgeLonghands(properties, "border-top", Edge.Top, width, color, style);
        ApplyEdgeLonghands(properties, "border-right", Edge.Right, width, color, style);
        ApplyEdgeLonghands(properties, "border-bottom", Edge.Bottom, width, color, style);
        ApplyEdgeLonghands(properties, "border-left", Edge.Left, width, color, style);
        ApplyEdgeLonghands(properties, "border-before", Edge.Top, width, color, style);
        ApplyEdgeLonghands(properties, "border-after", Edge.Bottom, width, color, style);
        ApplyEdgeLonghands(properties, "border-start", Edge.Left, width, color, style);
        ApplyEdgeLonghands(properties, "border-end", Edge.Right, width, color, style);

        // Padding: shorthand (1-4 values) then per-edge longhands.
        var padding = new FoLength[4];
        ApplyComponentShorthand(properties.GetRaw("padding"), padding,
            (raw, fontSize) => ParsePadding(raw, fontSize));
        ApplyPaddingLonghand(properties, "padding-top", Edge.Top, padding);
        ApplyPaddingLonghand(properties, "padding-right", Edge.Right, padding);
        ApplyPaddingLonghand(properties, "padding-bottom", Edge.Bottom, padding);
        ApplyPaddingLonghand(properties, "padding-left", Edge.Left, padding);
        ApplyPaddingLonghand(properties, "padding-before", Edge.Top, padding);
        ApplyPaddingLonghand(properties, "padding-after", Edge.Bottom, padding);
        ApplyPaddingLonghand(properties, "padding-start", Edge.Left, padding);
        ApplyPaddingLonghand(properties, "padding-end", Edge.Right, padding);

        FopColor? background = ResolveBackground(properties.GetRaw("background-color"));

        return new BoxProperties(
            new BorderEdge(width[(int)Edge.Top], color[(int)Edge.Top], style[(int)Edge.Top]),
            new BorderEdge(width[(int)Edge.Right], color[(int)Edge.Right], style[(int)Edge.Right]),
            new BorderEdge(width[(int)Edge.Bottom], color[(int)Edge.Bottom], style[(int)Edge.Bottom]),
            new BorderEdge(width[(int)Edge.Left], color[(int)Edge.Left], style[(int)Edge.Left]),
            padding[(int)Edge.Top],
            padding[(int)Edge.Right],
            padding[(int)Edge.Bottom],
            padding[(int)Edge.Left],
            background);

        double FontSize() => properties.FontSizeMpt;

        FoLength ParsePadding(string raw, double fontSize)
            => FoLength.ParseOrDefault(raw, FoLength.Zero, fontSize);

        void ApplyComponentShorthand<T>(string? raw, T[] target, Func<string, double, T> parse)
        {
            if (string.IsNullOrWhiteSpace(raw))
            {
                return;
            }

            string[] parts = raw.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
            if (parts.Length == 0)
            {
                return;
            }

            // CSS 1-to-4 value expansion: top, right, bottom, left.
            (string t, string r, string b, string l) = parts.Length switch
            {
                1 => (parts[0], parts[0], parts[0], parts[0]),
                2 => (parts[0], parts[1], parts[0], parts[1]),
                3 => (parts[0], parts[1], parts[2], parts[1]),
                _ => (parts[0], parts[1], parts[2], parts[3]),
            };
            double fontSize = FontSize();
            target[(int)Edge.Top] = parse(t, fontSize);
            target[(int)Edge.Right] = parse(r, fontSize);
            target[(int)Edge.Bottom] = parse(b, fontSize);
            target[(int)Edge.Left] = parse(l, fontSize);
        }

        void ApplyBorderShorthand(string? raw, Edge[] edges, FoLength[] w, FopColor[] c, BorderStyle[] s,
            FopColor fallbackColor)
        {
            if (string.IsNullOrWhiteSpace(raw))
            {
                return;
            }

            (FoLength wi, FopColor? co, BorderStyle st) = ParseBorderEdgeShorthand(raw, FontSize());
            foreach (Edge edge in edges)
            {
                w[(int)edge] = wi;
                c[(int)edge] = co ?? fallbackColor;
                s[(int)edge] = st;
            }
        }

        void ApplyEdgeShorthand(PropertyList props, string baseName, Edge edge, FoLength[] w, FopColor[] c,
            BorderStyle[] s, FopColor fallbackColor)
        {
            string? raw = props.GetRaw(baseName);
            if (string.IsNullOrWhiteSpace(raw))
            {
                return;
            }

            (FoLength wi, FopColor? co, BorderStyle st) = ParseBorderEdgeShorthand(raw, FontSize());
            w[(int)edge] = wi;
            c[(int)edge] = co ?? fallbackColor;
            s[(int)edge] = st;
        }

        void ApplyEdgeLonghands(PropertyList props, string baseName, Edge edge, FoLength[] w, FopColor[] c,
            BorderStyle[] s)
        {
            string? rawWidth = props.GetRaw(baseName + "-width");
            if (rawWidth is not null)
            {
                w[(int)edge] = ParseBorderWidth(rawWidth, FontSize());
            }

            string? rawColor = props.GetRaw(baseName + "-color");
            if (rawColor is not null)
            {
                FopColor? parsed = ColorUtil.ParseColorString(null, rawColor);
                if (parsed is not null)
                {
                    c[(int)edge] = parsed;
                }
            }

            string? rawStyle = props.GetRaw(baseName + "-style");
            if (rawStyle is not null)
            {
                s[(int)edge] = FoEnumParsing.ParseBorderStyle(rawStyle);
            }
        }

        void ApplyPaddingLonghand(PropertyList props, string name, Edge edge, FoLength[] target)
        {
            string? raw = props.GetRaw(name);
            if (raw is not null)
            {
                target[(int)edge] = FoLength.ParseOrDefault(raw, FoLength.Zero, FontSize());
            }
        }
    }

    /// <summary>
    /// Parses a single edge's <c>border</c>/<c>border-{edge}</c> shorthand (e.g. <c>"1pt solid black"</c>)
    /// into width/colour/style. Order-independent: a length-with-unit is the width, a recognised
    /// keyword is the style, anything else is attempted as a colour.
    /// </summary>
    private static (FoLength Width, FopColor? Color, BorderStyle Style) ParseBorderEdgeShorthand(
        string raw, double fontSizeMpt)
    {
        FoLength width = FoLength.Zero;
        FopColor? color = null;
        BorderStyle style = BorderStyle.None;
        bool widthSet = false;
        bool styleSet = false;

        foreach (string token in raw.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries))
        {
            if (!styleSet && IsBorderStyleKeyword(token))
            {
                style = FoEnumParsing.ParseBorderStyle(token);
                styleSet = true;
                continue;
            }

            FoLength? len = TryParseBorderWidth(token, fontSizeMpt);
            if (!widthSet && len is not null)
            {
                width = len.Value;
                widthSet = true;
                continue;
            }

            FopColor? parsed = ColorUtil.ParseColorString(null, token);
            if (parsed is not null)
            {
                color = parsed;
            }
        }

        return (width, color, style);
    }

    private static bool IsBorderStyleKeyword(string token) => token.Trim().ToLowerInvariant() switch
    {
        "none" or "hidden" or "dotted" or "dashed" or "solid" or "double" or "groove" or "ridge"
            or "inset" or "outset" => true,
        _ => false,
    };

    /// <summary>Parses a border width, mapping the <c>thin/medium/thick</c> keywords to 1/3/5pt.</summary>
    private static FoLength ParseBorderWidth(string raw, double fontSizeMpt)
        => TryParseBorderWidth(raw, fontSizeMpt) ?? FoLength.Zero;

    private static FoLength? TryParseBorderWidth(string raw, double fontSizeMpt)
        => raw.Trim().ToLowerInvariant() switch
        {
            "thin" => FoLength.FromPoints(1),
            "medium" => FoLength.FromPoints(3),
            "thick" => FoLength.FromPoints(5),
            _ => FoLength.TryParse(raw, fontSizeMpt),
        };

    private static FopColor? ResolveBackground(string? raw)
    {
        if (string.IsNullOrWhiteSpace(raw))
        {
            return null;
        }

        if (raw.Trim().Equals("transparent", StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        return ColorUtil.ParseColorString(null, raw);
    }
}
