/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.fop.traits.MinOptMax;

/**
 * Utilities for Knuth element lists.
 */
public class ElementListUtils {

    /**
     * Removes all legal breaks in an element list.
     * @param elements the element list
     */
    public static void removeLegalBreaks(LinkedList elements) {
        ListIterator i = elements.listIterator();
        while (i.hasNext()) {
            ListElement el = (ListElement)i.next();
            if (el.isPenalty()) {
                BreakElement breakPoss = (BreakElement)el;
                //Convert all penalties no break inhibitors
                if (breakPoss.getPenaltyValue() < KnuthPenalty.INFINITE) {
                    breakPoss.setPenaltyValue(KnuthPenalty.INFINITE);
                    /*
                    i.set(new KnuthPenalty(penalty.getW(), KnuthPenalty.INFINITE, 
                            penalty.isFlagged(), penalty.getPosition(), penalty.isAuxiliary()));
                    */
                }
            } else if (el.isGlue()) {
                i.previous();
                if (el.isBox()) {
                    i.next();
                    i.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, false, 
                            /*new Position(getTableLM())*/null, false));
                }
            }
        }
    }
    
    /**
     * Removes all legal breaks in an element list. A constraint can be specified to limit the
     * range in which the breaks are removed. Legal breaks occuring before at least 
     * constraint.opt space is filled will be removed.
     * @param elements the element list
     * @param constraint min/opt/max value to restrict the range in which the breaks are removed.
     * @return true if the opt constraint is bigger than the list contents
     */
    public static boolean removeLegalBreaks(LinkedList elements, MinOptMax constraint) {
        int len = 0;
        ListIterator i = elements.listIterator();
        while (i.hasNext()) {
            KnuthElement el = (KnuthElement)i.next();
            if (el.isPenalty()) {
                KnuthPenalty penalty = (KnuthPenalty)el;
                //Convert all penalties no break inhibitors
                if (penalty.getP() < KnuthPenalty.INFINITE) {
                    i.set(new KnuthPenalty(penalty.getW(), KnuthPenalty.INFINITE, 
                            penalty.isFlagged(), penalty.getPosition(), penalty.isAuxiliary()));
                }
            } else if (el.isGlue()) {
                len += el.getW();
                i.previous();
                if (el.isBox()) {
                    i.next();
                    i.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, false, 
                            /*new Position(getTableLM())*/null, false));
                }
            } else {
                len += el.getW();
            }
            if (len > constraint.opt) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Calculates the content length of the given element list. Warning: It doesn't take any
     * stretch and shrink possibilities into account.
     * @param elems the element list
     * @param start element at which to start
     * @param end element at which to stop
     * @return the content length
     */
    public static int calcContentLength(List elems, int start, int end) {
        ListIterator iter = elems.listIterator(start);
        int count = end - start + 1;
        int len = 0;
        while (iter.hasNext()) {
            ListElement el = (ListElement)iter.next();
            if (el.isBox()) {
                len += ((KnuthElement)el).getW();
            } else if (el.isGlue()) {
                len += ((KnuthElement)el).getW();
            } else {
                //log.debug("Ignoring penalty: " + el);
                //ignore penalties
            }
            count--;
            if (count == 0) {
                break;
            }
        }
        return len;
    }
    
    /**
     * Calculates the content length of the given element list. Warning: It doesn't take any
     * stretch and shrink possibilities into account.
     * @param elems the element list
     * @return the content length
     */
    public static int calcContentLength(List elems) {
        return calcContentLength(elems, 0, elems.size() - 1);
    }
    
    /**
     * Indicates whether the given element list ends with a forced break.
     * @param elems the element list
     * @return true if the list ends with a forced break
     */
    public static boolean endsWithForcedBreak(LinkedList elems) {
        ListElement last = (ListElement)elems.getLast();
        return last.isForcedBreak();
    }
    
    /**
     * Determines the position of the previous break before the start index on an
     * element list.
     * @param elems the element list
     * @param startIndex the start index
     * @return the position of the previous break, or -1 if there was no previous break
     */
    public static int determinePreviousBreak(List elems, int startIndex) {
        int prevBreak = startIndex - 1;
        while (prevBreak >= 0) {
            KnuthElement el = (KnuthElement)elems.get(prevBreak);
            if (el.isPenalty() && el.getP() < KnuthElement.INFINITE) {
                break;
            }
            prevBreak--;
        }
        return prevBreak;
    }
    
}
