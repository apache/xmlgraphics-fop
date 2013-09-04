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

import java.util.List;
import java.util.ListIterator;

import org.apache.fop.traits.MinOptMax;
import org.apache.fop.util.ListUtil;

/**
 * Utilities for Knuth element lists.
 */
public final class ElementListUtils {

    private ElementListUtils() {
        // Utility class.
    }

    /**
     * Removes legal breaks in an element list. A constraint can be specified to limit the
     * range in which the breaks are removed. Legal breaks occuring before at least
     * constraint.opt space is filled will be removed.
     * @param elements the element list
     * @param constraint min/opt/max value to restrict the range in which the breaks are removed.
     * @return true if the opt constraint is bigger than the list contents
     */
    public static boolean removeLegalBreaks(List elements, MinOptMax constraint) {
        return removeLegalBreaks(elements, constraint.getOpt());
    }

    /**
     * Removes legal breaks in an element list. A constraint can be specified to limit the
     * range in which the breaks are removed. Legal breaks occuring before at least
     * constraint space is filled will be removed.
     * @param elements the element list
     * @param constraint value to restrict the range in which the breaks are removed.
     * @return true if the constraint is bigger than the list contents
     */
    public static boolean removeLegalBreaks(List elements, int constraint) {
        return removeLegalBreaks(elements, constraint, false);
    }

    /**
     * Removes legal breaks in an element list. A constraint can be specified to limit the
     * range in which the breaks are removed. Legal breaks within the space specified through the
     * constraint (starting from the end of the element list) will be removed.
     * @param elements the element list
     * @param constraint value to restrict the range in which the breaks are removed.
     * @return true if the constraint is bigger than the list contents
     */
    public static boolean removeLegalBreaksFromEnd(List elements, int constraint) {
        return removeLegalBreaks(elements, constraint, true);
    }

    private static boolean removeLegalBreaks(List elements, int constraint, boolean fromEnd) {

        int len = 0;
        ListElement el;

        for (ListIterator iter = elements.listIterator(fromEnd ? elements.size() : 0);
                (fromEnd ? iter.hasPrevious() : iter.hasNext());) {

            if (fromEnd) {
                el = (ListElement) iter.previous();
            } else {
                el = (ListElement) iter.next();
            }

            if (el.isPenalty()) {
                KnuthPenalty penalty = (KnuthPenalty)el;
                //Convert penalty to break inhibitor
                if (penalty.getPenalty() < KnuthPenalty.INFINITE) {
                    iter.set(new KnuthPenalty(penalty.getWidth(), KnuthPenalty.INFINITE,
                            penalty.isPenaltyFlagged(), penalty.getPosition(),
                            penalty.isAuxiliary()));
                }
            } else if (el.isGlue()) {
                KnuthGlue glue = (KnuthGlue)el;
                len += glue.getWidth();
                //check if previous is a box
                if (!fromEnd) {
                    iter.previous();
                }
                el = (ListElement)iter.previous();
                iter.next();
                if (el.isBox()) {
                    //add break inhibitor
                    iter.add(new KnuthPenalty(0, KnuthPenalty.INFINITE, false,
                            null, false));
                }
                if (!fromEnd) {
                    iter.next();
                }
            } else if (el.isUnresolvedElement()) {
                if (el instanceof BreakElement) {
                    BreakElement breakEl = (BreakElement)el;
                    if (breakEl.getPenaltyValue() < KnuthPenalty.INFINITE) {
                        breakEl.setPenaltyValue(KnuthPenalty.INFINITE);
                    }
                } else if (el instanceof UnresolvedListElementWithLength) {
                    UnresolvedListElementWithLength uel = (UnresolvedListElementWithLength)el;
                    len += uel.getLength().getOpt();
                }
            } else {
                KnuthElement kel = (KnuthElement)el;
                len += kel.getWidth();
            }

            if (len >= constraint) {
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
                len += ((KnuthElement)el).getWidth();
            } else if (el.isGlue()) {
                len += ((KnuthElement)el).getWidth();
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
     * Calculates the full content length of the given element list. Warning: It doesn't take any
     * stretch and shrink possibilities into account.
     * @param elems the element list
     * @return the content length
     */
    public static int calcFullContentLength(List elems) {
        ListIterator iter = elems.listIterator(0);
        int count = elems.size();
        int len = 0;
        while (iter.hasNext()) {
            ListElement el = (ListElement)iter.next();
            if (el.isBox()) {
                len += ((KnuthElement)el).getWidth();
            } else if (el.isGlue()) {
                len += ((KnuthElement)el).getWidth();
            } else if (el.isUnresolvedElement() && !(el instanceof BreakElement)) {
                // Handle properties space-before and space-after
                UnresolvedListElementWithLength uresolvedEl = (UnresolvedListElementWithLength)el;
                if (uresolvedEl.isLast() || uresolvedEl.isFirst()) {
                    len += uresolvedEl.getLength().getOpt();
                }
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
    public static boolean endsWithForcedBreak(List elems) {
        return ((ListElement) ListUtil.getLast(elems)).isForcedBreak();
    }

    /**
     * Indicates whether the given element list starts with a forced break.
     * @param elems the element list
     * @return true if the list starts with a forced break
     */
    public static boolean startsWithForcedBreak(List elems) {
        return !elems.isEmpty() && ((ListElement) elems.get(0)).isForcedBreak();
    }

    /**
     * Indicates whether the given element list ends with a penalty with a non-infinite penalty
     * value.
     * @param elems the element list
     * @return true if the list ends with a non-infinite penalty
     */
    public static boolean endsWithNonInfinitePenalty(List elems) {
        ListElement last = (ListElement) ListUtil.getLast(elems);
        if (last.isPenalty() && ((KnuthPenalty)last).getPenalty() < KnuthElement.INFINITE) {
            return true;
        } else if (last instanceof BreakElement
                && ((BreakElement)last).getPenaltyValue() < KnuthElement.INFINITE) {
            return true;
        }
        return false;
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
            if (el.isPenalty() && el.getPenalty() < KnuthElement.INFINITE) {
                break;
            }
            prevBreak--;
        }
        return prevBreak;
    }

}
