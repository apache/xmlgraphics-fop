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

namespace Fop.Layout;

/// <summary>
/// The Knuth-Plass total-fit line-breaking algorithm: it models a paragraph as a sequence of
/// <em>boxes</em> (unbreakable material such as words), <em>glue</em> (inter-word spaces that may
/// stretch or shrink) and <em>penalties</em> (break opportunities, including hyphenation points and a
/// final forced break) and chooses the set of breakpoints that minimises the total demerits over the
/// whole paragraph rather than filling each line greedily.
/// <para>
/// This is the C# realisation of the algorithm FOP itself uses
/// (<c>org.apache.fop.layoutmgr.BreakingAlgorithm</c> / Donald Knuth and Michael Plass,
/// "Breaking Paragraphs into Lines", 1981). It works over an abstract item stream so the engine can
/// build that stream from its inline model (words carry their measured width, glue is the space width
/// in the relevant font, leaders are glue with very large stretch, and hyphenation candidates are
/// flagged penalties carrying the hyphen-character width).
/// </para>
/// <para>
/// The algorithm always produces a feasible set of breaks: if no breaking is feasible within the
/// configured tolerance it retries with progressively looser tolerance, and finally with effectively
/// infinite tolerance plus forced emergency breaks, so an over-long unbreakable word still lays out
/// (it simply overflows its line).
/// </para>
/// </summary>
internal static class LineBreaker
{
    /// <summary>
    /// The classification of a feasible breakpoint by how tight or loose its line is. Knuth-Plass adds
    /// an extra demerit when two adjacent lines differ by more than one fitness class, which discourages
    /// a very loose line next to a very tight one.
    /// </summary>
    private enum Fitness
    {
        Tight = 0,
        Normal = 1,
        Loose = 2,
        VeryLoose = 3,
    }

    /// <summary>The kind of an item in the paragraph stream.</summary>
    internal enum ItemKind
    {
        /// <summary>Unbreakable material (a word or a fixed-length leader): contributes width only.</summary>
        Box,

        /// <summary>Stretchable/shrinkable space (an inter-word gap or an expanding leader).</summary>
        Glue,

        /// <summary>A legal breakpoint with an associated penalty cost and optional hyphen width.</summary>
        Penalty,
    }

    /// <summary>
    /// One item of the paragraph stream. A <see cref="ItemKind.Box"/> carries only its
    /// <see cref="Width"/>; <see cref="ItemKind.Glue"/> carries width plus <see cref="Stretch"/> and
    /// <see cref="Shrink"/>; <see cref="ItemKind.Penalty"/> carries its <see cref="PenaltyValue"/>, the
    /// extra <see cref="Width"/> that materialises if the line is broken here (a hyphen), and whether
    /// the break is <see cref="Flagged"/> (consecutive flagged breaks are penalised, discouraging two
    /// hyphenated lines in a row).
    /// </summary>
    internal readonly record struct Item(
        ItemKind Kind, double Width, double Stretch = 0, double Shrink = 0,
        double PenaltyValue = 0, bool Flagged = false)
    {
        public static Item Box(double width) => new(ItemKind.Box, width);

        public static Item Glue(double width, double stretch, double shrink) =>
            new(ItemKind.Glue, width, stretch, shrink);

        public static Item Penalty(double penalty, double width = 0, bool flagged = false) =>
            new(ItemKind.Penalty, width, PenaltyValue: penalty, Flagged: flagged);
    }

    /// <summary>A penalty value meaning "a break here is mandatory" (a forced break).</summary>
    internal const double ForcedBreak = -10_000;

    /// <summary>A penalty value meaning "a break here is impossible".</summary>
    internal const double InfiniteBreak = 10_000;

    /// <summary>
    /// The penalty charged for breaking at a hyphenation point. A modest positive cost so the optimiser
    /// hyphenates only when it meaningfully improves the paragraph (a hyphenated line is slightly worse
    /// than an unhyphenated one of equal badness), matching typesetting practice.
    /// </summary>
    internal const double HyphenPenalty = 50;

    // ----- Tuning constants -----------------------------------------------------------------
    //
    // These mirror the conventional Knuth-Plass parameters. They are expressed as doubles because the
    // engine works in millipoints. The demerit weights are unitless multipliers applied to the
    // (dimensionless) badness, so their exact scale only matters relative to one another.

    /// <summary>Added to every line's demerits so the optimiser also minimises the line count.</summary>
    private const double LinePenalty = 10;

    /// <summary>Extra demerits when two consecutive lines both end on a flagged (hyphenation) break.</summary>
    private const double FlaggedDemerit = 100;

    /// <summary>Extra demerits when adjacent lines differ by more than one fitness class.</summary>
    private const double FitnessDemerit = 100;

