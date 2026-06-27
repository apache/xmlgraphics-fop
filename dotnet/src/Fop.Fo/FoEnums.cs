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
