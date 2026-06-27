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

using Fop.Fonts.Substitution;
using Fop.Util;

namespace Fop.Fonts;

/// <summary>
/// Holds font state information and provides access to the font metrics.
/// <para>
/// Port of <c>org.apache.fop.fonts.Font</c>. The weight/style/priority constants and the
/// <c>DEFAULT_FONT</c> triplet are reused from <see cref="FontConstants"/> /
/// <see cref="FontTriplet.DefaultFontTriplet"/> rather than being redeclared.
/// </para>
/// <para>
/// Behaviour preserved from Java: width/ascent/etc. divide the metric value (in millipoints) by
/// 1000; <see cref="GetKernValue"/> isolates surrogate-pair halves and scales by font size;
/// <see cref="GetCharWidth(char)"/> reproduces the per-character space-width guessing table.
/// </para>
/// <para>
/// Deferred dependencies (clearly marked <c>TODO</c> below): the Java code special-cased
/// <c>CIDFont</c>, <c>LazyFont</c> and <c>render.java2d.CustomFontMetricsMapper</c> when mapping or
/// testing code points and when unwrapping the "real" metrics. None of those types are ported in
/// this slice, so <see cref="MapCodePoint"/>/<see cref="HasCodePoint"/> handle only the BMP fast
/// path and <see cref="GetRealFontMetrics"/> returns the metric unchanged. The complex-scripts
/// delegation uses the <see cref="ISubstitutable"/>/<see cref="IPositionable"/> markers.
/// </para>
/// </summary>
public class Font : ISubstitutable, IPositionable
{
    /// <summary>Extra Bold font weight.</summary>
    public const int WeightExtraBold = FontConstants.WeightExtraBold;

    /// <summary>Bold font weight.</summary>
    public const int WeightBold = FontConstants.WeightBold;

    /// <summary>Normal font weight.</summary>
    public const int WeightNormal = FontConstants.WeightNormal;

    /// <summary>Light font weight.</summary>
    public const int WeightLight = FontConstants.WeightLight;

    /// <summary>Normal font style.</summary>
    public const string StyleNormal = FontConstants.StyleNormal;

    /// <summary>Italic font style.</summary>
    public const string StyleItalic = FontConstants.StyleItalic;

    /// <summary>Oblique font style.</summary>
    public const string StyleOblique = FontConstants.StyleOblique;

    /// <summary>Inclined font style.</summary>
    public const string StyleInclined = FontConstants.StyleInclined;

    /// <summary>Default selection priority.</summary>
    public const int PriorityDefault = FontConstants.PriorityDefault;

    /// <summary>Default fallback key ("any", normal, normal weight, default priority).</summary>
    public static readonly FontTriplet DefaultFont =
        new("any", StyleNormal, WeightNormal, PriorityDefault);

    private readonly string fontName;
    private readonly FontTriplet? triplet;
    private readonly int fontSize;
    private readonly IFontMetrics metric;

    /// <summary>
    /// Main constructor.
    /// </summary>
    /// <param name="key">key of the font.</param>
    /// <param name="triplet">the font triplet that was used to look up this font (may be null).</param>
    /// <param name="met">font metrics.</param>
    /// <param name="fontSize">font size.</param>
    public Font(string key, FontTriplet? triplet, IFontMetrics met, int fontSize)
    {
        this.fontName = key;
        this.triplet = triplet;
        this.metric = met;
        this.fontSize = fontSize;
    }

    /// <summary>Gets the associated font metrics object.</summary>
    public IFontMetrics FontMetrics => metric;

    /// <summary>Determines whether the font is a multibyte font.</summary>
    public bool IsMultiByte => FontMetrics.IsMultiByte;

    /// <summary>Gets the font's ascender (em-relative, in points).</summary>
    public int Ascender => metric.GetAscender(fontSize) / 1000;

    /// <summary>Gets the font's CapHeight (in points).</summary>
    public int CapHeight => metric.GetCapHeight(fontSize) / 1000;

    /// <summary>Gets the font's Descender (in points).</summary>
    public int Descender => metric.GetDescender(fontSize) / 1000;

    /// <summary>Gets the font's name.</summary>
    public string FontName => fontName;

    /// <summary>Gets the font triplet that selected this font.</summary>
    public FontTriplet? FontTriplet => triplet;

    /// <summary>Gets the font size.</summary>
    public int FontSize => fontSize;

    /// <summary>Gets the XHeight (in points).</summary>
    public int XHeight => metric.GetXHeight(fontSize) / 1000;

    /// <summary>Gets a value indicating whether the font has kerning info.</summary>
    public bool HasKerning => metric.HasKerningInfo;

    /// <summary>Determines whether the font has a feature (i.e. at least one lookup matches).</summary>
    public bool HasFeature(int tableType, string script, string language, string feature) =>
        metric.HasFeature(tableType, script, language, feature);

