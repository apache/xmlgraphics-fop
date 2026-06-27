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
/// Turns a formatting-object tree into an <see cref="AreaTree"/> by resolving page geometry,
/// stacking blocks, breaking text into lines, and paginating.
/// <para>
/// This is the modern stand-in for FOP's layout-manager subsystem
/// (<c>org.apache.fop.layoutmgr</c>), scoped to block/inline text flow for the initial pipeline.
/// </para>
/// </summary>
public sealed class LayoutEngine
{
    private readonly IFontMeasurer measurer;

    /// <summary>Creates a layout engine that measures text via <paramref name="measurer"/>.</summary>
    public LayoutEngine(IFontMeasurer measurer)
    {
        this.measurer = measurer ?? throw new ArgumentNullException(nameof(measurer));
    }

    /// <summary>The font measurer used by this engine.</summary>
    public IFontMeasurer Measurer => measurer;

    /// <summary>Lays out the document rooted at <paramref name="root"/>.</summary>
    /// <returns>The paginated area tree.</returns>
    public AreaTree LayOut(FoRoot root)
    {
        ArgumentNullException.ThrowIfNull(root);

        // TODO(layout): implemented by the layout agent. This stub produces one blank page per
        // page-sequence so the contract compiles for the renderer.
        var tree = new AreaTree();
        foreach (var seq in root.PageSequences)
        {
            var master = root.LayoutMasterSet?.GetSimplePageMaster(seq.MasterReference);
            double w = master?.PageWidth.Millipoints ?? FoLength.FromPoints(595.276).Millipoints;
            double h = master?.PageHeight.Millipoints ?? FoLength.FromPoints(841.89).Millipoints;
            tree.AddPage(new PageArea(w, h));
        }

        return tree;
    }
}
