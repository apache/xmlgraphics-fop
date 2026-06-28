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

namespace Fop.Fo;

/// <summary>
/// Expands XSL-FO/CSS <em>shorthand</em> properties into their longhand sub-properties, the role
/// FOP's property "makers" play during refinement. Given a longhand name and the raw attributes
/// declared on an element, it derives the longhand's value from any shorthand the element declares
/// (e.g. <c>margin-top</c> from <c>margin</c>, <c>font-size</c> from <c>font</c>, <c>page-width</c>
/// from <c>size</c>).
/// <para>
/// A directly-declared longhand always wins over a shorthand: <see cref="PropertyList"/> consults its
/// own attribute map first and only falls back to this expander, so the cascade order is
/// longhand-on-element &gt; shorthand-on-element &gt; inherited value.
/// </para>
/// <para>The border/padding shorthands are intentionally <em>not</em> handled here -- they are resolved
/// by <see cref="BoxPropertyResolver"/>, which already implements their full cascade.</para>
/// </summary>
internal static class PropertyShorthands
{
    /// <summary>
    /// Returns the value of longhand <paramref name="name"/> derived from a shorthand declared in
    /// <paramref name="own"/>, or <c>null</c> when no shorthand supplies it.
    /// </summary>
    public static string? FromShorthand(string name, IReadOnlyDictionary<string, string> own)
    {
        switch (name)
        {
            case "margin-top" or "margin-right" or "margin-bottom" or "margin-left":
                return own.TryGetValue("margin", out string? margin)
                    ? EdgeValue(margin, name["margin-".Length..])
                    : null;

            case "page-width" or "page-height":
                return own.TryGetValue("size", out string? size) ? SizeValue(size, name == "page-width") : null;

            case "font-family" or "font-size" or "font-style" or "font-weight" or "font-variant"
                or "line-height":
                return own.TryGetValue("font", out string? font) ? FontValue(font, name) : null;

            case "background-color":
                return own.TryGetValue("background", out string? bg) ? BackgroundColor(bg) : null;

            case "break-before":
                return own.TryGetValue("page-break-before", out string? pbb) ? PageBreak(pbb) : null;

            case "break-after":
                return own.TryGetValue("page-break-after", out string? pba) ? PageBreak(pba) : null;

            case "keep-together.within-page":
                // page-break-inside:avoid maps to keep-together within the page.
                return own.TryGetValue("page-break-inside", out string? pbi)
                    && pbi.Trim().Equals("avoid", StringComparison.OrdinalIgnoreCase)
                    ? "always"
                    : null;

            case "wrap-option":
                return own.TryGetValue("white-space", out string? wsWrap) ? WhiteSpaceWrap(wsWrap) : null;

            case "white-space-collapse":
                return own.TryGetValue("white-space", out string? wsCollapse)
                    ? WhiteSpaceCollapse(wsCollapse)
                    : null;

            case "linefeed-treatment":
                return own.TryGetValue("white-space", out string? wsLf) ? WhiteSpaceLinefeed(wsLf) : null;

            default:
                return null;
        }
    }

    /// <summary>The CSS 1-to-4 value expansion of a box shorthand (top/right/bottom/left), by edge name.</summary>
    private static string? EdgeValue(string shorthand, string edge)
    {
        string[] p = shorthand.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
        if (p.Length == 0)
        {
            return null;
        }

        (string t, string r, string b, string l) = p.Length switch
        {
            1 => (p[0], p[0], p[0], p[0]),
            2 => (p[0], p[1], p[0], p[1]),
            3 => (p[0], p[1], p[2], p[1]),
            _ => (p[0], p[1], p[2], p[3]),
        };

        return edge switch
        {
            "top" => t,
            "right" => r,
            "bottom" => b,
            "left" => l,
            _ => null,
        };
    }

    /// <summary>
    /// The page <c>size</c> shorthand: a named page size and/or orientation, or one/two explicit
    /// lengths (width then height; a single length applies to both). Returns the requested dimension.
    /// </summary>
    private static string? SizeValue(string size, bool wantWidth)
    {
        string[] tokens = size.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
        if (tokens.Length == 0)
        {
            return null;
        }

        // Named page sizes (portrait, in points). Orientation keywords swap width/height.
        (double w, double h)? named = null;
        bool landscape = false;
        bool portrait = false;
        var lengths = new List<string>();

        foreach (string token in tokens)
        {
            string t = token.Trim().ToLowerInvariant();
            switch (t)
            {
                case "landscape": landscape = true; break;
                case "portrait": portrait = true; break;
                case "auto": break;
                case "a3": named = (841.89, 1190.55); break;
                case "a4": named = (595.28, 841.89); break;
                case "a5": named = (419.53, 595.28); break;
                case "b5": named = (498.90, 708.66); break;
                case "letter": named = (612, 792); break;
                case "legal": named = (612, 1008); break;
                case "ledger": named = (1224, 792); break;
                default:
                    lengths.Add(token);
                    break;
            }
        }

        if (named is { } size2)
        {
            double w = size2.w, h = size2.h;
            if (landscape || (!portrait && w > h))
            {
                (w, h) = (Math.Max(w, h), Math.Min(w, h));
            }

            if (portrait)
            {
                (w, h) = (Math.Min(w, h), Math.Max(w, h));
            }

            string pts = (wantWidth ? w : h).ToString(CultureInfo.InvariantCulture) + "pt";
            return pts;
        }

        if (lengths.Count >= 2)
        {
            return wantWidth ? lengths[0] : lengths[1];
        }

        if (lengths.Count == 1)
        {
            return lengths[0]; // a single length applies to both dimensions
        }

        return null;
    }

