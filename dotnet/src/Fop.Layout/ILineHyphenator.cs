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

using Fop.Hyphenation;

namespace Fop.Layout;

/// <summary>
/// The seam the <see cref="LayoutEngine"/> uses to obtain hyphenation points for a word. The real
/// implementation is <see cref="DefaultLineHyphenator"/> (backed by the bundled patterns); tests can
/// inject a deterministic stub. Keeping it an interface lets layout depend only on offsets, not on the
/// hyphenation tree internals.
/// </summary>
public interface ILineHyphenator
{
    /// <summary>
    /// Returns the hyphenation break offsets within <paramref name="word"/> (positions before which a
    /// hyphen may be inserted, ascending), or <c>null</c>/empty when the word cannot or should not be
    /// hyphenated (including an unknown language).
    /// </summary>
    /// <param name="language">the language code, or <c>null</c>.</param>
    /// <param name="country">the country code, or <c>null</c>.</param>
    /// <param name="word">the word to hyphenate.</param>
    /// <param name="remainCharCount">minimum characters before a break.</param>
    /// <param name="pushCharCount">minimum characters after a break.</param>
    int[]? Hyphenate(string? language, string? country, string word, int remainCharCount, int pushCharCount);
}

/// <summary>
/// The default <see cref="ILineHyphenator"/>: delegates to a <see cref="Hyphenator"/> (the shared
/// instance by default), loading patterns from the embedded resources. A word in a language with no
/// bundled patterns yields no break offsets, so layout simply leaves it unhyphenated.
/// </summary>
public sealed class DefaultLineHyphenator : ILineHyphenator
{
    private readonly Hyphenator hyphenator;

    /// <summary>Creates a hyphenator-backed line hyphenator (shared instance by default).</summary>
    /// <param name="hyphenator">the hyphenator to use, or <c>null</c> for <see cref="Hyphenator.Default"/>.</param>
    public DefaultLineHyphenator(Hyphenator? hyphenator = null) =>
        this.hyphenator = hyphenator ?? Hyphenator.Default;

    /// <inheritdoc/>
    public int[]? Hyphenate(string? language, string? country, string word, int remainCharCount,
        int pushCharCount)
    {
        global::Fop.Hyphenation.Hyphenation? result = hyphenator.Hyphenate(language, country, word, remainCharCount, pushCharCount);
        return result?.GetHyphenationPoints();
    }
}
