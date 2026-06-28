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

namespace Fop.Render.Text;

/// <summary>
/// Extracts the <em>logical</em> content of an XSL-FO document into the renderer-neutral
/// <see cref="DocBlock"/> model that the text/Markdown/HTML back-ends format. Unlike the area tree
/// (positioned primitives), this preserves document structure -- paragraphs, headings, lists, tables,
/// links and images -- which is what flowing/semantic output formats need. Page geometry, pagination,
/// static content (running headers/footers) and page numbers are intentionally dropped, since they
/// have no meaning in a non-paginated format.
/// </summary>
internal static class DocExtractor
{
    private const double BaseFontSizeMpt = 12_000;

    /// <summary>Extracts the block content of every page-sequence's main flow, in document order.</summary>
    public static IReadOnlyList<DocBlock> Extract(FoRoot root)
    {
        var blocks = new List<DocBlock>();
        foreach (FoPageSequence sequence in root.PageSequences)
        {
            if (sequence.Flow is { } flow)
            {
                ExtractBlocksInto(blocks, BlockLevelChildren(flow));
            }
        }

        return blocks;
    }

    private static void ExtractBlocksInto(List<DocBlock> output, IEnumerable<FObj> blockLevel)
    {
        foreach (FObj obj in blockLevel)
        {
            switch (obj)
            {
                case FoBlock block:
                    ExtractBlock(output, block);
                    break;
                case FoBlockContainer container:
                    ExtractBlocksInto(output, container.BlockLevelChildren);
                    break;
                case FoListBlock list:
                    output.Add(ExtractList(list));
                    break;
                case FoTable table:
                    output.Add(ExtractTable(table));
                    break;
                case FoExternalGraphic graphic:
                    output.Add(new DocImage(graphic.Source, "image"));
                    break;
                case FoInstreamForeignObject:
                    output.Add(new DocImage(string.Empty, "embedded graphic"));
                    break;
            }
        }
    }

    /// <summary>
    /// Extracts a block: its direct inline content becomes a paragraph (or heading, by font size),
    /// and nested block-level children are extracted in document order so interleaved text and nested
    /// blocks keep their order.
    /// </summary>
    private static void ExtractBlock(List<DocBlock> output, FoBlock block)
    {
        int headingLevel = HeadingLevel(block.FontSizeMpt);
        (bool bold, bool italic) = Style(block);
        var pending = new List<DocInline>();

        void Flush()
        {
            if (pending.Count > 0)
            {
                output.Add(new DocParagraph(headingLevel, Normalize(pending)));
                pending = new List<DocInline>();
            }
        }

        foreach (FONode child in block.Children)
        {
            switch (child)
            {
                case FoBlock or FoBlockContainer or FoListBlock or FoTable or FoExternalGraphic
                    or FoInstreamForeignObject:
                    Flush();
                    ExtractBlocksInto(output, [(FObj)child]);
                    break;
                case FONode inline:
                    CollectInlines(pending, inline, bold, italic, link: null);
                    break;
            }
        }

        Flush();
    }

    private static DocList ExtractList(FoListBlock list)
    {
        var items = new List<DocListItem>();
        foreach (FoListItem item in list.Items)
        {
            string label = item.Label is { } l ? InlineText(l.BlockLevelChildren) : string.Empty;
            var body = new List<DocBlock>();
            if (item.Body is { } b)
            {
                ExtractBlocksInto(body, b.BlockLevelChildren);
            }

            items.Add(new DocListItem(label, body));
        }

        return new DocList(items);
    }

    private static DocTable ExtractTable(FoTable table)
    {
        var rows = new List<DocTableRow>();

        void AddRows(FoTablePart? part, bool header)
        {
            if (part is null)
            {
                return;
            }

            foreach (FoTableRow row in part.Rows)
            {
                var cells = new List<DocTableCell>();
                foreach (FoTableCell cell in row.Cells)
                {
                    var body = new List<DocBlock>();
                    ExtractBlocksInto(body, cell.BlockLevelChildren);
                    cells.Add(new DocTableCell(body, Math.Max(1, cell.NumberColumnsSpanned)));
                }

                rows.Add(new DocTableRow(header, cells));
            }
        }

        AddRows(table.Header, header: true);
        foreach (FoTableBody body in table.Bodies)
        {
            AddRows(body, header: false);
        }

        AddRows(table.Footer, header: false);
        return new DocTable(rows);
    }

