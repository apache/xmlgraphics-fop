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
/// Optional resolution context threaded through <see cref="InlineContent.Flatten"/>.
/// <para>
/// <see cref="ResolveCitation"/> maps an <c>fo:page-number-citation</c>/<c>-last</c> <c>ref-id</c> to
/// the page-number text it should render. It is <c>null</c> on the engine's first (measuring) pass, in
/// which case citations render a placeholder ("?") -- they are re-laid in the second pass once the
/// id-to-page map is known. <see cref="OnFootnote"/>, when set, is invoked for each
/// <see cref="FoFootnote"/> encountered, in inline order, so the flow can place its body at the page
/// bottom.
/// </para>
/// </summary>
internal sealed class FlattenContext
{
    /// <summary>Maps a citation <c>ref-id</c> to its resolved page-number text, or <c>null</c> if unknown.</summary>
    public Func<string, string?>? ResolveCitation { get; init; }

    /// <summary>Invoked (in inline order) for each footnote encountered while flattening.</summary>
    public Action<FoFootnote>? OnFootnote { get; init; }

    /// <summary>
    /// Resolves an <c>fo:retrieve-marker</c> (by retrieve-class-name + retrieve-position) to the
    /// styled words of the matching marker for the page being laid out, or <c>null</c>/empty when no
    /// marker qualifies (the retrieve-marker then renders nothing). Only set during static-content
    /// layout, where markers recorded by the body pass are available.
    /// </summary>
    public Func<string, RetrievePosition, IReadOnlyList<StyledWord>?>? ResolveMarker { get; init; }
}

/// <summary>
/// Flattens a block's mixed <see cref="FOText"/>/<see cref="FoInline"/> content into a flat sequence
/// of <see cref="StyledWord"/>s, resolving font and colour from each owning formatting object.
/// Nested <see cref="FoBlock"/>s are <em>not</em> flattened here; the block-stacking walk recurses
/// into them separately.
/// </summary>
internal static class InlineContent
{
    /// <summary>The placeholder rendered for an unresolved citation (or one resolved on the first pass).</summary>
    public const string UnresolvedCitation = "?";

    /// <summary>
    /// Builds the styled word list for the direct inline content of <paramref name="owner"/>.
    /// <para>
    /// <paramref name="pageNumber"/> is the 1-based number of the page the content is being laid out
    /// on; an <see cref="FoPageNumber"/> contributes a single word with that number (in its own
    /// resolved font and colour). For body flow content the caller passes the page index where the
    /// containing block begins (see the engine's body page-number handling).
    /// </para>
    /// <para>
    /// <paramref name="context"/> optionally supplies a citation resolver and a footnote sink (see
    /// <see cref="FlattenContext"/>). When omitted, citations render the
    /// <see cref="UnresolvedCitation"/> placeholder and footnotes contribute only their inline anchor.
    /// </para>
    /// </summary>
    public static List<StyledWord> Flatten(FObj owner, int pageNumber = 1, FlattenContext? context = null)
    {
        var words = new List<StyledWord>();
        Collect(owner, words, pageNumber, context);
        return words;
    }

    /// <summary>
    /// Flattens the content of an <see cref="FoMarker"/> into styled words. Block-level marker content
    /// is flattened as inline text for this cut (a marker is commonly inline text such as a chapter
    /// title), so nested <see cref="FoBlock"/>s contribute their text rather than being stacked. The
    /// resulting words carry the styling of their owning formatting object.
    /// </summary>
    public static List<StyledWord> FlattenMarker(FoMarker marker)
    {
        var words = new List<StyledWord>();
        CollectMarker(marker, words);
        return words;
    }

    private static void CollectMarker(FObj owner, List<StyledWord> words)
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

                // A nested marker is not part of this marker's rendered content.
                case FoMarker:
                    break;

                // Both block- and inline-level descendants contribute their text inline (block content
                // is rendered as inline text for this cut).
                case FObj inner:
                    CollectMarker(inner, words);
                    break;
            }
        }
    }

    private static void Collect(FObj owner, List<StyledWord> words, int pageNumber, FlattenContext? context)
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

                // fo:page-number-citation[-last] resolves to the referenced id's page number via the
                // context resolver (a placeholder on the first/measuring pass or for an unknown ref-id).
                case FoPageNumberCitation citation:
                    AppendCitation(citation, citation.RefId, words, context);
                    break;
                case FoPageNumberCitationLast citationLast:
                    AppendCitation(citationLast, citationLast.RefId, words, context);
                    break;

                // fo:footnote: its inline anchor flows here; its body is placed at the page bottom by
                // the flow (notified via the context). The body is never flattened inline.
                case FoFootnote footnote:
                    context?.OnFootnote?.Invoke(footnote);
                    foreach (FObj anchor in footnote.AnchorChildren)
                    {
                        Collect(anchor, words, pageNumber, context);
                    }

                    break;

                // fo:retrieve-marker renders the matching marker's content for this page (resolved via
                // the context, available during static-content layout). It contributes the marker's
                // own styled words, or nothing when no marker of the class qualifies.
                case FoRetrieveMarker retrieve:
                    IReadOnlyList<StyledWord>? marker =
                        context?.ResolveMarker?.Invoke(retrieve.RetrieveClassName, retrieve.RetrievePosition);
                    if (marker is not null)
                    {
                        words.AddRange(marker);
                    }

                    break;

                // fo:marker holds running-header content that is recorded per page, never flowed in
                // place; the engine collects it separately, so skip it here.
                case FoMarker:
                    break;

                // Nested blocks are stacked by the engine, not flowed inline here.
                case FoBlock:
                    break;

                // Inlines (and neutral wrappers) contribute inline content with their own style.
                case FObj inline:
                    Collect(inline, words, pageNumber, context);
                    break;
            }
        }
    }

    private static void AppendCitation(FObj citation, string refId, List<StyledWord> words,
        FlattenContext? context)
    {
        string text = context?.ResolveCitation?.Invoke(refId) ?? UnresolvedCitation;
        AppendWords(text, FontKeyFor(citation), citation.Properties.GetColor(), words);
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