    /// <summary>
    /// How much more an overfull emergency line costs than an underfull one of the same magnitude. A
    /// large factor makes overprint (overfull) strictly worse than a ragged gap (underfull), so the
    /// emergency pass always prefers leaving white space to running text past the edge.
    /// </summary>
    private const double OverfullWeight = 1_000_000;

    /// <summary>
    /// The adjustment ratio returned when a line must stretch/shrink but has no glue to do it with -- a
    /// large but finite value (matching FOP's <c>INFINITE_RATIO</c>) so demerits stay real numbers.
    /// </summary>
    private const double InfiniteRatio = 1000;

    /// <summary>
    /// Computes an optimal set of breakpoints for the paragraph described by <paramref name="items"/>,
    /// laid out at <paramref name="lineWidth"/> wide. The returned list gives the item index of each
    /// chosen break (a penalty or glue), in order, with the final entry being the paragraph's mandatory
    /// end break. Feeding the slices between consecutive breaks back to the caller reproduces the lines.
    /// <para>
    /// The search runs at increasing tolerance until a feasible breaking is found; the final pass uses
    /// effectively infinite tolerance with forced emergency breaks so a result is always produced.
    /// </para>
    /// </summary>
    /// <param name="items">the paragraph item stream (the last item must be a forced penalty).</param>
    /// <param name="lineWidth">the line width in millipoints.</param>
    /// <returns>the chosen break item indices, in paragraph order.</returns>
    public static IReadOnlyList<int> Break(IReadOnlyList<Item> items, double lineWidth)
    {
        // Try increasing tolerances. A higher tolerance admits looser/tighter lines; the emergency pass
        // accepts any line at all (so even an over-wide unbreakable word lays out, overflowing its line).
        foreach (double tolerance in (ReadOnlySpan<double>)[1.0, 5.0, 20.0])
        {
            if (TryBreak(items, lineWidth, tolerance, emergency: false) is { } feasible)
            {
                return feasible;
            }
        }

        // Final guaranteed pass: infinite tolerance and emergency breaks. This always succeeds.
        return TryBreak(items, lineWidth, double.MaxValue, emergency: true)
            ?? FallbackEveryGlue(items);
    }

    /// <summary>
    /// A node in the active-breakpoint list: a position at which a line may end. Each node remembers the
    /// total demerits of the best path that reaches it, the line number it produces, its fitness class,
    /// and a back-pointer to the previous break, so the optimal path can be reconstructed.
    /// </summary>
    private sealed class Node(int position, int line, Fitness fitness, double totalDemerits, Node? previous)
    {
        public int Position { get; } = position;

        public int Line { get; } = line;

        public Fitness Fitness { get; } = fitness;

        public double TotalDemerits { get; } = totalDemerits;

        public Node? Previous { get; } = previous;
    }

