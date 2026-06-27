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

using Fop.Traits;

namespace Fop.Fo;

/// <summary>The <c>text-align</c> property values.</summary>
public enum TextAlign
{
    /// <summary>Align to the start edge (left in lr-tb).</summary>
    Start,

    /// <summary>Centre the content.</summary>
    Center,

    /// <summary>Align to the end edge (right in lr-tb).</summary>
    End,

    /// <summary>Justify (stretch) the content.</summary>
    Justify,
}

/// <summary>
/// The resolved kind of a <c>break-before</c>/<c>break-after</c> property: where (if anywhere) a
/// forced break is placed relative to the object.
/// </summary>
public enum BreakKind
{
    /// <summary>No forced break (the <c>auto</c> default).</summary>
    Auto,

    /// <summary>Break to the next page (also used for <c>column</c> in this single-column engine).</summary>
    Page,

    /// <summary>Break to the next even-numbered page (inserting a blank page if required).</summary>
    EvenPage,

    /// <summary>Break to the next odd-numbered page (inserting a blank page if required).</summary>
    OddPage,
}

/// <summary>The resolved <c>keep-together</c> strength, scoped to the within-page constraint.</summary>
public enum KeepStrength
{
    /// <summary>No keep constraint (the <c>auto</c> default).</summary>
    Auto,

    /// <summary>The object must not be split (treated as <c>keep-together = always</c>).</summary>
    Always,
}

/// <summary>
/// The <c>absolute-position</c> property values of an <c>fo:block-container</c>: whether it flows
/// normally or is taken out of the flow and positioned by its <c>top</c>/<c>left</c>/etc.
/// </summary>
public enum AbsolutePosition
{
    /// <summary>Normal flow (the default): the container behaves like an ordinary block.</summary>
    Auto,

    /// <summary>
    /// Positioned absolutely relative to the containing reference area (the body region/content area).
    /// The container is removed from the normal flow.
    /// </summary>
    Absolute,

    /// <summary>
    /// Positioned relative to the page (fixed). Removed from the normal flow. This engine treats
    /// <c>fixed</c> the same as <c>absolute</c> but relative to the page rather than the content area.
    /// </summary>
    Fixed,
}

/// <summary>
/// The active <c>text-decoration</c> lines, as a combinable flag set. XSL-FO/CSS allow several to be
/// switched on at once (e.g. <c>underline overline</c>), and the <c>no-*</c> keywords clear a line
/// that an ancestor turned on; <see cref="None"/> is the unset/cleared state.
/// </summary>
[Flags]
public enum TextDecoration
{
    /// <summary>No decoration.</summary>
    None = 0,

    /// <summary>A line beneath the text baseline (<c>underline</c>).</summary>
    Underline = 1,

    /// <summary>A line above the text (<c>overline</c>).</summary>
    Overline = 2,

    /// <summary>A line through the middle of the text (<c>line-through</c>).</summary>
    LineThrough = 4,
}

/// <summary>The <c>font-style</c> property values.</summary>
public enum FontStyle
{
    /// <summary>Upright.</summary>
    Normal,

    /// <summary>Italic.</summary>
    Italic,

    /// <summary>Oblique (slanted).</summary>
    Oblique,
}

/// <summary>Helpers for parsing the curated FO enum properties.</summary>
public static class FoEnumParsing
{
    /// <summary>Parses a <c>text-align</c> keyword, defaulting to <see cref="TextAlign.Start"/>.</summary>
    public static TextAlign ParseTextAlign(string? value) => value?.Trim().ToLowerInvariant() switch
    {
        "center" => TextAlign.Center,
        "end" or "right" => TextAlign.End,
        "justify" => TextAlign.Justify,
        "start" or "left" => TextAlign.Start,
        _ => TextAlign.Start,
    };

    /// <summary>Parses a <c>font-style</c> keyword, defaulting to <see cref="FontStyle.Normal"/>.</summary>
    public static FontStyle ParseFontStyle(string? value) => value?.Trim().ToLowerInvariant() switch
    {
        "italic" => FontStyle.Italic,
        "oblique" => FontStyle.Oblique,
        _ => FontStyle.Normal,
    };

    /// <summary>
    /// Parses a CSS2 <c>font-weight</c> value to a numeric weight (100-900), mapping the
    /// <c>normal</c>/<c>bold</c> keywords to 400/700. Defaults to 400.
    /// </summary>
    public static int ParseFontWeight(string? value)
    {
        switch (value?.Trim().ToLowerInvariant())
        {
            case "bold":
            case "bolder":
                return 700;
            case "normal":
            case "lighter":
            case null:
            case "":
                return 400;
            default:
                if (int.TryParse(value, out int w))
                {
                    // round to nearest 100, clamp 100..900
                    int rounded = (int)(Math.Round(w / 100.0) * 100);
                    return Math.Clamp(rounded == 0 ? 400 : rounded, 100, 900);
                }

                return 400;
        }
    }

