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

package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.traits.MinOptMax;

/**
 * This class resolves spaces and conditional borders and paddings by replacing the
 * UnresolvedListElements descendants by the right combination of KnuthElements on an element
 * list.
 */
public class SpaceResolver {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(SpaceResolver.class);
    
    private UnresolvedListElementWithLength[] firstPart;
    private BreakElement breakPoss;
    private UnresolvedListElementWithLength[] secondPart;
    private UnresolvedListElementWithLength[] noBreak;
    
    private MinOptMax[] firstPartLengths;
    private MinOptMax[] secondPartLengths;
    private MinOptMax[] noBreakLengths;
    
    private boolean isFirst;
    private boolean isLast;
    
    /**
     * Main constructor.
     * @param first Element list before a break (optional)
     * @param breakPoss Break possibility (optional)
     * @param second Element list after a break (or if no break possibility in vicinity)
     * @param isFirst Resolution at the beginning of a (full) element list
     * @param isLast Resolution at the end of a (full) element list
     */
    private SpaceResolver(List first, BreakElement breakPoss, List second, 
            boolean isFirst, boolean isLast) {
        this.isFirst = isFirst;
        this.isLast = isLast;
        //Create combined no-break list
        int c = 0;
        if (first != null) {
            c += first.size();
        }
        if (second != null) {
            c += second.size();
        }
        noBreak = new UnresolvedListElementWithLength[c];
        noBreakLengths = new MinOptMax[c];
        int i = 0;
        ListIterator iter;
        if (first != null) {
            iter = first.listIterator();
            while (iter.hasNext()) {
                noBreak[i] = (UnresolvedListElementWithLength)iter.next();
                noBreakLengths[i] = noBreak[i].getLength();
                i++;
            }
        }
        if (second != null) {
            iter = second.listIterator();
            while (iter.hasNext()) {
                noBreak[i] = (UnresolvedListElementWithLength)iter.next();
                noBreakLengths[i] = noBreak[i].getLength();
                i++;
            }
        }

        //Add pending elements from higher level FOs
        if (breakPoss != null) {
            if (breakPoss.getPendingAfterMarks() != null) {
                if (log.isTraceEnabled()) {
                    log.trace("    adding pending before break: " 
                            + breakPoss.getPendingAfterMarks());
                }
                first.addAll(0, breakPoss.getPendingAfterMarks());
            }
            if (breakPoss.getPendingBeforeMarks() != null) {
                if (log.isTraceEnabled()) {
                    log.trace("    adding pending after break: " 
                            + breakPoss.getPendingBeforeMarks());
                }
                second.addAll(0, breakPoss.getPendingBeforeMarks());
            }
        }
        if (log.isTraceEnabled()) {
            log.trace("before: " + first);
            log.trace("  break: " + breakPoss);
            log.trace("after: " + second);
            log.trace("NO-BREAK: " + toString(noBreak, noBreakLengths));
        }

        if (first != null) {
            firstPart = new UnresolvedListElementWithLength[first.size()];
            firstPartLengths = new MinOptMax[firstPart.length];
            first.toArray(firstPart);
            for (i = 0; i < firstPart.length; i++) {
                firstPartLengths[i] = firstPart[i].getLength();
            }
        }
        this.breakPoss = breakPoss;
        if (second != null) {
            secondPart = new UnresolvedListElementWithLength[second.size()];
            secondPartLengths = new MinOptMax[secondPart.length];
            second.toArray(secondPart);
            for (i = 0; i < secondPart.length; i++) {
                secondPartLengths[i] = secondPart[i].getLength();
            }
        }
        resolve();
    }
    
