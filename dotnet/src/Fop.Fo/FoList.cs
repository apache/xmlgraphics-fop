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

/// <summary>
/// A list block, <c>fo:list-block</c>. Port of the role of
/// <c>org.apache.fop.fo.flow.ListBlock</c>, scoped to the current pipeline.
/// <para>
/// A list-block stacks its <c>fo:list-item</c> children. Each item has a label column whose start
/// edge coincides with the item start edge and a body column that begins
/// <see cref="ProvisionalDistanceBetweenStarts"/> after the item start; the label column is that
/// distance less <see cref="ProvisionalLabelSeparation"/> wide.
/// </para>
/// </summary>
public sealed class FoListBlock(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "list-block";

    /// <summary>
    /// The <c>provisional-distance-between-starts</c>: the start-edge distance from the item start to
    /// the body start (i.e. the label column width plus the label separation). Defaults to 24pt.
    /// </summary>
    public FoLength ProvisionalDistanceBetweenStarts =>
        Properties.GetLength("provisional-distance-between-starts", FoLength.FromPoints(24));

    /// <summary>
    /// The <c>provisional-label-separation</c>: the gap between the end of the label column and the
    /// start of the body. Defaults to 6pt.
    /// </summary>
    public FoLength ProvisionalLabelSeparation =>
        Properties.GetLength("provisional-label-separation", FoLength.FromPoints(6));

    /// <summary>Space before the list-block (optimum).</summary>
    public FoLength SpaceBefore =>
        Properties.GetLength("space-before", Properties.GetLength("space-before.optimum", FoLength.Zero));

    /// <summary>Space after the list-block (optimum).</summary>
    public FoLength SpaceAfter =>
        Properties.GetLength("space-after", Properties.GetLength("space-after.optimum", FoLength.Zero));

    /// <summary>Start indent.</summary>
    public FoLength StartIndent => Properties.GetLength("start-indent", FoLength.Zero);

    /// <summary>End indent.</summary>
    public FoLength EndIndent => Properties.GetLength("end-indent", FoLength.Zero);

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>The <c>fo:list-item</c> children, in document order.</summary>
    public IEnumerable<FoListItem> Items => ChildObjects.OfType<FoListItem>();
}

/// <summary>
/// A list item, <c>fo:list-item</c>. Port of the role of
/// <c>org.apache.fop.fo.flow.ListItem</c>. Contains exactly one <see cref="FoListItemLabel"/> and one
/// <see cref="FoListItemBody"/>.
/// </summary>
public sealed class FoListItem(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "list-item";

    /// <summary>Space before the item (optimum).</summary>
    public FoLength SpaceBefore =>
        Properties.GetLength("space-before", Properties.GetLength("space-before.optimum", FoLength.Zero));

    /// <summary>Space after the item (optimum).</summary>
    public FoLength SpaceAfter =>
        Properties.GetLength("space-after", Properties.GetLength("space-after.optimum", FoLength.Zero));

    /// <summary>The resolved box-model properties (borders, padding, background colour).</summary>
    public BoxProperties Box => Properties.GetBox();

    /// <summary>The label (the bullet/number), if present.</summary>
    public FoListItemLabel? Label => ChildObjects.OfType<FoListItemLabel>().FirstOrDefault();

    /// <summary>The body (the item content), if present.</summary>
    public FoListItemBody? Body => ChildObjects.OfType<FoListItemBody>().FirstOrDefault();
}

/// <summary>
/// A list-item label, <c>fo:list-item-label</c>. Port of the role of
/// <c>org.apache.fop.fo.flow.ListItemLabel</c>. A container of block-level content.
/// </summary>
public sealed class FoListItemLabel(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "list-item-label";

    /// <summary>The <c>fo:block</c> children of this label, in document order.</summary>
    public IEnumerable<FoBlock> Blocks => ChildObjects.OfType<FoBlock>();

    /// <summary>
    /// The block-level children of this label in document order: blocks, nested tables and
    /// list-blocks. A label is normally a single block but may hold any block-level content.
    /// </summary>
    public IEnumerable<FObj> BlockLevelChildren =>
        ChildObjects.Where(c => c is FoBlock or FoTable or FoListBlock);
}

/// <summary>
/// A list-item body, <c>fo:list-item-body</c>. Port of the role of
/// <c>org.apache.fop.fo.flow.ListItemBody</c>. A container of block-level content (which may itself
/// contain nested list-blocks).
/// </summary>
public sealed class FoListItemBody(PropertyList properties) : FObj(properties)
{
    /// <inheritdoc/>
    public override string LocalName => "list-item-body";

    /// <summary>The <c>fo:block</c> children of this body, in document order.</summary>
    public IEnumerable<FoBlock> Blocks => ChildObjects.OfType<FoBlock>();

    /// <summary>
    /// The block-level children of this body in document order: blocks, nested tables and
    /// list-blocks (which may themselves nest further). The layout engine walks this so a table or
    /// list nested directly in a list-item body lays out via the same shared mechanism.
    /// </summary>
    public IEnumerable<FObj> BlockLevelChildren =>
        ChildObjects.Where(c => c is FoBlock or FoTable or FoListBlock);
}
