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

using System.Reflection;
using System.Xml;

namespace Fop.Hyphenation;

/// <summary>
/// The main entry point to the hyphenation package: resolves and caches a
/// <see cref="HyphenationTree"/> for a language (and optional country) and hyphenates words.
/// <para>
/// Modern reformulation of <c>org.apache.fop.hyphenation.Hyphenator</c>. Where FOP loads precompiled
/// <c>.hyp</c> serialized trees or user XML through a resource resolver, this port loads the XML
/// pattern files bundled as embedded resources (<c>Patterns/{lang}.xml</c>, in the FOP
/// <c>hyphenation.dtd</c> format). Additional languages are added simply by dropping another embedded
/// <c>Patterns/{lang}.xml</c> in. An unknown language resolves to <c>null</c> (no hyphenation), so the
/// caller degrades gracefully.
/// </para>
/// </summary>
public sealed class Hyphenator
{
    private const string ResourcePrefix = "Fop.Hyphenation.Patterns.";

    /// <summary>A shared default instance backed by the bundled embedded patterns.</summary>
    public static Hyphenator Default { get; } = new();

    private readonly Lock gate = new();

    // A null cached value records a language whose patterns are absent, so we don't retry the lookup.
    private readonly Dictionary<string, HyphenationTree?> cache = new(StringComparer.Ordinal);

    /// <summary>
    /// Returns the cached hyphenation tree for <paramref name="lang"/> (and optional
    /// <paramref name="country"/>), loading it from the embedded patterns on first use, or <c>null</c>
    /// when no patterns are bundled for the language. Lookup tries the full <c>lang_country</c> key
    /// first, then falls back to the bare language.
    /// </summary>
    /// <param name="lang">the language code (e.g. "en"), case-insensitive.</param>
    /// <param name="country">an optional country code (e.g. "US"), or <c>null</c>/"none".</param>
    public HyphenationTree? GetHyphenationTree(string? lang, string? country = null)
    {
        if (string.IsNullOrWhiteSpace(lang))
        {
            return null;
        }

        string language = lang.Trim().ToLowerInvariant();
        string? ctry = string.IsNullOrWhiteSpace(country) || country.Equals("none", StringComparison.OrdinalIgnoreCase)
            ? null
            : country.Trim().ToLowerInvariant();
        string llccKey = ctry is null ? language : language + "_" + ctry;

        lock (gate)
        {
            if (cache.TryGetValue(llccKey, out HyphenationTree? cached))
            {
                return cached;
            }

            HyphenationTree? tree = LoadFromResource(llccKey);

            // Fall back to the bare language when a country-specific file is absent.
            if (tree is null && ctry is not null)
            {
                tree = GetHyphenationTree(language, null);
            }

            cache[llccKey] = tree;
            return tree;
        }
    }

    /// <summary>
    /// Hyphenates <paramref name="word"/> for the given language, or returns <c>null</c> when the
    /// language has no patterns or the word cannot be hyphenated under the given minimum counts.
    /// </summary>
    /// <param name="lang">the language code.</param>
    /// <param name="country">an optional country code.</param>
    /// <param name="word">the word to hyphenate.</param>
    /// <param name="remainCharCount">minimum characters before a hyphenation point.</param>
    /// <param name="pushCharCount">minimum characters after a hyphenation point.</param>
    public Hyphenation? Hyphenate(string? lang, string? country, string word, int remainCharCount,
        int pushCharCount)
    {
        HyphenationTree? tree = GetHyphenationTree(lang, country);
        return tree?.Hyphenate(word, remainCharCount, pushCharCount);
    }

    /// <summary>Hyphenates <paramref name="word"/> for the given language (no country).</summary>
    public Hyphenation? Hyphenate(string? lang, string word, int remainCharCount, int pushCharCount) =>
        Hyphenate(lang, null, word, remainCharCount, pushCharCount);

    private static HyphenationTree? LoadFromResource(string key)
    {
        Assembly asm = typeof(Hyphenator).Assembly;
        string resourceName = ResourcePrefix + key + ".xml";
        using Stream? stream = asm.GetManifestResourceStream(resourceName);
        if (stream is null)
        {
            return null;
        }

        var tree = new HyphenationTree();
        var settings = new XmlReaderSettings
        {
            DtdProcessing = DtdProcessing.Ignore,
            IgnoreComments = true,
            IgnoreProcessingInstructions = true,
        };
        using XmlReader reader = XmlReader.Create(stream, settings);
        tree.LoadPatterns(reader);
        return tree;
    }
}
