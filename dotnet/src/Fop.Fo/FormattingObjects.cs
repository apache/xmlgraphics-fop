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

namespace Fop.Fo;

/// <summary>The document root, <c>fo:root</c>.</summary>
public sealed class FoRoot(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "root";

    /// <summary>The layout-master-set, if present.</summary>
    public FoLayoutMasterSet? LayoutMasterSet => ChildObjects.OfType<FoLayoutMasterSet>().FirstOrDefault();

    /// <summary>The page sequences in document order.</summary>
    public IEnumerable<FoPageSequence> PageSequences => ChildObjects.OfType<FoPageSequence>();
}

/// <summary>The set of page masters, <c>fo:layout-master-set</c>.</summary>
public sealed class FoLayoutMasterSet(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "layout-master-set";

    /// <summary>Finds a simple-page-master by its <c>master-name</c>.</summary>
    public FoSimplePageMaster? GetSimplePageMaster(string masterName) =>
        ChildObjects.OfType<FoSimplePageMaster>().FirstOrDefault(m => m.MasterName == masterName);
}

/// <summary>A simple page master, <c>fo:simple-page-master</c>.</summary>
public sealed class FoSimplePageMaster(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "simple-page-master";

    /// <summary>The <c>master-name</c>.</summary>
    public string MasterName => Properties.GetString("master-name", string.Empty);

    /// <summary>The page width (default A4, 210mm).</summary>
    public FoLength PageWidth => Properties.GetLength("page-width", FoLength.FromPoints(595.276));

    /// <summary>The page height (default A4, 297mm).</summary>
    public FoLength PageHeight => Properties.GetLength("page-height", FoLength.FromPoints(841.89));

    /// <summary>Top margin.</summary>
    public FoLength MarginTop => Properties.GetLength("margin-top", FoLength.Zero);

    /// <summary>Bottom margin.</summary>
    public FoLength MarginBottom => Properties.GetLength("margin-bottom", FoLength.Zero);

    /// <summary>Left margin.</summary>
    public FoLength MarginLeft => Properties.GetLength("margin-left", FoLength.Zero);

    /// <summary>Right margin.</summary>
    public FoLength MarginRight => Properties.GetLength("margin-right", FoLength.Zero);

    /// <summary>The body region, if present.</summary>
    public FoRegionBody? RegionBody => ChildObjects.OfType<FoRegionBody>().FirstOrDefault();

    /// <summary>The before (top) region, if present.</summary>
    public FoRegionBefore? RegionBefore => ChildObjects.OfType<FoRegionBefore>().FirstOrDefault();

    /// <summary>The after (bottom) region, if present.</summary>
    public FoRegionAfter? RegionAfter => ChildObjects.OfType<FoRegionAfter>().FirstOrDefault();

    /// <summary>The start (left, in lr-tb) side region, if present.</summary>
    public FoRegionStart? RegionStart => ChildObjects.OfType<FoRegionStart>().FirstOrDefault();

    /// <summary>The end (right, in lr-tb) side region, if present.</summary>
    public FoRegionEnd? RegionEnd => ChildObjects.OfType<FoRegionEnd>().FirstOrDefault();
}

/// <summary>The body region, <c>fo:region-body</c>.</summary>
public sealed class FoRegionBody(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "region-body";

    /// <summary>Top margin within the page.</summary>
    public FoLength MarginTop => Properties.GetLength("margin-top", FoLength.Zero);

    /// <summary>Bottom margin within the page.</summary>
    public FoLength MarginBottom => Properties.GetLength("margin-bottom", FoLength.Zero);

    /// <summary>Left margin within the page.</summary>
    public FoLength MarginLeft => Properties.GetLength("margin-left", FoLength.Zero);

    /// <summary>Right margin within the page.</summary>
    public FoLength MarginRight => Properties.GetLength("margin-right", FoLength.Zero);
}

/// <summary>
/// The before (top) region, <c>fo:region-before</c>. Its <c>extent</c> is the height of the band
/// reserved at the top of the page (below the page top margin) for running header content.
/// </summary>
public sealed class FoRegionBefore(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "region-before";

    /// <summary>The region's extent (its band height), defaulting to zero.</summary>
    public FoLength Extent => Properties.GetLength("extent", FoLength.Zero);
}

