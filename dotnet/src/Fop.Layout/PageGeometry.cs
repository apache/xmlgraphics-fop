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

using Fop.Fo;

namespace Fop.Layout;

/// <summary>
/// The resolved geometry of a page and its body region, in millipoints with a top-left origin. The
/// body content rectangle is the page minus the page-master margins minus the region-body margins.
/// </summary>
/// <param name="PageWidthMpt">Page width.</param>
/// <param name="PageHeightMpt">Page height.</param>
/// <param name="ContentLeftMpt">Left edge of the body content rectangle.</param>
/// <param name="ContentTopMpt">Top edge of the body content rectangle.</param>
/// <param name="ContentWidthMpt">Width of the body content rectangle.</param>
/// <param name="ContentHeightMpt">Height of the body content rectangle.</param>
/// <param name="RegionLeftMpt">Left edge of the region-before/after bands (page minus left/right page margins).</param>
/// <param name="RegionWidthMpt">Width of the region-before/after bands.</param>
/// <param name="RegionBeforeTopMpt">Top edge of the region-before band (the page top margin).</param>
/// <param name="RegionBeforeExtentMpt">Height of the region-before band (its <c>extent</c>).</param>
/// <param name="RegionAfterTopMpt">Top edge of the region-after band.</param>
/// <param name="RegionAfterExtentMpt">Height of the region-after band (its <c>extent</c>).</param>
/// <param name="RegionStartLeftMpt">Left edge of the region-start (left) side band (the page left margin).</param>
/// <param name="RegionStartExtentMpt">Width of the region-start side band (its <c>extent</c>).</param>
/// <param name="RegionEndLeftMpt">Left edge of the region-end (right) side band.</param>
/// <param name="RegionEndExtentMpt">Width of the region-end side band (its <c>extent</c>).</param>
/// <param name="SideRegionTopMpt">Top edge of the side bands (below the region-before band).</param>
/// <param name="SideRegionHeightMpt">Height of the side bands (between the before and after bands).</param>
internal readonly record struct PageGeometry(
    double PageWidthMpt,
    double PageHeightMpt,
    double ContentLeftMpt,
    double ContentTopMpt,
    double ContentWidthMpt,
    double ContentHeightMpt,
    double RegionLeftMpt,
    double RegionWidthMpt,
    double RegionBeforeTopMpt,
    double RegionBeforeExtentMpt,
    double RegionAfterTopMpt,
    double RegionAfterExtentMpt,
    double RegionStartLeftMpt,
    double RegionStartExtentMpt,
    double RegionEndLeftMpt,
    double RegionEndExtentMpt,
    double SideRegionTopMpt,
    double SideRegionHeightMpt)
{
    /// <summary>Right edge of the body content rectangle.</summary>
    public double ContentRightMpt => ContentLeftMpt + ContentWidthMpt;

    /// <summary>Bottom edge of the body content rectangle.</summary>
    public double ContentBottomMpt => ContentTopMpt + ContentHeightMpt;

    // A4 portrait, matching the FO-tree defaults, when no master is resolvable.
    private static readonly double DefaultWidth = FoLength.FromPoints(595.276).Millipoints;
    private static readonly double DefaultHeight = FoLength.FromPoints(841.89).Millipoints;

    /// <summary>Resolves the geometry from a simple-page-master (falling back to A4 with no margins).</summary>
    /// <remarks>
    /// The region-before band sits at the top of the page, below the page top margin, with height
    /// equal to the region-before <c>extent</c>; the region-after band sits at the bottom, above the
    /// page bottom margin, with height equal to the region-after <c>extent</c>. The body content
    /// rectangle is inset by the region-body margins; standard practice requires the region-body
    /// margin-top/bottom be at least the before/after extents, and margin-left/right at least the
    /// region-start/end extents, so the body does not overlap the bands. This engine does not enforce
    /// that overlap constraint -- it trusts the FO author to set it.
    /// <para>
    /// The region-start band is the left vertical band of width = its <c>extent</c>; the region-end
    /// band is the right vertical band. Both sit between the region-before and region-after bands
    /// vertically and within the page left/right margins horizontally.
    /// </para>
    /// </remarks>
    public static PageGeometry Resolve(FoSimplePageMaster? master)
    {
        if (master is null)
        {
            return new PageGeometry(DefaultWidth, DefaultHeight, 0, 0, DefaultWidth, DefaultHeight,
                0, DefaultWidth, 0, 0, DefaultHeight, 0,
                0, 0, DefaultWidth, 0, 0, DefaultHeight);
        }

        double pageWidth = master.PageWidth.Millipoints;
        double pageHeight = master.PageHeight.Millipoints;

        double marginLeft = master.MarginLeft.Millipoints;
        double marginRight = master.MarginRight.Millipoints;
        double marginTop = master.MarginTop.Millipoints;
        double marginBottom = master.MarginBottom.Millipoints;

        FoRegionBody? body = master.RegionBody;
        double bodyLeft = body?.MarginLeft.Millipoints ?? 0;
        double bodyRight = body?.MarginRight.Millipoints ?? 0;
        double bodyTop = body?.MarginTop.Millipoints ?? 0;
        double bodyBottom = body?.MarginBottom.Millipoints ?? 0;

        double left = marginLeft + bodyLeft;
        double top = marginTop + bodyTop;
        double width = Math.Max(0, pageWidth - left - marginRight - bodyRight);
        double height = Math.Max(0, pageHeight - top - marginBottom - bodyBottom);

        // The header/footer bands span the page between the left and right page margins.
        double regionLeft = marginLeft;
        double regionWidth = Math.Max(0, pageWidth - marginLeft - marginRight);

        double beforeExtent = master.RegionBefore?.Extent.Millipoints ?? 0;
        double afterExtent = master.RegionAfter?.Extent.Millipoints ?? 0;

        double beforeTop = marginTop;
        double afterTop = pageHeight - marginBottom - afterExtent;

        // Side bands: left/right vertical strips of width = extent, between the before/after bands.
        double startExtent = master.RegionStart?.Extent.Millipoints ?? 0;
        double endExtent = master.RegionEnd?.Extent.Millipoints ?? 0;
        double startLeft = marginLeft;
        double endLeft = pageWidth - marginRight - endExtent;
        double sideTop = beforeTop + beforeExtent;
        double sideHeight = Math.Max(0, afterTop - sideTop);

        return new PageGeometry(pageWidth, pageHeight, left, top, width, height,
            regionLeft, regionWidth, beforeTop, beforeExtent, afterTop, afterExtent,
            startLeft, startExtent, endLeft, endExtent, sideTop, sideHeight);
    }
}