    /// <summary>
    /// The <c>font</c> shorthand: <c>[style] [variant] [weight] size[/line-height] family...</c>. The
    /// size token (a length/keyword, optionally <c>size/line-height</c>) splits the leading style tokens
    /// from the trailing family list.
    /// </summary>
    private static string? FontValue(string font, string longhand)
    {
        string[] tokens = font.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries);
        int sizeIndex = -1;
        for (int i = 0; i < tokens.Length; i++)
        {
            if (LooksLikeFontSize(tokens[i]))
            {
                sizeIndex = i;
                break;
            }
        }

        if (sizeIndex < 0)
        {
            return null; // not a well-formed font shorthand
        }

        string style = "normal";
        string variant = "normal";
        string weight = "normal";
        for (int i = 0; i < sizeIndex; i++)
        {
            string t = tokens[i].Trim().ToLowerInvariant();
            if (t is "italic" or "oblique")
            {
                style = t;
            }
            else if (t == "small-caps")
            {
                variant = t;
            }
            else if (t is "bold" or "bolder" or "lighter" || (int.TryParse(t, out int _) && t.Length == 3))
            {
                weight = t;
            }
        }

        string sizeToken = tokens[sizeIndex];
        string sizePart = sizeToken;
        string? lineHeight = null;
        int slash = sizeToken.IndexOf('/');
        if (slash >= 0)
        {
            sizePart = sizeToken[..slash];
            lineHeight = sizeToken[(slash + 1)..];
        }

        string family = sizeIndex + 1 < tokens.Length
            ? string.Join(' ', tokens[(sizeIndex + 1)..])
            : string.Empty;

        return longhand switch
        {
            "font-style" => style,
            "font-variant" => variant,
            "font-weight" => weight,
            "font-size" => sizePart,
            "line-height" => lineHeight,
            "font-family" => family.Length > 0 ? family : null,
            _ => null,
        };
    }

    private static bool LooksLikeFontSize(string token)
    {
        string t = token.Trim().ToLowerInvariant();
        if (t is "xx-small" or "x-small" or "small" or "medium" or "large" or "x-large" or "xx-large"
            or "larger" or "smaller")
        {
            return true;
        }

        // A number, optionally with a unit/percentage and an optional /line-height.
        string head = t.Split('/')[0];
        return head.Length > 0 && (char.IsDigit(head[0]) || head[0] == '.');
    }

    private static string? BackgroundColor(string background)
    {
        // The first token that parses as a colour keyword/value; "transparent"/"none" yield no colour.
        foreach (string token in background.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries))
        {
            string t = token.Trim();
            if (t.Equals("none", StringComparison.OrdinalIgnoreCase)
                || t.Equals("transparent", StringComparison.OrdinalIgnoreCase)
                || t.StartsWith("url(", StringComparison.OrdinalIgnoreCase)
                || t is "repeat" or "no-repeat" or "scroll" or "fixed" or "center" or "left" or "right"
                    or "top" or "bottom")
            {
                continue;
            }

            return t; // candidate colour (PropertyList parses/validates it)
        }

        return null;
    }

    private static string? PageBreak(string value) => value.Trim().ToLowerInvariant() switch
    {
        "always" => "page",
        "left" => "even-page",
        "right" => "odd-page",
        // "avoid" cannot be expressed as a forced break; "auto" is the default. Both leave break auto.
        _ => null,
    };

    private static string? WhiteSpaceWrap(string value) => value.Trim().ToLowerInvariant() switch
    {
        "nowrap" or "pre" => "no-wrap",
        "normal" or "pre-wrap" or "pre-line" => "wrap",
        _ => null,
    };

    private static string? WhiteSpaceCollapse(string value) => value.Trim().ToLowerInvariant() switch
    {
        "pre" or "pre-wrap" => "false",
        "normal" or "nowrap" or "pre-line" => "true",
        _ => null,
    };

    private static string? WhiteSpaceLinefeed(string value) => value.Trim().ToLowerInvariant() switch
    {
        "pre" or "pre-wrap" or "pre-line" => "preserve",
        "normal" or "nowrap" => "treat-as-space",
        _ => null,
    };
}
