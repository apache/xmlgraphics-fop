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

namespace Fop.Fonts;

/// <summary>
/// The FontInfo holds font information for the layout and rendering of a fo document. This stores
/// the list of available fonts that are set up by the renderer. The font name can be retrieved for
/// the family, style and weight.
/// <para>
/// Port of <c>org.apache.fop.fonts.FontInfo</c>. The triplet/font registry, the fuzzy
/// font-lookup/fallback algorithm, the weight-adjustment search and the per-(triplet,size) font
/// instance cache are ported faithfully.
/// </para>
/// <para>
/// Deferred collaborators (clearly marked <c>TODO</c>):
/// <list type="bullet">
/// <item><description>
/// The Java <c>FontEventListener</c> is not ported in this slice; font-substitution and
/// SVG-stroking notifications are no-ops here. The fields/methods are retained so callers compile.
/// </description></item>
/// <item><description>
/// <c>getFontInstanceForAWTFont(java.awt.Font)</c> depended on <c>java.awt.Font</c> and is omitted;
/// the <c>getTripletsForName</c> helper that supported it is ported because it is self-contained and
/// useful.</description></item>
/// <item><description>
/// the registry stores <see cref="IFontMetrics"/> (the Java map was <c>Map&lt;String, Typeface&gt;</c>,
/// but all callers only need the metrics surface; <c>Typeface.setEventListener</c> wiring is deferred).
/// </description></item>
/// </list>
/// </para>
/// </summary>
public class FontInfo
{
    /// <summary>Map containing fonts that have been used (key = font key).</summary>
    private readonly Dictionary<string, IFontMetrics> usedFonts;

    /// <summary>Look up a font-triplet to find a font-name (value = font key).</summary>
    private readonly Dictionary<FontTriplet, string> triplets;

    /// <summary>
    /// Look up a font-triplet to find its priority (only used inside
    /// <see cref="AddFontProperties(string, FontTriplet)"/>).
    /// </summary>
    private Dictionary<FontTriplet, int>? tripletPriorities;

    /// <summary>Look up a font-name to get a font (that implements <see cref="IFontMetrics"/>).</summary>
    private readonly Dictionary<string, IFontMetrics> fonts;

    /// <summary>Cache for <see cref="Font"/> instances.</summary>
    private Dictionary<FontTriplet, Dictionary<int, Font>>? fontInstanceCache;

    // TODO: the Java FontEventListener is not ported in this slice. The field is retained so the
    // notify* methods compile; once events are wired in, fire fontSubstituted/svgTextStrokedAsShapes.
    private object? eventListener;

    /// <summary>
    /// Main constructor.
    /// </summary>
    public FontInfo()
    {
        this.triplets = [];
        this.tripletPriorities = [];
        this.fonts = [];
        this.usedFonts = [];
    }

    /// <summary>
    /// Sets the font event listener that can be used to receive events about particular events in
    /// this class.
    /// </summary>
    /// <param name="listener">the font event listener.</param>
    public void SetEventListener(object? listener) => this.eventListener = listener;

    /// <summary>
    /// Checks if the font setup is valid (at least the ultimate fallback font must be registered).
    /// </summary>
    /// <returns><c>true</c> if valid.</returns>
    public bool IsSetupValid()
    {
        // We're only called when font setup is done.
        tripletPriorities = null; // candidate for garbage collection
        return triplets.ContainsKey(Font.DefaultFont);
    }

    /// <summary>Adds a new font triplet.</summary>
    /// <param name="name">internal key.</param>
    /// <param name="family">font family name.</param>
    /// <param name="style">font style (normal, italic, oblique...).</param>
    /// <param name="weight">font weight.</param>
    public void AddFontProperties(string name, string family, string style, int weight) =>
        AddFontProperties(name, CreateFontKey(family, style, weight));

    /// <summary>Adds a series of new font triplets given an array of font family names.</summary>
    /// <param name="name">internal key.</param>
    /// <param name="families">an array of font family names.</param>
    /// <param name="style">font style (normal, italic, oblique...).</param>
    /// <param name="weight">font weight.</param>
    public void AddFontProperties(string name, string[] families, string style, int weight)
    {
        foreach (string family in families)
        {
            AddFontProperties(name, family, style, weight);
        }
    }

