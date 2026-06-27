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
}

/// <summary>An inline, <c>fo:inline</c>.</summary>
public sealed class FoInline(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "inline";
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
