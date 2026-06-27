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
using Fop.Fo;

namespace Fop.Layout;

/// <summary>
/// A single word of inline content carrying its own resolved styling. Words are the atoms of greedy
/// line breaking: each is measured and placed independently, and adjacent words sharing a style on
/// the same line are coalesced into one <see cref="TextRun"/>.
/// </summary>
/// <param name="Text">The word text (no surrounding whitespace).</param>
/// <param name="Font">The resolved font for this word.</param>
/// <param name="Color">The resolved fill colour for this word.</param>
internal readonly record struct StyledWord(string Text, FontKey Font, FopColor Color);

/// <summary>
/// Flattens a block's mixed <see cref="FOText"/>/<see cref="FoInline"/> content into a flat sequence
/// of <see cref="StyledWord"/>s, resolving font and colour from each owning formatting object.
/// Nested <see cref="FoBlock"/>s are <em>not</em> flattened here; the block-stacking walk recurses
/// into them separately.
/// </summary>
internal static class InlineContent
{
    /// <summary>
    /// Builds the styled word list for the direct inline content of <paramref name="owner"/>.
    /// <para>
    /// <paramref name="pageNumber"/> is the 1-based number of the page the content is being laid out
    /// on; an <see cref="FoPageNumber"/> contributes a single word with that number (in its own
    /// resolved font and colour). For body flow content the caller passes the page index where the
    /// containing block begins (see the engine's body page-number handling).
    /// </para>
    /// </summary>
    public static List<StyledWord> Flatten(FObj owner, int pageNumber = 1)
    {
        var words = new List<StyledWord>();
        Collect(owner, words, pageNumber);
        return words;
    }

    private static void Collect(FObj owner, List<StyledWord> words, int pageNumber)
    {
        FontKey font = FontKeyFor(owner);
        FopColor color = owner.Properties.GetColor();

        foreach (FONode child in owner.Children)
        {
            switch (child)
            {
                case FOText text:
                    AppendWords(text.Text, font, color, words);
                    break;

                // fo:page-number resolves to the current page number as a single styled word.
                case FoPageNumber pageNum:
                    words.Add(new StyledWord(
                        pageNumber.ToString(System.Globalization.CultureInfo.InvariantCulture),
                        FontKeyFor(pageNum),
                        pageNum.Properties.GetColor()));
                    break;

                // Nested blocks are stacked by the engine, not flowed inline here.
                case FoBlock:
                    break;

                // Inlines (and neutral wrappers) contribute inline content with their own style.
                case FObj inline:
                    Collect(inline, words, pageNumber);
                    break;
            }
        }
    }

    private static void AppendWords(string text, FontKey font, FopColor color, List<StyledWord> words)
    {
        foreach (string token in text.Split(' ', StringSplitOptions.RemoveEmptyEntries))
        {
            words.Add(new StyledWord(token, font, color));
        }
    }

    /// <summary>Resolves the <see cref="FontKey"/> for a formatting object from its properties.</summary>
    public static FontKey FontKeyFor(FObj obj) =>
        new(obj.FontFamily, obj.FontSizeMpt, obj.FontWeight, obj.FontStyle);
}
