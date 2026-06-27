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
using Fop.Fo.Expr;

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
        "hyphenate",
        "hyphenation-character",
        "hyphenation-remain-character-count",
        "hyphenation-push-character-count",
        "letter-spacing",
        "word-spacing",
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
            _ => ResolveFontSizeValue(raw, parentSize),
        };
    }

    private double ResolveFontSizeValue(string raw, double parentSize)
    {
        if (LooksLikeExpression(raw))
        {
            // Evaluate with em/% resolved against the parent font size.
            var context = new EvaluationContext(this, parentSize, parentSize);
            ExprValue? value = TryEvaluate(raw, context);
            if (value is { IsLength: true } length)
            {
                return length.Value;
            }
        }

        return FoLength.TryParse(raw, parentSize, parentSize)?.Millipoints ?? parentSize;
    }

    /// <summary>Resolves a length property in millipoints.</summary>
    /// <param name="name">The property name.</param>
    /// <param name="default">The fallback length when unset or invalid.</param>
    /// <param name="percentBaseMpt">The base length for resolving percentages.</param>
    public FoLength GetLength(string name, FoLength @default, double percentBaseMpt = 0)
    {
        string? raw = GetRaw(name);

        // Expression path: only when the raw value looks like an arithmetic/function expression, so
        // plain values (12pt, 80%) keep the existing fast path and behaviour.
        if (LooksLikeExpression(raw))
        {
            FoLength? evaluated = TryEvaluateLength(raw!, percentBaseMpt);
            if (evaluated is not null)
            {
                return evaluated.Value;
            }
        }

        return FoLength.ParseOrDefault(raw, @default, FontSizeMpt, percentBaseMpt);
    }

    /// <summary>Resolves the <c>color</c> property, defaulting to black.</summary>
    public FopColor GetColor(string name = "color")
    {
        string? raw = GetRaw(name);
        if (raw is not null)
        {
            // Expression path (e.g. rgb(255,0,0) wrapped in arithmetic, or from-parent("color")).
            if (LooksLikeExpression(raw))
            {
                FopColor? evaluated = TryEvaluateColor(raw);
                if (evaluated is not null)
                {
                    return evaluated;
                }
            }

            FopColor? parsed = TryParseColorLenient(raw);
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

    /// <summary>The resolved <c>break-before</c> (not inherited; defaults to <see cref="BreakKind.Auto"/>).</summary>
    public BreakKind BreakBefore => FoEnumParsing.ParseBreak(GetRaw("break-before"));

    /// <summary>The resolved <c>break-after</c> (not inherited; defaults to <see cref="BreakKind.Auto"/>).</summary>
    public BreakKind BreakAfter => FoEnumParsing.ParseBreak(GetRaw("break-after"));

    /// <summary>
    /// The resolved <c>keep-together</c> within-page strength (not inherited; defaults to
    /// <see cref="KeepStrength.Auto"/>). The <c>keep-together.within-page</c> component takes
    /// precedence over the <c>keep-together</c> shorthand when both are set.
    /// </summary>
    public KeepStrength KeepTogetherWithinPage =>
        FoEnumParsing.ParseKeep(GetRaw("keep-together.within-page") ?? GetRaw("keep-together"));

    /// <summary>The resolved <c>text-align</c>.</summary>
    public TextAlign TextAlign => FoEnumParsing.ParseTextAlign(GetRaw("text-align"));

    /// <summary>
    /// The resolved set of active <c>text-decoration</c> lines. Although XSL-FO does not list
    /// text-decoration as an inheriting property, its visual effect propagates to descendant inline
    /// content, so this composes the ancestor chain: the parent's resolved decoration is the base over
    /// which this object's own <c>text-decoration</c> (including the <c>no-*</c> clears) is applied.
    /// </summary>
    public TextDecoration TextDecoration =>
        FoEnumParsing.ParseTextDecoration(
            own.TryGetValue("text-decoration", out var v) ? v : null,
            parent?.TextDecoration ?? TextDecoration.None);

    /// <summary>
    /// The resolved <c>letter-spacing</c> in millipoints: the extra space inserted after each glyph
    /// (inherited, default 0). The <c>normal</c> keyword and an unset value both resolve to 0. A length
    /// (including a negative one, which tightens spacing) is resolved against the current font size for
    /// <c>em</c> units.
    /// </summary>
    public double LetterSpacingMpt
    {
        get
        {
            string? raw = GetRaw("letter-spacing");
            if (raw is null || raw.Trim().Equals("normal", StringComparison.OrdinalIgnoreCase))
            {
                return 0;
            }

            return GetLength("letter-spacing", FoLength.Zero, FontSizeMpt).Millipoints;
        }
    }

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

    // ---- Hyphenation properties (all inherited) ----------------------------------------------

    /// <summary>
    /// The resolved <c>hyphenate</c> flag (inherited, default <c>false</c>). Any value other than the
    /// keyword <c>true</c> (case-insensitively) -- including the unset state and <c>false</c> -- is
    /// treated as off.
    /// </summary>
    public bool Hyphenate
    {
        get
        {
            string? raw = GetRaw("hyphenate");
            return raw is not null && raw.Trim().Equals("true", StringComparison.OrdinalIgnoreCase);
        }
    }

    /// <summary>
    /// The resolved <c>language</c> (inherited), trimmed and lower-cased, or <c>null</c> when unset or
    /// set to the <c>none</c>/<c>auto</c> keywords.
    /// </summary>
    public string? Language => NormalizeLangCountry(GetRaw("language"));

    /// <summary>
    /// The resolved <c>country</c> (inherited), trimmed and lower-cased, or <c>null</c> when unset or
    /// set to the <c>none</c>/<c>auto</c> keywords.
    /// </summary>
    public string? Country => NormalizeLangCountry(GetRaw("country"));

    private static string? NormalizeLangCountry(string? raw)
    {
        if (string.IsNullOrWhiteSpace(raw))
        {
            return null;
        }

        string v = raw.Trim();
        return v.Equals("none", StringComparison.OrdinalIgnoreCase)
            || v.Equals("auto", StringComparison.OrdinalIgnoreCase)
            ? null
            : v.ToLowerInvariant();
    }

    /// <summary>
    /// The resolved <c>hyphenation-character</c> (inherited), defaulting to the soft hyphen
    /// (<c>U+00AD</c>). The default soft hyphen is rendered as "-" by callers (see the layout engine).
    /// </summary>
    public string HyphenationCharacter => GetString("hyphenation-character", "\u00AD");

    /// <summary>The resolved <c>hyphenation-remain-character-count</c> (inherited, default 2, minimum 1).</summary>
    public int HyphenationRemainCharacterCount => GetPositiveInt("hyphenation-remain-character-count", 2);

    /// <summary>The resolved <c>hyphenation-push-character-count</c> (inherited, default 2, minimum 1).</summary>
    public int HyphenationPushCharacterCount => GetPositiveInt("hyphenation-push-character-count", 2);

    private int GetPositiveInt(string name, int @default)
    {
        string? raw = GetRaw(name);
        if (raw is not null
            && int.TryParse(raw.Trim(), System.Globalization.NumberStyles.Integer,
                System.Globalization.CultureInfo.InvariantCulture, out int value)
            && value >= 1)
        {
            return value;
        }

        return @default;
    }

    // ---- XSL-FO expression integration -------------------------------------------------------

    /// <summary>
    /// A heuristic gate: whether a raw value looks like an XSL-FO expression (an operator token or a
    /// function call) rather than a plain length/colour/keyword. Plain values keep their fast path so
    /// nothing regresses. The <c>div</c>/<c>mod</c> keyword operators and a <c>+</c>/<c>-</c> placed
    /// between operands count as operators; a leading sign or a unit's exponent <c>e</c> does not.
    /// </summary>
    internal static bool LooksLikeExpression(string? raw)
    {
        if (string.IsNullOrWhiteSpace(raw))
        {
            return false;
        }

        string text = raw.Trim();

        // A function call: name immediately followed by '('. (Covers rgb(), max(), from-parent(),
        // and the deferred proportional-column-width()/label-end()/body-start()/from-table-column().)
        if (ContainsFunctionCall(text))
        {
            return true;
        }

        // Multiplication and the keyword operators.
        if (text.Contains('*'))
        {
            return true;
        }

        if (ContainsKeywordOperator(text, "div") || ContainsKeywordOperator(text, "mod"))
        {
            return true;
        }

        // A '+' or '-' acting as a binary operator: it must sit between two operands (i.e. it is not
        // the first non-space character and is not part of a number's exponent or a leading sign).
        return ContainsBinaryAddSub(text);
    }

    private static bool ContainsFunctionCall(string text)
    {
        for (int i = 0; i < text.Length; i++)
        {
            if (text[i] != '(')
            {
                continue;
            }

            // Look back over whitespace to the preceding non-space char; a name char means a call.
            int j = i - 1;
            while (j >= 0 && char.IsWhiteSpace(text[j]))
            {
                j--;
            }

            if (j >= 0 && (char.IsLetterOrDigit(text[j]) || text[j] is '-' or '_'))
            {
                return true;
            }
        }

        return false;
    }

    private static bool ContainsKeywordOperator(string text, string op)
    {
        int from = 0;
        while (true)
        {
            int idx = text.IndexOf(op, from, StringComparison.Ordinal);
            if (idx < 0)
            {
                return false;
            }

            bool leftBoundary = idx == 0 || char.IsWhiteSpace(text[idx - 1]);
            int after = idx + op.Length;
            bool rightBoundary = after >= text.Length || char.IsWhiteSpace(text[after]) || text[after] == '(';
            if (leftBoundary && rightBoundary)
            {
                return true;
            }

            from = idx + 1;
        }
    }

    private static bool ContainsBinaryAddSub(string text)
    {
        for (int i = 1; i < text.Length; i++)
        {
            char c = text[i];
            if (c is not ('+' or '-'))
            {
                continue;
            }

            // Part of a number's exponent (e.g. 1.0e-3)?
            char prev = text[i - 1];
            if (prev is 'e' or 'E')
            {
                continue;
            }

            // The operator must be separated from its left operand by whitespace, mirroring the
            // XSL-FO requirement that '+'/'-' between operands be surrounded by spaces. This avoids
            // treating a hyphenated keyword (e.g. "even-page") or a signed unit as arithmetic.
            if (char.IsWhiteSpace(prev))
            {
                return true;
            }
        }

        return false;
    }

    private FoLength? TryEvaluateLength(string raw, double percentBaseMpt)
    {
        var context = new EvaluationContext(this, FontSizeMpt, percentBaseMpt == 0 ? null : percentBaseMpt);
        ExprValue? value = TryEvaluate(raw, context);
        return value switch
        {
            { IsLength: true } v => new FoLength(v.Value),
            { Kind: ExprValueKind.Numeric } v => new FoLength(v.Value * 1000.0), // unitless -> points
            _ => null,
        };
    }

    private FopColor? TryEvaluateColor(string raw)
    {
        var context = new EvaluationContext(this, FontSizeMpt, null);
        ExprValue? value = TryEvaluate(raw, context);
        return value is { Kind: ExprValueKind.Color } v ? v.Color : null;
    }

    /// <summary>Evaluates an expression, swallowing any malformed-expression error (lenient pipeline).</summary>
    private static ExprValue? TryEvaluate(string raw, IExpressionContext context)
    {
        try
        {
            return ExprEvaluator.Evaluate(raw, context);
        }
        catch (PropertyException)
        {
            // TODO: surface as an event once the FO event pipeline is wired in here.
            return null;
        }
    }

    private static FopColor? TryParseColorLenient(string raw)
    {
        try
        {
            return ColorUtil.ParseColorString(null, raw);
        }
        catch (Exception)
        {
            return null;
        }
    }

    /// <summary>
    /// The <see cref="IExpressionContext"/> the evaluator consults, backed by this property list's
    /// inheritance chain. Carries the current font size and an optional percentage base.
    /// </summary>
    private sealed class EvaluationContext(PropertyList owner, double fontSizeMpt, double? percentBaseMpt)
        : IExpressionContext
    {
        public double FontSizeMpt => fontSizeMpt;

        public double? PercentBaseMpt => percentBaseMpt;

        public PropertyReference? ResolvePropertyReference(string propertyName, PropertyReferenceKind kind)
        {
            // from-parent / inherited-property-value: the value as seen on the parent FO.
            // from-nearest-specified-value: the nearest ancestor that declared the property.
            PropertyList? source = kind == PropertyReferenceKind.FromNearestSpecified
                ? owner.parent?.FindNearestSpecified(propertyName)
                : owner.parent;

            if (source is null)
            {
                return null;
            }

            string? raw = source.GetRaw(propertyName);
            if (raw is null)
            {
                return null;
            }

            // Recurse against the source's own context (its font size; no percent base by default).
            var sourceContext = new EvaluationContext(source, source.FontSizeMpt, null);
            return new PropertyReference(raw, sourceContext);
        }
    }

    private PropertyList? FindNearestSpecified(string propertyName)
    {
        for (PropertyList? p = this; p is not null; p = p.parent)
        {
            if (p.own.ContainsKey(propertyName))
            {
                return p;
            }
        }

        return null;
    }
}