    /// <summary>
    /// Returns the font's kerning table (an empty map if the font has no kerning info).
    /// </summary>
    /// <returns>the kerning table.</returns>
    public IReadOnlyDictionary<int, IReadOnlyDictionary<int, int>> GetKerning() =>
        metric.HasKerningInfo
            ? metric.GetKerningInfo()
            : new Dictionary<int, IReadOnlyDictionary<int, int>>();

    /// <summary>
    /// Returns the amount of kerning between two characters. The value returned measures in pt, so
    /// it is already adjusted for font size.
    /// </summary>
    /// <param name="ch1">first character.</param>
    /// <param name="ch2">second character.</param>
    /// <returns>the distance to adjust for kerning, 0 if there's no kerning.</returns>
    public int GetKernValue(int ch1, int ch2)
    {
        // Isolate surrogate pair (note: the Java bounds are inclusive of 0xE000, preserved here).
        if (ch1 is >= 0xD800 and <= 0xE000)
        {
            return 0;
        }
        else if (ch2 is >= 0xD800 and <= 0xE000)
        {
            return 0;
        }

        if (GetKerning().TryGetValue(ch1, out IReadOnlyDictionary<int, int>? kernPair)
            && kernPair.TryGetValue(ch2, out int width))
        {
            return width * FontSize / 1000;
        }

        return 0;
    }

    /// <summary>Returns the width of a character.</summary>
    /// <param name="charnum">character to look up.</param>
    /// <returns>width of the character (in millipoints).</returns>
    public int GetWidth(int charnum) => metric.GetWidth(charnum, fontSize) / 1000;

    /// <summary>
    /// Maps a Java character (Unicode) to a font character. Default uses
    /// <see cref="CodePointMapping"/>.
    /// </summary>
    /// <param name="c">character to map.</param>
    /// <returns>the mapped character.</returns>
    public char MapChar(char c)
    {
        if (metric is Typeface typeface)
        {
            return typeface.MapChar(c);
        }

        // Use default CodePointMapping
        char d = CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING).MapChar(c);
        if (d != SingleByteEncoding.NotFoundCodePoint)
        {
            c = d;
        }
        else
        {
            // TODO: when FontEventListener/logging is wired in, log
            // "Glyph <code> not available in font <fontName>" here.
            c = Typeface.NotFound;
        }