    /// <summary>Adds a new font triplet.</summary>
    /// <param name="internalFontKey">internal font key.</param>
    /// <param name="triplet">the font triplet to associate with the internal key.</param>
    public void AddFontProperties(string internalFontKey, FontTriplet triplet)
    {
        // Add the given family, style and weight as a lookup for the font with the given name.
        int newPriority = triplet.Priority;
        if (triplets.TryGetValue(triplet, out string? oldName))
        {
            // tripletPriorities is non-null whenever a triplet is already registered (it is only
            // nulled out by IsSetupValid, which signals the end of setup).
            int oldPriority = tripletPriorities![triplet];
            if (oldPriority < newPriority)
            {
                // The existing (lower-priority-number) mapping wins; the new one is not registered.
                _ = oldName;
                return;
            }

            // Otherwise the new mapping replaces the existing one (logged in Java).
        }

        this.triplets[triplet] = internalFontKey;
        this.tripletPriorities![triplet] = newPriority;
    }

    /// <summary>Adds font metrics for a specific font.</summary>
    /// <param name="internalFontKey">internal key.</param>
    /// <param name="metrics">metrics to register.</param>
    public void AddMetrics(string internalFontKey, IFontMetrics metrics)
    {
        // TODO: the Java original called ((Typeface)metrics).setEventListener(eventListener) when
        // the metrics were a Typeface. setEventListener is not ported in this slice.
        this.fonts[internalFontKey] = metrics;
    }

    /// <summary>
    /// Looks up a font, locating the font name for a given family, style and weight. This also adds
    /// the font to the list of used fonts.
    /// </summary>
    /// <param name="family">font family.</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <param name="substitutable">
    /// <c>true</c> if the font may be substituted with the default font if not found.</param>
    /// <returns>internal font triplet key, or <c>null</c>.</returns>
    private FontTriplet? FontLookup(string family, string style, int weight, bool substitutable)
    {
        FontTriplet startKey = CreateFontKey(family, style, weight);
        FontTriplet? fontTriplet = startKey;

        // first try given parameters
        string? internalFontKey = GetInternalFontKey(fontTriplet);
        if (internalFontKey is null)
        {
            fontTriplet = FuzzyFontLookup(family, style, weight, startKey, substitutable);
        }

        if (fontTriplet is not null)
        {
            if (!ReferenceEquals(fontTriplet, startKey))
            {
                NotifyFontReplacement(startKey, fontTriplet);
            }

            return fontTriplet;
        }

        return null;
    }

    private FontTriplet? FuzzyFontLookup(
        string family, string style, int weight, FontTriplet startKey, bool substitutable)
    {
        FontTriplet? key = null;
        string? internalFontKey = null;
        if (family != startKey.Name)
        {
            key = CreateFontKey(family, style, weight);
            internalFontKey = GetInternalFontKey(key);
            if (internalFontKey is not null)
            {
                return key;
            }
        }

        // adjust weight, favouring normal or bold
        key = FindAdjustWeight(family, style, weight);
        if (key is not null)
        {
            internalFontKey = GetInternalFontKey(key);
        }

        // return null if not found and not substitutable
        if (!substitutable && internalFontKey is null)
        {
            return null;
        }

        // only if the font may be substituted
        // fallback 1: try the same font-family and weight with default style
        if (internalFontKey is null && style != Font.StyleNormal)
        {
            key = CreateFontKey(family, Font.StyleNormal, weight);
            internalFontKey = GetInternalFontKey(key);
        }

        // fallback 2: try the same font-family with default style and try to adjust weight
        if (internalFontKey is null && style != Font.StyleNormal)
        {
            key = FindAdjustWeight(family, Font.StyleNormal, weight);
            if (key is not null)
            {
                internalFontKey = GetInternalFontKey(key);
            }
        }

        // fallback 3: try any family with original style/weight
        if (internalFontKey is null)
        {
            return FuzzyFontLookup("any", style, weight, startKey, false);
        }

        // last resort: use default
        if (key is null && internalFontKey is null)
        {
            key = Font.DefaultFont;
            internalFontKey = GetInternalFontKey(key);
        }

        return internalFontKey is not null ? key : null;
    }

    /// <summary>Tells this class that the font with the given internal name has been used.</summary>
    /// <param name="internalName">the internal font name (F1, F2 etc.).</param>
    public void UseFont(string internalName)
    {
        if (fonts.TryGetValue(internalName, out IFontMetrics? metrics))
        {
            usedFonts[internalName] = metrics;
        }
    }

    private Dictionary<FontTriplet, Dictionary<int, Font>> GetFontInstanceCache() =>
        fontInstanceCache ??= [];