/// <summary>
/// The after (bottom) region, <c>fo:region-after</c>. Its <c>extent</c> is the height of the band
/// reserved at the bottom of the page (above the page bottom margin) for running footer content.
/// </summary>
public sealed class FoRegionAfter(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "region-after";

    /// <summary>The region's extent (its band height), defaulting to zero.</summary>
    public FoLength Extent => Properties.GetLength("extent", FoLength.Zero);
}

/// <summary>
/// The start side region, <c>fo:region-start</c> (the left vertical band in the default lr-tb
/// writing mode). Its <c>extent</c> is the <em>width</em> of the band reserved down the start edge of
/// the page, between the region-before and region-after bands.
/// </summary>
public sealed class FoRegionStart(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "region-start";

    /// <summary>The region's extent (its band width), defaulting to zero.</summary>
    public FoLength Extent => Properties.GetLength("extent", FoLength.Zero);
}

/// <summary>
/// The end side region, <c>fo:region-end</c> (the right vertical band in the default lr-tb writing
/// mode). Its <c>extent</c> is the <em>width</em> of the band reserved down the end edge of the page,
/// between the region-before and region-after bands.
/// </summary>
public sealed class FoRegionEnd(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "region-end";

    /// <summary>The region's extent (its band width), defaulting to zero.</summary>
    public FoLength Extent => Properties.GetLength("extent", FoLength.Zero);
}

/// <summary>A page sequence, <c>fo:page-sequence</c>.</summary>
public sealed class FoPageSequence(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "page-sequence";

    /// <summary>The <c>master-reference</c> naming the page master to use.</summary>
    public string MasterReference => Properties.GetString("master-reference", string.Empty);

    /// <summary>The main flow, if present.</summary>
    public FoFlow? Flow => ChildObjects.OfType<FoFlow>().FirstOrDefault();

    /// <summary>
    /// The <c>initial-page-number</c> as a 1-based page number, or <c>null</c> when not specified
    /// (the layout engine then defaults to 1). Only an explicit integer is honoured; the keyword
    /// forms (<c>auto</c>/<c>auto-odd</c>/<c>auto-even</c>) are treated as unspecified.
    /// </summary>
    public int? InitialPageNumber
    {
        get
        {
            string? raw = Properties.GetRaw("initial-page-number");
            if (raw is not null
                && int.TryParse(raw.Trim(), System.Globalization.NumberStyles.Integer,
                    System.Globalization.CultureInfo.InvariantCulture, out int value)
                && value >= 1)
            {
                return value;
            }

            return null;
        }
    }

    /// <summary>The static-content children, in document order.</summary>
    public IEnumerable<FoStaticContent> StaticContents => ChildObjects.OfType<FoStaticContent>();

    /// <summary>Returns the static-content whose <c>flow-name</c> targets <paramref name="flowName"/>, if any.</summary>
    public FoStaticContent? GetStaticContent(string flowName) =>
        StaticContents.FirstOrDefault(s => s.FlowName == flowName);
}

/// <summary>A flow of content, <c>fo:flow</c>.</summary>
public sealed class FoFlow(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "flow";

    /// <summary>The <c>flow-name</c> (the region this flow targets).</summary>
    public string FlowName => Properties.GetString("flow-name", "xsl-region-body");
}

/// <summary>
/// Static (repeated) content for a non-body region, <c>fo:static-content</c>. Like <see cref="FoFlow"/>
/// it is a container of block-level children, but its content is laid out into a page region (e.g.
/// <c>xsl-region-before</c>/<c>xsl-region-after</c>) on every page of the sequence.
/// </summary>
public sealed class FoStaticContent(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "static-content";

    /// <summary>The <c>flow-name</c> (the region this static content targets).</summary>
    public string FlowName => Properties.GetString("flow-name", string.Empty);
}

