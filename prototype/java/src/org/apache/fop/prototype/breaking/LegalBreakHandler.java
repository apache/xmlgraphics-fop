/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.prototype.breaking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.fop.prototype.breaking.FeasibleBreaks.BestBreak;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.ProgressInfo;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * Base class for handling legal breaks. It iterates over active layouts and considers
 * whether they complete a part (a page, a line...).
 */
public class LegalBreakHandler<L extends Layout> {

    static final int INFINITE_RATIO = 1000;

    private double threshold;

    public LegalBreakHandler() {
        this(1.0);
    }

    public LegalBreakHandler(double threshold) {
        this.threshold = threshold;
    }

    public Collection<CompletedPart> considerBreak(Penalty p, ActiveLayouts<L> layouts) {
        Collection<CompletedPart> completedParts = new ArrayList<CompletedPart>();
        for (Iterator<Iterator<L>> classIter = layouts.getClassIterator(); classIter.hasNext();) {
            FeasibleBreaks<L> feasibleBreaks = new FeasibleBreaks<L>();
            for (Iterator<L> layoutIter = classIter.next(); layoutIter.hasNext();) {
                L layout = layoutIter.next();
                int difference = layout.getDifference() - p.getLength();
                double ratio = computeAdjustmentRatio(layout.getProgress(), difference);
                if (ratio < -1.0 || p.isForcedBreak()) {
                    difference = 0; // TODO
                    layoutIter.remove();
                }
                if (-1.0 <= ratio && ratio <= threshold) {
                    double d = computeDemerits(p, ratio) + layout.getDemerits();
                    feasibleBreaks.add(layout, d, difference);
                }
            }
            BestBreak<L> best = feasibleBreaks.getBest();
            if (best != null) {
                completedParts.add(new CompletedPart(best.layout, best.demerits, best.difference,
                        feasibleBreaks.getAlternatives()));
            }
        }
        return completedParts;
    }

    public/*TODO*/ static double computeAdjustmentRatio(ProgressInfo progress, int difference) {
        if (difference > 0.0) {
            int stretch = progress.getTotalStretch();
            if (stretch > 0) {
                return ((double) difference) / ((double) stretch);
            } else {
                return INFINITE_RATIO;
            }
        } else if (difference < 0.0) {
            int shrink = progress.getTotalShrink();
            if (shrink > 0) {
                return ((double) difference) / ((double) shrink);
            } else {
                return -INFINITE_RATIO;
            }
        } else {
            return 0.0;
        }
    }

    public/*TODO*/ static double computeDemerits(Penalty penalty, double ratio) {
        double d = 1 + 100 * Math.pow(Math.abs(ratio), 3);
        int p = penalty.getPenalty();
        if (p > 0) {
            return Math.pow(d + p, 2);
        } else if (!penalty.isForcedBreak()) {
            return d * d - p * p;
        } else {
            return d * d;
        }
    }
}
