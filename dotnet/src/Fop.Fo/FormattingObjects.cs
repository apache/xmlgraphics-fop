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

/// <summary>A page sequence, <c>fo:page-sequence</c>.</summary>
public sealed class FoPageSequence(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "page-sequence";

    /// <summary>The <c>master-reference</c> naming the page master to use.</summary>
    public string MasterReference => Properties.GetString("master-reference", string.Empty);

    /// <summary>The main flow, if present.</summary>
    public FoFlow? Flow => ChildObjects.OfType<FoFlow>().FirstOrDefault();
}

/// <summary>A flow of content, <c>fo:flow</c>.</summary>
public sealed class FoFlow(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "flow";

    /// <summary>The <c>flow-name</c> (the region this flow targets).</summary>
    public string FlowName => Properties.GetString("flow-name", "xsl-region-body");
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
}

/// <summary>An inline, <c>fo:inline</c>.</summary>
public sealed class FoInline(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "inline";
}

/// <summary>A neutral wrapper, <c>fo:wrapper</c>, and the fallback for unmodelled elements.</summary>
public sealed class FoGeneric(PropertyList properties, string localName) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName { get; } = localName;
}