/// <summary>A block, <c>fo:block</c>.</summary>
public sealed class FoBlock(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "block";

    /// <summary>Space before the block (optimum).</summary>
    public FoLength SpaceBefore =>
        Properties.GetLength("space-before", Properties.GetLength("space-before.optimum", FoLength.Zero));

    /// <summary>Space after the block (optimum).</summary>
    public FoLength SpaceAfter =>
        Properties.GetLength("space-after", Properties.GetLength("space-after.optimum", FoLength.Zero));

    /// <summary>Start indent.</summary>
    public FoLength StartIndent => Properties.GetLength("start-indent", FoLength.Zero);

    /// <summary>End indent.</summary>
    public FoLength EndIndent => Properties.GetLength("end-indent", FoLength.Zero);

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>
    /// The <c>fo:marker</c> children of this block, in document order. Markers carry running-header
    /// content that is <em>not</em> rendered in place; the layout engine records them per page so a
    /// matching <c>fo:retrieve-marker</c> in a region's static content can render them.
    /// </summary>
    public IEnumerable<FoMarker> Markers => ChildObjects.OfType<FoMarker>();
}

/// <summary>An inline, <c>fo:inline</c>.</summary>
public sealed class FoInline(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "inline";
}

/// <summary>
/// A basic link, <c>fo:basic-link</c>. An inline-level container whose children flow inline as
/// ordinary content (typically styled as a link) and whose extent becomes a clickable region. The
/// link targets either an internal area by its <see cref="InternalDestination"/> (a <c>ref-id</c>,
/// resolved during layout to the page that id lands on) or an external <see cref="ExternalDestination"/>
/// URI. When both are set the internal destination takes precedence (matching FOP).
/// </summary>
public sealed class FoBasicLink(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "basic-link";

    /// <summary>
    /// The <c>internal-destination</c>: the <c>id</c>/<c>ref-id</c> of the in-document area to link to,
    /// or the empty string when unset. Resolved to a page during layout.
    /// </summary>
    public string InternalDestination => Properties.GetString("internal-destination", string.Empty).Trim();

    /// <summary>
    /// The <c>external-destination</c> URI with any <c>url(...)</c> wrapper and surrounding quotes
    /// stripped, or the empty string when unset.
    /// </summary>
    public string ExternalDestination => UnwrapUri(Properties.GetString("external-destination", string.Empty));

    /// <summary>Strips a CSS-style <c>url(...)</c> wrapper and surrounding quotes from a URI value.</summary>
    internal static string UnwrapUri(string value)
    {
        string trimmed = value.Trim();
        if (trimmed.StartsWith("url(", StringComparison.OrdinalIgnoreCase) && trimmed.EndsWith(')'))
        {
            trimmed = trimmed[4..^1].Trim();
        }

        return trimmed.Trim('\'', '"');
    }
}

/// <summary>The <c>leader-pattern</c> of an <see cref="FoLeader"/>: what fills the leader's width.</summary>
public enum LeaderPattern
{
    /// <summary>An invisible gap (the default).</summary>
    Space,

    /// <summary>A horizontal rule (a thin line) across the leader width.</summary>
    Rule,

    /// <summary>Repeated dots across the leader width (the classic TOC dot leader).</summary>
    Dots,

    /// <summary>
    /// Repeated user content. Not modelled in this cut; treated like <see cref="Dots"/> so a leader
    /// declared <c>use-content</c> still fills visibly.
    /// </summary>
    UseContent,
}

/// <summary>
/// A leader, <c>fo:leader</c>. An inline-level token that expands to consume the remaining inline
/// space on its line, pushing the content that follows it toward the end edge (the classic TOC
/// "Title .......... 42"). Its <see cref="LeaderPattern"/> selects the visible fill: dots, a rule, or
/// an invisible gap.
/// </summary>
public sealed class FoLeader(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "leader";

    /// <summary>The resolved <c>leader-pattern</c>, defaulting to <see cref="LeaderPattern.Space"/>.</summary>
    public LeaderPattern Pattern =>
        Properties.GetString("leader-pattern", string.Empty).Trim().ToLowerInvariant() switch
        {
            "rule" => LeaderPattern.Rule,
            "dots" => LeaderPattern.Dots,
            "use-content" => LeaderPattern.UseContent,
            _ => LeaderPattern.Space,
        };

    /// <summary>
    /// The fixed <c>leader-length</c> (its optimum), or <c>null</c> when unset (the leader then
    /// expands to fill the remaining inline space). The <c>.optimum</c> component is honoured when
    /// present, else the shorthand.
    /// </summary>
    public FoLength? LeaderLength
    {
        get
        {
            string? raw = Properties.GetRaw("leader-length.optimum") ?? Properties.GetRaw("leader-length");
            if (raw is null)
            {
                return null;
            }

            string trimmed = raw.Trim();
            if (trimmed.Length == 0 || trimmed.Contains('%'))
            {
                // A percentage leader-length has no inline-progression base here; treat as expanding.
                return null;
            }

            return FoLength.TryParse(trimmed, Properties.FontSizeMpt);
        }
    }

    /// <summary>The <c>rule-thickness</c> for a rule leader, defaulting to 1pt.</summary>
    public FoLength RuleThickness => Properties.GetLength("rule-thickness", FoLength.FromPoints(1));
}