    /// <summary>Collects the inline runs of <paramref name="node"/> with the active emphasis/link.</summary>
    private static void CollectInlines(List<DocInline> output, FONode node, bool bold, bool italic, string? link)
    {
        switch (node)
        {
            case FOText text:
                if (text.Text.Length > 0)
                {
                    output.Add(new DocInline(text.Text, bold, italic, link));
                }

                break;

            case FoBasicLink basicLink:
            {
                string uri = basicLink.ExternalDestination.Length > 0
                    ? basicLink.ExternalDestination
                    : basicLink.InternalDestination.Length > 0 ? "#" + basicLink.InternalDestination : string.Empty;
                string? effective = uri.Length > 0 ? uri : link;
                (bool b, bool i) = Style(basicLink);
                foreach (FONode child in basicLink.Children)
                {
                    CollectInlines(output, child, bold || b, italic || i, effective);
                }

                break;
            }

            case FoLeader:
                output.Add(new DocInline(" ", bold, italic, link));
                break;

            // page-number / citations have no meaning without pagination; markers are running content.
            case FoPageNumber or FoPageNumberCitation or FoPageNumberCitationLast or FoMarker:
                break;

            case FoFootnote footnote:
                foreach (FObj anchor in footnote.AnchorChildren)
                {
                    CollectInlines(output, anchor, bold, italic, link);
                }

                break;

            // Nested block-level objects are not inline content; the block walk handles them.
            case FoBlock or FoBlockContainer or FoListBlock or FoTable:
                break;

            case FObj other:
            {
                // fo:inline and neutral wrappers contribute inline content with their own emphasis.
                (bool b, bool i) = Style(other);
                foreach (FONode child in other.Children)
                {
                    CollectInlines(output, child, bold || b, italic || i, link);
                }

                break;
            }
        }
    }

    /// <summary>The collapsed plain text of the inline content of a set of blocks (for labels/cells).</summary>
    private static string InlineText(IEnumerable<FObj> blocks)
    {
        var inlines = new List<DocInline>();
        foreach (FObj block in blocks)
        {
            (bool bold, bool italic) = Style(block);
            foreach (FONode child in block.Children)
            {
                CollectInlines(inlines, child, bold, italic, link: null);
            }
        }

        return string.Concat(inlines.Select(i => i.Text)).Trim();
    }

    /// <summary>Collapses leading/trailing whitespace across the run list (keeping interior spacing).</summary>
    private static IReadOnlyList<DocInline> Normalize(List<DocInline> inlines)
    {
        if (inlines.Count > 0)
        {
            inlines[0] = inlines[0] with { Text = inlines[0].Text.TrimStart() };
            inlines[^1] = inlines[^1] with { Text = inlines[^1].Text.TrimEnd() };
        }

        return inlines.Where(i => i.Text.Length > 0).ToList();
    }

    private static (bool Bold, bool Italic) Style(FObj obj) =>
        (obj.FontWeight >= 700, obj.FontStyle is FontStyle.Italic or FontStyle.Oblique);

    /// <summary>Infers a heading level (1-4) from a block's font size, or 0 for a normal paragraph.</summary>
    private static int HeadingLevel(double fontSizeMpt) => fontSizeMpt switch
    {
        >= 24_000 => 1,
        >= 18_000 => 2,
        >= 15_000 => 3,
        >= 13_500 => 4,
        _ => 0,
    };

    private static IEnumerable<FObj> BlockLevelChildren(FObj container) =>
        container.ChildObjects.Where(c =>
            c is FoBlock or FoBlockContainer or FoListBlock or FoTable or FoExternalGraphic
                or FoInstreamForeignObject);
}
