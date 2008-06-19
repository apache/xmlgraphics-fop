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

import java.util.Collection;
import java.util.Iterator;

import org.apache.fop.prototype.breaking.FeasibleBreaks.BestBreak;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.breaking.layout.ProgressInfo;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * Base class for handling legal breaks. It iterates over active layouts and considers
 * whether they complete a part (a page, a line...).
 */
abstract class LegalBreakHandler<L extends Layout> {

    static final int INFINITE_RATIO = 1000;

    ActiveLayouts<L> considerBreak(Penalty p, ActiveLayouts<L> layouts) {
        ActiveLayouts<L> newLayouts = new ActiveLayouts<L>();
        for (Iterator<Iterator<L>> classIter = getClassIter(layouts); classIter.hasNext();) {
            FeasibleBreaks<L> feasibleBreaks = new FeasibleBreaks<L>();
            for (Iterator<L> layoutIter = classIter.next(); layoutIter.hasNext();) {
                L layout = layoutIter.next();
                int difference = computeDifference(layout, p) - p.getLength();
                double ratio = computeAdjustmentRatio(getProgress(layout), difference);
                if (ratio < -1.0 || p.isForcedBreak()) {
                    difference = 0; // TODO
                    layoutIter.remove();
                }
                if (-1.0 <= ratio && ratio <= getThreshold()) {
                    double d = computeDemerits(p, ratio) + getDemerits(layout);
                    feasibleBreaks.add(layout, d, difference);
                }
            }
            BestBreak<L> best = feasibleBreaks.getBest();
            if (best != null) {
                newLayouts.add(createLayout(best, feasibleBreaks.getAlternatives()));
            }
        }
        if (!newLayouts.isEmpty()) {
            for (L l : newLayouts) {
                layouts.add(l);
                // TODO $log.debug("After break:")
                // TODO $log.debug("#{layouts}")
            }
        }
        return newLayouts;
    }

    /**
     * Returns an iterator for iterating over sets of layouts of the same class. The
     * notion of class equivalence differs whether we are at the page or line level.
     * 
     * @param layouts active layouts to iterate over
     * @return an iterator over sets of layouts of the same class
     */
    protected abstract Iterator<Iterator<L>> getClassIter(ActiveLayouts<L> layouts);

    /**
     * Returns the maximum adjustment ratio allowed for feasible breaks.
     * 
     * @return a threshold above which layouts are considered to be too much stretched
     */
    protected double getThreshold() {
        return 1.0;
    }

    /**
     * Computes the difference between the dimension of the part corresponding to the
     * given layout, and the space actually occupied by that latter.
     * 
     * @param layout a layout
     * @param p the penalty corresponding to the currently considered legal break
     * @return the difference between the part's dimension and the natural length of the
     * layout elements
     */
    protected abstract int computeDifference(L layout, Penalty p);

    /**
     * Returns the (sub-)layout of the given layout that holds progress information for
     * this kind of breaking. For page breaking this is the layout itself, for line
     * breaking this is the enclosed layout that holds line-level information.
     * 
     * @param layout a layout
     * @return the sub-layout corresponding to the kind of breaking performed by this
     * object.
     * @see LineLayout#getLineLayout()
     */
    protected abstract ProgressInfo getProgress(L layout);  // TODO redundant with Breaker#getLayout

    /**
     * Returns the demerits associated to the given layout, for the kind of breaking
     * performed by this object (line-level or block-level).
     * 
     * @param layout a layout
     * @return the layout's demerits
     */
    protected abstract double getDemerits(L layout);

    /**
     * Creates and returns a new layout based on the given best break.
     * 
     * @param best the best layout for the currently considered layout class
     * @param alternatives alternative feasible layouts
     * @return a layout for the new part
     */
    protected abstract L createLayout(BestBreak<L> best, Collection<Layout> alternatives);

    private double computeAdjustmentRatio(ProgressInfo progress, int difference) {
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

    private double computeDemerits(Penalty penalty, double ratio) {
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
