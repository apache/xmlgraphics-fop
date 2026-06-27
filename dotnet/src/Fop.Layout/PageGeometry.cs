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
internal readonly record struct PageGeometry(
    double PageWidthMpt,
    double PageHeightMpt,
    double ContentLeftMpt,
    double ContentTopMpt,
    double ContentWidthMpt,
    double ContentHeightMpt)
{
    /// <summary>Right edge of the body content rectangle.</summary>
    public double ContentRightMpt => ContentLeftMpt + ContentWidthMpt;

    /// <summary>Bottom edge of the body content rectangle.</summary>
    public double ContentBottomMpt => ContentTopMpt + ContentHeightMpt;

    // A4 portrait, matching the FO-tree defaults, when no master is resolvable.
    private static readonly double DefaultWidth = FoLength.FromPoints(595.276).Millipoints;
    private static readonly double DefaultHeight = FoLength.FromPoints(841.89).Millipoints;

    /// <summary>Resolves the geometry from a simple-page-master (falling back to A4 with no margins).</summary>
    public static PageGeometry Resolve(FoSimplePageMaster? master)
    {
        if (master is null)
        {
            return new PageGeometry(DefaultWidth, DefaultHeight, 0, 0, DefaultWidth, DefaultHeight);
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

        return new PageGeometry(pageWidth, pageHeight, left, top, width, height);
    }
}