    /// <summary>
    /// Parses a <c>break-before</c>/<c>break-after</c> keyword. <c>column</c> is treated as
    /// <see cref="BreakKind.Page"/> (this engine is single-column); unset/<c>auto</c> yields
    /// <see cref="BreakKind.Auto"/>.
    /// </summary>
    public static BreakKind ParseBreak(string? value) => value?.Trim().ToLowerInvariant() switch
    {
        "page" or "column" => BreakKind.Page,
        "even-page" => BreakKind.EvenPage,
        "odd-page" => BreakKind.OddPage,
        _ => BreakKind.Auto,
    };

    /// <summary>
    /// Parses a <c>keep-together</c> value into a <see cref="KeepStrength"/>. A bare keyword
    /// (<c>always</c>/<c>auto</c>) or the <c>.within-page</c> component is honoured; an integer keep
    /// strength is treated as <see cref="KeepStrength.Always"/> (any positive keep forces no split).
    /// </summary>
    public static KeepStrength ParseKeep(string? value)
    {
        string? v = value?.Trim().ToLowerInvariant();
        if (string.IsNullOrEmpty(v) || v == "auto")
        {
            return KeepStrength.Auto;
        }

        return KeepStrength.Always;
    }

    /// <summary>
    /// Parses an <c>absolute-position</c> keyword, defaulting to <see cref="AbsolutePosition.Auto"/>.
    /// The <c>absolute</c> and <c>fixed</c> keywords select out-of-flow positioning.
    /// </summary>
    public static AbsolutePosition ParseAbsolutePosition(string? value) => value?.Trim().ToLowerInvariant() switch
    {
        "absolute" => AbsolutePosition.Absolute,
        "fixed" => AbsolutePosition.Fixed,
        _ => AbsolutePosition.Auto,
    };

    /// <summary>
    /// Parses a <c>reference-orientation</c> value (an integer number of degrees) and normalizes it to
    /// one of 0/90/180/270. The XSL-FO permitted values are 0, 90, 180, 270, -90, -180, -270; a negative
    /// value is normalized into the [0, 360) range (e.g. -90 =&gt; 270). Any other or unparseable value
    /// defaults to 0.
    /// </summary>
    public static int ParseReferenceOrientation(string? value)
    {
        if (string.IsNullOrWhiteSpace(value)
            || !int.TryParse(value.Trim(), System.Globalization.NumberStyles.Integer,
                System.Globalization.CultureInfo.InvariantCulture, out int degrees))
        {
            return 0;
        }

        // Normalize into [0, 360). Only the right-angle multiples are valid; anything else falls to 0.
        int normalized = ((degrees % 360) + 360) % 360;
        return normalized is 0 or 90 or 180 or 270 ? normalized : 0;
    }

    /// <summary>
    /// Parses a <c>text-decoration</c> value into the set of lines it leaves active, applied over an
    /// <paramref name="inherited"/> set (text-decoration is not a normally-inheriting property in
    /// XSL-FO, but its effect propagates to descendants, so callers thread the ancestor set through).
    /// The value is a space-separated list of <c>underline</c>/<c>overline</c>/<c>line-through</c>
    /// (each turning a line on), their <c>no-*</c> counterparts (turning it off), <c>none</c> (clears
    /// all) and <c>blink</c> (ignored). An unset value leaves the inherited set unchanged.
    /// </summary>
    public static TextDecoration ParseTextDecoration(string? value, TextDecoration inherited = TextDecoration.None)
    {
        if (string.IsNullOrWhiteSpace(value))
        {
            return inherited;
        }

        TextDecoration result = inherited;
        foreach (string token in value.Split((char[]?)null, StringSplitOptions.RemoveEmptyEntries))
        {
            switch (token.Trim().ToLowerInvariant())
            {
                case "none": result = TextDecoration.None; break;
                case "underline": result |= TextDecoration.Underline; break;
                case "overline": result |= TextDecoration.Overline; break;
                case "line-through": result |= TextDecoration.LineThrough; break;
                case "no-underline": result &= ~TextDecoration.Underline; break;
                case "no-overline": result &= ~TextDecoration.Overline; break;
                case "no-line-through": result &= ~TextDecoration.LineThrough; break;
                // "blink" and any unknown keyword are ignored.
                default: break;
            }
        }

        return result;
    }

    /// <summary>
    /// Parses a CSS/XSL-FO <c>border-style</c> keyword, defaulting to <see cref="BorderStyle.None"/>
    /// for unset or unrecognised values.
    /// </summary>
    public static BorderStyle ParseBorderStyle(string? value) => value?.Trim().ToLowerInvariant() switch
    {
        "hidden" => BorderStyle.Hidden,
        "dotted" => BorderStyle.Dotted,
        "dashed" => BorderStyle.Dashed,
        "solid" => BorderStyle.Solid,
        "double" => BorderStyle.Double,
        "groove" => BorderStyle.Groove,
        "ridge" => BorderStyle.Ridge,
        "inset" => BorderStyle.Inset,
        "outset" => BorderStyle.Outset,
        _ => BorderStyle.None,
    };
}
