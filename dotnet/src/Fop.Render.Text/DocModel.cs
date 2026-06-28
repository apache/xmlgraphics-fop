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

namespace Fop.Render.Text;

/// <summary>
/// A styled run of inline text in the extracted logical document model: its text and the emphasis /
/// link that applies to it. This is the renderer-neutral atom the text/Markdown/HTML back-ends format.
/// </summary>
/// <param name="Text">The run text.</param>
/// <param name="Bold">Whether the run is bold (font-weight &gt;= 700).</param>
/// <param name="Italic">Whether the run is italic/oblique.</param>
/// <param name="Uri">The link target when this run is inside an <c>fo:basic-link</c>, else <c>null</c>.</param>
public sealed record DocInline(string Text, bool Bold, bool Italic, string? Uri);

/// <summary>Base type for a block-level node of the extracted logical document.</summary>
public abstract record DocBlock;

/// <summary>
/// A paragraph (or heading) of inline content. <see cref="HeadingLevel"/> is 0 for a normal
/// paragraph, or 1-6 for a heading (inferred from the block's font size).
/// </summary>
public sealed record DocParagraph(int HeadingLevel, IReadOnlyList<DocInline> Inlines) : DocBlock;

/// <summary>A bullet list (mapped from <c>fo:list-block</c>); each item carries its own block content.</summary>
public sealed record DocList(IReadOnlyList<DocListItem> Items) : DocBlock;

/// <summary>One list item: an optional label (the bullet/marker text) and its block-level body.</summary>
/// <param name="Label">The label text (e.g. a bullet), or the empty string.</param>
/// <param name="Body">The item's block-level content.</param>
public sealed record DocListItem(string Label, IReadOnlyList<DocBlock> Body);

/// <summary>A table (mapped from <c>fo:table</c>).</summary>
public sealed record DocTable(IReadOnlyList<DocTableRow> Rows) : DocBlock;

/// <summary>One table row; <see cref="IsHeader"/> marks rows from the table header.</summary>
public sealed record DocTableRow(bool IsHeader, IReadOnlyList<DocTableCell> Cells);

/// <summary>One table cell: its block-level content and how many columns it spans.</summary>
public sealed record DocTableCell(IReadOnlyList<DocBlock> Body, int ColumnSpan);

/// <summary>An image (from <c>fo:external-graphic</c> or an embedded <c>fo:instream-foreign-object</c>).</summary>
/// <param name="Source">The image source URI/path, or the empty string for inline (e.g. SVG) graphics.</param>
/// <param name="Alt">Alternate text describing the image.</param>
public sealed record DocImage(string Source, string Alt) : DocBlock;
