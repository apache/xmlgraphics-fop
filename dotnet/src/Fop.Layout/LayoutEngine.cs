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

namespace Fop.Layout;

/// <summary>
/// Turns a formatting-object tree into an <see cref="AreaTree"/> by resolving page geometry,
/// stacking blocks, breaking text into lines, and paginating.
/// <para>
/// This is the modern stand-in for FOP's layout-manager subsystem
/// (<c>org.apache.fop.layoutmgr</c>), scoped to block/inline text flow for the initial pipeline.
/// Line breaking is greedy (first-fit) rather than FOP's Knuth total-fit algorithm, which keeps the
/// engine readable while producing correct, deterministic output for this milestone.
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

        var tree = new AreaTree();
        foreach (FoPageSequence seq in root.PageSequences)
        {
            LayOutSequence(root, seq, tree);
        }

        return tree;
    }

    private void LayOutSequence(FoRoot root, FoPageSequence seq, AreaTree tree)
    {
        PageGeometry geometry = PageGeometry.Resolve(root.LayoutMasterSet?.GetSimplePageMaster(seq.MasterReference));
        var flow = new FlowContext(this, geometry, tree);

        FoFlow? flowFo = seq.Flow;
        if (flowFo is not null)
        {
            foreach (FObj child in flowFo.ChildObjects)
            {
                switch (child)
                {
                    case FoBlock block:
                        flow.LayOutBlock(block, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        break;
                    case FoTable table:
                        flow.LayOutTable(table, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
                        break;
                }
            }
        }

        // A page-sequence always occupies at least one page, even with empty/absent content.
        flow.EnsurePage();
    }

    // ----- Greedy line fill -----------------------------------------------------------------

    /// <summary>
    /// Greedily packs words from <paramref name="words"/> starting at <paramref name="start"/> until
    /// the next word would exceed <paramref name="availableMpt"/>. Always places at least one word so
    /// an over-wide word cannot stall the loop (it simply overflows its line).
    /// </summary>
    private LineBox FillLine(List<StyledWord> words, int start, double availableMpt)
    {
        var placed = new List<StyledWord>();
        double naturalWidth = 0;
        double maxFontHeight = 0;
        int i = start;

        while (i < words.Count)
        {
            StyledWord word = words[i];
            double wordWidth = measurer.MeasureWidthMpt(word.Text, word.Font);
            double spaceBefore = placed.Count == 0 ? 0 : SpaceWidth(words[i - 1].Font);
            if (placed.Count > 0 && naturalWidth + spaceBefore + wordWidth > availableMpt)
            {
                break;
            }

            placed.Add(word);
            naturalWidth += spaceBefore + wordWidth;
            maxFontHeight = Math.Max(maxFontHeight, FontHeight(word.Font));
            i++;
        }

        return new LineBox(placed, naturalWidth, maxFontHeight, i);
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

        EmitRuns(target, line, startX, baseline, extraPerGap);
    }

    private void EmitRuns(IPrimitiveSink target, LineBox line, double startX, double baseline, double extraPerGap)
    {
        double x = startX;
        int count = line.Words.Count;
        int runStart = 0;

        while (runStart < count)
        {
            StyledWord first = line.Words[runStart];

            // Extend the run across adjacent words sharing the exact same style.
            int runEnd = runStart;
            while (runEnd + 1 < count && SameStyle(line.Words[runEnd + 1], first))
            {
                runEnd++;
            }

            double runX = x;
            var text = new StringBuilder();
            for (int j = runStart; j <= runEnd; j++)
            {
                StyledWord word = line.Words[j];
                if (j > runStart)
                {
                    text.Append(' ');
                    x += SpaceWidth(word.Font) + extraPerGap;
                }

                text.Append(word.Text);
                x += measurer.MeasureWidthMpt(word.Text, word.Font);
            }

            target.Add(new TextRun(runX, baseline, text.ToString(), first.Font, first.Color));

            // Advance across the gap to the next run (a word space plus any justification slack).
            if (runEnd + 1 < count)
            {
                x += SpaceWidth(line.Words[runEnd + 1].Font) + extraPerGap;
            }

            runStart = runEnd + 1;
        }
    }

    private double FontHeight(FontKey font) => measurer.AscenderMpt(font) + measurer.DescenderMpt(font);

    private double SpaceWidth(FontKey font) => measurer.MeasureWidthMpt(" ", font);

    private static bool SameStyle(StyledWord a, StyledWord b) =>
        a.Font.Equals(b.Font) && a.Color.Equals(b.Color);

    /// <summary>A greedily-filled line: its words, natural (unjustified) width, content height, and next word index.</summary>
    private readonly record struct LineBox(
        IReadOnlyList<StyledWord> Words, double NaturalWidth, double Height, int NextIndex);

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
    }

    /// <summary>An <see cref="IPrimitiveSink"/> that appends straight onto a <see cref="PageArea"/>.</summary>
    private sealed class PageSink(PageArea page) : IPrimitiveSink
    {
        public void Add(TextRun run) => page.Add(run);

        public void Add(RectFill rect) => page.Add(rect);

        public void Add(ImageRun image) => page.Add(image);
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

        public void Add(TextRun run) => runs.Add(run);

        public void Add(RectFill rect) => rects.Add(rect);

        public void Add(ImageRun image) => images.Add(image);

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

            foreach (TextRun run in runs)
            {
                target.Add(run with { XMpt = run.XMpt + dx, BaselineYMpt = run.BaselineYMpt + dy });
            }
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

        if (box.BorderTop.IsVisible)
        {
            target.Add(new RectFill(leftMpt, topMpt, widthMpt, box.BorderTop.Width.Millipoints,
                box.BorderTop.Color));
        }

        if (box.BorderBottom.IsVisible)
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
    private sealed class FlowContext(LayoutEngine engine, PageGeometry geometry, AreaTree tree)
    {
        private PageArea? page;
        private double cursorY = geometry.ContentTopMpt;

        /// <summary>The bottom of the body content rectangle, in millipoints.</summary>
        private double ContentBottomMpt => geometry.ContentBottomMpt;

        /// <summary>Ensures a page exists, creating a blank one if none has been started yet.</summary>
        public void EnsurePage()
        {
            if (page is null)
            {
                StartNewPage();
            }
        }

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
            => LayOutBlock(block, leftMpt, widthMpt, FlowTarget.Instance);

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
            List<StyledWord> words = InlineContent.Flatten(block);
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
                switch (child)
                {
                    case FoBlock childBlock:
                        LayOutBlock(childBlock, childLeft, childWidth, target);
                        break;
                    case FoExternalGraphic graphic:
                        LayOutImage(graphic, childLeft, childWidth, target);
                        break;
                    case FoTable table when target == FlowTarget.Instance:
                        // Nested tables only flow in the main region (not inside table cells).
                        LayOutTable(table, childLeft, childWidth);
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

            // Intrinsic/specified size. Without a decoder in the layout layer we use the specified
            // content-width/height, defaulting to a square placeholder when unset.
            double defaultSize = FoLength.FromPoints(72).Millipoints;
            double imageWidth = graphic.ContentWidth?.Millipoints ?? defaultSize;
            double imageHeight = graphic.ContentHeight?.Millipoints ?? defaultSize;

            double borderBoxWidth = imageWidth + box.LeftInsetMpt + box.RightInsetMpt;
            double borderBoxHeight = imageHeight + box.TopInsetMpt + box.BottomInsetMpt;

            target.Advance(this, graphic.Properties.GetLength("space-before", FoLength.Zero).Millipoints);

            IPrimitiveSink sink = target.SinkForAdvance(this, borderBoxHeight);
            double boxTop = target.Cursor(this);

            double imageX = leftMpt + box.LeftInsetMpt;
            double imageY = boxTop + box.TopInsetMpt;

            string source = graphic.Source;
            string? path = source.Length > 0 ? source : null;
            sink.Add(new ImageRun(imageX, imageY, imageWidth, imageHeight, path, SourceBytes: null));

            EmitBox(sink, box, leftMpt, boxTop, borderBoxWidth, borderBoxHeight);

            target.SetCursor(this, boxTop + borderBoxHeight);
            target.Advance(this, graphic.Properties.GetLength("space-after", FoLength.Zero).Millipoints);

            _ = widthMpt;
        }

        private void LayOutLines(FoBlock block, List<StyledWord> words, double leftMpt, double widthMpt,
            IBlockTarget target)
        {
            double lineHeight = block.LineHeightMpt;
            TextAlign align = block.TextAlign;

            int index = 0;
            while (index < words.Count)
            {
                LineBox line = engine.FillLine(words, index, widthMpt);
                bool isLastLine = line.NextIndex >= words.Count;
                double advance = Math.Max(lineHeight, line.Height);

                IPrimitiveSink sink = target.SinkForAdvance(this, advance);
                engine.EmitLine(sink, line, leftMpt, widthMpt, lineHeight, target.Cursor(this), align, isLastLine);

                target.Advance(this, advance);
                index = line.NextIndex;
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
        {
            cursorY += table.SpaceBefore.Millipoints;

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

            EnsurePage();
            PageArea tableStartPage = page!;
            double tableTop = cursorY;

            // Header rows repeat on each page. Lay them out once and re-emit per page.
            // TODO: header repetition emits a fresh copy on every page; this is best-effort and does not
            // de-duplicate when a body fits entirely on the first page.
            var laidHeader = headerRows.Select(r => LayOutRow(r, columnWidths)).ToList();
            double headerHeight = laidHeader.Sum(r => r.Height);

            EmitTableHeaderIfRoom(laidHeader, contentLeft, columnWidths);

            foreach (RowModel row in bodyRows)
            {
                LaidRow laid = LayOutRow(row, columnWidths);
                PlaceRowPaginated(laid, contentLeft, columnWidths, laidHeader, headerHeight);
            }

            foreach (RowModel row in footerRows)
            {
                LaidRow laid = LayOutRow(row, columnWidths);
                PlaceRowPaginated(laid, contentLeft, columnWidths, laidHeader, headerHeight);
            }

            // Paint the table's own border/background over the grid extent on the page it started on.
            // TODO: a table split across pages paints its own box only on the first page.
            double tableBottom = tableStartPage == page ? cursorY + tableBox.BottomInsetMpt : geometry.ContentBottomMpt;
            EmitBox(new PageSink(tableStartPage), tableBox, leftMpt, tableTop,
                tableBox.LeftInsetMpt + gridWidth + tableBox.RightInsetMpt, tableBottom - tableTop);

            cursorY += tableBox.BottomInsetMpt;
            cursorY += table.SpaceAfter.Millipoints;
        }

        /// <summary>Emits the header rows at the current cursor if there is room; advances the cursor.</summary>
        private void EmitTableHeaderIfRoom(List<LaidRow> header, double contentLeft, double[] columnWidths)
        {
            foreach (LaidRow row in header)
            {
                PageArea target = PageForLine(row.Height);
                EmitRowAt(target, row, contentLeft, cursorY, columnWidths);
                cursorY += row.Height;
            }
        }

        /// <summary>
        /// Places a laid-out row, paginating first if it would overflow the region bottom. When a new
        /// page is started, the header rows are re-emitted at the top before the row.
        /// </summary>
        private void PlaceRowPaginated(LaidRow row, double contentLeft, double[] columnWidths,
            List<LaidRow> header, double headerHeight)
        {
            EnsurePage();

            bool overflow = cursorY + row.Height > geometry.ContentBottomMpt && cursorY > geometry.ContentTopMpt;
            if (overflow)
            {
                StartNewPage();

                // Repeat the header on the new page (best-effort).
                if (header.Count > 0 && cursorY + headerHeight <= geometry.ContentBottomMpt)
                {
                    foreach (LaidRow h in header)
                    {
                        EmitRowAt(page!, h, contentLeft, cursorY, columnWidths);
                        cursorY += h.Height;
                    }
                }
            }

            EmitRowAt(page!, row, contentLeft, cursorY, columnWidths);
            cursorY += row.Height;
        }

        /// <summary>
        /// Emits one laid-out row at (<paramref name="contentLeft"/>, <paramref name="rowTop"/>): each
        /// cell's box (background + borders) then its buffered content offset to its content origin.
        /// </summary>
        private static void EmitRowAt(PageArea target, LaidRow row, double contentLeft, double rowTop,
            double[] columnWidths)
        {
            var sink = new PageSink(target);

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

                // Cell border box fills the full row height so adjacent cell borders align.
                EmitBox(sink, cell.Box, cellLeft, rowTop, cellWidth, row.Height);

                double contentX = cellLeft + cell.Box.LeftInsetMpt;
                double contentY = rowTop + cell.Box.TopInsetMpt;
                cell.Content.FlushTo(sink, contentX, contentY);
            }
        }

        /// <summary>Lays out a single row's cells into relocatable buffers and computes the row height.</summary>
        private LaidRow LayOutRow(RowModel row, double[] columnWidths)
        {
            var laidCells = new List<LaidCell>();
            double maxContentHeight = 0;

            int autoColumn = 0;
            foreach (FoTableCell cell in row.Cells)
            {
                int span = Math.Max(1, cell.NumberColumnsSpanned);
                int startColumn = (cell.ColumnNumber.HasValue ? cell.ColumnNumber.Value - 1 : autoColumn);
                startColumn = Math.Clamp(startColumn, 0, Math.Max(0, columnWidths.Length - 1));
                span = Math.Min(span, Math.Max(1, columnWidths.Length - startColumn));

                double cellWidth = SpannedWidth(columnWidths, startColumn, span);
                BoxProperties cellBox = cell.Box;
                double contentWidth = Math.Max(0, cellWidth - cellBox.LeftInsetMpt - cellBox.RightInsetMpt);

                var buffer = new BufferedSink();
                double consumed = LayOutBlocksIntoBuffer(cell.Blocks, buffer, contentWidth);

                laidCells.Add(new LaidCell(startColumn, span, cellBox, buffer, consumed));
                maxContentHeight = Math.Max(maxContentHeight, consumed + cellBox.TopInsetMpt + cellBox.BottomInsetMpt);

                autoColumn = startColumn + span;
            }

            double rowHeight = Math.Max(maxContentHeight, row.MinHeightMpt);
            return new LaidRow(laidCells, rowHeight, row.Box);
        }

        /// <summary>
        /// Lays out a sequence of blocks within a fixed content width into a relocatable buffer,
        /// returning the consumed height. Reuses the exact same block-stacking, line-breaking and
        /// box-model logic as the main flow via the shared <see cref="LayOutBlock(FoBlock, double, double, IBlockTarget)"/>
        /// walk, driven by a non-paginating <see cref="BufferTarget"/>. The buffer is in content-local
        /// coordinates (origin at 0,0).
        /// </summary>
        private double LayOutBlocksIntoBuffer(IEnumerable<FoBlock> blocks, BufferedSink buffer, double widthMpt)
        {
            var target = new BufferTarget(buffer);
            foreach (FoBlock block in blocks)
            {
                LayOutBlock(block, 0, widthMpt, target);
            }

            return target.LocalCursor;
        }

        // ----- Block-target abstraction -----------------------------------------------------

        /// <summary>
        /// Abstracts the differences between laying out a block into the paginating main flow and into a
        /// relocatable cell buffer: the vertical cursor, where primitives go, and whether an advance may
        /// trigger pagination. Methods take the owning <see cref="FlowContext"/> so the flow target can
        /// read/write its page cursor.
        /// </summary>
        private interface IBlockTarget
        {
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

                // TODO: when a bordered block splits across pages the box is painted only on the page where
                // it started; per-page box fragments are not yet emitted.
                double boxBottom = startPage == ctx.page ? ctx.cursorY : ctx.ContentBottomMpt;
                EmitBox(new PageSink(startPage), box, leftMpt, boxTop, widthMpt, boxBottom - boxTop);
            }
        }

        /// <summary>The cell-buffer target: writes to a buffer with a local, non-paginating cursor.</summary>
        private sealed class BufferTarget(BufferedSink buffer) : IBlockTarget
        {
            public double LocalCursor { get; private set; }

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
            if (cursorY + advance > geometry.ContentBottomMpt && cursorY > geometry.ContentTopMpt)
            {
                StartNewPage();
            }

            return page!;
        }

        private void StartNewPage()
        {
            page = new PageArea(geometry.PageWidthMpt, geometry.PageHeightMpt);
            tree.AddPage(page);
            cursorY = geometry.ContentTopMpt;
        }
    }

    // ----- Table model helpers --------------------------------------------------------------

    /// <summary>A row of cells with its minimum height and box, in source order.</summary>
    private sealed record RowModel(IReadOnlyList<FoTableCell> Cells, double MinHeightMpt, BoxProperties Box);

    /// <summary>A laid-out cell: its grid position/span, box, buffered content and content height.</summary>
    private sealed record LaidCell(
        int StartColumn, int ColumnSpan, BoxProperties Box, BufferedSink Content, double ContentHeightMpt);

    /// <summary>A laid-out row: its cells, final height and box.</summary>
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