        return c;
    }

    /// <summary>
    /// Maps a Unicode code point to a font character. Default uses <see cref="CodePointMapping"/>.
    /// </summary>
    /// <param name="cp">code point to map.</param>
    /// <returns>the mapped character.</returns>
    public int MapCodePoint(int cp)
    {
        // TODO: the Java original unwrapped LazyFont/CustomFontMetricsMapper here and delegated to
        // CIDFont.mapCodePoint for CID-keyed fonts. Those types are not ported in this slice, so we
        // only handle the BMP fast path.
        if (CharUtilities.IsBmpCodePoint(cp))
        {
            return MapChar((char)cp);
        }

        return Typeface.NotFound;
    }

    /// <summary>Determines whether this font contains a particular character/glyph.</summary>
    /// <param name="c">character to check.</param>
    /// <returns><c>true</c> if the character is supported, <c>false</c> otherwise.</returns>
    public bool HasChar(char c)
    {
        if (metric is Typeface typeface)
        {
            return typeface.HasChar(c);
        }

        // Use default CodePointMapping
        return CodePointMapping.GetMapping(CodePointMapping.WIN_ANSI_ENCODING).MapChar(c) > 0;
    }

    /// <summary>Determines whether this font contains a particular code point/glyph.</summary>
    /// <param name="cp">code point to check.</param>
    /// <returns><c>true</c> if the code point is supported, <c>false</c> otherwise.</returns>
    public bool HasCodePoint(int cp)
    {
        // TODO: as with MapCodePoint, the Java original delegated to CIDFont.hasCodePoint after
        // unwrapping LazyFont/CustomFontMetricsMapper. Not ported in this slice; BMP fast path only.
        if (CharUtilities.IsBmpCodePoint(cp))
        {
            return HasChar((char)cp);
        }

        return false;
    }

    /// <summary>
    /// Gets the real underlying font if it is wrapped inside some container.
    /// </summary>
    /// <returns>the underlying metrics instance.</returns>
    private IFontMetrics GetRealFontMetrics()
    {
        // TODO: the Java original unwrapped CustomFontMetricsMapper (render.java2d) and LazyFont
        // here. Neither is ported in this slice, so the metric is returned unchanged.
        return metric;
    }

    /// <inheritdoc/>
    public override string ToString() => $"{base.ToString()}{{{fontName},{fontSize}}}";

    /// <summary>
    /// Helper method for getting the width of a Unicode char from the current font state. This also
    /// performs some guessing on widths on various versions of space that might not exist in the
    /// font.
    /// </summary>
    /// <param name="c">character to inspect.</param>
    /// <returns>the width of the character or -1 if no width available.</returns>
    public int GetCharWidth(char c)
    {
        int width;

        // '\u00A0' is a non-breaking space, treated like a normal space here (matches Java).
        if (c is '\n' or '\r' or '\t' or '\u00A0')
        {
            width = GetCharWidth(' ');
        }
        else
        {
            if (HasChar(c))
            {
                int mappedChar = MapChar(c);
                width = GetWidth(mappedChar);
            }
            else
            {
                width = -1;
            }

            if (width <= 0)
            {
                // Estimate the width of spaces not represented in the font. The code points below
                // are the various Unicode space/format characters handled specially in Java.
                int em = FontSize; // http://en.wikipedia.org/wiki/Em_(typography)
                int en = em / 2; // http://en.wikipedia.org/wiki/En_(typography)

                width = c switch
                {
                    ' ' => em,
                    '\u2000' => en, // EN QUAD
                    '\u2001' => em, // EM QUAD
                    '\u2002' => em / 2, // EN SPACE
                    '\u2003' => FontSize, // EM SPACE
                    '\u2004' => em / 3, // THREE-PER-EM SPACE
                    '\u2005' => em / 4, // FOUR-PER-EM SPACE
                    '\u2006' => em / 6, // SIX-PER-EM SPACE
                    '\u2007' => GetCharWidth('0'), // FIGURE SPACE
                    '\u2008' => GetCharWidth('.'), // PUNCTUATION SPACE
                    '\u2009' => em / 5, // THIN SPACE
                    '\u200A' => em / 10, // HAIR SPACE
                    '\u200B' => 0, // ZERO WIDTH SPACE
                    '\u202F' => GetCharWidth(' ') / 2, // NARROW NO-BREAK SPACE
                    '\u2060' => 0, // WORD JOINER
                    '\u3000' => GetCharWidth(' ') * 2, // IDEOGRAPHIC SPACE
                    '\uFEFF' => 0, // ZERO WIDTH NO-BREAK SPACE

                    // Will be internally replaced by "#" if not found.
                    _ => GetWidth(MapChar(c)),
                };
            }
        }

        return width;
    }

    /// <summary>
    /// Helper method for getting the width of a Unicode code point from the current font state.
    /// </summary>
    /// <param name="c">code point to inspect.</param>
    /// <returns>the width of the character or -1 if no width available.</returns>
    public int GetCharWidth(int c)
    {
        if (c < 0x10000)
        {
            return GetCharWidth((char)c);
        }

        if (HasCodePoint(c))
        {
            int mappedChar = MapCodePoint(c);
            return GetWidth(mappedChar);
        }

        return -1;
    }

    /// <summary>Calculates the word width.</summary>
    /// <param name="word">text to get width for.</param>
    /// <returns>the width of the text.</returns>
    public int GetWordWidth(string? word)
    {
        if (word is null)
        {
            return 0;
        }

        int width = 0;
        foreach (char c in word)
        {
            width += GetCharWidth(c);
        }

        return width;
    }

    /// <inheritdoc/>
    public bool PerformsSubstitution() =>
        metric is ISubstitutable s && s.PerformsSubstitution();

    /// <inheritdoc/>
    public string PerformSubstitution(
        string cs,
        string script,
        string language,
        IList<object>? associations,
        bool retainControls)
    {
        if (metric is ISubstitutable s)
        {
            return s.PerformSubstitution(cs, script, language, associations, retainControls);
        }

        throw new NotSupportedException();
    }

    /// <inheritdoc/>
    public string ReorderCombiningMarks(
        string cs,
        int[][]? gpa,
        string script,
        string language,
        IList<object>? associations)
    {
        if (metric is ISubstitutable s)
        {
            return s.ReorderCombiningMarks(cs, gpa, script, language, associations);
        }

        throw new NotSupportedException();
    }

    /// <inheritdoc/>
    public bool PerformsPositioning() =>
        metric is IPositionable p && p.PerformsPositioning();

    /// <inheritdoc/>
    public int[][]? PerformPositioning(string cs, string script, string language, int fontSize)
    {
        if (metric is IPositionable p)
        {
            return p.PerformPositioning(cs, script, language, fontSize);
        }

        throw new NotSupportedException();
    }

    /// <summary>
    /// Perform glyph positioning using this font's own size.
    /// </summary>
    /// <param name="cs">character sequence to map to position offsets.</param>
    /// <param name="script">a script identifier.</param>
    /// <param name="language">a language identifier.</param>
    /// <returns>array of 4-tuples of placement and advance adjustments, or <c>null</c>.</returns>
    public int[][]? PerformPositioning(string cs, string script, string language) =>
        PerformPositioning(cs, script, language, fontSize);
}