/// <summary>
/// The retrieve-position of an <c>fo:retrieve-marker</c>: which qualifying marker of the class on (or
/// carried over to) the page is rendered.
/// </summary>
public enum RetrievePosition
{
    /// <summary>The first marker of the class that starts within the page (the default).</summary>
    FirstStartingWithinPage,

    /// <summary>
    /// The first marker of the class that starts within the page, falling back to the last marker
    /// of the class that was carried over (started on an earlier page) when none starts on this page.
    /// </summary>
    FirstIncludingCarryover,

    /// <summary>The last marker of the class that starts within the page.</summary>
    LastStartingWithinPage,

    /// <summary>The last marker of the class that ends within the page (approximated as last-starting here).</summary>
    LastEndingWithinPage,
}

/// <summary>
/// A marker, <c>fo:marker</c>. A child of a flow formatting object (typically a block) that holds
/// block/inline content which is <em>not</em> rendered in place; instead it is recorded per page so a
/// matching <see cref="FoRetrieveMarker"/> in a region's static content can render it (a running
/// "current chapter" header). Block-level marker content is rendered as inline text for this cut.
/// </summary>
public sealed class FoMarker(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "marker";

    /// <summary>The <c>marker-class-name</c> grouping this marker with its retrieving counterpart.</summary>
    public string MarkerClassName => Properties.GetString("marker-class-name", string.Empty);
}

/// <summary>
/// A retrieve-marker, <c>fo:retrieve-marker</c>. Appears in a region's static content (a header/footer
/// or side band) and renders, in place, the content of the matching <see cref="FoMarker"/> (by
/// <see cref="RetrieveClassName"/>) for the page being laid out, selected per
/// <see cref="RetrievePosition"/>. Resolves to empty when no marker of the class qualifies.
/// </summary>
public sealed class FoRetrieveMarker(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "retrieve-marker";

    /// <summary>The <c>retrieve-class-name</c> naming the marker class to render.</summary>
    public string RetrieveClassName => Properties.GetString("retrieve-class-name", string.Empty);

    /// <summary>
    /// The <c>retrieve-position</c>, defaulting to <see cref="RetrievePosition.FirstStartingWithinPage"/>.
    /// Both the FO 1.0 keywords (e.g. <c>first-starting-within-page</c>) and the
    /// <c>retrieve-boundary</c>-era spellings are accepted.
    /// </summary>
    public RetrievePosition RetrievePosition =>
        Properties.GetString("retrieve-position", string.Empty).Trim().ToLowerInvariant() switch
        {
            "first-including-carryover" => RetrievePosition.FirstIncludingCarryover,
            "last-starting-within-page" => RetrievePosition.LastStartingWithinPage,
            "last-ending-within-page" => RetrievePosition.LastEndingWithinPage,
            _ => RetrievePosition.FirstStartingWithinPage,
        };
}

/// <summary>
/// The current page number, <c>fo:page-number</c>. An empty inline-level FO whose rendered text is
/// the 1-based number of the page on which it is laid out (resolved during layout, not here).
/// </summary>
public sealed class FoPageNumber(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "page-number";
}

/// <summary>
/// A page-number citation, <c>fo:page-number-citation</c>. An empty inline-level FO whose rendered
/// text is the page number of the page on which the area with id <see cref="RefId"/> was generated
/// (resolved during layout, not here). An unresolved <c>ref-id</c> renders as "?".
/// </summary>
public sealed class FoPageNumberCitation(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "page-number-citation";

    /// <summary>The <c>ref-id</c> naming the referenced formatting object's <c>id</c>.</summary>
    public string RefId => Properties.GetString("ref-id", string.Empty);
}