    /// <summary>
    /// Retrieves a (possibly cached) <see cref="Font"/> instance based on a
    /// <see cref="FontTriplet"/> and a font size.
    /// </summary>
    /// <param name="triplet">the font triplet designating the requested font.</param>
    /// <param name="fontSize">the font size.</param>
    /// <returns>the requested <see cref="Font"/> instance.</returns>
    public Font GetFontInstance(FontTriplet triplet, int fontSize)
    {
        Dictionary<FontTriplet, Dictionary<int, Font>> cache = GetFontInstanceCache();
        if (!cache.TryGetValue(triplet, out Dictionary<int, Font>? sizes))
        {
            sizes = [];
            cache[triplet] = sizes;
        }

        if (!sizes.TryGetValue(fontSize, out Font? font))
        {
            string? fontKey = GetInternalFontKey(triplet);

            // fontKey may be null if the triplet was never registered; mirror Java, which would
            // throw an NRE in useFont/getMetricsFor in that case.
            UseFont(fontKey!);
            IFontMetrics metrics = GetMetricsFor(fontKey!);
            font = new Font(fontKey!, triplet, metrics, fontSize);
            sizes[fontSize] = font;
        }

        return font;
    }

    private List<FontTriplet> GetTripletsForName(string fontName)
    {
        List<FontTriplet> matchedTriplets = [];
        foreach (FontTriplet triplet in triplets.Keys)
        {
            string? tripletName = triplet.Name;
            if (tripletName is not null
                && string.Equals(tripletName, fontName, StringComparison.OrdinalIgnoreCase))
            {
                matchedTriplets.Add(triplet);
            }
        }

        return matchedTriplets;
    }

    /// <summary>
    /// Looks up a font, locating the font name for a given family, style and weight. This also adds
    /// the font to the list of used fonts.
    /// </summary>
    /// <param name="family">font family.</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <returns>the font triplet of the font chosen.</returns>
    public FontTriplet? FontLookup(string family, string style, int weight) =>
        FontLookup(family, style, weight, true);

    private List<FontTriplet> FontLookup(string[] families, string style, int weight, bool substitutable)
    {
        List<FontTriplet> matchingTriplets = [];
        foreach (string family in families)
        {
            FontTriplet? triplet = FontLookup(family, style, weight, substitutable);
            if (triplet is not null)
            {
                matchingTriplets.Add(triplet);
            }
        }

        return matchingTriplets;
    }

    /// <summary>
    /// Looks up a set of fonts, locating the font name(s) for the given families, style and weight.
    /// This also adds the fonts to the list of used fonts.
    /// </summary>
    /// <param name="families">font families (priority list).</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <returns>the set of font triplets of all supported and chosen font-families in the specified
    /// style and weight.</returns>
    public FontTriplet[] FontLookup(string[] families, string style, int weight)
    {
        if (families.Length == 0)
        {
            throw new ArgumentException("Specify at least one font family");
        }

        // try matching without substitutions
        List<FontTriplet> matchedTriplets = FontLookup(families, style, weight, false);

        // if there are no matching font triplets found try with substitutions
        if (matchedTriplets.Count == 0)
        {
            matchedTriplets = FontLookup(families, style, weight, true);
        }

        // no matching font triplets found!
        if (matchedTriplets.Count == 0)
        {
            throw new InvalidOperationException(
                "fontLookup must return an array with at least one FontTriplet on the last call. "
                + "Lookup: " + string.Join(", ", families));
        }

        // found some matching fonts so return them
        return [.. matchedTriplets];
    }

    private void NotifyFontReplacement(FontTriplet replacedKey, FontTriplet newKey)
    {
        // TODO: when FontEventListener is ported, fire eventListener.fontSubstituted(this,
        // replacedKey, newKey) here.
        _ = replacedKey;
        _ = newKey;
    }

    /// <summary>
    /// Notify listeners that the SVG text for the given font will be stroked as shapes.
    /// </summary>
    /// <param name="fontFamily">a SVG font family.</param>
    public void NotifyStrokingSVGTextAsShapes(string fontFamily)
    {
        // TODO: when FontEventListener is ported, fire
        // eventListener.svgTextStrokedAsShapes(this, fontFamily) here.
        _ = fontFamily;
    }

    /// <summary>
    /// Find a font with a given family and style by trying different font weights according to the
    /// spec.
    /// </summary>
    /// <param name="family">font family.</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <returns>internal key, or <c>null</c>.</returns>
    public FontTriplet? FindAdjustWeight(string family, string style, int weight)
    {
        FontTriplet? key = null;
        string? f = null;
        int newWeight = weight;
        if (newWeight < 400)
        {
            while (f is null && newWeight > 100)
            {
                newWeight -= 100;
                key = CreateFontKey(family, style, newWeight);
                f = GetInternalFontKey(key);
            }

            newWeight = weight;
            while (f is null && newWeight < 400)
            {
                newWeight += 100;
                key = CreateFontKey(family, style, newWeight);
                f = GetInternalFontKey(key);
            }
        }
        else if (newWeight is 400 or 500)
        {
            key = CreateFontKey(family, style, 400);
            f = GetInternalFontKey(key);
        }
        else if (newWeight > 500)
        {
            while (f is null && newWeight < 1000)
            {
                newWeight += 100;
                key = CreateFontKey(family, style, newWeight);
                f = GetInternalFontKey(key);
            }

            newWeight = weight;
            while (f is null && newWeight > 400)
            {
                newWeight -= 100;
                key = CreateFontKey(family, style, newWeight);
                f = GetInternalFontKey(key);
            }
        }

        if (f is null && weight != 400)
        {
            key = CreateFontKey(family, style, 400);
            f = GetInternalFontKey(key);
        }

        return f is not null ? key : null;
    }