    private String toString(Object[] arr1, Object[] arr2) {
        if (arr1.length != arr2.length) {
            new IllegalArgumentException("The length of both arrays must be equal");
        }
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < arr1.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.valueOf(arr1[i]));
            sb.append("/");
            sb.append(String.valueOf(arr2[i]));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private void removeConditionalBorderAndPadding(
                UnresolvedListElement[] elems, MinOptMax[] lengths, boolean reverse) {
        for (int i = 0; i < elems.length; i++) {
            int effIndex;
            if (reverse) {
                effIndex = elems.length - 1 - i;
            } else {
                effIndex = i;
            }
            if (elems[effIndex] instanceof BorderOrPaddingElement) {
                BorderOrPaddingElement bop = (BorderOrPaddingElement)elems[effIndex];
                if (bop.isConditional() && !(bop.isFirst() || bop.isLast())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Nulling conditional element: " + bop);
                    }
                    lengths[effIndex] = null;
                }
            }
        }
        if (log.isTraceEnabled() && elems.length > 0) {
            log.trace("-->Resulting list: " + toString(elems, lengths));
        }
    }
    
    private void performSpaceResolutionRule1(UnresolvedListElement[] elems, MinOptMax[] lengths,
                    boolean reverse) {
        for (int i = 0; i < elems.length; i++) {
            int effIndex;
            if (reverse) {
                effIndex = elems.length - 1 - i;
            } else {
                effIndex = i;
            }
            if (lengths[effIndex] == null) {
                //Zeroed border or padding doesn't create a fence
                continue;
            } else if (elems[effIndex] instanceof BorderOrPaddingElement) {
                //Border or padding form fences!
                break;
            } else if (!elems[effIndex].isConditional()) {
                break;
            }
            if (log.isDebugEnabled()) {
                log.debug("Nulling conditional element using 4.3.1, rule 1: " + elems[effIndex]);
            }
            lengths[effIndex] = null;
        }
        if (log.isTraceEnabled() && elems.length > 0) {
            log.trace("-->Resulting list: " + toString(elems, lengths));
        }
    }

    private void performSpaceResolutionRules2to3(UnresolvedListElement[] elems, 
            MinOptMax[] lengths, int start, int end) {
        if (log.isTraceEnabled()) {
            log.trace("rule 2-3: " + start + "-" + end);
        }
        SpaceElement space;
        int remaining;
        
        //Rule 2 (4.3.1, XSL 1.0)
        boolean hasForcing = false;
        remaining = 0;
        for (int i = start; i <= end; i++) {
            if (lengths[i] == null) {
                continue;
            }
            remaining++;
            space = (SpaceElement)elems[i];
            if (space.isForcing()) {
                hasForcing = true;
                break;
            }
        }
        if (remaining == 0) {
            return; //shortcut
        }
        if (hasForcing) {
            for (int i = start; i <= end; i++) {
                if (lengths[i] == null) {
                    continue;
                }
                space = (SpaceElement)elems[i];
                if (!space.isForcing()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Nulling non-forcing space-specifier using 4.3.1, rule 2: " 
                                + elems[i]);
                    }
                    lengths[i] = null;
                }
            }
            return; //If rule is triggered skip rule 3
        }
        
        //Rule 3 (4.3.1, XSL 1.0)
        //Determine highes precedence
        int highestPrecedence = Integer.MIN_VALUE;
        for (int i = start; i <= end; i++) {
            if (lengths[i] == null) {
                continue;
            }
            space = (SpaceElement)elems[i];
            highestPrecedence = Math.max(highestPrecedence, space.getPrecedence());
        }
        if (highestPrecedence != 0 && log.isDebugEnabled()) {
            log.debug("Highest precedence is " + highestPrecedence);
        }
        //Suppress space-specifiers with lower precedence
        remaining = 0;
        int greatestOptimum = Integer.MIN_VALUE;
        for (int i = start; i <= end; i++) {
            if (lengths[i] == null) {
                continue;
            }
            space = (SpaceElement)elems[i];
            if (space.getPrecedence() != highestPrecedence) {
                if (log.isDebugEnabled()) {
                    log.debug("Nulling space-specifier with precedence " 
                            + space.getPrecedence() + " using 4.3.1, rule 3: " 
                            + elems[i]);
                }
                lengths[i] = null;
            } else {
                greatestOptimum = Math.max(greatestOptimum, space.getLength().opt);
                remaining++;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Greatest optimum: " + greatestOptimum);
        }
        if (remaining <= 1) {
            return;
        }
        //Suppress space-specifiers with smaller optimum length
        remaining = 0;
        for (int i = start; i <= end; i++) {
            if (lengths[i] == null) {
                continue;
            }
            space = (SpaceElement)elems[i];
            if (space.getLength().opt < greatestOptimum) {
                if (log.isDebugEnabled()) {
                    log.debug("Nulling space-specifier with smaller optimum length "
                            + "using 4.3.1, rule 3: " 
                            + elems[i]);
                }
                lengths[i] = null;
            } else {
                remaining++;
            }
        }
        if (remaining <= 1) {
            return;
        }
        //Construct resolved space-specifier from the remaining spaces
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        for (int i = start; i <= end; i++) {
            if (lengths[i] == null) {
                continue;
            }
            space = (SpaceElement)elems[i];
            min = Math.max(min, space.getLength().min);
            max = Math.min(max, space.getLength().max);
            if (remaining > 1) {
                if (log.isDebugEnabled()) {
                    log.debug("Nulling non-last space-specifier using 4.3.1, rule 3, second part: " 
                            + elems[i]);
                }
                lengths[i] = null;
                remaining--;
            } else {
                lengths[i].min = min;
                lengths[i].max = max;
            }
        }

        if (log.isTraceEnabled() && elems.length > 0) {
            log.trace("Remaining spaces: " + remaining);
            log.trace("-->Resulting list: " + toString(elems, lengths));
        }
    }
    
    private void performSpaceResolutionRules2to3(UnresolvedListElement[] elems,
            MinOptMax[] lengths) {
        int start = 0;
        int i = start;
        while (i < elems.length) {
            if (elems[i] instanceof SpaceElement) {
                while (i < elems.length) {
                    if (elems[i] == null || elems[i] instanceof SpaceElement) {
                        i++;
                    } else {
                        break;
                    }
                }
                performSpaceResolutionRules2to3(elems, lengths, start, i - 1);
            }
            i++;
            start = i;
        }
    }
    
    private boolean hasFirstPart() {
        return firstPart != null && firstPart.length > 0;
    }
    
    private boolean hasSecondPart() {
        return secondPart != null && secondPart.length > 0;
    }
    
    private void resolve() {
        if (breakPoss != null) {
            if (hasFirstPart()) {
                removeConditionalBorderAndPadding(firstPart, firstPartLengths, true);
                performSpaceResolutionRule1(firstPart, firstPartLengths, true);
                performSpaceResolutionRules2to3(firstPart, firstPartLengths);
            }
            if (hasSecondPart()) {
                removeConditionalBorderAndPadding(secondPart, secondPartLengths, false);
                performSpaceResolutionRule1(secondPart, secondPartLengths, false);
                performSpaceResolutionRules2to3(secondPart, secondPartLengths);
            }
            if (noBreak != null) {
                performSpaceResolutionRules2to3(noBreak, noBreakLengths);
            }
        } else {
            if (isFirst) {
                removeConditionalBorderAndPadding(secondPart, secondPartLengths, false);
                performSpaceResolutionRule1(secondPart, secondPartLengths, false);
            }
            if (isLast) {
                removeConditionalBorderAndPadding(firstPart, firstPartLengths, true);
                performSpaceResolutionRule1(firstPart, firstPartLengths, true);
            }
            
            if (hasFirstPart()) {
                //Now that we've handled isFirst/isLast conditions, we need to look at the
                //active part in its normal order so swap it back.
                log.trace("Swapping first and second parts.");
                UnresolvedListElementWithLength[] tempList;
                MinOptMax[] tempLengths;
                tempList = secondPart;
                tempLengths = secondPartLengths;
                secondPart = firstPart;
                secondPartLengths = firstPartLengths;
                firstPart = tempList;
                firstPartLengths = tempLengths;
                if (hasFirstPart()) {
                    throw new IllegalStateException("Didn't expect more than one parts in a"
                            + "no-break condition.");
                }
            }
            performSpaceResolutionRules2to3(secondPart, secondPartLengths);
        }
    }
    
    private MinOptMax sum(MinOptMax[] lengths) {
        MinOptMax sum = new MinOptMax();
        for (int i = 0; i < lengths.length; i++) {
            if (lengths[i] != null) {
                sum.add(lengths[i]);
            }
        }
        return sum;
    }
    
    private void generate(ListIterator iter) {
        MinOptMax noBreakLength = new MinOptMax();
        MinOptMax glue1; //space before break possibility if break occurs
        //MinOptMax glue2; //difference between glue 1 and 3 when no break occurs
        MinOptMax glue3; //space after break possibility if break occurs
        glue1 = sum(firstPartLengths);
        glue3 = sum(secondPartLengths);
        noBreakLength = sum(noBreakLengths);
        
        //This doesn't produce the right glue2
        //glue2 = new MinOptMax(noBreakLength);
        //glue2.subtract(glue1);
        //glue2.subtract(glue3);
        
        int glue2w = noBreakLength.opt - glue1.opt - glue3.opt;
        int glue2stretch = (noBreakLength.max - noBreakLength.opt);
        int glue2shrink = (noBreakLength.opt - noBreakLength.min);
        glue2stretch -= glue1.max - glue1.opt;
        glue2stretch -= glue3.max - glue3.opt;
        glue2shrink -= glue1.opt - glue1.min;
        glue2shrink -= glue3.opt - glue3.min;
        
        boolean hasPrecedingNonBlock = false;
        if (log.isDebugEnabled()) {
            log.debug("noBreakLength=" + noBreakLength 
                    + ", glue1=" + glue1 
                    + ", glue2=" + glue2w + "+" + glue2stretch + "-" + glue2shrink 
                    + ", glue3=" + glue3);
        }
        if (breakPoss != null) {
            boolean forcedBreak = breakPoss.isForcedBreak();
            if (glue1.isNonZero()) {
                iter.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, 
                        false, (Position)null, true));
                iter.add(new KnuthGlue(glue1.opt, glue1.max - glue1.opt, glue1.opt - glue1.min, 
                        (Position)null, true));
                if (forcedBreak) {
                    //Otherwise, the preceding penalty and glue will be cut off
                    iter.add(new KnuthBox(0, (Position)null, true));
                }
            }
            iter.add(new KnuthPenalty(breakPoss.getPenaltyWidth(), breakPoss.getPenaltyValue(), 
                    false, breakPoss.getBreakClass(), 
                    new SpaceHandlingBreakPosition(this, breakPoss), false));
            if (breakPoss.getPenaltyValue() <= -KnuthPenalty.INFINITE) {
                return; //return early. Not necessary (even wrong) to add additional elements
            }
            if (glue2w != 0 || glue2stretch != 0 || glue2shrink != 0) {
                iter.add(new KnuthGlue(glue2w, glue2stretch, glue2shrink, 
                        (Position)null, true));
            }
        } else {
            if (glue1.isNonZero()) {
                throw new IllegalStateException("glue1 should be 0 in this case");
            }
        }
        Position pos = null;
        if (breakPoss == null) {
            pos = new SpaceHandlingPosition(this);
        }
        if (glue3.isNonZero() || pos != null) {
            iter.add(new KnuthBox(0, pos, true));
        }
        if (glue3.isNonZero()) {
            iter.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, 
                    false, (Position)null, true));
            iter.add(new KnuthGlue(glue3.opt, glue3.max - glue3.opt, glue3.opt - glue3.min, 
                    (Position)null, true));
            hasPrecedingNonBlock = true;
        }
        if (isLast && hasPrecedingNonBlock) {
            //Otherwise, the preceding penalty and glue will be cut off
            iter.add(new KnuthBox(0, (Position)null, true));
        }
    }
    
    /**
     * Position class for break possibilities. It is used to notify layout manager about the
     * effective spaces and conditional lengths.
     */
    public static class SpaceHandlingBreakPosition extends Position {

        private SpaceResolver resolver;
        private Position originalPosition;
        
        /**
         * Main constructor.
         * @param resolver the space resolver that provides the info about the actual situation
         * @param breakPoss the original break possibility that creates this Position 
         */
        public SpaceHandlingBreakPosition(SpaceResolver resolver, BreakElement breakPoss) {
            super(null);
            this.resolver = resolver;
            this.originalPosition = breakPoss.getPosition();
            //Unpack since the SpaceHandlingBreakPosition is a non-wrapped Position, too
            while (this.originalPosition instanceof NonLeafPosition) {
                this.originalPosition = this.originalPosition.getPosition();
            }
        }
        
        /** @return the space resolver */
        public SpaceResolver getSpaceResolver() {
            return this.resolver;
        }
        
        /**
         * Notifies all affected layout managers about the current situation in the part to be
         * handled for area generation.
         * @param isBreakSituation true if this is a break situation.
         * @param side defines to notify about the situation whether before or after the break.
         *             May be null if isBreakSituation is null. 
         */
        public void notifyBreakSituation(boolean isBreakSituation, RelSide side) {
            if (isBreakSituation) {
                if (RelSide.BEFORE == side) {
                    for (int i = 0; i < resolver.secondPart.length; i++) {
                        resolver.secondPart[i].notifyLayoutManager(resolver.secondPartLengths[i]);
                    }
                } else {
                    for (int i = 0; i < resolver.firstPart.length; i++) {
                        resolver.firstPart[i].notifyLayoutManager(resolver.firstPartLengths[i]);
                    }
                }
            } else {
                for (int i = 0; i < resolver.noBreak.length; i++) {
                    resolver.noBreak[i].notifyLayoutManager(resolver.noBreakLengths[i]);
                }
            }
        }
        
        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("SpaceHandlingBreakPosition(");
            sb.append(this.originalPosition);
            sb.append(")");
            return sb.toString();
        }

        /** 
         * @return the original Position instance set at the BreakElement that this Position was
         *         created for.
         */
        public Position getOriginalBreakPosition() {
            return this.originalPosition;
        }
    }
    
    /**
     * Position class for no-break situations. It is used to notify layout manager about the
     * effective spaces and conditional lengths.
     */
    public static class SpaceHandlingPosition extends Position {

        private SpaceResolver resolver;
        
        /**
         * Main constructor.
         * @param resolver the space resolver that provides the info about the actual situation
         */
        public SpaceHandlingPosition(SpaceResolver resolver) {
            super(null);
            this.resolver = resolver;
        }
        
        /** @return the space resolver */
        public SpaceResolver getSpaceResolver() {
            return this.resolver;
        }
        
        /**
         * Notifies all affected layout managers about the current situation in the part to be
         * handled for area generation.
         */
        public void notifySpaceSituation() {
            if (resolver.breakPoss != null) {
                throw new IllegalStateException("Only applicable to no-break situations");
            }
            for (int i = 0; i < resolver.secondPart.length; i++) {
                resolver.secondPart[i].notifyLayoutManager(resolver.secondPartLengths[i]);
            }
        }
        
        /** {@inheritDoc} */
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("SpaceHandlingPosition");
            return sb.toString();
        }
    }
    
    /**
     * Resolves unresolved elements applying the space resolution rules defined in 4.3.1.
     * @param elems the element list
     */
    public static void resolveElementList(LinkedList elems) {
        if (log.isTraceEnabled()) {
            log.trace(elems);
        }
        boolean first = true;
        boolean last = false;
        boolean skipNextElement = false;
        List unresolvedFirst = new java.util.ArrayList();
        List unresolvedSecond = new java.util.ArrayList();
        List currentGroup;
        ListIterator iter = elems.listIterator();
        while (iter.hasNext()) {
            ListElement el = (ListElement)iter.next();
            if (el.isUnresolvedElement()) {
                if (log.isTraceEnabled()) {
                    log.trace("unresolved found: " + el + " " + first + "/" + last);
                }
                BreakElement breakPoss = null;
                //Clear temp lists
                unresolvedFirst.clear();
                unresolvedSecond.clear();
                //Collect groups
                if (el instanceof BreakElement) {
                    breakPoss = (BreakElement)el;
                    currentGroup = unresolvedSecond;
                } else {
                    currentGroup = unresolvedFirst;
                    currentGroup.add(el);
                }
                iter.remove();
                last = true;
                skipNextElement = true;
                while (iter.hasNext()) {
                    el = (ListElement)iter.next();
                    if (el instanceof BreakElement && breakPoss != null) {
                        skipNextElement = false;
                        last = false;
                        break;
                    } else if (currentGroup == unresolvedFirst && (el instanceof BreakElement)) {
                        breakPoss = (BreakElement)el;
                        iter.remove();
                        currentGroup = unresolvedSecond;
                    } else if (el.isUnresolvedElement()) {
                        currentGroup.add(el);
                        iter.remove();
                    } else {
                        last = false;
                        break;
                    }
                }
                //last = !iter.hasNext();
                if (breakPoss == null && unresolvedSecond.size() == 0 && !last) {
                    log.trace("Swap first and second parts in no-break condition,"
                            + " second part is empty.");
                    //The first list is reversed, so swap if this shouldn't happen
                    List swapList = unresolvedSecond;
                    unresolvedSecond = unresolvedFirst;
                    unresolvedFirst = swapList;
                }
                
                log.debug("----start space resolution (first=" + first + ", last=" + last + ")...");
                SpaceResolver resolver = new SpaceResolver(
                        unresolvedFirst, breakPoss, unresolvedSecond, first, last);
                if (!last) {
                    iter.previous();
                }
                resolver.generate(iter);
                if (!last && skipNextElement) {
                    iter.next();
                }
                log.debug("----end space resolution.");
            }
            first = false;
        }
    }
    
    /**
     * Inspects an effective element list and notifies all layout managers about the state of
     * the spaces and conditional lengths.
     * @param effectiveList the effective element list
     * @param startElementIndex index of the first element in the part to be processed
     * @param endElementIndex index of the last element in the part to be processed
     * @param prevBreak index of the the break possibility just before this part (used to
     *                  identify a break condition, lastBreak <= 0 represents a no-break condition)
     */
    public static void performConditionalsNotification(List effectiveList, 
            int startElementIndex, int endElementIndex, int prevBreak) {
        KnuthElement el = null;
        if (prevBreak > 0) {
            el = (KnuthElement)effectiveList.get(prevBreak);
        }
        SpaceResolver.SpaceHandlingBreakPosition beforeBreak = null;
        SpaceResolver.SpaceHandlingBreakPosition afterBreak = null;
        if (el != null && el.isPenalty()) {
            Position pos = el.getPosition();
            if (pos instanceof SpaceResolver.SpaceHandlingBreakPosition) {
                beforeBreak = (SpaceResolver.SpaceHandlingBreakPosition)pos; 
                beforeBreak.notifyBreakSituation(true, RelSide.BEFORE);
            }
        }
        el = (KnuthElement)effectiveList.get(endElementIndex);
        if (el != null && el.isPenalty()) {
            Position pos = el.getPosition();
            if (pos instanceof SpaceResolver.SpaceHandlingBreakPosition) {
                afterBreak = (SpaceResolver.SpaceHandlingBreakPosition)pos; 
                afterBreak.notifyBreakSituation(true, RelSide.AFTER);
            }
        }
        for (int i = startElementIndex; i <= endElementIndex; i++) {
            Position pos = ((KnuthElement)effectiveList.get(i)).getPosition();
            if (pos instanceof SpaceResolver.SpaceHandlingPosition) {
                ((SpaceResolver.SpaceHandlingPosition)pos).notifySpaceSituation();
            } else if (pos instanceof SpaceResolver.SpaceHandlingBreakPosition) {
                SpaceResolver.SpaceHandlingBreakPosition noBreak;
                noBreak = (SpaceResolver.SpaceHandlingBreakPosition)pos;
                if (noBreak != beforeBreak && noBreak != afterBreak) {
                    noBreak.notifyBreakSituation(false, null);
                }
            }
        }
    }
    
    

}