    /// <summary>
    /// Runs one pass of the dynamic program at the given <paramref name="tolerance"/>. Returns the
    /// optimal break list, or <c>null</c> when no feasible breaking exists at this tolerance (so the
    /// caller can retry looser). In <paramref name="emergency"/> mode every breakpoint is admitted
    /// regardless of badness, which guarantees a result.
    /// </summary>
    private static IReadOnlyList<int>? TryBreak(IReadOnlyList<Item> items, double lineWidth,
        double tolerance, bool emergency)
    {
        // Running sums of width / stretch / shrink up to (but not including) each item, so the
        // dimensions of the material between two breakpoints can be obtained by subtraction.
        int n = items.Count;
        var sumWidth = new double[n + 1];
        var sumStretch = new double[n + 1];
        var sumShrink = new double[n + 1];
        for (int i = 0; i < n; i++)
        {
            Item it = items[i];
            sumWidth[i + 1] = sumWidth[i] + (it.Kind == ItemKind.Penalty ? 0 : it.Width);
            sumStretch[i + 1] = sumStretch[i] + (it.Kind == ItemKind.Glue ? it.Stretch : 0);
            sumShrink[i + 1] = sumShrink[i] + (it.Kind == ItemKind.Glue ? it.Shrink : 0);
        }

        // The active list always contains at least the start node (a break before item 0).
        var active = new List<Node> { new(position: 0, line: 0, Fitness.Normal, totalDemerits: 0, previous: null) };
        Node? best = null;

        for (int b = 0; b < n; b++)
        {
            if (!IsLegalBreak(items, b))
            {
                continue;
            }

            // For every active node, evaluate the line that would run from that node's position to b.
            // Feasible candidates spawn new active nodes; nodes that can no longer reach b without
            // overfull lines are deactivated.
            var newNodes = new List<Node>();
            for (int a = active.Count - 1; a >= 0; a--)
            {
                Node from = active[a];

                // A line starts at the item AFTER the previous break (the broken-at glue/penalty is
                // discarded), except the very first line, which starts at item 0. Any glue at the very
                // start of a line is ignored (the standard Knuth-Plass "ignore at start" rule), so a
                // line never inherits the trailing glue of the previous break's break sequence. The line
                // covers [start, b); a penalty break at b additionally materialises its own width (a
                // hyphen).
                int start = from.Previous is null ? 0 : from.Position + 1;
                while (start < b && items[start].Kind == ItemKind.Glue)
                {
                    start++;
                }

                double naturalWidth = sumWidth[b] - sumWidth[start]
                    + (items[b].Kind == ItemKind.Penalty ? items[b].Width : 0);
                double ratio = AdjustmentRatio(naturalWidth, lineWidth,
                    sumStretch[b] - sumStretch[start],
                    sumShrink[b] - sumShrink[start]);

                bool forced = items[b].Kind == ItemKind.Penalty && items[b].PenaltyValue <= ForcedBreak;

                // A line that must shrink by more than it can (ratio < -1) is infeasible: the node can
                // never use a later break either (lines only get longer), so deactivate it. Unless this
                // is a forced break, in which case we must still close the line here.
                if (ratio < -1 || forced)
                {
                    active.RemoveAt(a);
                }

                if (ratio < -1 && !emergency)
                {
                    continue;
                }

                double badness = Badness(ratio);

                // Feasibility is judged on the adjustment RATIO against the pass tolerance (as FOP does):
                // the line must not shrink past its glue (ratio >= -1) and must not stretch beyond the
                // tolerated looseness (ratio <= tolerance). The emergency pass accepts any line.
                bool withinTolerance = emergency || (ratio >= -1 && ratio <= tolerance);
                if (!withinTolerance && !forced)
                {
                    continue;
                }

                Fitness fitness = Classify(ratio);
                double demerits;
                bool atCap = ratio <= -InfiniteRatio || ratio >= InfiniteRatio || ratio < -1;
                if (emergency && atCap)
                {
                    // Emergency pass for a line that cannot be fitted within its glue: replace the
                    // (astronomically large, direction-blind) cubic badness with a length-based cost that
                    // ranks options sensibly. Overfull lines are charged the squared length they overflow
                    // by (overprint is the worst outcome); underfull lines are charged a much smaller cost
                    // in the gap (a ragged gap is acceptable), with overfull strictly dominating. This is
                    // what lets the optimiser break an over-long word onto its own line and prefer an
                    // underfull break to an overfull one, instead of cramming the paragraph onto one line.
                    double gap = naturalWidth - lineWidth;
                    demerits = gap > 0
                        ? OverfullWeight * gap * gap   // overfull: heavy quadratic in overflow length
                        : gap * gap / OverfullWeight;  // underfull: light cost in the gap length
                    demerits += LinePenalty * LinePenalty;
                }
                else
                {
                    demerits = Demerits(items[b], badness, from, fitness, items);
                }

                var node = new Node(b, from.Line + 1, fitness, from.TotalDemerits + demerits, from);
                newNodes.Add(node);
            }

            // Merge new nodes for this breakpoint, keeping only the best path to each (line, fitness)
            // bucket so the active list stays small. The forced final break also records the overall
            // best terminal node.
            foreach (Node node in newNodes)
            {
                if (items[b].Kind == ItemKind.Penalty && items[b].PenaltyValue <= ForcedBreak)
                {
                    if (best is null || node.TotalDemerits < best.TotalDemerits)
                    {
                        best = node;
                    }
                }
                else
                {
                    InsertActive(active, node);
                }
            }
        }

        if (best is null)
        {
            return null;
        }

        // Reconstruct the chosen breaks from the terminal node back to the start.
        var breaks = new List<int>();
        for (Node? node = best; node is not null && node.Previous is not null; node = node.Previous)
        {
            breaks.Add(node.Position);
        }

        breaks.Reverse();
        return breaks;
    }

    /// <summary>
    /// Inserts <paramref name="node"/> into the active list, replacing any existing node in the same
    /// (line, fitness) bucket whose path is no better. This dominance pruning is what keeps the active
    /// set small without affecting optimality (two nodes with the same line count and fitness compete
    /// purely on total demerits).
    /// </summary>
    private static void InsertActive(List<Node> active, Node node)
    {
        for (int i = 0; i < active.Count; i++)
        {
            if (active[i].Position == node.Position && active[i].Line == node.Line
                && active[i].Fitness == node.Fitness)
            {
                if (node.TotalDemerits < active[i].TotalDemerits)
                {
                    active[i] = node;
                }

                return;
            }
        }

        active.Add(node);
    }