    /// <summary>Determines if a particular font is available.</summary>
    /// <param name="family">font family.</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <returns><c>true</c> if available.</returns>
    public bool HasFont(string family, string style, int weight) =>
        this.triplets.ContainsKey(CreateFontKey(family, style, weight));

    /// <summary>
    /// Returns the internal font key (F1, F2, F3 etc.) for a given triplet.
    /// </summary>
    /// <param name="triplet">the font triplet.</param>
    /// <returns>the associated internal key or <c>null</c>, if not found.</returns>
    public string? GetInternalFontKey(FontTriplet triplet) =>
        triplets.GetValueOrDefault(triplet);

    /// <summary>Creates a key from the given strings.</summary>
    /// <param name="family">font family.</param>
    /// <param name="style">font style.</param>
    /// <param name="weight">font weight.</param>
    /// <returns>internal key.</returns>
    public static FontTriplet CreateFontKey(string family, string style, int weight) =>
        new(family, style, weight);

    /// <summary>Gets a read-only map of all registered fonts (font key/metrics pairs).</summary>
    public IReadOnlyDictionary<string, IFontMetrics> GetFonts() => this.fonts;

    /// <summary>Gets a map of all registered font triplets (triplet/font key pairs).</summary>
    public IDictionary<FontTriplet, string> GetFontTriplets() => this.triplets;

    /// <summary>
    /// Used by the renderers to retrieve all the fonts used in the document (for embedded fonts or
    /// creating a list of used fonts).
    /// </summary>
    public IReadOnlyDictionary<string, IFontMetrics> GetUsedFonts() => this.usedFonts;

    /// <summary>Returns the metrics for a particular font.</summary>
    /// <param name="fontName">internal key.</param>
    /// <returns>font metrics.</returns>
    public IFontMetrics GetMetricsFor(string fontName)
    {
        IFontMetrics metrics = fonts[fontName];
        usedFonts[fontName] = metrics;
        return metrics;
    }

    /// <summary>Returns all font triplets matching the given font name.</summary>
    /// <param name="fontName">the internal font key we are looking for.</param>
    /// <returns>a list of matching font triplets.</returns>
    public List<FontTriplet> GetTripletsFor(string fontName)
    {
        List<FontTriplet> foundTriplets = [];
        foreach (KeyValuePair<FontTriplet, string> tripletEntry in triplets)
        {
            if (fontName == tripletEntry.Value)
            {
                foundTriplets.Add(tripletEntry.Key);
            }
        }

        return foundTriplets;
    }

    /// <summary>
    /// Returns the first triplet matching the given font name. As there may be multiple triplets
    /// matching the font name the result set is sorted first to guarantee consistent results.
    /// </summary>
    /// <param name="fontName">the internal font key we are looking for.</param>
    /// <returns>the first triplet for the given font name, or <c>null</c>.</returns>
    public FontTriplet? GetTripletFor(string fontName)
    {
        List<FontTriplet> foundTriplets = GetTripletsFor(fontName);
        if (foundTriplets.Count > 0)
        {
            foundTriplets.Sort();
            return foundTriplets[0];
        }

        return null;
    }

    /// <summary>
    /// Returns the font style for a particular font. There may be multiple font styles matching
    /// this font; only the first found is returned (searching is done on a sorted list).
    /// </summary>
    /// <param name="fontName">internal key.</param>
    /// <returns>font style.</returns>
    public string GetFontStyleFor(string fontName) =>
        GetTripletFor(fontName)?.Style ?? string.Empty;

    /// <summary>
    /// Returns the font weight for a particular font. There may be multiple font weights matching
    /// this font; only the first found is returned (searching is done on a sorted list).
    /// </summary>
    /// <param name="fontName">internal key.</param>
    /// <returns>font weight.</returns>
    public int GetFontWeightFor(string fontName) =>
        GetTripletFor(fontName)?.Weight ?? 0;
}
