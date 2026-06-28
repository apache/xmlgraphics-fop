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

using System.Text;

using Fop.Colors;
using Fop.Fo;
using Fop.Svg;

namespace Fop.Layout;

/// <summary>
/// Turns a formatting-object tree into an <see cref="AreaTree"/> by resolving page geometry,
/// stacking blocks, breaking text into lines, and paginating.
/// <para>
/// This is the modern stand-in for FOP's layout-manager subsystem
/// (<c>org.apache.fop.layoutmgr</c>), scoped to block/inline text flow for the initial pipeline.
/// Line breaking is the Knuth-Plass total-fit algorithm (see <see cref="LineBreaker"/>) -- the same
/// family FOP itself uses -- which minimises total demerits over the whole paragraph rather than
/// filling each line greedily, while reusing the existing per-line emission/justification path.
/// </para>
/// </summary>
public sealed class LayoutEngine
{
    private readonly IFontMeasurer measurer;

    // ----- Two-pass citation-resolution state ---------------------------------------------
    //
    // Page-number citations reference the page an id lands on, which is only known after a full
    // layout. LayOut therefore runs twice: pass 1 lays the whole document out to populate the
    // id-to-page map (and the total page count), and pass 2 re-lays with a resolver so that each
    // fo:page-number-citation[-last] emits the referenced page number. The map is rebuilt on every
    // pass (page numbers are stable across passes because layout is deterministic), and the pass-2
    // resolver reads the map captured at the end of pass 1.

    /// <summary>The id-to-page-number map populated by the pass currently running.</summary>
    private Dictionary<string, int> currentIds = new(StringComparer.Ordinal);

    /// <summary>The id-to-page-number map captured at the end of pass 1 (used by pass 2's resolver).</summary>
    private IReadOnlyDictionary<string, int>? resolvedIds;

    /// <summary>
    /// The id-to-page-INDEX map populated by the pass currently running: the 0-based index into the
    /// area tree's pages that each id's area lands on (distinct from the 1-based page number, which is
    /// per-sequence and offset by initial-page-number). Used to target internal links.
    /// </summary>
    private Dictionary<string, int> currentPageIndexes = new(StringComparer.Ordinal);

    /// <summary>The id-to-page-index map captured at the end of pass 1 (used by pass 2's link resolver).</summary>
    private IReadOnlyDictionary<string, int>? resolvedPageIndexes;

    /// <summary>Whether a citation resolver is active (pass 2). On pass 1 citations render a placeholder.</summary>
    private bool resolving;

    // ----- Marker collection state --------------------------------------------------------
    //
    // fo:marker content is recorded per page as the body flow places the block that carries it, much
    // like ids are recorded. For each page we keep, per marker-class, the FIRST and LAST marker that
    // STARTED on that page (in document order), plus a running "carryover": the last marker of each
    // class seen on this or any earlier page of the sequence. A fo:retrieve-marker in a region's
    // static content for page P then resolves against that page's record (with carryover fallback).
    //
    // The store is rebuilt on every pass (layout is deterministic). Markers are keyed by the PageArea
    // object so a page number reused across sequences (via initial-page-number) cannot collide.

    /// <summary>The per-page marker records for the pass currently running, keyed by page area.</summary>
    private readonly Dictionary<PageArea, PageMarkers> markersByPage = new(ReferenceEqualityComparer.Instance);

    /// <summary>The flattened content of one recorded marker.</summary>
    private readonly record struct MarkerContent(IReadOnlyList<StyledWord> Words);

    /// <summary>
    /// The markers recorded for a single page: per class, the first and last marker that started on
    /// the page, and the running carryover (the last marker of the class seen on this or an earlier
    /// page of the sequence).
    /// </summary>
    private sealed class PageMarkers
    {
        public Dictionary<string, MarkerContent> First { get; } = new(StringComparer.Ordinal);

        public Dictionary<string, MarkerContent> Last { get; } = new(StringComparer.Ordinal);

        public Dictionary<string, MarkerContent> Carryover { get; } = new(StringComparer.Ordinal);
    }

    private readonly IImageResolver? imageResolver;

    /// <summary>Creates a layout engine that measures text via <paramref name="measurer"/>.</summary>
    public LayoutEngine(IFontMeasurer measurer)
        : this(measurer, imageResolver: null)
    {
    }

    /// <summary>
    /// Creates a layout engine with the given text <paramref name="measurer"/> and an optional
    /// <paramref name="imageResolver"/> used to size <c>fo:external-graphic</c> images intrinsically.
    /// </summary>
    public LayoutEngine(IFontMeasurer measurer, IImageResolver? imageResolver)
    {
        this.measurer = measurer ?? throw new ArgumentNullException(nameof(measurer));
        this.imageResolver = imageResolver;
    }

    /// <summary>The font measurer used by this engine.</summary>
    public IFontMeasurer Measurer => measurer;

    /// <summary>The image resolver used to size external graphics, or <c>null</c> when none is set.</summary>
    public IImageResolver? ImageResolver => imageResolver;

    /// <summary>
    /// The hyphenator consulted when a block enables <c>hyphenate</c> and a word does not fit on a line.
    /// Defaults to the embedded-pattern-backed <see cref="DefaultLineHyphenator"/>; tests may replace it
    /// with a deterministic stub. Setting it to <c>null</c> disables hyphenation entirely.
    /// </summary>
    public ILineHyphenator? Hyphenator { get; set; } = new DefaultLineHyphenator();

    /// <summary>Lays out the document rooted at <paramref name="root"/>.</summary>
    /// <returns>The paginated area tree.</returns>
    /// <remarks>
    /// Runs a two-pass layout so forward and backward <c>fo:page-number-citation</c> references
    /// resolve. Pass 1 lays the document out only to record which page each <c>id</c> lands on (and
    /// the total page count); citations render a placeholder ("?") there. Pass 2 re-lays the document
    /// with a resolver over the pass-1 map, producing the returned area tree with citations resolved.
    /// An unresolved <c>ref-id</c> (no matching id) resolves to "?" and never throws.
    /// </remarks>
    public AreaTree LayOut(FoRoot root)
    {
        ArgumentNullException.ThrowIfNull(root);

        // Pass 1: measure-only. Build the id-to-page map; citations render the placeholder.
        resolving = false;
        currentIds = new Dictionary<string, int>(StringComparer.Ordinal);
        currentPageIndexes = new Dictionary<string, int>(StringComparer.Ordinal);
        markersByPage.Clear();
        _ = LayOutAllSequences(root);
        resolvedIds = currentIds;
        resolvedPageIndexes = currentPageIndexes;

        // Pass 2: resolved. Re-lay with the citation resolver active. Layout is deterministic, so the
        // page each id lands on is identical to pass 1; the resolver reads the pass-1 map. Markers are
        // re-collected against the pass-2 page areas so static content resolves against this tree.
        resolving = true;
        currentIds = new Dictionary<string, int>(StringComparer.Ordinal);
        currentPageIndexes = new Dictionary<string, int>(StringComparer.Ordinal);
        markersByPage.Clear();
        AreaTree tree = LayOutAllSequences(root);

        // The document is now fully laid out and the id-to-page-index map (resolvedPageIndexes) is
        // populated. Walk the fo:bookmark-tree (if any) to build the outline, resolving each bookmark's
        // internal-destination to the page its target id lands on. Left empty when there is no tree.
        tree.Outline = BuildOutline(root, tree.Pages.Count);
        return tree;
    }

    /// <summary>
    /// Builds the document outline (PDF bookmarks) from <paramref name="root"/>'s
    /// <c>fo:bookmark-tree</c>, or an empty list when none is present. Each <c>fo:bookmark</c> becomes
    /// an <see cref="OutlineEntry"/>: its title is the flattened <c>fo:bookmark-title</c> text, its
    /// <see cref="OutlineEntry.Open"/> follows <c>starting-state</c> (show =&gt; open), and its target is
    /// resolved from <c>internal-destination</c> via the pass-1 id-to-page-index map. Child bookmarks
    /// recurse to nested entries.
    /// </summary>
    /// <remarks>
    /// Target resolution:
    /// <list type="bullet">
    /// <item>An <c>internal-destination</c> ref-id that resolves maps to that 0-based page index.</item>
    /// <item>A bookmark with only an <c>external-destination</c> carries that URI and, so the entry is
    /// still navigable in renderers whose outlines are page-targeted, targets the first page (index 0).</item>
    /// <item>An unresolved internal ref-id (unknown/missing id) and no external destination targets the
    /// first page when one exists (index 0), else <c>null</c> -- documented best-effort, never throws.</item>
    /// </list>
    /// </remarks>
    private IReadOnlyList<OutlineEntry> BuildOutline(FoRoot root, int pageCount)
    {
        FoBookmarkTree? bookmarkTree = root.BookmarkTree;
        if (bookmarkTree is null)
        {
            return [];
        }

        int? firstPage = pageCount > 0 ? 0 : null;
        return [.. bookmarkTree.Bookmarks.Select(b => BuildOutlineEntry(b, firstPage))];
    }

    private OutlineEntry BuildOutlineEntry(FoBookmark bookmark, int? firstPage)
    {
        string title = bookmark.Title?.Text ?? string.Empty;
        bool open = bookmark.StartingState == StartingState.Show;

        // Resolve the internal-destination ref-id to a 0-based page index via the pass-1 map. When it
        // does not resolve, fall back to the first page (best-effort) so the entry stays navigable.
        int? target;
        string? uri = null;
        if (bookmark.InternalDestination.Length > 0
            && resolvedPageIndexes is not null
            && resolvedPageIndexes.TryGetValue(bookmark.InternalDestination, out int index))
        {
            target = index;
        }
        else if (bookmark.ExternalDestination.Length > 0)
        {
            uri = bookmark.ExternalDestination;
            target = firstPage;
        }
        else
        {
            target = firstPage;
        }

        IReadOnlyList<OutlineEntry> children =
            [.. bookmark.Children.Select(c => BuildOutlineEntry(c, firstPage))];
        return new OutlineEntry(title, target, uri, open, children);
    }

    private AreaTree LayOutAllSequences(FoRoot root)
    {
        var tree = new AreaTree();
        foreach (FoPageSequence seq in root.PageSequences)
        {
            LayOutSequence(root, seq, tree);
        }

        return tree;
    }

    /// <summary>Records that the area generated by <paramref name="id"/> lands on <paramref name="pageNumber"/>.</summary>
    /// <remarks>
    /// First-write-wins for a given id within a pass, so an id maps to the page its area first appears
    /// on. For <c>fo:page-number-citation-last</c> the flat model records a single page per id, so the
    /// "last" page equals that page (documented approximation; areas are not split per page here).
    /// </remarks>
    private void RecordId(string id, int pageNumber, int pageIndex)
    {
        if (id.Length > 0)
        {
            currentIds[id] = pageNumber;
            if (pageIndex >= 0 && !currentPageIndexes.ContainsKey(id))
            {
                currentPageIndexes[id] = pageIndex;
            }
        }
    }

    /// <summary>
    /// Resolves an <c>fo:basic-link</c> <c>internal-destination</c> ref-id to the 0-based area-tree page
    /// index its target lands on, using the pass-1 map; <c>null</c> on pass 1 or for an unknown id (the
    /// link then records no internal target this pass).
    /// </summary>
    private int? ResolveInternalDestination(string refId)
    {
        if (!resolving || resolvedPageIndexes is null)
        {
            return null;
        }

        return resolvedPageIndexes.TryGetValue(refId, out int index) ? index : null;
    }

    /// <summary>
    /// Resolves a citation <c>ref-id</c> to its page-number text using the pass-1 map, or <c>null</c>
    /// (rendered as "?") when the id is unknown. Returns <c>null</c> on pass 1 (no resolver active).
    /// </summary>
    private string? ResolveCitation(string refId)
    {
        if (!resolving || resolvedIds is null)
        {
            return null;
        }

        return resolvedIds.TryGetValue(refId, out int page)
            ? page.ToString(System.Globalization.CultureInfo.InvariantCulture)
            : null;
    }

    /// <summary>
    /// Records the <c>id</c> of <paramref name="block"/> and of its inline-level descendants against
    /// <paramref name="pageNumber"/>, so a <c>fo:page-number-citation</c> referencing any of them
    /// resolves to that page. Inline descendants are walked through inlines, page-numbers, citations
    /// and footnote anchors; nested <see cref="FoBlock"/>s are skipped (each self-records when it is
    /// itself laid out on its own page).
    /// </summary>
    private void RecordIdsOnPage(FoBlock block, int pageNumber, int pageIndex)
    {
        RecordId(IdOf(block), pageNumber, pageIndex);
        foreach (FONode child in block.Children)
        {
            RecordInlineIds(child, pageNumber, pageIndex);
        }
    }

    private void RecordInlineIds(FONode node, int pageNumber, int pageIndex)
    {
        if (node is FoBlock)
        {
            // A nested block records its own id when the block walk reaches it.
            return;
        }

        if (node is FObj obj)
        {
            RecordId(IdOf(obj), pageNumber, pageIndex);
            foreach (FONode child in obj.Children)
            {
                RecordInlineIds(child, pageNumber, pageIndex);
            }
        }
    }

    /// <summary>
    /// Records the <c>id</c> of <paramref name="root"/> and of <em>every</em> descendant formatting
    /// object against <paramref name="pageNumber"/>. Used for content laid out into a relocatable
    /// buffer (a table cell, list item label/body, footnote body) and then placed on a known page: the
    /// whole buffer lands on one page in this model, so every id it contains resolves to that page.
    /// This complements <see cref="RecordIdsOnPage"/>, which handles the paginating flow where nested
    /// blocks self-record on their own pages.
    /// </summary>
    private void RecordIdsInSubtree(FObj root, int pageNumber, int pageIndex)
    {
        RecordId(IdOf(root), pageNumber, pageIndex);
        foreach (FObj child in root.ChildObjects)
        {
            RecordIdsInSubtree(child, pageNumber, pageIndex);
        }
    }

    /// <summary>The declared <c>id</c> attribute of an object (keyed by local name), or empty if unset.</summary>
    private static string IdOf(FObj obj) => obj.Properties.GetString("id", string.Empty);

    /// <summary>Returns (creating if needed) the marker record for <paramref name="page"/>.</summary>
    private PageMarkers MarkersFor(PageArea page)
    {
        if (!markersByPage.TryGetValue(page, out PageMarkers? record))
        {
            record = new PageMarkers();
            markersByPage[page] = record;
        }

        return record;
    }