    /// <summary>
    /// Whether item <paramref name="b"/> is a legal breakpoint: a penalty that is not the
    /// "impossible" value, or a glue that immediately follows a box (the classic legal break position).
    /// </summary>
    private static bool IsLegalBreak(IReadOnlyList<Item> items, int b)
    {
        Item it = items[b];
        if (it.Kind == ItemKind.Penalty)
        {
            return it.PenaltyValue < InfiniteBreak;
        }

        return it.Kind == ItemKind.Glue && b > 0 && items[b - 1].Kind == ItemKind.Box;
    }

    /// <summary>
    /// The adjustment ratio for a line of the given natural width: positive when the line must stretch
    /// (there is spare width and stretchable glue), negative when it must shrink, zero when it fits
    /// exactly. A line that needs to stretch but has no stretch available is treated as infinitely
    /// loose (only legal at a forced/emergency break).
    /// </summary>
    private static double AdjustmentRatio(double naturalWidth, double lineWidth, double stretch, double shrink)
    {
        double diff = lineWidth - naturalWidth;
        if (Math.Abs(diff) < 1e-9)
        {
            return 0;
        }

        if (diff > 0)
        {
            return stretch > 0 ? diff / stretch : InfiniteRatio;
        }

        return shrink > 0 ? diff / shrink : -InfiniteRatio;
    }

    /// <summary>
    /// The badness of a line with the given adjustment ratio: 100 * |ratio|^3, the conventional
    /// Knuth-Plass measure. With <see cref="InfiniteRatio"/> capping the ratio, badness is always a
    /// (large but) finite number, so the emergency pass can compare otherwise-infeasible lines.
    /// </summary>
    private static double Badness(double ratio)
    {
        double abs = Math.Abs(ratio);
        return 100.0 * abs * abs * abs;
    }

    /// <summary>
    /// The demerits added by closing a line at <paramref name="breakItem"/> with the given
    /// <paramref name="badness"/>, coming from active node <paramref name="from"/>. Combines the squared
    /// (line-penalty + badness) term with the penalty's own squared contribution and the
    /// flagged/fitness adjustments, matching the standard Knuth-Plass demerit function.
    /// </summary>
    private static double Demerits(Item breakItem, double badness, Node from, Fitness fitness,
        IReadOnlyList<Item> items)
    {
        double penalty = breakItem.Kind == ItemKind.Penalty ? breakItem.PenaltyValue : 0;
        double bad = double.IsInfinity(badness) ? 1e7 : badness;

        double basePart = LinePenalty + bad;
        double demerits = basePart * basePart;

        // A positive penalty adds its square; a (non-forced) negative penalty subtracts its square --
        // a desirable break (e.g. at a deliberately encouraged position). Forced breaks contribute 0.
        if (penalty >= 0 && penalty < InfiniteBreak)
        {
            demerits += penalty * penalty;
        }
        else if (penalty > ForcedBreak && penalty < 0)
        {
            demerits -= penalty * penalty;
        }

        // Penalise two flagged (hyphenated) breaks in a row.
        bool thisFlagged = breakItem.Kind == ItemKind.Penalty && breakItem.Flagged;
        bool prevFlagged = from.Position > 0 && from.Position < items.Count
            && items[from.Position].Kind == ItemKind.Penalty && items[from.Position].Flagged;
        if (thisFlagged && prevFlagged)
        {
            demerits += FlaggedDemerit;
        }

        // Penalise a large jump in fitness class between adjacent lines (a loose line next to a tight
        // one), so the optimiser also favours visually consistent spacing.
        if (Math.Abs((int)fitness - (int)from.Fitness) > 1)
        {
            demerits += FitnessDemerit;
        }

        return demerits;
    }

    /// <summary>Classifies a line's tightness from its adjustment ratio into a fitness class.</summary>
    private static Fitness Classify(double ratio) => ratio switch
    {
        < -0.5 => Fitness.Tight,
        <= 0.5 => Fitness.Normal,
        <= 1.0 => Fitness.Loose,
        _ => Fitness.VeryLoose,
    };

    /// <summary>
    /// The last-resort breaking: break at every legal glue/penalty. Used only if even the emergency
    /// pass somehow finds no terminal node (it should not), so a result is always returned.
    /// </summary>
    private static IReadOnlyList<int> FallbackEveryGlue(IReadOnlyList<Item> items)
    {
        var breaks = new List<int>();
        for (int i = 0; i < items.Count; i++)
        {
            if (IsLegalBreak(items, i))
            {
                breaks.Add(i);
            }
        }

        if (breaks.Count == 0 || breaks[^1] != items.Count - 1)
        {
            breaks.Add(items.Count - 1);
        }

        return breaks;
    }
}