/// <summary>
/// A last-page-number citation, <c>fo:page-number-citation-last</c>. An empty inline-level FO whose
/// rendered text is the number of the last page on which any area with id <see cref="RefId"/> was
/// generated. With the engine's flat (single-page-per-id) model this is the same page recorded for
/// the id, so it renders identically to <see cref="FoPageNumberCitation"/>; an unresolved
/// <c>ref-id</c> renders as "?".
/// </summary>
public sealed class FoPageNumberCitationLast(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "page-number-citation-last";

    /// <summary>The <c>ref-id</c> naming the referenced formatting object's <c>id</c>.</summary>
    public string RefId => Properties.GetString("ref-id", string.Empty);
}

/// <summary>
/// A footnote, <c>fo:footnote</c>. An inline-level FO whose content is an inline anchor (typically an
/// <c>fo:inline</c>) that flows inline in the body text, plus an <see cref="FoFootnoteBody"/> whose
/// block content is laid out at the bottom of the page on which the anchor lands.
/// </summary>
public sealed class FoFootnote(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "footnote";

    /// <summary>The footnote body (the block content shown at the page bottom), if present.</summary>
    public FoFootnoteBody? Body => ChildObjects.OfType<FoFootnoteBody>().FirstOrDefault();

    /// <summary>
    /// The inline anchor children: every direct child <em>except</em> the footnote-body. These flow
    /// inline in the body text where the footnote occurs (typically a single <c>fo:inline</c>).
    /// </summary>
    public IEnumerable<FObj> AnchorChildren => ChildObjects.Where(c => c is not FoFootnoteBody);
}

/// <summary>
/// A footnote body, <c>fo:footnote-body</c>. A block container whose block-level children are laid
/// out at the bottom of the anchor's page, above the region-after band.
/// </summary>
public sealed class FoFootnoteBody(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "footnote-body";

    /// <summary>
    /// The block-level children of this body in document order: blocks, nested tables and
    /// list-blocks. A footnote body is normally one or more blocks.
    /// </summary>
    public IEnumerable<FObj> BlockLevelChildren =>
        ChildObjects.Where(c => c is FoBlock or FoTable or FoListBlock);
}

/// <summary>An external image, <c>fo:external-graphic</c>.</summary>
public sealed class FoExternalGraphic(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "external-graphic";

    /// <summary>The <c>src</c> URI/path of the image (the <c>url(...)</c> wrapper, if any, stripped).</summary>
    public string Source => UnwrapUri(Properties.GetString("src", string.Empty));

    /// <summary>
    /// The specified content width (<c>content-width</c>), or <c>null</c> for <c>auto</c>/unset
    /// (use the intrinsic width).
    /// </summary>
    public FoLength? ContentWidth => ParseSizeProperty("content-width");

    /// <summary>
    /// The specified content height (<c>content-height</c>), or <c>null</c> for <c>auto</c>/unset
    /// (use the intrinsic height).
    /// </summary>
    public FoLength? ContentHeight => ParseSizeProperty("content-height");

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    private FoLength? ParseSizeProperty(string name)
    {
        string? raw = Properties.GetRaw(name);
        if (raw is null)
        {
            return null;
        }

        string trimmed = raw.Trim();
        if (trimmed.Length == 0
            || trimmed.Equals("auto", StringComparison.OrdinalIgnoreCase)
            || trimmed.Equals("scale-to-fit", StringComparison.OrdinalIgnoreCase))
        {
            return null;
        }

        return FoLength.TryParse(trimmed, Properties.FontSizeMpt);
    }

    private static string UnwrapUri(string value)
    {
        string trimmed = value.Trim();
        if (trimmed.StartsWith("url(", StringComparison.OrdinalIgnoreCase) && trimmed.EndsWith(')'))
        {
            trimmed = trimmed[4..^1].Trim();
        }

        return trimmed.Trim('\'', '"');
    }
}

/// <summary>A neutral wrapper, <c>fo:wrapper</c>, and the fallback for unmodelled elements.</summary>
public sealed class FoGeneric(PropertyList properties, string localName) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName { get; } = localName;
}