    /// <summary>
    /// Records every <c>fo:marker</c> carried by <paramref name="block"/> against the page it starts
    /// on (<paramref name="page"/>), in document order: the first marker of a class on the page wins
    /// the "first" slot, the last wins the "last" slot, and the running per-class
    /// <paramref name="carryover"/> (the last marker of the class seen so far in the sequence) is
    /// updated. The marker's block/inline content is flattened to styled words once, here.
    /// </summary>
    private void RecordMarkersOnPage(FoBlock block, PageArea page, Dictionary<string, MarkerContent> carryover)
    {
        PageMarkers record = MarkersFor(page);
        foreach (FoMarker marker in block.Markers)
        {
            string cls = marker.MarkerClassName;
            if (cls.Length == 0)
            {
                continue;
            }

            var content = new MarkerContent(InlineContent.FlattenMarker(marker));
            if (!record.First.ContainsKey(cls))
            {
                record.First[cls] = content;
            }

            record.Last[cls] = content;
            carryover[cls] = content;
        }
    }

    /// <summary>
    /// Resolves an <c>fo:retrieve-marker</c> for <paramref name="page"/>: the first/last marker of
    /// <paramref name="retrieveClassName"/> that started on the page per <paramref name="position"/>,
    /// falling back to the page's carryover (the last marker of the class from an earlier page) for
    /// the carryover and last positions. Returns <c>null</c> when nothing qualifies (render empty).
    /// </summary>
    private IReadOnlyList<StyledWord>? ResolveMarker(PageArea page, string retrieveClassName,
        RetrievePosition position)
    {
        if (!markersByPage.TryGetValue(page, out PageMarkers? record))
        {
            return null;
        }

        switch (position)
        {
            case RetrievePosition.FirstStartingWithinPage:
                // First on the page only; no carryover fallback.
                return record.First.TryGetValue(retrieveClassName, out MarkerContent f) ? f.Words : null;

            case RetrievePosition.FirstIncludingCarryover:
                // First on the page, else the carried-over last marker from an earlier page.
                if (record.First.TryGetValue(retrieveClassName, out MarkerContent fc))
                {
                    return fc.Words;
                }

                return record.Carryover.TryGetValue(retrieveClassName, out MarkerContent fco)
                    ? fco.Words : null;

            case RetrievePosition.LastStartingWithinPage:
            case RetrievePosition.LastEndingWithinPage:
            default:
                // Last on the page, else the carried-over last marker from an earlier page.
                if (record.Last.TryGetValue(retrieveClassName, out MarkerContent l))
                {
                    return l.Words;
                }

                return record.Carryover.TryGetValue(retrieveClassName, out MarkerContent lco)
                    ? lco.Words : null;
        }
    }

