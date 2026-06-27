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
            foreach (FoBlock block in flowFo.ChildObjects.OfType<FoBlock>())
            {
                flow.LayOutBlock(block, geometry.ContentLeftMpt, geometry.ContentWidthMpt);
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
    private void EmitLine(PageArea target, LineBox line, double leftMpt, double availableMpt,
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

    private void EmitRuns(PageArea target, LineBox line, double startX, double baseline, double extraPerGap)
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

    // ----- Block stacking + pagination ------------------------------------------------------

    /// <summary>
    /// Per-page-sequence layout state: holds the current page, the vertical cursor and the page
    /// geometry, and drives block stacking, line breaking and pagination.
    /// </summary>
    private sealed class FlowContext(LayoutEngine engine, PageGeometry geometry, AreaTree tree)
    {
        private PageArea? page;
        private double cursorY = geometry.ContentTopMpt;

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
        /// <paramref name="leftMpt"/> (absolute page-left edge of the block's content) with the
        /// available content width <paramref name="widthMpt"/>.
        /// </summary>
        public void LayOutBlock(FoBlock block, double leftMpt, double widthMpt)
        {
            cursorY += block.SpaceBefore.Millipoints;

            // Inline content of this block (text + inlines), excluding nested blocks.
            List<StyledWord> words = InlineContent.Flatten(block);
            if (words.Count > 0)
            {
                LayOutLines(block, words, leftMpt, widthMpt);
            }

            // Recurse into nested blocks, narrowing the content rectangle by this block's indents.
            double childLeft = leftMpt + block.StartIndent.Millipoints;
            double childWidth = Math.Max(0,
                widthMpt - block.StartIndent.Millipoints - block.EndIndent.Millipoints);
            foreach (FoBlock child in block.ChildObjects.OfType<FoBlock>())
            {
                LayOutBlock(child, childLeft, childWidth);
            }

            cursorY += block.SpaceAfter.Millipoints;
        }

        private void LayOutLines(FoBlock block, List<StyledWord> words, double leftMpt, double widthMpt)
        {
            double lineHeight = block.LineHeightMpt;
            TextAlign align = block.TextAlign;

            int index = 0;
            while (index < words.Count)
            {
                LineBox line = engine.FillLine(words, index, widthMpt);
                bool isLastLine = line.NextIndex >= words.Count;
                double advance = Math.Max(lineHeight, line.Height);

                PageArea target = PageForLine(advance);
                engine.EmitLine(target, line, leftMpt, widthMpt, lineHeight, cursorY, align, isLastLine);

                cursorY += advance;
                index = line.NextIndex;
            }
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
}
