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

namespace Fop.Fo;

/// <summary>
/// Holds the raw XSL-FO property values declared on a single formatting object and resolves them
/// against the inheritance chain, applying initial values and the curated subset of inheritance
/// rules.
/// <para>
/// This is a pragmatic, modern reformulation of FOP's <c>org.apache.fop.fo.PropertyList</c> +
/// <c>PropertyMaker</c> machinery: rather than the full ~290-property maker system, it resolves the
/// subset of properties the current layout/render pipeline understands. New properties slot in by
/// extending <see cref="InheritedProperties"/> and adding a typed accessor.
/// </para>
/// </summary>
public sealed class PropertyList
{
    /// <summary>The XSL-FO properties that inherit from the parent formatting object.</summary>
    private static readonly HashSet<string> InheritedProperties = new(StringComparer.Ordinal)
    {
        "color",
        "font-family",
        "font-size",
        "font-style",
        "font-weight",
        "line-height",
        "text-align",
        "text-indent",
        "language",
        "country",
        "white-space-collapse",
        "wrap-option",
    };

    private const double DefaultFontSizeMpt = 12_000;

    private readonly Dictionary<string, string> own;
    private readonly PropertyList? parent;
    private double? resolvedFontSizeMpt;

    /// <summary>Creates a property list for an object with the given raw attributes.</summary>
    /// <param name="attributes">The raw attribute name/value pairs declared on the element.</param>
    /// <param name="parent">The parent object's property list, for inheritance.</param>
    public PropertyList(IReadOnlyDictionary<string, string> attributes, PropertyList? parent)
    {
        ArgumentNullException.ThrowIfNull(attributes);
        own = new Dictionary<string, string>(StringComparer.Ordinal);
        foreach (var (key, value) in attributes)
        {
            own[key] = value;
        }

        this.parent = parent;
    }

    /// <summary>
    /// Returns the raw declared value of a property, walking up the inheritance chain for inherited
    /// properties. Returns <c>null</c> when unset.
    /// </summary>
    public string? GetRaw(string name)
    {
        if (own.TryGetValue(name, out var value))
        {
            return value;
        }

        if (InheritedProperties.Contains(name) && parent is not null)
        {
            return parent.GetRaw(name);
        }

        return null;
    }

    /// <summary>Returns whether the property is declared locally (ignoring inheritance).</summary>
    public bool HasOwn(string name) => own.ContainsKey(name);

    /// <summary>Returns a string property value or <paramref name="default"/> if unset.</summary>
    public string GetString(string name, string @default) => GetRaw(name) ?? @default;

    /// <summary>The resolved <c>font-size</c> in millipoints, resolving <c>em</c>/<c>%</c> against the parent.</summary>
    public double FontSizeMpt => resolvedFontSizeMpt ??= ResolveFontSize();

    private double ResolveFontSize()
    {
        double parentSize = parent?.FontSizeMpt ?? DefaultFontSizeMpt;
        string? raw = own.TryGetValue("font-size", out var v) ? v : null;
        if (raw is null)
        {
            // font-size inherits.
            return parent?.FontSizeMpt ?? DefaultFontSizeMpt;
        }

        return raw.Trim().ToLowerInvariant() switch
        {
            "xx-small" => 6_000,
            "x-small" => 7_500,
            "small" => 10_000,
            "medium" => 12_000,
            "large" => 14_000,
            "x-large" => 18_000,
            "xx-large" => 24_000,
            "larger" => parentSize * 1.2,
            "smaller" => parentSize / 1.2,
            _ => (FoLength.TryParse(raw, parentSize, parentSize)?.Millipoints) ?? parentSize,
        };
    }

    /// <summary>Resolves a length property in millipoints.</summary>
    /// <param name="name">The property name.</param>
    /// <param name="default">The fallback length when unset or invalid.</param>
    /// <param name="percentBaseMpt">The base length for resolving percentages.</param>
    public FoLength GetLength(string name, FoLength @default, double percentBaseMpt = 0)
    {
        string? raw = GetRaw(name);
        return FoLength.ParseOrDefault(raw, @default, FontSizeMpt, percentBaseMpt);
    }

    /// <summary>Resolves the <c>color</c> property, defaulting to black.</summary>
    public FopColor GetColor(string name = "color")
    {
        string? raw = GetRaw(name);
        if (raw is not null)
        {
            FopColor? parsed = ColorUtil.ParseColorString(null, raw);
            if (parsed is not null)
            {
                return parsed;
            }
        }

        return FopColor.FromRgb(0, 0, 0);
    }

    /// <summary>
    /// Resolves the box-model properties (background colour, per-edge borders and padding). Border,
    /// padding and background are not inherited; <c>border-color</c> defaults to the current
    /// <c>color</c>. Edges are physical (top/right/bottom/left), mapping the writing-mode-relative
    /// before/after/start/end edges under the default lr-tb writing mode.
    /// </summary>
    public BoxProperties GetBox() => BoxPropertyResolver.Resolve(this);

    /// <summary>The resolved <c>text-align</c>.</summary>
    public TextAlign TextAlign => FoEnumParsing.ParseTextAlign(GetRaw("text-align"));

    /// <summary>The resolved <c>font-style</c>.</summary>
    public FontStyle FontStyle => FoEnumParsing.ParseFontStyle(GetRaw("font-style"));

    /// <summary>The resolved numeric <c>font-weight</c> (100-900).</summary>
    public int FontWeight => FoEnumParsing.ParseFontWeight(GetRaw("font-weight"));

    /// <summary>The resolved <c>font-family</c> (first family, trimmed of quotes), defaulting to serif.</summary>
    public string FontFamily
    {
        get
        {
            string raw = GetString("font-family", "serif");
            string first = raw.Split(',')[0].Trim().Trim('\'', '"');
            return first.Length == 0 ? "serif" : first;
        }
    }

    /// <summary>The resolved <c>line-height</c> in millipoints (defaults to 1.2 * font-size).</summary>
    public double LineHeightMpt
    {
        get
        {
            string? raw = GetRaw("line-height");
            if (raw is null || raw.Trim().Equals("normal", StringComparison.OrdinalIgnoreCase))
            {
                return FontSizeMpt * 1.2;
            }

            // A bare number is a multiplier of the font size.
            if (double.TryParse(raw.Trim(), System.Globalization.NumberStyles.Float,
                    System.Globalization.CultureInfo.InvariantCulture, out double factor) && !raw.Contains('%')
                && !HasUnit(raw))
            {
                return FontSizeMpt * factor;
            }

            return GetLength("line-height", new FoLength(FontSizeMpt * 1.2), FontSizeMpt).Millipoints;
        }
    }

    private static bool HasUnit(string value)
        => value.Contains("pt") || value.Contains("mm") || value.Contains("cm") || value.Contains("in")
           || value.Contains("px") || value.Contains("pc") || value.Contains("em") || value.Contains('%');
}