    private void LayOutSequence(FoRoot root, FoPageSequence seq, AreaTree tree)
    {
        PageGeometry geometry = PageGeometry.Resolve(root.LayoutMasterSet?.GetSimplePageMaster(seq.MasterReference));
        int initialPageNumber = seq.InitialPageNumber ?? 1;
        var flow = new FlowContext(this, geometry, tree, initialPageNumber);

        // Remember which pages already exist so we only attach static content to this sequence's pages.
        int firstPageIndex = tree.Pages.Count;

        FoFlow? flowFo = seq.Flow;
        if (flowFo is not null)
        {
            foreach (FObj child in flowFo.ChildObjects)
            {
                switch (child)
                {
                    case FoBlock block:
                        flow.ApplyBreak(block.BreakBefore);
                        flow.LayOutBlock(block, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        flow.ApplyBreak(block.BreakAfter);
                        break;
                    case FoTable table:
                        flow.ApplyBreak(table.BreakBefore);
                        flow.LayOutTable(table, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        flow.ApplyBreak(table.BreakAfter);
                        break;
                    case FoListBlock list:
                        flow.ApplyBreak(list.BreakBefore);
                        flow.LayOutList(list, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        flow.ApplyBreak(list.BreakAfter);
                        break;
                    case FoBlockContainer container:
                        flow.ApplyBreak(container.BreakBefore);
                        flow.LayOutBlockContainer(container, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        flow.ApplyBreak(container.BreakAfter);
                        break;
                }
            }
        }

        // A page-sequence always occupies at least one page, even with empty/absent content.
        flow.EnsurePage();

        // Finalize the last page: flush its queued footnote bodies (earlier pages were flushed as the
        // flow paginated onto each new page).
        flow.FinishPage();

        // The body flow has now created all of this sequence's pages. Render the running header/footer
        // static content into the region-before/after bands of each page, with the page number set to
        // that page's 1-based number (so a fo:page-number in a header increments across pages).
        FoStaticContent? before = seq.GetStaticContent("xsl-region-before");
        FoStaticContent? after = seq.GetStaticContent("xsl-region-after");
        FoStaticContent? start = seq.GetStaticContent("xsl-region-start");
        FoStaticContent? end = seq.GetStaticContent("xsl-region-end");
        if (before is not null || after is not null || start is not null || end is not null)
        {
            for (int i = firstPageIndex; i < tree.Pages.Count; i++)
            {
                int pageNumber = initialPageNumber + (i - firstPageIndex);
                PageArea page = tree.Pages[i];
                if (before is not null)
                {
                    LayOutStaticContent(before, page, geometry, geometry.RegionLeftMpt,
                        geometry.RegionWidthMpt, geometry.RegionBeforeTopMpt, pageNumber);
                }

                if (after is not null)
                {
                    LayOutStaticContent(after, page, geometry, geometry.RegionLeftMpt,
                        geometry.RegionWidthMpt, geometry.RegionAfterTopMpt, pageNumber);
                }

                // Side regions: the start (left) and end (right) vertical bands, laid out into their
                // band width and flushed at the band's top-left corner.
                if (start is not null)
                {
                    LayOutStaticContent(start, page, geometry, geometry.RegionStartLeftMpt,
                        geometry.RegionStartExtentMpt, geometry.SideRegionTopMpt, pageNumber);
                }

                if (end is not null)
                {
                    LayOutStaticContent(end, page, geometry, geometry.RegionEndLeftMpt,
                        geometry.RegionEndExtentMpt, geometry.SideRegionTopMpt, pageNumber);
                }
            }
        }
    }

    /// <summary>
    /// Lays out the block-level children of a <see cref="FoStaticContent"/> into a region band of the
    /// given page: its content is left-aligned at <paramref name="bandLeftMpt"/>, top-aligned at
    /// <paramref name="bandTopMpt"/>, laid out at <paramref name="bandWidthMpt"/> wide, with the page
    /// number fixed at <paramref name="pageNumber"/>. The same block-stacking/line-breaking mechanism
    /// the body flow uses is reused via a relocatable buffer (so the band never paginates), then
    /// flushed to the page at the band origin. A <c>fo:page-number</c> resolves to the page number and a
    /// <c>fo:retrieve-marker</c> to the matching marker recorded for this page.
    /// </summary>
    private void LayOutStaticContent(FoStaticContent content, PageArea page, PageGeometry geometry,
        double bandLeftMpt, double bandWidthMpt, double bandTopMpt, int pageNumber)
    {
        // A throwaway flow context drives the shared block walk; the band itself never paginates, so
        // the content is collected into a relocatable buffer and flushed to the page at the band origin.
        // The context carries a marker resolver bound to this page so retrieve-markers resolve.
        var flow = new FlowContext(this, geometry, new AreaTree(), pageNumber)
        {
            MarkerResolver = (cls, position) => ResolveMarker(page, cls, position),
        };
        var buffer = new BufferedSink();
        flow.LayOutStaticBlocks(content, bandWidthMpt, buffer);
        buffer.FlushTo(new PageSink(page), bandLeftMpt, bandTopMpt);
    }

    // ----- Total-fit (Knuth-Plass) line breaking -------------------------------------------
    //
    // Lines are chosen by the Knuth-Plass total-fit algorithm (see LineBreaker), the same family of
    // algorithm FOP itself uses. The paragraph's styled words are turned into an abstract item stream
    // -- boxes (words / fixed leaders), glue (inter-word spaces and expanding leaders), and penalties
    // (hyphenation candidates plus a final forced break) -- and the breaker returns the optimal set of
    // breakpoints minimising total demerits over the whole paragraph. The chosen line slices are then
    // rebuilt into LineBoxes and handed to the existing EmitLine machinery, so positioning,
    // justification, leader expansion, run coalescing and link recording are entirely unchanged: only
    // the *choice* of break points moved from greedy first-fit to optimal total-fit.

    /// <summary>
    /// For justified text an inter-word space may stretch by half its width so the optimiser packs lines
    /// tightly and the emission machinery distributes the slack. Inter-word spaces are not allowed to
    /// shrink (shrink = 0): like greedy first-fit, a word that does not fit moves to the next line rather
    /// than being crammed in by compressing spaces, which keeps optimal breaking from ever overprinting.
    /// For ragged text (start/center/end) the slack instead lives in a line-end glue contributed only
    /// when a break is chosen there, so a short line is cheap rather than badly underfull -- this mirrors
    /// FOP's own element generation.
    /// </summary>
    private const double GlueStretchFraction = 0.5;

    private const double GlueShrinkFraction = 0.0;

    /// <summary>The stretch (in space-widths) given to a ragged line's end glue, matching FOP.</summary>
    private const double RaggedLineEndStretch = 3.0;

    /// <summary>
    /// The render-time facet attached to each Knuth-Plass <em>box</em> item: the styled fragment to
    /// paint, whether an inter-word space precedes it on its line, and whether it is a continuation of
    /// the previous box's word (a hyphenation fragment, joined with no space). A box that ends at a
    /// chosen flagged hyphen break has the block's hyphenation character appended to render "frag-".
    /// Glue and penalty items carry a default (empty) box and are never themselves rendered.
    /// </summary>
    private readonly record struct BoxContent(StyledWord Word, bool SpaceBefore, bool ContinuesWord);

    /// <summary>
    /// The paragraph item stream plus the per-item render facets needed to rebuild lines from chosen
    /// breakpoints. <see cref="Items"/> is what the breaker sees; <see cref="Boxes"/> maps each item
    /// index to its <see cref="BoxContent"/> (only box items have meaningful content);
    /// <see cref="HyphenAfter"/> records, for a flagged hyphen penalty item, the index of the box it
    /// follows (so a break there appends a hyphen to that box's fragment).
    /// </summary>
    private sealed class Paragraph
    {
        public List<LineBreaker.Item> Items { get; } = new();

        public List<BoxContent> Boxes { get; } = new();

        /// <summary>Maps a flagged hyphen-penalty item index to the box item it immediately follows.</summary>
        public Dictionary<int, int> HyphenAfter { get; } = new();

        public void AddBox(LineBreaker.Item item, BoxContent content)
        {
            Items.Add(item);
            Boxes.Add(content);
        }

        public void AddSynthetic(LineBreaker.Item item)
        {
            Items.Add(item);
            Boxes.Add(default);
        }
    }

    /// <summary>
    /// Builds the Knuth-Plass item stream for a block's flattened <paramref name="words"/> at the given
    /// line width. Each rendered word becomes a box (a fixed leader is a box of its fixed length, an
    /// expanding leader is a glue with very large stretch so it fills the line). A word-space between two
    /// rendered words becomes a stretchable/shrinkable glue and a legal breakpoint; a leader abuts its
    /// neighbours (no surrounding space), matching emission. When the block enables hyphenation, each
    /// internal hyphenation point of a word is emitted as a flagged penalty (carrying the hyphen-character
    /// width) between fragment boxes, so a break may fall inside the word. The stream ends with an
    /// infinitely-stretchable finishing glue and a forced break so the last line is never penalised for
    /// being short.
    /// </summary>
    private Paragraph BuildParagraph(FoBlock block, List<StyledWord> words, double lineWidth)
    {
        var para = new Paragraph();
        bool hyphenate = block.Hyphenate && Hyphenator is not null;
        bool justify = block.TextAlign == TextAlign.Justify;

        // A very large (but finite) stretch makes a glue behave as "infinitely" stretchable relative to
        // ordinary word spaces, while staying a real number for the ratio arithmetic. Scaled off the
        // line width so it dominates regardless of font size.
        double infiniteStretch = Math.Max(lineWidth, 1) * 1000.0;

        bool previousWasRendered = false;
        foreach (StyledWord word in words)
        {
            if (word.IsLeader)
            {
                // A leader abuts its neighbours (no inter-word glue). A fixed leader is a box of its
                // length; an expanding leader is a glue with very large stretch so it fills the line.
                double? fixedLen = word.Leader!.Value.FixedLengthMpt;
                if (fixedLen is double len)
                {
                    para.AddBox(LineBreaker.Item.Box(len), new BoxContent(word, SpaceBefore: false, ContinuesWord: false));
                }
                else
                {
                    // The expanding leader is a glue (so it fills the line). It is also a box for
                    // reconstruction: record its content under the same item index.
                    para.AddBox(LineBreaker.Item.Glue(0, infiniteStretch, 0),
                        new BoxContent(word, SpaceBefore: false, ContinuesWord: false));
                }

                previousWasRendered = false;
                continue;
            }

            // A word-space precedes this word unless it is the first rendered item or abuts a leader.
            bool spaceBefore = previousWasRendered;
            if (spaceBefore)
            {
                AppendInterWordGlue(para, SpaceWidth(word.Font), justify);
            }

            AppendWord(para, block, word, hyphenate, spaceBefore);
            previousWasRendered = true;
        }

        // Finishing glue (infinitely stretchable, so the last line is never short-penalised) plus the
        // mandatory paragraph-end break.
        para.AddSynthetic(LineBreaker.Item.Glue(0, infiniteStretch, 0));
        para.AddSynthetic(LineBreaker.Item.Penalty(LineBreaker.ForcedBreak));
        return para;
    }

    /// <summary>
    /// Emits the inter-word break opportunity for a space of the given width. For justified text this is
    /// a single stretchable/shrinkable glue (a legal break, since it follows a box): the optimiser packs
    /// lines tightly and the leftover is distributed at emission. For ragged text it is FOP's three-part
    /// sequence -- a line-end stretch glue, the legal break penalty, then the actual space as a glue that
    /// cancels the stretch when the space is mid-line -- so the slack of a short line lives at the line
    /// end (cheap badness) rather than spread through the line.
    /// </summary>
    private static void AppendInterWordGlue(Paragraph para, double space, bool justify)
    {
        if (justify)
        {
            para.AddSynthetic(LineBreaker.Item.Glue(space, space * GlueStretchFraction,
                space * GlueShrinkFraction));
            return;
        }

        // Ragged: the first glue contributes the line-end stretch only when the following penalty is the
        // chosen break; the trailing glue cancels that stretch when the space sits mid-line (net width =
        // space, net stretch = 0). An INFINITE penalty before the stretch glue forbids breaking there, so
        // the only legal break is the zero penalty between the two glues.
        para.AddSynthetic(LineBreaker.Item.Penalty(LineBreaker.InfiniteBreak));
        para.AddSynthetic(LineBreaker.Item.Glue(0, space * RaggedLineEndStretch, 0));
        para.AddSynthetic(LineBreaker.Item.Penalty(0));
        para.AddSynthetic(LineBreaker.Item.Glue(space, -space * RaggedLineEndStretch, 0));
    }

    /// <summary>
    /// Appends a single rendered word to the stream as either one box (no hyphenation) or a sequence of
    /// fragment boxes joined by flagged hyphen penalties. Each fragment box carries its own fragment
    /// text so line reconstruction never re-measures or duplicates the word; fragments of the same word
    /// are marked as continuations so they join with no inter-word space.
    /// </summary>
    private void AppendWord(Paragraph para, FoBlock block, StyledWord word, bool hyphenate, bool spaceBefore)
    {
        int[]? points = hyphenate && word.Text.Length > 0
            ? Hyphenator!.Hyphenate(block.Language, block.Country, word.Text,
                block.HyphenationRemainCharacterCount, block.HyphenationPushCharacterCount)
            : null;

        if (points is null || points.Length == 0)
        {
            para.AddBox(LineBreaker.Item.Box(MeasuredAdvance(word.Text, word.Font, word.LetterSpacingMpt)),
                new BoxContent(word, spaceBefore, ContinuesWord: false));
            return;
        }

        double hyphenWidth = measurer.MeasureWidthMpt(block.HyphenationCharacter, word.Font);
        int prev = 0;
        bool first = true;
        foreach (int cut in points)
        {
            if (cut <= prev || cut >= word.Text.Length)
            {
                continue;
            }

            // The fragment between the previous cut and this one (a continuation of the same word for all
            // but the first fragment), then a flagged hyphen penalty recording the box it follows.
            string fragment = word.Text[prev..cut];
            int boxItem = para.Items.Count;
            para.AddBox(LineBreaker.Item.Box(MeasuredAdvance(fragment, word.Font, word.LetterSpacingMpt)),
                new BoxContent(word with { Text = fragment }, spaceBefore && first, ContinuesWord: !first));
            first = false;

            int penaltyItem = para.Items.Count;
            para.AddSynthetic(LineBreaker.Item.Penalty(LineBreaker.HyphenPenalty, hyphenWidth, flagged: true));
            para.HyphenAfter[penaltyItem] = boxItem;
            prev = cut;
        }

        // The trailing fragment after the last hyphenation point.
        string tail = word.Text[prev..];
        para.AddBox(LineBreaker.Item.Box(MeasuredAdvance(tail, word.Font, word.LetterSpacingMpt)),
            new BoxContent(word with { Text = tail }, spaceBefore && first, ContinuesWord: !first));
    }

    /// <summary>
    /// The inline advance of <paramref name="text"/> in <paramref name="font"/>. With no
    /// letter-spacing this is the measured string width. With letter-spacing it is the sum of the
    /// individual glyph advances plus the tracking inserted <em>between</em> glyphs (n-1 gaps): FOP
    /// positions each glyph by its own advance + the letter-space adjust and trims the trailing space
    /// at a word end, so a non-spaced word of <c>n</c> glyphs gains <c>(n-1)*letter-spacing</c>. The
    /// renderer reproduces the same per-glyph walk so layout and output agree.
    /// </summary>
    private double MeasuredAdvance(string text, FontKey font, double letterSpacingMpt)
    {
        if (letterSpacingMpt == 0)
        {
            return measurer.MeasureWidthMpt(text, font);
        }

        double width = 0;
        foreach (char c in text)
        {
            width += measurer.MeasureWidthMpt(c.ToString(), font);
        }

        return width + letterSpacingMpt * Math.Max(0, text.Length - 1);
    }

    /// <summary>
    /// Builds the styled-word line slices for a block from the optimal breakpoints. Each returned
    /// <see cref="LineBox"/> carries the words/fragments on that line (a hyphenation fragment ending a
    /// line is rendered as "frag-"), the line's natural width (matching the emission machinery's
    /// spacing), its content height and whether it carries a leader.
    /// </summary>
    private List<LineBox> BreakIntoLines(FoBlock block, List<StyledWord> words, double lineWidth)
    {
        Paragraph para = BuildParagraph(block, words, lineWidth);
        IReadOnlyList<int> breaks = LineBreaker.Break(para.Items, lineWidth);

        var lines = new List<LineBox>(breaks.Count);
        int itemStart = 0;
        foreach (int breakItem in breaks)
        {
            lines.Add(BuildLine(para, block, itemStart, breakItem));

            // The next line starts just after this break (the broken-at glue/penalty is consumed).
            itemStart = breakItem + 1;
        }

        return lines;
    }

    /// <summary>
    /// Rebuilds one line's <see cref="LineBox"/> from the box items in the range
    /// [<paramref name="itemStart"/>, <paramref name="breakItem"/>). The natural width is computed
    /// exactly as emission does: a word-space precedes each box that records one, except a box that
    /// continues the previous box's word (a hyphenation fragment) or abuts a leader. When the break is a
    /// flagged hyphen penalty the box it follows gets the block's hyphenation character appended so the
    /// line renders "frag-"; the trailing fragment opens the next line as an ordinary box.
    /// </summary>
    private LineBox BuildLine(Paragraph para, FoBlock block, int itemStart, int breakItem)
    {
        var placed = new List<StyledWord>();
        double naturalWidth = 0;
        double maxFontHeight = 0;
        bool hasLeader = false;

        for (int i = itemStart; i < breakItem; i++)
        {
            if (para.Items[i].Kind == LineBreaker.ItemKind.Penalty
                || (para.Items[i].Kind == LineBreaker.ItemKind.Glue && !IsExpandingLeader(para, i)))
            {
                // Synthetic glue/penalty: not rendered. (An expanding-leader glue is also a box.)
                continue;
            }

            BoxContent box = para.Boxes[i];
            AddBoxToLine(placed, ref naturalWidth, ref maxFontHeight, ref hasLeader, box);
        }

        // A flagged hyphen break ends the line inside a word: append the hyphenation character to the
        // last fragment so it renders "frag-". The trailing fragment is a separate box that opens the
        // next line, so nothing needs to be carried.
        if (para.HyphenAfter.ContainsKey(breakItem) && placed.Count > 0)
        {
            StyledWord tailFragment = placed[^1];
            placed[^1] = tailFragment with { Text = tailFragment.Text + block.HyphenationCharacter };
            double hyphenWidth = measurer.MeasureWidthMpt(block.HyphenationCharacter, tailFragment.Font);
            naturalWidth += hyphenWidth;
        }

        return new LineBox(placed, naturalWidth, maxFontHeight, breakItem + 1, hasLeader);
    }

    /// <summary>Whether item <paramref name="i"/> is the glue that represents an expanding leader.</summary>
    private static bool IsExpandingLeader(Paragraph para, int i) =>
        para.Items[i].Kind == LineBreaker.ItemKind.Glue && para.Boxes[i].Word.IsLeader;

    /// <summary>
    /// Adds one box's styled fragment to a line being built, accumulating natural width/height. A
    /// fragment that continues the previous box's word (two hyphenation fragments that ended up on the
    /// same line because the point between them was not chosen as a break) is merged back into the last
    /// placed word with no inter-fragment space, so emission never inserts a spurious space inside it.
    /// </summary>
    private void AddBoxToLine(List<StyledWord> placed, ref double naturalWidth, ref double maxFontHeight,
        ref bool hasLeader, BoxContent box)
    {
        StyledWord word = box.Word;
        double wordWidth = word.IsLeader
            ? word.Leader!.Value.FixedLengthMpt ?? 0
            : MeasuredAdvance(word.Text, word.Font, word.LetterSpacingMpt);

        if (box.ContinuesWord && placed.Count > 0)
        {
            // Re-join with the previous fragment of the same word (no space, no extra height change).
            StyledWord head = placed[^1];
            placed[^1] = head with { Text = head.Text + word.Text };
            naturalWidth += wordWidth;
            return;
        }

        // A leader abuts its neighbours; otherwise a word-space precedes a box that records one.
        bool abuts = placed.Count == 0 || word.IsLeader || placed[^1].IsLeader;
        double space = abuts || !box.SpaceBefore ? 0 : SpaceWidth(word.Font);

        placed.Add(word);
        naturalWidth += space + wordWidth;
        maxFontHeight = Math.Max(maxFontHeight, FontHeight(word.Font));
        hasLeader |= word.IsLeader;
    }

    /// <summary>
    /// Positions a filled line on <paramref name="target"/>, applying text-align
    /// (start/center/end/justify) and coalescing adjacent words that share a style into one
    /// <see cref="TextRun"/>. <paramref name="lineTopMpt"/> is the top of the line box in page
    /// coordinates.
    /// </summary>
    private void EmitLine(IPrimitiveSink target, LineBox line, double leftMpt, double availableMpt,
        double lineHeightMpt, double lineTopMpt, TextAlign align, bool isLastLine)
    {
        if (line.Words.Count == 0)
        {
            return;
        }

        double effectiveHeight = Math.Max(lineHeightMpt, line.Height);
        double leading = effectiveHeight - line.Height;
        double ascender = line.Words.Max(w => measurer.AscenderMpt(w.Font));
        double baseline = lineTopMpt + leading / 2 + ascender;

        double slack = Math.Max(0, availableMpt - line.NaturalWidth);
        int gaps = line.Words.Count - 1;
        double startX = leftMpt;
        double extraPerGap = 0;

        // A line carrying a leader gives its slack to the leader (so trailing content reaches the end
        // edge); alignment/justification slack is suppressed. Otherwise the usual text-align applies.
        if (!line.HasLeader)
        {
            switch (align)
            {
                case TextAlign.Center:
                    startX = leftMpt + slack / 2;
                    break;
                case TextAlign.End:
                    startX = leftMpt + slack;
                    break;
                case TextAlign.Justify when !isLastLine && gaps > 0:
                    extraPerGap = slack / gaps;
                    break;
            }
        }

        EmitRuns(target, line, startX, baseline, extraPerGap, line.HasLeader ? slack : 0, effectiveHeight,
            lineTopMpt);
    }

    /// <summary>
    /// Positions the line's words left to right at <paramref name="startX"/>: emits coalesced text runs
    /// (consecutive words sharing style and link), expands any leader token to fill
    /// <paramref name="leaderSlackMpt"/> (painting its pattern), and records one <see cref="LinkArea"/>
    /// per contiguous run of words sharing the same link target.
    /// </summary>
    private void EmitRuns(IPrimitiveSink target, LineBox line, double startX, double baseline,
        double extraPerGap, double leaderSlackMpt, double lineHeightMpt, double lineTopMpt)
    {
        IReadOnlyList<StyledWord> words = line.Words;
        int count = words.Count;

        // Distribute the line's leftover space across its leader tokens (equally; the common case is a
        // single leader). A fixed-length leader keeps its length and does not draw from the slack pool.
        int expandingLeaders = 0;
        for (int k = 0; k < count; k++)
        {
            if (words[k].IsLeader && words[k].Leader!.Value.FixedLengthMpt is null)
            {
                expandingLeaders++;
            }
        }

        double perLeaderSlack = expandingLeaders > 0 ? leaderSlackMpt / expandingLeaders : 0;

        // Pass 1: assign each word a start x and an advance, recording link spans as we go.
        var positions = new double[count];
        var advances = new double[count];
        double x = startX;
        for (int j = 0; j < count; j++)
        {
            StyledWord word = words[j];
            if (j > 0)
            {
                // A word space precedes every word except a leader (a leader abuts its neighbours).
                if (!word.IsLeader && !words[j - 1].IsLeader)
                {
                    x += SpaceWidth(word.Font) + extraPerGap;
                }
            }

            positions[j] = x;
            double advance = word.IsLeader
                ? (word.Leader!.Value.FixedLengthMpt ?? perLeaderSlack)
                : MeasuredAdvance(word.Text, word.Font, word.LetterSpacingMpt);
            advances[j] = advance;
            x += advance;
        }

        // Pass 2: emit text runs (coalescing adjacent same-style, same-link, non-leader words) and
        // leader visuals.
        int runStart = 0;
        while (runStart < count)
        {
            StyledWord first = words[runStart];
            if (first.IsLeader)
            {
                EmitLeader(target, first, positions[runStart], advances[runStart], baseline);
                runStart++;
                continue;
            }

            int runEnd = runStart;
            while (runEnd + 1 < count && !words[runEnd + 1].IsLeader
                && SameStyleAndLink(words[runEnd + 1], first))
            {
                runEnd++;
            }

            var text = new StringBuilder();
            for (int j = runStart; j <= runEnd; j++)
            {
                if (j > runStart)
                {
                    text.Append(' ');
                }

                text.Append(words[j].Text);
            }

            // The decoration travels on the run as a trait: the renderer paints the lines after the
            // glyphs (overlaying them), exactly as FOP's renderTextDecoration does.
            target.Add(new TextRun(positions[runStart], baseline, text.ToString(), first.Font, first.Color,
                first.LetterSpacingMpt, first.Decoration));

            runStart = runEnd + 1;
        }

        // Pass 3: record a LinkArea over each contiguous span of words sharing the same link target.
        EmitLinkAreas(target, words, positions, advances, lineHeightMpt, lineTopMpt);
    }

    /// <summary>
    /// Paints an expandable leader across [<paramref name="leaderX"/>, leaderX + <paramref name="width"/>]:
    /// a rule is a thin <see cref="RectFill"/> centred on the baseline; a dots (or use-content) leader is
    /// a <see cref="TextRun"/> of repeated middle-dots in the leader font; a space leader paints nothing.
    /// </summary>
    private void EmitLeader(IPrimitiveSink target, StyledWord leader, double leaderX, double width,
        double baseline)
    {
        if (width <= 0)
        {
            return;
        }

        LeaderInfo info = leader.Leader!.Value;
        switch (info.Pattern)
        {
            case LeaderPattern.Rule:
                double thickness = Math.Max(0, info.RuleThicknessMpt);
                if (thickness > 0)
                {
                    // Centre the rule on the baseline.
                    target.Add(new RectFill(leaderX, baseline - thickness / 2, width, thickness, leader.Color));
                }

                break;

            case LeaderPattern.Dots:
            case LeaderPattern.UseContent:
                double dotWidth = measurer.MeasureWidthMpt(InlineContent.LeaderDot.ToString(), leader.Font);
                if (dotWidth > 0)
                {
                    int dots = (int)Math.Floor(width / dotWidth);
                    if (dots > 0)
                    {
                        target.Add(new TextRun(leaderX, baseline,
                            new string(InlineContent.LeaderDot, dots), leader.Font, leader.Color));
                    }
                }

                break;

            case LeaderPattern.Space:
            default:
                // Nothing visible: the leader is just the gap.
                break;
        }
    }

    /// <summary>
    /// Records one <see cref="LinkArea"/> per maximal run of consecutive words that share the same
    /// <see cref="LinkRef"/>, covering the run's inline extent over the full line box height.
    /// </summary>
    private static void EmitLinkAreas(IPrimitiveSink target, IReadOnlyList<StyledWord> words,
        double[] positions, double[] advances, double lineHeightMpt, double lineTopMpt)
    {
        int count = words.Count;
        int i = 0;
        while (i < count)
        {
            LinkRef? link = words[i].Link;
            if (link is null)
            {
                i++;
                continue;
            }

            int j = i;
            while (j + 1 < count && Nullable.Equals(words[j + 1].Link, link))
            {
                j++;
            }

            double left = positions[i];
            double right = positions[j] + advances[j];
            LinkRef dest = link.Value;
            target.Add(new LinkArea(left, lineTopMpt, Math.Max(0, right - left), lineHeightMpt,
                dest.TargetPageIndex, dest.Uri));
            i = j + 1;
        }
    }

    private double FontHeight(FontKey font) => measurer.AscenderMpt(font) + measurer.DescenderMpt(font);

    private double SpaceWidth(FontKey font) => measurer.MeasureWidthMpt(" ", font);

    private static bool SameStyle(StyledWord a, StyledWord b) =>
        a.Font.Equals(b.Font) && a.Color.Equals(b.Color);

    /// <summary>
    /// Whether two words share style, link target <em>and</em> decoration (so they coalesce into one
    /// run). Words with non-zero letter-spacing never coalesce: each is positioned individually so the
    /// renderer reproduces the exact per-glyph advance the layout used.
    /// </summary>
    private static bool SameStyleAndLink(StyledWord a, StyledWord b) =>
        SameStyle(a, b) && Nullable.Equals(a.Link, b.Link) && a.Decoration == b.Decoration
        && a.LetterSpacingMpt == 0 && b.LetterSpacingMpt == 0;

    /// <summary>
    /// A greedily-filled line: its words, natural (unjustified) width, content height, next word index,
    /// and whether it carries an expandable leader token.
    /// </summary>
    private readonly record struct LineBox(
        IReadOnlyList<StyledWord> Words, double NaturalWidth, double Height, int NextIndex, bool HasLeader);

    // ----- Primitive sinks ------------------------------------------------------------------

    /// <summary>
    /// A target that positioned primitives are written to. The flow path writes straight to the
    /// current <see cref="PageArea"/>; cell content is written to a relocatable
    /// <see cref="BufferedSink"/> so it can be measured then offset to its grid origin and replayed.
    /// </summary>
    private interface IPrimitiveSink
    {
        void Add(TextRun run);

        void Add(RectFill rect);

        void Add(ImageRun image);

        void Add(VectorPath vector);

        void Add(LinkArea link);
    }

    /// <summary>An <see cref="IPrimitiveSink"/> that appends straight onto a <see cref="PageArea"/>.</summary>
    private sealed class PageSink(PageArea page) : IPrimitiveSink
    {
        public void Add(TextRun run) => page.Add(run);

        public void Add(RectFill rect) => page.Add(rect);

        public void Add(ImageRun image) => page.Add(image);

        public void Add(VectorPath vector) => page.Add(vector);

        public void Add(LinkArea link) => page.Add(link);
    }

    /// <summary>An <see cref="IPrimitiveSink"/> that appends straight onto an <see cref="AreaGroup"/>.</summary>
    private sealed class GroupSink(AreaGroup group) : IPrimitiveSink
    {
        public void Add(TextRun run) => group.Add(run);

        public void Add(RectFill rect) => group.Add(rect);

        public void Add(ImageRun image) => group.Add(image);

        public void Add(VectorPath vector) => group.Add(vector);

        public void Add(LinkArea link) => group.Add(link);
    }

    /// <summary>
    /// An <see cref="IPrimitiveSink"/> that collects primitives in memory so they can later be
    /// translated by a fixed (dx, dy) and flushed to a page. Backgrounds/borders are buffered
    /// separately so they always replay before text/images (matching the page paint order).
    /// </summary>
    private sealed class BufferedSink : IPrimitiveSink
    {
        private readonly List<RectFill> rects = new();
        private readonly List<TextRun> runs = new();
        private readonly List<ImageRun> images = new();
        private readonly List<VectorPath> vectors = new();
        private readonly List<LinkArea> links = new();

        public void Add(TextRun run) => runs.Add(run);

        public void Add(RectFill rect) => rects.Add(rect);

        public void Add(ImageRun image) => images.Add(image);

        public void Add(VectorPath vector) => vectors.Add(vector);

        public void Add(LinkArea link) => links.Add(link);

        /// <summary>Replays the buffered primitives onto <paramref name="target"/>, offset by (dx, dy).</summary>
        public void FlushTo(IPrimitiveSink target, double dx, double dy)
        {
            foreach (RectFill r in rects)
            {
                target.Add(r with { XMpt = r.XMpt + dx, YMpt = r.YMpt + dy });
            }

            foreach (ImageRun img in images)
            {
                target.Add(img with { XMpt = img.XMpt + dx, YMpt = img.YMpt + dy });
            }

            foreach (VectorPath vector in vectors)
            {
                target.Add(vector with { Segments = TranslateSegments(vector.Segments, dx, dy) });
            }

            foreach (TextRun run in runs)
            {
                target.Add(run with { XMpt = run.XMpt + dx, BaselineYMpt = run.BaselineYMpt + dy });
            }

            foreach (LinkArea link in links)
            {
                target.Add(link with { XMpt = link.XMpt + dx, YMpt = link.YMpt + dy });
            }
        }

        /// <summary>Returns a copy of <paramref name="segments"/> with every point offset by (dx, dy).</summary>
        private static IReadOnlyList<PathSegment> TranslateSegments(
            IReadOnlyList<PathSegment> segments, double dx, double dy)
        {
            var moved = new List<PathSegment>(segments.Count);
            foreach (PathSegment seg in segments)
            {
                moved.Add(seg with
                {
                    X0 = seg.X0 + dx, Y0 = seg.Y0 + dy,
                    X1 = seg.X1 + dx, Y1 = seg.Y1 + dy,
                    X2 = seg.X2 + dx, Y2 = seg.Y2 + dy,
                });
            }

            return moved;
        }
    }

    // ----- Box emission (shared) ------------------------------------------------------------

    /// <summary>
    /// Emits the background fill and border edges for a border box at (<paramref name="leftMpt"/>,
    /// <paramref name="topMpt"/>) of the given size onto <paramref name="target"/>. Each border edge
    /// is a thin filled rectangle laid along its side of the box; an edge only paints when it is
    /// visible (width &gt; 0 and a paintable style).
    /// </summary>
    private static void EmitBox(IPrimitiveSink target, BoxProperties box, double leftMpt, double topMpt,
        double widthMpt, double heightMpt)
        => EmitBoxSegment(target, box, leftMpt, topMpt, widthMpt, heightMpt, paintTop: true, paintBottom: true);

    /// <summary>
    /// Emits one segment of a (possibly page-spanning) border box. <paramref name="paintTop"/> /
    /// <paramref name="paintBottom"/> control whether the top/bottom border edges paint on this
    /// segment: when a box is split across pages only the first segment paints the top border and only
    /// the last segment paints the bottom border, while the side borders and background paint on every
    /// segment. With both flags set this is the ordinary single-box case.
    /// </summary>
    private static void EmitBoxSegment(IPrimitiveSink target, BoxProperties box, double leftMpt,
        double topMpt, double widthMpt, double heightMpt, bool paintTop, bool paintBottom)
    {
        if (box.IsEmpty || widthMpt <= 0 || heightMpt <= 0)
        {
            return;
        }

        if (box.BackgroundColor is FopColor background)
        {
            target.Add(new RectFill(leftMpt, topMpt, widthMpt, heightMpt, background));
        }

        double rightMpt = leftMpt + widthMpt;
        double bottomMpt = topMpt + heightMpt;

        if (paintTop && box.BorderTop.IsVisible)
        {
            target.Add(new RectFill(leftMpt, topMpt, widthMpt, box.BorderTop.Width.Millipoints,
                box.BorderTop.Color));
        }

        if (paintBottom && box.BorderBottom.IsVisible)
        {
            double h = box.BorderBottom.Width.Millipoints;
            target.Add(new RectFill(leftMpt, bottomMpt - h, widthMpt, h, box.BorderBottom.Color));
        }

        if (box.BorderLeft.IsVisible)
        {
            target.Add(new RectFill(leftMpt, topMpt, box.BorderLeft.Width.Millipoints, heightMpt,
                box.BorderLeft.Color));
        }

        if (box.BorderRight.IsVisible)
        {
            double w = box.BorderRight.Width.Millipoints;
            target.Add(new RectFill(rightMpt - w, topMpt, w, heightMpt, box.BorderRight.Color));
        }
    }

    // ----- Block stacking + pagination ------------------------------------------------------

    /// <summary>
    /// Per-page-sequence layout state: holds the current page, the vertical cursor and the page
    /// geometry, and drives block stacking, line breaking and pagination.
    /// </summary>
    private sealed class FlowContext(LayoutEngine engine, PageGeometry geometry, AreaTree tree,
        int initialPageNumber)
    {
        private PageArea? page;
        private double cursorY = geometry.ContentTopMpt;

        /// <summary>
        /// The 1-based number of the page currently being filled. Starts at the sequence's
        /// initial-page-number and increments on each new page, so a <c>fo:page-number</c> in the body
        /// resolves to the page on which its containing block begins.
        /// </summary>
        private int currentPageNumber = initialPageNumber;

        /// <summary>
        /// The running per-class marker carryover for this sequence: the last marker of each class
        /// seen on this or any earlier page. Snapshotted into each new page's carryover record so a
        /// retrieve-marker on a page with no marker of the class can fall back to an earlier one.
        /// </summary>
        private readonly Dictionary<string, MarkerContent> markerCarryover = new(StringComparer.Ordinal);

        /// <summary>
        /// Resolves an <c>fo:retrieve-marker</c> (retrieve-class-name + retrieve-position) to the
        /// styled words to render, or <c>null</c> when none qualifies. Set only on the throwaway
        /// context used for static-content layout, where it is bound to that page's marker record;
        /// <c>null</c> in the body flow (a retrieve-marker in the body renders nothing).
        /// </summary>
        public Func<string, RetrievePosition, IReadOnlyList<StyledWord>?>? MarkerResolver { get; init; }

        // ----- Footnotes ------------------------------------------------------------------------
        //
        // Footnotes are placed at the bottom of the page their anchor lands on, above the region-after
        // band, stacked, with a thin separator rule above the area. The footnote area's height reduces
        // the body content height for that page via a greedily-computed reserve: when an anchor is
        // placed on the current page the body is laid into a buffer (at body width) to measure it, its
        // height (plus the separator on the first footnote) is added to the reserve, and the effective
        // content bottom shrinks. Pending bodies are flushed just above the region-after band when the
        // page is finalized (on pagination, and at the end of the sequence).

        /// <summary>Height reserved at the bottom of the current page for its footnotes (0 if none).</summary>
        private double footnoteReserveMpt;

        /// <summary>The footnote bodies queued for the current page, with their measured heights.</summary>
        private readonly List<(BufferedSink Body, double HeightMpt)> pendingFootnotes = new();

        /// <summary>
        /// The bottom of the body content rectangle, reduced by this page's footnote reserve. Body
        /// content paginates against this effective bottom, so reserving footnote space stops body
        /// content higher up the page.
        /// </summary>
        private double ContentBottomMpt => geometry.ContentBottomMpt - footnoteReserveMpt;

        /// <summary>
        /// Lays out the block-level children of a <see cref="FoStaticContent"/> into a relocatable
        /// buffer at the given content width, reusing the shared non-paginating block walk. The buffer
        /// is in band-local coordinates (origin at 0,0) and the caller flushes it to a page at the band
        /// origin. The page number this context was created with is used for any <c>fo:page-number</c>.
        /// </summary>
        public void LayOutStaticBlocks(FoStaticContent content, double widthMpt, BufferedSink buffer)
            => LayOutBlockLevelIntoBuffer(content.ChildObjects, buffer, widthMpt);

        /// <summary>The width of the thin separator rule drawn above the footnote area.</summary>
        private static readonly double SeparatorHeightMpt = FoLength.FromPoints(0.5).Millipoints;

        /// <summary>The vertical gap between the separator rule and the first footnote body.</summary>
        private static readonly double SeparatorGapMpt = FoLength.FromPoints(2).Millipoints;

        /// <summary>
        /// Reserves space at the bottom of the current page for <paramref name="footnote"/>'s body and
        /// queues the body for flushing when the page is finalized. The body is laid into a relocatable
        /// buffer at the body content width to measure it; its height (plus a separator rule + gap on
        /// the first footnote of the page) is added to the page's footnote reserve, shrinking the
        /// effective content bottom so subsequent body content paginates earlier.
        /// <para>
        /// TODO: the reserve is computed greedily, not iteratively. An anchor that lands very low on a
        /// page may, after the reserve shrinks the content bottom, force its own line onto the next page
        /// while the footnote stays reserved on this one; and a footnote body taller than the remaining
        /// space is still placed (it may overflow into the region-after band). The body is attributed to
        /// the page the containing block began on, not necessarily the exact anchor line's page.
        /// </para>
        /// </summary>
        private void ReserveFootnote(FoFootnote footnote)
        {
            EnsurePage();

            FoFootnoteBody? body = footnote.Body;
            if (body is null)
            {
                return;
            }

            var buffer = new BufferedSink();
            double height = LayOutBlockLevelIntoBuffer(body.BlockLevelChildren, buffer, geometry.ContentWidthMpt);

            // The footnote body is placed on the current (anchor's) page; record its ids so a citation
            // to footnote content resolves to this page.
            engine.RecordIdsInSubtree(body, currentPageNumber, IndexOfPage(page!));

            // The separator rule (+ gap) is added once, ahead of the first footnote on the page.
            if (pendingFootnotes.Count == 0)
            {
                footnoteReserveMpt += SeparatorHeightMpt + SeparatorGapMpt;
            }

            footnoteReserveMpt += height;
            pendingFootnotes.Add((buffer, height));
        }

        /// <summary>
        /// Flushes the current page's queued footnote bodies to the bottom of the page, just above the
        /// region-after band: a thin separator rule, then each body stacked downward. Resets the page's
        /// footnote reserve. A no-op when no footnotes are pending.
        /// </summary>
        private void FlushFootnotes()
        {
            if (pendingFootnotes.Count == 0 || page is null)
            {
                return;
            }

            var sink = new PageSink(page);

            // The footnote area sits at the bottom of the body content rectangle (its top is the body
            // bottom minus the reserve). The separator rule spans the body content width.
            double areaTop = geometry.ContentBottomMpt - footnoteReserveMpt;
            sink.Add(new RectFill(geometry.ContentLeftMpt, areaTop, geometry.ContentWidthMpt,
                SeparatorHeightMpt, FopColor.FromRgb(0, 0, 0)));

            double y = areaTop + SeparatorHeightMpt + SeparatorGapMpt;
            foreach ((BufferedSink bodyBuffer, double height) in pendingFootnotes)
            {
                bodyBuffer.FlushTo(sink, geometry.ContentLeftMpt, y);
                y += height;
            }

            pendingFootnotes.Clear();
            footnoteReserveMpt = 0;
        }

        /// <summary>Finalizes the current page, flushing any pending footnote bodies. Called at sequence end.</summary>
        public void FinishPage() => FlushFootnotes();

        /// <summary>Ensures a page exists, creating a blank one if none has been started yet.</summary>
        public void EnsurePage()
        {
            if (page is null)
            {
                StartNewPage();
            }
        }

        /// <summary>
        /// Applies a forced <c>break-before</c> on a flow-level object: starts a new page (unless the
        /// current page is still empty, in which case the break is a no-op) and, for even/odd breaks,
        /// inserts one more blank page when needed so the object lands on a page of the requested parity.
        /// </summary>
        public void ApplyBreak(BreakKind kind)
        {
            if (kind == BreakKind.Auto)
            {
                return;
            }

            // A break at the very top of a fresh page is a no-op (no spurious blank pages). A page that
            // has not been started yet, or whose cursor is still at the content top, is "fresh".
            bool atPageTop = page is null || cursorY <= geometry.ContentTopMpt;
            if (!atPageTop)
            {
                StartNewPage();
            }
            else
            {
                EnsurePage();
            }

            // Even/odd: insert blank pages until the current page number has the requested parity.
            while (NeedsParityBreak(kind))
            {
                StartNewPage();
            }
        }

        private bool NeedsParityBreak(BreakKind kind) => kind switch
        {
            BreakKind.EvenPage => currentPageNumber % 2 != 0,
            BreakKind.OddPage => currentPageNumber % 2 == 0,
            _ => false,
        };

        /// <summary>
        /// Lays out <paramref name="block"/> (and its nested blocks) at inline-progression offset
        /// <paramref name="leftMpt"/> (absolute page-left edge of the block's <em>border box</em>) with
        /// the available width <paramref name="widthMpt"/> for that border box.
        /// <para>
        /// The block's content is inset on every edge by border-width + padding: the inline-progression
        /// width is reduced accordingly and the vertical cursor advances by the top inset before content
        /// and the bottom inset after it (in addition to space-before/after). After the content extent is
        /// known, a background rectangle and up to four border edges are emitted covering the border box.
        /// </para>
        /// </summary>
        public void LayOutBlock(FoBlock block, double leftMpt, double widthMpt)
            => LayOutBlockInFlow(block, leftMpt, widthMpt);

        /// <summary>
        /// Lays out a block in the paginating main flow, honouring <c>keep-together.within-page</c>.
        /// A non-keep block uses the normal line-by-line walk (which paginates per line). A keep block
        /// is laid out into a relocatable buffer first so its full height is known, then placed whole:
        /// if it does not fit in the remaining space on the current page but would fit on an empty page,
        /// it is moved to a fresh page rather than split.
        /// <para>
        /// A kept block that does not fit even on an empty page cannot be honoured without overflowing,
        /// so it degrades gracefully: the keep is abandoned and the block is laid out with the normal
        /// line-by-line walk, which paginates across pages instead of overflowing a single one.
        /// </para>
        /// <para>
        /// TODO: a kept block placed via the buffer resolves any <c>fo:page-number</c> it contains to
        /// the page it lands on (correct for the common single-page case), and a kept block that fits on
        /// an empty page but carries a border that would itself span pages paints on a single page (no
        /// per-page box fragments for the buffered keep path). A single line/word taller than a full page
        /// still overflows even on the line-by-line fallback (the line-fill loop always places at least
        /// one line so it cannot stall).
        /// </para>
        /// </summary>
        private void LayOutBlockInFlow(FoBlock block, double leftMpt, double widthMpt)
        {
            if (block.KeepTogetherWithinPage != KeepStrength.Always)
            {
                LayOutBlock(block, leftMpt, widthMpt, FlowTarget.Instance);
                return;
            }

            EnsurePage();

            // Lay the whole block (space-before/after included) into a relocatable buffer to measure it.
            var buffer = new BufferedSink();
            var probe = new BufferTarget(buffer);
            LayOutBlock(block, leftMpt, widthMpt, probe);
            double blockHeight = probe.LocalCursor;

            bool atPageTop = cursorY <= geometry.ContentTopMpt;
            bool fitsHere = cursorY + blockHeight <= ContentBottomMpt;
            bool fitsOnEmptyPage = blockHeight <= geometry.ContentHeightMpt;

            if (!fitsOnEmptyPage)
            {
                // The block is taller than a full page: the keep cannot be satisfied without overflow.
                // Fall back to the normal line-by-line walk so it paginates (degrades) gracefully rather
                // than spilling off a single page. Discard the probe buffer and re-lay against the flow.
                LayOutBlock(block, leftMpt, widthMpt, FlowTarget.Instance);
                return;
            }

            if (!fitsHere && !atPageTop)
            {
                StartNewPage();
            }

            // Flush the buffered block at the current cursor. The buffer was laid out in absolute
            // inline (x) coordinates already, so only the vertical offset is applied.
            buffer.FlushTo(new PageSink(page!), 0, cursorY);
            cursorY += blockHeight;
        }

        /// <summary>
        /// The shared block-stacking walk. It applies space-before/after, insets, lays out inline
        /// content into lines and recurses into nested blocks/images/tables, emitting the border box
        /// after the content extent is known. The <see cref="IBlockTarget"/> abstracts the only thing
        /// that differs between the main flow and a buffered table cell: where primitives go and whether
        /// a line/image may trigger pagination.
        /// </summary>
        private void LayOutBlock(FoBlock block, double leftMpt, double widthMpt, IBlockTarget target)
        {
            target.Advance(this, block.SpaceBefore.Millipoints);

            // Record this block's id (and any inline-descendant ids) against the page it begins on, for
            // page-number-citation resolution. Only the paginating flow has a meaningful page number;
            // ids inside relocatable buffers (table cells, list items, footnote bodies) are recorded
            // separately when those buffers are placed on a page (see PlaceRow/LayOutListItem/
            // ReserveFootnote).
            if (target.CanPaginate)
            {
                EnsurePage();
                engine.RecordIdsOnPage(block, currentPageNumber, IndexOfPage(page!));

                // Record this block's fo:marker children against the page it begins on (for
                // retrieve-marker resolution), updating the sequence's running carryover. Markers are
                // only collected in the paginating flow -- a block laid into a relocatable buffer (a
                // table cell, footnote body, kept block) has no page to attribute them to.
                EnsurePage();
                engine.RecordMarkersOnPage(block, page!, markerCarryover);
            }

            BoxProperties box = block.Box;

            // Record where the border box starts (for box painting). If the content forces pagination
            // this start anchor still places the box on the page it began on.
            object startAnchor = target.BeginBox(this);
            double boxTop = target.Cursor(this);

            // The content box is inset from the border box by border-width + padding on every edge.
            double contentLeft = leftMpt + box.LeftInsetMpt;
            double contentWidth = Math.Max(0, widthMpt - box.LeftInsetMpt - box.RightInsetMpt);

            target.Advance(this, box.TopInsetMpt);

            // Inline content of this block (text + inlines), excluding nested blocks and images.
            // The page number is resolved to the page on which the containing block begins (best-effort;
            // a block that spans pages still uses its start page for any fo:page-number it contains).
            // Citations resolve via the engine's pass-1 map; footnotes encountered while flattening are
            // reserved at the current page bottom (flow path only -- a buffered cell has no page).
            var flatten = new FlattenContext
            {
                ResolveCitation = engine.ResolveCitation,
                ResolveInternalDestination = engine.ResolveInternalDestination,
                OnFootnote = target.CanPaginate ? ReserveFootnote : null,
                ResolveMarker = MarkerResolver,
            };
            List<StyledWord> words = InlineContent.Flatten(block, currentPageNumber, flatten);
            if (words.Count > 0)
            {
                LayOutLines(block, words, contentLeft, contentWidth, target);
            }

            // Recurse into nested blocks and images, narrowing the content rectangle by this block's indents.
            double childLeft = contentLeft + block.StartIndent.Millipoints;
            double childWidth = Math.Max(0,
                contentWidth - block.StartIndent.Millipoints - block.EndIndent.Millipoints);
            foreach (FObj child in block.ChildObjects)
            {
                // break-before/after only force pagination in the paginating main flow; inside a
                // relocatable buffer (table cell / list body) there is no page to break to.
                bool paginating = target == FlowTarget.Instance;
                switch (child)
                {
                    case FoBlock childBlock:
                        if (paginating)
                        {
                            ApplyBreak(childBlock.BreakBefore);
                            LayOutBlockInFlow(childBlock, childLeft, childWidth);
                            ApplyBreak(childBlock.BreakAfter);
                        }
                        else
                        {
                            LayOutBlock(childBlock, childLeft, childWidth, target);
                        }

                        break;
                    case FoExternalGraphic graphic:
                        LayOutImage(graphic, childLeft, childWidth, target);
                        break;
                    case FoInstreamForeignObject foreign:
                        LayOutInstreamForeignObject(foreign, childLeft, childWidth, target);
                        break;
                    case FoTable table:
                        // Tables flow both in the main region and inside a relocatable buffer (so a
                        // table nested in a block lays out via the same shared, target-driven mechanism).
                        // break-before/after only force pagination in the paginating main flow.
                        if (paginating)
                        {
                            ApplyBreak(table.BreakBefore);
                        }

                        LayOutTable(table, childLeft, childWidth, target);
                        if (paginating)
                        {
                            ApplyBreak(table.BreakAfter);
                        }

                        break;
                    case FoListBlock list:
                        // Lists flow both in the main region and inside a relocatable buffer (so a
                        // list nested in a list-item body lays out via the same shared mechanism).
                        if (paginating)
                        {
                            ApplyBreak(list.BreakBefore);
                        }

                        LayOutList(list, childLeft, childWidth, target);
                        if (paginating)
                        {
                            ApplyBreak(list.BreakAfter);
                        }

                        break;
                    case FoBlockContainer container:
                        if (paginating)
                        {
                            ApplyBreak(container.BreakBefore);
                            LayOutBlockContainer(container, childLeft, childWidth);
                            ApplyBreak(container.BreakAfter);
                        }
                        else
                        {
                            LayOutBlockContainer(container, childLeft, childWidth, target);
                        }

                        break;
                }
            }

            target.Advance(this, box.BottomInsetMpt);

            // Paint the border box behind the content. RectFills always render before text/images, so
            // emitting them now (after content) still places the background behind the content.
            target.EndBox(this, startAnchor, box, leftMpt, boxTop, widthMpt);

            target.Advance(this, block.SpaceAfter.Millipoints);
        }

        /// <summary>
        /// Lays out an <see cref="FoExternalGraphic"/> as a block-level box of its specified size,
        /// paginating (in the flow) if it would overflow the current page.
        /// </summary>
        private void LayOutImage(FoExternalGraphic graphic, double leftMpt, double widthMpt, IBlockTarget target)
        {
            BoxProperties box = graphic.Box;

            string source = graphic.Source;
            string? path = source.Length > 0 ? source : null;

            // Intrinsic size from the image (via the injected resolver, which reads pixel size + DPI).
            // When no resolver is set or the image cannot be read, fall back to a 72pt square so an
            // unsized graphic still reserves a visible, square placeholder area.
            double defaultSize = FoLength.FromPoints(72).Millipoints;
            ImageIntrinsics? intrinsic = engine.ImageResolver?.Resolve(path, bytes: null);
            double intrinsicWidth = intrinsic?.WidthMpt ?? defaultSize;
            double intrinsicHeight = intrinsic?.HeightMpt ?? defaultSize;

            // content-width/content-height (with scaling + percentages) over the intrinsic size.
            (double imageWidth, double imageHeight) = graphic.ResolveContentSize(intrinsicWidth, intrinsicHeight);

            double borderBoxWidth = imageWidth + box.LeftInsetMpt + box.RightInsetMpt;
            double borderBoxHeight = imageHeight + box.TopInsetMpt + box.BottomInsetMpt;

            target.Advance(this, graphic.Properties.GetLength("space-before", FoLength.Zero).Millipoints);

            IPrimitiveSink sink = target.SinkForAdvance(this, borderBoxHeight);
            double boxTop = target.Cursor(this);

            double imageX = leftMpt + box.LeftInsetMpt;
            double imageY = boxTop + box.TopInsetMpt;

            sink.Add(new ImageRun(imageX, imageY, imageWidth, imageHeight, path, SourceBytes: null));

            EmitBox(sink, box, leftMpt, boxTop, borderBoxWidth, borderBoxHeight);

            target.SetCursor(this, boxTop + borderBoxHeight);
            target.Advance(this, graphic.Properties.GetLength("space-after", FoLength.Zero).Millipoints);

            _ = widthMpt;
        }

        /// <summary>
        /// Lays out an <see cref="FoInstreamForeignObject"/> (an embedded SVG) as a block-level box. The
        /// SVG is parsed and flattened into vector paths/text sized to the object's content box; the box
        /// dimensions come from <c>content-width</c>/<c>content-height</c> (or <c>width</c>/<c>height</c>),
        /// falling back to the SVG's intrinsic size and finally a square placeholder. A graphic that
        /// fails to parse emits just its border box (an empty reserved area).
        /// </summary>
        private void LayOutInstreamForeignObject(FoInstreamForeignObject foreign, double leftMpt,
            double widthMpt, IBlockTarget target)
        {
            BoxProperties box = foreign.Box;
            SvgGraphic? graphic = SvgArea.TryParse(foreign.ForeignXml);

            double defaultSize = FoLength.FromPoints(72).Millipoints;
            double intrinsicW = graphic is { IntrinsicWidth: > 0 } ? graphic.IntrinsicWidth * 1000.0 : defaultSize;
            double intrinsicH = graphic is { IntrinsicHeight: > 0 } ? graphic.IntrinsicHeight * 1000.0 : defaultSize;

            double contentWidth = (foreign.ContentWidth ?? foreign.Width)?.Millipoints ?? intrinsicW;
            double contentHeight = (foreign.ContentHeight ?? foreign.Height)?.Millipoints ?? intrinsicH;

            double borderBoxWidth = contentWidth + box.LeftInsetMpt + box.RightInsetMpt;
            double borderBoxHeight = contentHeight + box.TopInsetMpt + box.BottomInsetMpt;

            target.Advance(this, foreign.SpaceBefore.Millipoints);

            IPrimitiveSink sink = target.SinkForAdvance(this, borderBoxHeight);
            double boxTop = target.Cursor(this);

            // Background/border behind the graphic.
            EmitBox(sink, box, leftMpt, boxTop, borderBoxWidth, borderBoxHeight);

            if (graphic is not null)
            {
                double graphicX = leftMpt + box.LeftInsetMpt;
                double graphicY = boxTop + box.TopInsetMpt;
                PlacedSvg placed = SvgArea.Place(graphic, graphicX, graphicY, contentWidth, contentHeight,
                    engine.Measurer);
                foreach (VectorPath path in placed.Paths)
                {
                    sink.Add(path);
                }

                foreach (TextRun run in placed.Texts)
                {
                    sink.Add(run);
                }
            }

            target.SetCursor(this, boxTop + borderBoxHeight);
            target.Advance(this, foreign.SpaceAfter.Millipoints);

            _ = widthMpt;
        }

        private void LayOutLines(FoBlock block, List<StyledWord> words, double leftMpt, double widthMpt,
            IBlockTarget target)
        {
            double lineHeight = block.LineHeightMpt;
            TextAlign align = block.TextAlign;

            // Choose the optimal break points for the whole paragraph (Knuth-Plass total-fit), then emit
            // each resulting line slice through the unchanged EmitLine machinery. Hyphenation candidates
            // are modelled inside the breaker as flagged penalties, so a word may be split mid-line when
            // that improves the paragraph; the chosen fragment is rendered with the hyphenation character
            // and the remainder carried to the next line by BreakIntoLines.
            List<LineBox> lines = engine.BreakIntoLines(block, words, widthMpt);
            for (int i = 0; i < lines.Count; i++)
            {
                LineBox line = lines[i];
                if (line.Words.Count == 0)
                {
                    continue;
                }

                bool isLastLine = i == lines.Count - 1;
                double advance = Math.Max(lineHeight, line.Height);

                IPrimitiveSink sink = target.SinkForAdvance(this, advance);
                engine.EmitLine(sink, line, leftMpt, widthMpt, lineHeight, target.Cursor(this), align, isLastLine);

                target.Advance(this, advance);
            }
        }

        // ----- Tables -----------------------------------------------------------------------

        /// <summary>
        /// Lays out an <see cref="FoTable"/> in the normal block flow at border-box left
        /// <paramref name="leftMpt"/> with the available width <paramref name="availableWidthMpt"/>.
        /// <para>
        /// Column widths are resolved from <c>fo:table-column</c> declarations (absolute, percentage of
        /// the table width, or proportional shares); columns with no usable width share the remaining
        /// space equally (or by proportional shares). Each cell's block children are laid out within the
        /// cell content width (spanned column widths minus the cell's border+padding) using the same
        /// block-layout logic as the main flow, into a relocatable buffer; the row height is the tallest
        /// cell (respecting the row <c>height</c> as a minimum). Rows are then positioned in the grid and
        /// paginated: a row that would overflow the region bottom starts a new page.
        /// </para>
        /// </summary>
        public void LayOutTable(FoTable table, double leftMpt, double availableWidthMpt)
            => LayOutTable(table, leftMpt, availableWidthMpt, FlowTarget.Instance);

        /// <summary>
        /// Lays out an <see cref="FoTable"/> against the given <see cref="IBlockTarget"/> -- the
        /// paginating main flow or a relocatable buffer (a table cell, list body, kept block, or static
        /// content). All vertical advancement and box painting go through the target, so the pagination
        /// behaviour is a property of the target: the flow target breaks rows across pages and repeats
        /// the header, while a buffer target simply advances a local cursor (a nested table contributes
        /// its measured height to the surrounding cell/item, exactly like a nested block does).
        /// </summary>
        private void LayOutTable(FoTable table, double leftMpt, double availableWidthMpt, IBlockTarget target)
        {
            target.Advance(this, table.SpaceBefore.Millipoints);

            BoxProperties tableBox = table.Box;
            double tableWidth = table.Width?.Millipoints ?? availableWidthMpt;
            tableWidth = Math.Min(tableWidth, availableWidthMpt);

            double contentLeft = leftMpt + tableBox.LeftInsetMpt;
            double gridWidth = Math.Max(0, tableWidth - tableBox.LeftInsetMpt - tableBox.RightInsetMpt);

            // Gather rows (header, then bodies, then footer) and the declared column count.
            List<RowModel> headerRows = CollectRows(table.Header);
            var bodyRows = new List<RowModel>();
            foreach (FoTableBody body in table.Bodies)
            {
                bodyRows.AddRange(CollectRows(body));
            }

            List<RowModel> footerRows = CollectRows(table.Footer);

            int maxCellColumns = 0;
            foreach (RowModel row in headerRows.Concat(bodyRows).Concat(footerRows))
            {
                int span = 0;
                foreach (FoTableCell cell in row.Cells)
                {
                    span += Math.Max(1, cell.NumberColumnsSpanned);
                }

                maxCellColumns = Math.Max(maxCellColumns, span);
            }

            double[] columnWidths = ResolveColumnWidths(table, gridWidth, maxCellColumns);

            // Capture the box anchor at the table top (a page in the flow, nothing in a buffer).
            object tableAnchor = target.BeginBox(this);
            double tableTop = target.Cursor(this);

            // Header rows repeat on each page. Lay them out once (as a grid) and re-emit per page (a
            // buffer target never paginates, so the header is emitted once at the top).
            // TODO: header repetition emits a fresh copy on every page; this is best-effort and does not
            // de-duplicate when a body fits entirely on the first page.
            List<LaidRow> laidHeader = LayOutRows(headerRows, columnWidths);
            double headerHeight = laidHeader.Sum(r => r.Height);

            foreach (LaidRow row in laidHeader)
            {
                PlaceRow(row, contentLeft, columnWidths, target);
            }

            foreach (LaidRow laid in LayOutRows(bodyRows, columnWidths))
            {
                PlaceRowPaginated(laid, contentLeft, columnWidths, laidHeader, headerHeight, target);
            }

            foreach (LaidRow laid in LayOutRows(footerRows, columnWidths))
            {
                PlaceRowPaginated(laid, contentLeft, columnWidths, laidHeader, headerHeight, target);
            }

            target.Advance(this, tableBox.BottomInsetMpt);

            // Paint the table's own border/background over the grid extent. In the flow the box is
            // segmented per page when the table spilled onto later pages (top border on the first
            // segment, bottom on the last, side borders/background on every segment); in a buffer it is
            // a single box covering the table's local extent.
            target.EndBox(this, tableAnchor, tableBox, leftMpt, tableTop,
                tableBox.LeftInsetMpt + gridWidth + tableBox.RightInsetMpt);

            target.Advance(this, table.SpaceAfter.Millipoints);
        }

        /// <summary>
        /// Places a laid-out row at the current cursor, paginating first (in the flow) if it would
        /// overflow the region bottom and, when a new page is started, re-emitting the header rows.
        /// Against a buffer target this never paginates -- the row is simply emitted at the local cursor.
        /// </summary>
        private void PlaceRowPaginated(LaidRow row, double contentLeft, double[] columnWidths,
            List<LaidRow> header, double headerHeight, IBlockTarget target)
        {
            // SinkForAdvance paginates the flow target if the row would overflow the page; the buffer
            // target returns its buffer unchanged. When the flow target moved to a fresh page the cursor
            // is now at the content top, which is how we detect that the header should be repeated.
            bool wasAtPageTop = target.Cursor(this) <= geometry.ContentTopMpt;
            _ = target.SinkForAdvance(this, row.Height);
            bool movedToNewPage = target.CanPaginate && !wasAtPageTop
                && target.Cursor(this) <= geometry.ContentTopMpt;

            if (movedToNewPage && header.Count > 0
                && target.Cursor(this) + headerHeight <= ContentBottomMpt)
            {
                foreach (LaidRow h in header)
                {
                    EmitRowAt(target.SinkForAdvance(this, h.Height), h, contentLeft, target.Cursor(this), columnWidths);
                    target.Advance(this, h.Height);
                }
            }

            PlaceRow(row, contentLeft, columnWidths, target);
        }

        /// <summary>Emits a laid-out row at the current cursor (no pagination) and advances the cursor.</summary>
        private void PlaceRow(LaidRow row, double contentLeft, double[] columnWidths, IBlockTarget target)
        {
            IPrimitiveSink sink = target.SinkForAdvance(this, row.Height);

            // Record the ids inside each cell against the page the row lands on, so a
            // page-number-citation referencing content in a table cell resolves (the cell's whole
            // buffer is placed on this page). Only the paginating flow has a page to attribute to.
            if (target.CanPaginate)
            {
                EnsurePage();
                int pageNumber = currentPageNumber;
                int pageIndex = IndexOfPage(page!);
                foreach (LaidCell cell in row.Cells)
                {
                    engine.RecordIdsInSubtree(cell.Source, pageNumber, pageIndex);
                }
            }

            EmitRowAt(sink, row, contentLeft, target.Cursor(this), columnWidths);
            target.Advance(this, row.Height);
        }

        /// <summary>
        /// Emits one laid-out row onto <paramref name="sink"/> at (<paramref name="contentLeft"/>,
        /// <paramref name="rowTop"/>): each cell's box (background + borders) then its buffered content
        /// offset to its content origin.
        /// </summary>
        private static void EmitRowAt(IPrimitiveSink sink, LaidRow row, double contentLeft, double rowTop,
            double[] columnWidths)
        {
            // Row background/border spanning the whole row width.
            double rowWidth = 0;
            for (int c = 0; c < columnWidths.Length; c++)
            {
                rowWidth += columnWidths[c];
            }

            EmitBox(sink, row.Box, contentLeft, rowTop, rowWidth, row.Height);

            foreach (LaidCell cell in row.Cells)
            {
                double cellLeft = contentLeft + ColumnOffset(columnWidths, cell.StartColumn);
                double cellWidth = SpannedWidth(columnWidths, cell.StartColumn, cell.ColumnSpan);

                // A cell's border box fills its full (row- and column-spanned) extent so a spanning
                // cell covers the rows below it and adjacent cell borders align.
                // TODO: a row-spanning cell whose extent crosses a page break is painted at its origin
                // page only (it may overflow that page's region bottom); per-page span fragments are
                // future work.
                double cellHeight = cell.SpannedHeightMpt > 0 ? cell.SpannedHeightMpt : row.Height;
                EmitBox(sink, cell.Box, cellLeft, rowTop, cellWidth, cellHeight);

                double contentX = cellLeft + cell.Box.LeftInsetMpt;
                double contentY = rowTop + cell.Box.TopInsetMpt;
                cell.Content.FlushTo(sink, contentX, contentY);
            }
        }

        /// <summary>
        /// Lays out all rows of a table part as a grid, honouring <c>number-columns-spanned</c> and
        /// <c>number-rows-spanned</c>. Each cell is assigned a grid origin that skips any slots already
        /// occupied by a cell spanning down from an earlier row; row band heights are computed from the
        /// cells that fit within a single row, then grown so the rows a spanning cell covers are jointly
        /// tall enough; finally each spanning cell records the combined height it should be painted with.
        /// </summary>
        private List<LaidRow> LayOutRows(IReadOnlyList<RowModel> rows, double[] columnWidths)
        {
            int columnCount = columnWidths.Length;
            int rowCount = rows.Count;

            // occupiedUntil[c] = the first row index at which column c becomes free again.
            var occupiedUntil = new int[columnCount];

            // Per-origin-row cells, and the spanning metadata needed to resolve heights afterwards.
            var perRow = new List<LaidCell>[rowCount];
            var spanInfo = new List<(LaidCell Cell, int StartRow)>();
            var rowHeights = new double[rowCount];

            for (int r = 0; r < rowCount; r++)
            {
                perRow[r] = new List<LaidCell>();
                rowHeights[r] = rows[r].MinHeightMpt;
            }

            for (int r = 0; r < rowCount; r++)
            {
                int autoColumn = 0;
                foreach (FoTableCell cell in rows[r].Cells)
                {
                    int colSpan = Math.Max(1, cell.NumberColumnsSpanned);
                    int rowSpan = Math.Max(1, cell.NumberRowsSpanned);

                    // Resolve the start column: an explicit column-number, else the next free slot,
                    // skipping any columns still occupied by a cell spanning down from an earlier row.
                    int startColumn = cell.ColumnNumber.HasValue ? cell.ColumnNumber.Value - 1 : autoColumn;
                    startColumn = Math.Clamp(startColumn, 0, Math.Max(0, columnCount - 1));
                    while (startColumn < columnCount && occupiedUntil[startColumn] > r)
                    {
                        startColumn++;
                    }

                    startColumn = Math.Clamp(startColumn, 0, Math.Max(0, columnCount - 1));
                    colSpan = Math.Min(colSpan, Math.Max(1, columnCount - startColumn));
                    rowSpan = Math.Min(rowSpan, Math.Max(1, rowCount - r));

                    double cellWidth = SpannedWidth(columnWidths, startColumn, colSpan);
                    BoxProperties cellBox = cell.Box;
                    double contentWidth = Math.Max(0, cellWidth - cellBox.LeftInsetMpt - cellBox.RightInsetMpt);

                    // A cell's content is any block-level children (blocks, nested tables, lists),
                    // laid into a relocatable buffer via the shared non-paginating walk.
                    var buffer = new BufferedSink();
                    double consumed = LayOutBlockLevelIntoBuffer(cell.BlockLevelChildren, buffer, contentWidth);
                    double cellHeight = consumed + cellBox.TopInsetMpt + cellBox.BottomInsetMpt;

                    var laid = new LaidCell(startColumn, colSpan, rowSpan, cellBox, buffer, consumed, cell);
                    perRow[r].Add(laid);

                    if (rowSpan == 1)
                    {
                        rowHeights[r] = Math.Max(rowHeights[r], cellHeight);
                    }
                    else
                    {
                        spanInfo.Add((laid, r));
                    }

                    // Mark the spanned grid slots occupied for the rows this cell covers.
                    for (int c = startColumn; c < startColumn + colSpan && c < columnCount; c++)
                    {
                        occupiedUntil[c] = r + rowSpan;
                    }

                    autoColumn = startColumn + colSpan;
                }
            }

            // Grow rows so each spanning cell's covered rows are jointly tall enough; if short, the
            // shortfall is added to the cell's last spanned row.
            foreach (var (cell, startRow) in spanInfo)
            {
                int lastRow = Math.Min(rowCount - 1, startRow + cell.RowSpan - 1);
                double needed = cell.ContentHeightMpt + cell.Box.TopInsetMpt + cell.Box.BottomInsetMpt;
                double have = 0;
                for (int r = startRow; r <= lastRow; r++)
                {
                    have += rowHeights[r];
                }

                if (needed > have)
                {
                    rowHeights[lastRow] += needed - have;
                }
            }

            // Resolve each cell's painted (spanned) height now that all row heights are final.
            var laidRows = new List<LaidRow>(rowCount);
            for (int r = 0; r < rowCount; r++)
            {
                foreach (LaidCell cell in perRow[r])
                {
                    int lastRow = Math.Min(rowCount - 1, r + cell.RowSpan - 1);
                    double h = 0;
                    for (int rr = r; rr <= lastRow; rr++)
                    {
                        h += rowHeights[rr];
                    }

                    cell.SpannedHeightMpt = h;
                }

                laidRows.Add(new LaidRow(perRow[r], rowHeights[r], rows[r].Box));
            }

            return laidRows;
        }

        /// <summary>
        /// Lays out a sequence of block-level objects (blocks, nested tables and list-blocks) within a
        /// fixed content width into a relocatable buffer, returning the consumed height. This is the one
        /// non-paginating block walk shared by table cells, list label/body content, static content and
        /// kept blocks: each child is dispatched to the same target-driven
        /// <see cref="LayOutBlock(FoBlock, double, double, IBlockTarget)"/> /
        /// <see cref="LayOutTable(FoTable, double, double, IBlockTarget)"/> /
        /// <see cref="LayOutList(FoListBlock, double, double, IBlockTarget)"/> core that the main flow
        /// uses, but against a <see cref="BufferTarget"/> whose local cursor never paginates. The buffer is in
        /// content-local coordinates (origin at 0,0), so arbitrary nesting (a table in a cell, a list in
        /// a cell, a table in a list-item body) just works.
        /// </summary>
        private double LayOutBlockLevelIntoBuffer(IEnumerable<FObj> children, BufferedSink buffer, double widthMpt)
        {
            var target = new BufferTarget(buffer);
            foreach (FObj child in children)
            {
                switch (child)
                {
                    case FoBlock block:
                        LayOutBlock(block, 0, widthMpt, target);
                        break;
                    case FoTable table:
                        LayOutTable(table, 0, widthMpt, target);
                        break;
                    case FoListBlock list:
                        LayOutList(list, 0, widthMpt, target);
                        break;
                    case FoBlockContainer container:
                        LayOutBlockContainer(container, 0, widthMpt, target);
                        break;
                    case FoInstreamForeignObject foreign:
                        LayOutInstreamForeignObject(foreign, 0, widthMpt, target);
                        break;
                }
            }

            return target.LocalCursor;
        }

        // ----- Lists ------------------------------------------------------------------------

        /// <summary>
        /// Lays out an <see cref="FoListBlock"/> in the main block flow at border-box left
        /// <paramref name="leftMpt"/> with the available width <paramref name="availableWidthMpt"/>.
        /// </summary>
        public void LayOutList(FoListBlock list, double leftMpt, double availableWidthMpt)
            => LayOutList(list, leftMpt, availableWidthMpt, FlowTarget.Instance);

        /// <summary>
        /// Lays out an <see cref="FoListBlock"/> against the given <see cref="IBlockTarget"/> (the main
        /// flow or a relocatable buffer, so nested lists "just work"). Items are stacked vertically with
        /// their space-before/after; each item's label and body blocks are laid out into separate
        /// relocatable buffers using the same shared block-layout walk the table cells use.
        /// <para>
        /// Within an item the label column starts at the item start edge and is
        /// <c>provisional-distance-between-starts - provisional-label-separation</c> wide; the body
        /// starts at <c>start + provisional-distance-between-starts</c>. The item height is the taller of
        /// the two columns. A list honours its <c>start-indent</c> (so nested lists indent), and breaks
        /// between items when an item would overflow the region bottom (in the paginating flow target).
        /// </para>
        /// </summary>
        private void LayOutList(FoListBlock list, double leftMpt, double availableWidthMpt, IBlockTarget target)
        {
            target.Advance(this, list.SpaceBefore.Millipoints);

            BoxProperties listBox = list.Box;

            object listAnchor = target.BeginBox(this);
            double listTop = target.Cursor(this);

            // The list's content box is inset by its own border + padding, then by start/end indent.
            double contentLeft = leftMpt + listBox.LeftInsetMpt + list.StartIndent.Millipoints;
            double contentWidth = Math.Max(0, availableWidthMpt
                - listBox.LeftInsetMpt - listBox.RightInsetMpt
                - list.StartIndent.Millipoints - list.EndIndent.Millipoints);

            target.Advance(this, listBox.TopInsetMpt);

            double distance = list.ProvisionalDistanceBetweenStarts.Millipoints;
            double separation = list.ProvisionalLabelSeparation.Millipoints;
            double labelWidth = Math.Max(0, distance - separation);
            double bodyLeftOffset = distance;
            double bodyWidth = Math.Max(0, contentWidth - distance);

            foreach (FoListItem item in list.Items)
            {
                LayOutListItem(item, contentLeft, labelWidth, bodyLeftOffset, bodyWidth, target);
            }

            target.Advance(this, listBox.BottomInsetMpt);
            target.EndBox(this, listAnchor, listBox, leftMpt, listTop, availableWidthMpt);
            target.Advance(this, list.SpaceAfter.Millipoints);
        }

        /// <summary>
        /// Lays out a single <see cref="FoListItem"/>: label into the label column and body into the body
        /// column (both relocatable buffers), positions them at the item top, and paginates the item as a
        /// unit. The item height is the taller of its label and body, plus the item's own insets.
        /// </summary>
        private void LayOutListItem(FoListItem item, double contentLeft, double labelWidth,
            double bodyLeftOffset, double bodyWidth, IBlockTarget target)
        {
            target.Advance(this, item.SpaceBefore.Millipoints);

            BoxProperties itemBox = item.Box;
            double innerLeft = contentLeft + itemBox.LeftInsetMpt;
            double bodyLeft = innerLeft + bodyLeftOffset;

            // The label and body content widths are reduced by the item's insets only on the side they
            // touch the item border box; the gap between them is fixed by the provisional distance.
            double labelContentWidth = Math.Max(0, labelWidth - itemBox.LeftInsetMpt);
            double bodyContentWidth = Math.Max(0, bodyWidth - itemBox.RightInsetMpt);

            var labelBuffer = new BufferedSink();
            double labelHeight = item.Label is { } label
                ? LayOutBlockLevelIntoBuffer(label.BlockLevelChildren, labelBuffer, labelContentWidth)
                : 0;

            var bodyBuffer = new BufferedSink();
            double bodyHeight = item.Body is { } body
                ? LayOutBlockLevelIntoBuffer(body.BlockLevelChildren, bodyBuffer, bodyContentWidth)
                : 0;

            double contentHeight = Math.Max(labelHeight, bodyHeight);
            double itemHeight = contentHeight + itemBox.TopInsetMpt + itemBox.BottomInsetMpt;

            // Place the whole item as a unit, paginating first if it would overflow the region bottom.
            // TODO: a single item taller than the region is not split; it overflows the page (consistent
            // with the current block/table behaviour). Knuth-style item splitting is future work.
            IPrimitiveSink sink = target.SinkForAdvance(this, itemHeight);
            double itemTop = target.Cursor(this);

            // Record the ids in the item's label/body (buffered) against the page it lands on, so a
            // citation to content inside a list item resolves. Flow-only (a buffer has no page).
            if (target.CanPaginate)
            {
                EnsurePage();
                engine.RecordIdsInSubtree(item, currentPageNumber, IndexOfPage(page!));
            }

            // The item's own border box spans the full content width (label column + gap + body column),
            // plus the item's own insets. The inner content width is bodyLeftOffset + bodyWidth (the
            // provisional distance carries the label column and the label separation).
            double itemBorderWidth = bodyLeftOffset + bodyWidth + itemBox.LeftInsetMpt + itemBox.RightInsetMpt;
            EmitBox(sink, itemBox, contentLeft, itemTop, itemBorderWidth, itemHeight);

            double innerTop = itemTop + itemBox.TopInsetMpt;
            labelBuffer.FlushTo(sink, innerLeft, innerTop);
            bodyBuffer.FlushTo(sink, bodyLeft, innerTop);

            target.SetCursor(this, itemTop + itemHeight);
            target.Advance(this, item.SpaceAfter.Millipoints);
        }

        // ----- Block containers -------------------------------------------------------------

        /// <summary>
        /// Lays out an <see cref="FoBlockContainer"/> in the paginating main flow at border-box left
        /// <paramref name="leftMpt"/> with the available width <paramref name="availableWidthMpt"/>.
        /// </summary>
        public void LayOutBlockContainer(FoBlockContainer container, double leftMpt, double availableWidthMpt)
            => LayOutBlockContainer(container, leftMpt, availableWidthMpt, FlowTarget.Instance);

        /// <summary>
        /// Lays out an <see cref="FoBlockContainer"/> against the given <see cref="IBlockTarget"/>.
        /// <para>
        /// When <c>absolute-position</c> is <c>absolute</c> or <c>fixed</c> the container is taken out of
        /// the normal flow: its content is laid into a relocatable buffer at the resolved content width
        /// and placed at (<c>left</c>, <c>top</c>) without advancing the target's cursor, so following
        /// flow content starts exactly where it would have without the container. <c>fixed</c> resolves
        /// its offset relative to the page; <c>absolute</c> relative to the current region/content area.
        /// </para>
        /// <para>
        /// When <c>absolute-position</c> is <c>auto</c> (the default) the container flows like a normal
        /// block, honouring its <c>width</c> (a width smaller than the available measure shrinks the
        /// content), and advances the cursor by its measured height.
        /// </para>
        /// <para>
        /// In both cases a non-zero <c>reference-orientation</c> rotates the container's content (and the
        /// box) about the border box's top-left corner by laying it into a buffer at the pre-rotation
        /// width and emitting it as a transform group (so the renderer rotates positions and glyphs).
        /// </para>
        /// </summary>
        private void LayOutBlockContainer(FoBlockContainer container, double leftMpt, double availableWidthMpt,
            IBlockTarget target)
        {
            if (container.AbsolutePosition is AbsolutePosition.Absolute or AbsolutePosition.Fixed)
            {
                LayOutAbsoluteContainer(container, target);
                return;
            }

            LayOutFlowContainer(container, leftMpt, availableWidthMpt, target);
        }

        /// <summary>
        /// Lays out a normal-flow (<c>absolute-position="auto"</c>) block-container: the container's box
        /// border-box width is its specified <c>width</c> clamped to the available width (or the full
        /// available width when <c>auto</c>); its content is laid into a buffer at the inset content
        /// width and placed at the current cursor, advancing it by the box height (the taller of the
        /// content height and a specified <c>height</c>). A non-zero reference-orientation rotates the
        /// box+content about its top-left.
        /// </summary>
        private void LayOutFlowContainer(FoBlockContainer container, double leftMpt, double availableWidthMpt,
            IBlockTarget target)
        {
            target.Advance(this, container.SpaceBefore.Millipoints);

            BoxProperties box = container.Box;
            double indent = container.StartIndent.Millipoints;
            double boxLeft = leftMpt + indent;
            double available = Math.Max(0, availableWidthMpt - indent - container.EndIndent.Millipoints);

            // The border-box width: the specified width (clamped to available) or the full available width.
            double borderWidth = container.Width is { } w ? Math.Min(w.Millipoints, available) : available;
            double contentWidth = Math.Max(0, borderWidth - box.LeftInsetMpt - box.RightInsetMpt);

            // Lay the content into a buffer at content-local origin (0,0), then size the box.
            var contentBuffer = new BufferedSink();
            double contentHeight = LayOutBlockLevelIntoBuffer(container.BlockLevelChildren, contentBuffer, contentWidth);
            double borderHeight = ResolveBorderHeight(container, box, contentHeight);

            IPrimitiveSink sink = target.SinkForAdvance(this, RotatedAdvance(borderWidth, borderHeight,
                container.ReferenceOrientation));
            double boxTop = target.Cursor(this);

            PlaceContainer(sink, container, box, contentBuffer, boxLeft, boxTop, borderWidth, borderHeight);

            target.Advance(this, RotatedAdvance(borderWidth, borderHeight, container.ReferenceOrientation));
            target.Advance(this, container.SpaceAfter.Millipoints);
        }

        /// <summary>
        /// Lays out an out-of-flow (<c>absolute</c>/<c>fixed</c>) block-container. The container does NOT
        /// advance the flow cursor: its content is laid into a buffer at the resolved content width and
        /// placed on the current page at the resolved (left, top) origin.
        /// <para>
        /// Simplification: <c>fixed</c> resolves its offset relative to the page top-left, while
        /// <c>absolute</c> resolves relative to the current content area's top-left. With single regions
        /// these coincide except for the region/body margins. <c>right</c>/<c>bottom</c> are honoured
        /// only to derive the origin when <c>left</c>/<c>top</c> are absent (origin = reference extent
        /// minus the offset minus the box size); when neither edge is given the offset defaults to 0.
        /// </para>
        /// </summary>
        private void LayOutAbsoluteContainer(FoBlockContainer container, IBlockTarget target)
        {
            // The container must land on a page; an out-of-flow container in a relocatable buffer (e.g.
            // inside a table cell) has no page, so it is placed against the buffer at its own origin.
            BoxProperties box = container.Box;
            bool fixedPos = container.AbsolutePosition == AbsolutePosition.Fixed;

            // The reference area: the page (fixed) or the body content area (absolute).
            double refLeft = fixedPos ? 0 : geometry.ContentLeftMpt;
            double refTop = fixedPos ? 0 : geometry.ContentTopMpt;
            double refWidth = fixedPos ? geometry.PageWidthMpt : geometry.ContentWidthMpt;
            double refHeight = fixedPos ? geometry.PageHeightMpt : geometry.ContentHeightMpt;

            // Content/border width: specified width, else the reference width minus the left offset.
            double leftOffset = container.Left?.Millipoints ?? 0;
            double borderWidth = container.Width?.Millipoints
                ?? Math.Max(0, refWidth - leftOffset);
            double contentWidth = Math.Max(0, borderWidth - box.LeftInsetMpt - box.RightInsetMpt);

            var contentBuffer = new BufferedSink();
            double contentHeight = LayOutBlockLevelIntoBuffer(container.BlockLevelChildren, contentBuffer, contentWidth);
            double borderHeight = ResolveBorderHeight(container, box, contentHeight);

            // Resolve the placement origin. left/top win; else right/bottom anchor from the far edge.
            double placeLeft = container.Left is { } l
                ? refLeft + l.Millipoints
                : container.Right is { } r
                    ? refLeft + refWidth - r.Millipoints - borderWidth
                    : refLeft;
            double placeTop = container.Top is { } t
                ? refTop + t.Millipoints
                : container.Bottom is { } b
                    ? refTop + refHeight - b.Millipoints - borderHeight
                    : refTop;

            // An out-of-flow container is placed on the current page without moving any flow cursor (the
            // flow target's nor a buffer target's): it is removed from the normal flow, so following
            // content starts exactly where it would have without the container. The placement origin is
            // page/region relative, so it always targets the current page directly.
            // Simplification: an out-of-flow container nested inside a relocatable buffer (e.g. a table
            // cell) is still placed against the current page at the page/region-relative origin, not
            // relative to the enclosing cell.
            _ = target;
            var sink = new PageSink(PageForContainer());
            PlaceContainer(sink, container, box, contentBuffer, placeLeft, placeTop, borderWidth, borderHeight);
        }

        /// <summary>Ensures a page exists for an out-of-flow container and returns it (without paginating).</summary>
        private PageArea PageForContainer()
        {
            EnsurePage();
            return page!;
        }

        /// <summary>
        /// Places a container's box (background/border) and its buffered content onto
        /// <paramref name="sink"/>, at border-box origin (<paramref name="boxLeft"/>,
        /// <paramref name="boxTop"/>). With no rotation the box and content are emitted directly (flat
        /// primitives). With a non-zero reference-orientation the box and content are emitted into an
        /// <see cref="AreaGroup"/> rotated about the border box's top-left corner, then the group is added
        /// to the current page (groups are page-level; a rotated container in a buffer also targets the page).
        /// </summary>
        private void PlaceContainer(IPrimitiveSink sink, FoBlockContainer container, BoxProperties box,
            BufferedSink contentBuffer, double boxLeft, double boxTop, double borderWidth, double borderHeight)
        {
            int rotation = container.ReferenceOrientation;
            if (rotation == 0)
            {
                // Flat path: box behind the content, content inset from the border box.
                EmitBox(sink, box, boxLeft, boxTop, borderWidth, borderHeight);
                contentBuffer.FlushTo(sink, boxLeft + box.LeftInsetMpt, boxTop + box.TopInsetMpt);
                return;
            }

            // Rotated path: build a group whose local origin is the border box top-left. Box and content
            // are emitted in local coordinates (box at 0,0; content inset), and the group carries the
            // translation to (boxLeft, boxTop) plus the rotation. Groups are page-level.
            var group = new AreaGroup(boxLeft, boxTop, rotation);
            var groupSink = new GroupSink(group);
            EmitBox(groupSink, box, 0, 0, borderWidth, borderHeight);
            contentBuffer.FlushTo(groupSink, box.LeftInsetMpt, box.TopInsetMpt);
            PageForContainer().Add(group);
        }

        /// <summary>
        /// The border-box height of a container: the specified <c>height</c> (border box) when given,
        /// else the content height plus the box's top/bottom insets.
        /// </summary>
        private static double ResolveBorderHeight(FoBlockContainer container, BoxProperties box, double contentHeight)
            => container.Height is { } h
                ? h.Millipoints
                : contentHeight + box.TopInsetMpt + box.BottomInsetMpt;

        /// <summary>
        /// The vertical extent a rotated container occupies in the flow: a 90/270 rotation about the
        /// top-left turns the box's width into its vertical extent, so the flow advance becomes the
        /// border width; 0/180 keep the border height.
        /// </summary>
        private static double RotatedAdvance(double borderWidth, double borderHeight, int rotationDegrees)
            => rotationDegrees is 90 or 270 ? borderWidth : borderHeight;

        // ----- Block-target abstraction -----------------------------------------------------

        /// <summary>
        /// Abstracts the differences between laying out a block into the paginating main flow and into a
        /// relocatable cell buffer: the vertical cursor, where primitives go, and whether an advance may
        /// trigger pagination. Methods take the owning <see cref="FlowContext"/> so the flow target can
        /// read/write its page cursor.
        /// </summary>
        private interface IBlockTarget
        {
            /// <summary>Whether this target paginates (the main flow does; a relocatable buffer does not).</summary>
            bool CanPaginate { get; }

            double Cursor(FlowContext ctx);

            void SetCursor(FlowContext ctx, double value);

            void Advance(FlowContext ctx, double delta);

            /// <summary>Returns the sink to emit a piece of content of the given height onto, paginating first if needed.</summary>
            IPrimitiveSink SinkForAdvance(FlowContext ctx, double advance);

            /// <summary>Captures the anchor (page + nothing for buffers) at the box's top, before content.</summary>
            object BeginBox(FlowContext ctx);

            /// <summary>Emits the box's background/borders using the anchor captured by <see cref="BeginBox"/>.</summary>
            void EndBox(FlowContext ctx, object anchor, BoxProperties box, double leftMpt, double boxTop, double widthMpt);
        }

        /// <summary>The main-flow target: writes to the current page and paginates per line/image.</summary>
        private sealed class FlowTarget : IBlockTarget
        {
            public static readonly FlowTarget Instance = new();

            public bool CanPaginate => true;

            public double Cursor(FlowContext ctx) => ctx.cursorY;

            public void SetCursor(FlowContext ctx, double value) => ctx.cursorY = value;

            public void Advance(FlowContext ctx, double delta) => ctx.cursorY += delta;

            public IPrimitiveSink SinkForAdvance(FlowContext ctx, double advance)
                => new PageSink(ctx.PageForLine(advance));

            public object BeginBox(FlowContext ctx)
            {
                ctx.EnsurePage();
                return ctx.page!;
            }

            public void EndBox(FlowContext ctx, object anchor, BoxProperties box, double leftMpt, double boxTop,
                double widthMpt)
            {
                var startPage = (PageArea)anchor;

                // A bordered block that splits across pages paints a box segment on each page it covers:
                // top border only on the first page, bottom border only on the last, side borders and
                // background on every segment.
                ctx.EmitBoxAcrossPages(startPage, ctx.page!, box, leftMpt, boxTop, widthMpt, ctx.cursorY);
            }
        }

        /// <summary>The cell-buffer target: writes to a buffer with a local, non-paginating cursor.</summary>
        private sealed class BufferTarget(BufferedSink buffer) : IBlockTarget
        {
            public double LocalCursor { get; private set; }

            public bool CanPaginate => false;

            public double Cursor(FlowContext ctx) => LocalCursor;

            public void SetCursor(FlowContext ctx, double value) => LocalCursor = value;

            public void Advance(FlowContext ctx, double delta) => LocalCursor += delta;

            public IPrimitiveSink SinkForAdvance(FlowContext ctx, double advance) => buffer;

            public object BeginBox(FlowContext ctx) => buffer;

            public void EndBox(FlowContext ctx, object anchor, BoxProperties box, double leftMpt, double boxTop,
                double widthMpt) => EmitBox(buffer, box, leftMpt, boxTop, widthMpt, LocalCursor - boxTop);
        }

        /// <summary>
        /// Returns the page the next line should be placed on, paginating first if a line of height
        /// <paramref name="advance"/> would overflow the bottom of the body region. A line at the very
        /// top of a region is never pushed forward (it would loop forever on an over-tall line).
        /// </summary>
        private PageArea PageForLine(double advance)
        {
            EnsurePage();
            if (cursorY + advance > ContentBottomMpt && cursorY > geometry.ContentTopMpt)
            {
                StartNewPage();
            }

            return page!;
        }

        private void StartNewPage()
        {
            // Finalize the page being left: flush its footnote bodies to the page bottom (this also
            // clears the footnote reserve so the new page starts with the full content height).
            FlushFootnotes();

            // The first page keeps the initial page number; each subsequent page increments it.
            if (page is not null)
            {
                currentPageNumber++;
            }

            page = new PageArea(geometry.PageWidthMpt, geometry.PageHeightMpt);
            tree.AddPage(page);
            cursorY = geometry.ContentTopMpt;

            // Snapshot the running carryover (markers from earlier pages of this sequence) into the new
            // page's record, so a retrieve-marker on this page can fall back to an earlier marker even
            // when nothing of its class starts here.
            Dictionary<string, MarkerContent> carryover = engine.MarkersFor(page).Carryover;
            foreach (var (cls, content) in markerCarryover)
            {
                carryover[cls] = content;
            }
        }

        /// <summary>
        /// Paints a border box that began at <paramref name="boxTop"/> on <paramref name="startPage"/>
        /// and ends at <paramref name="boxBottom"/> on <paramref name="endPage"/>. When the box stays on
        /// one page this paints a single box; when it spans pages it paints one segment per page using
        /// that page's content band (full from the box top to the region bottom on the first page, the
        /// whole region on any middle pages, and the region top down to the box bottom on the last),
        /// with the top border only on the first segment and the bottom border only on the last.
        /// </summary>
        private void EmitBoxAcrossPages(PageArea startPage, PageArea endPage, BoxProperties box,
            double leftMpt, double boxTop, double widthMpt, double boxBottom)
        {
            if (box.IsEmpty || widthMpt <= 0)
            {
                return;
            }

            int startIndex = IndexOfPage(startPage);
            int endIndex = IndexOfPage(endPage);
            if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex)
            {
                // Single page (or pages not both attached): one box from top to bottom.
                EmitBox(new PageSink(startPage), box, leftMpt, boxTop, widthMpt, boxBottom - boxTop);
                return;
            }

            for (int i = startIndex; i <= endIndex; i++)
            {
                PageArea seg = tree.Pages[i];
                bool first = i == startIndex;
                bool last = i == endIndex;
                double segTop = first ? boxTop : geometry.ContentTopMpt;
                double segBottom = last ? boxBottom : geometry.ContentBottomMpt;
                EmitBoxSegment(new PageSink(seg), box, leftMpt, segTop, widthMpt, segBottom - segTop,
                    paintTop: first, paintBottom: last);
            }
        }

        private int IndexOfPage(PageArea target)
        {
            for (int i = 0; i < tree.Pages.Count; i++)
            {
                if (ReferenceEquals(tree.Pages[i], target))
                {
                    return i;
                }
            }

            return -1;
        }
    }

    // ----- Table model helpers --------------------------------------------------------------

    /// <summary>A row of cells with its minimum height and box, in source order.</summary>
    private sealed record RowModel(IReadOnlyList<FoTableCell> Cells, double MinHeightMpt, BoxProperties Box);

    /// <summary>
    /// A laid-out cell: its grid position/span (columns and rows), box, buffered content and content
    /// height. <paramref name="RowSpan"/> is how many rows the cell covers; its painted box height (the
    /// combined height of the rows it spans) is resolved once all row heights are known and stored in
    /// <see cref="SpannedHeightMpt"/>.
    /// </summary>
    private sealed record LaidCell(
        int StartColumn, int ColumnSpan, int RowSpan, BoxProperties Box, BufferedSink Content,
        double ContentHeightMpt, FoTableCell Source)
    {
        /// <summary>The painted box height (spanned rows combined); set after row heights are resolved.</summary>
        public double SpannedHeightMpt { get; set; }
    }

    /// <summary>A laid-out row: the cells that <em>originate</em> in it, its final band height and box.</summary>
    private sealed record LaidRow(IReadOnlyList<LaidCell> Cells, double Height, BoxProperties Box);

    /// <summary>
    /// Collects the rows of a table part. Supports both the wrapped form (<c>fo:table-row</c>) and the
    /// wrapper-less form (cells directly under the part, delimited by <c>starts-row</c>/<c>ends-row</c>).
    /// </summary>
    private static List<RowModel> CollectRows(FoTablePart? part)
    {
        var rows = new List<RowModel>();
        if (part is null)
        {
            return rows;
        }

        if (!part.HasDirectCells)
        {
            foreach (FoTableRow row in part.Rows)
            {
                rows.Add(new RowModel(row.Cells.ToList(), row.Height?.Millipoints ?? 0, row.Box));
            }

            return rows;
        }

        // Wrapper-less: group consecutive cells into rows on starts-row/ends-row boundaries.
        var current = new List<FoTableCell>();
        foreach (FoTableCell cell in part.DirectCells)
        {
            if (cell.StartsRow && current.Count > 0)
            {
                rows.Add(new RowModel(current, 0, default));
                current = new List<FoTableCell>();
            }

            current.Add(cell);

            if (cell.EndsRow)
            {
                rows.Add(new RowModel(current, 0, default));
                current = new List<FoTableCell>();
            }
        }

        if (current.Count > 0)
        {
            rows.Add(new RowModel(current, 0, default));
        }

        return rows;
    }

    /// <summary>
    /// Resolves the per-column widths in millipoints. Declared <c>fo:table-column</c> widths (absolute,
    /// percentage of the table width, or proportional shares) are honoured; columns without a usable
    /// width, and any extra columns implied by cell spans beyond the declarations, share the remaining
    /// space equally (or by the total proportional share when proportional columns are present).
    /// </summary>
    private static double[] ResolveColumnWidths(FoTable table, double gridWidthMpt, int maxCellColumns)
    {
        // Map declared columns to slots honouring column-number and number-columns-repeated.
        var specs = new List<ColumnWidthSpec>();
        int nextSlot = 0;
        foreach (FoTableColumn column in table.Columns)
        {
            int start = column.ColumnNumber.HasValue ? column.ColumnNumber.Value - 1 : nextSlot;
            int repeat = Math.Max(1, column.NumberColumnsRepeated);
            ColumnWidthSpec spec = column.ColumnWidth;
            for (int r = 0; r < repeat; r++)
            {
                int slot = start + r;
                while (specs.Count <= slot)
                {
                    specs.Add(ColumnWidthSpec.Auto);
                }

                specs[slot] = spec;
            }

            nextSlot = start + repeat;
        }

        int columnCount = Math.Max(specs.Count, maxCellColumns);
        if (columnCount == 0)
        {
            columnCount = 1;
        }

        while (specs.Count < columnCount)
        {
            specs.Add(ColumnWidthSpec.Auto);
        }

        var widths = new double[columnCount];
        double usedFixed = 0;
        double totalProportional = 0;
        int autoCount = 0;

        for (int i = 0; i < columnCount; i++)
        {
            ColumnWidthSpec spec = specs[i];
            double? resolved = spec.ResolveMpt(gridWidthMpt);
            if (resolved is double mpt)
            {
                widths[i] = Math.Max(0, mpt);
                usedFixed += widths[i];
            }
            else if (spec.Kind == ColumnWidthKind.Proportional)
            {
                totalProportional += spec.Value;
            }
            else
            {
                autoCount++;
            }
        }

        double remaining = Math.Max(0, gridWidthMpt - usedFixed);

        if (totalProportional > 0)
        {
            // Proportional shares split the remaining space; any auto columns get an equal share too,
            // counted as one share each so the grid still fills the width.
            double shareUnit = remaining / (totalProportional + autoCount);
            for (int i = 0; i < columnCount; i++)
            {
                ColumnWidthSpec spec = specs[i];
                if (spec.Kind == ColumnWidthKind.Proportional)
                {
                    widths[i] = shareUnit * spec.Value;
                }
                else if (spec.Kind == ColumnWidthKind.Auto)
                {
                    widths[i] = shareUnit;
                }
            }
        }
        else if (autoCount > 0)
        {
            double each = remaining / autoCount;
            for (int i = 0; i < columnCount; i++)
            {
                if (specs[i].Kind == ColumnWidthKind.Auto)
                {
                    widths[i] = each;
                }
            }
        }

        return widths;
    }

    /// <summary>The left offset of column <paramref name="index"/> within the grid, in millipoints.</summary>
    private static double ColumnOffset(double[] columnWidths, int index)
    {
        double offset = 0;
        for (int i = 0; i < index && i < columnWidths.Length; i++)
        {
            offset += columnWidths[i];
        }

        return offset;
    }

    /// <summary>The summed width of <paramref name="span"/> columns starting at <paramref name="start"/>.</summary>
    private static double SpannedWidth(double[] columnWidths, int start, int span)
    {
        double width = 0;
        for (int i = start; i < start + span && i < columnWidths.Length; i++)
        {
            width += columnWidths[i];
        }

        return width;
    }
}
