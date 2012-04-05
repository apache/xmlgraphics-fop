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

/**
 * This class represents an unresolved break possibility.
 */
public class BreakElement extends UnresolvedListElement {

    private int penaltyWidth;
    private int penaltyValue;
    private int breakClass = -1;
    private List pendingBeforeMarks;
    private List pendingAfterMarks;

    /**
     * Main constructor
     * @param position the Position instance needed by the addAreas stage of the LMs.
     * @param penaltyValue the penalty value for the penalty element to be constructed
     * @param context the layout context which contains the pending conditional elements
     */
    public BreakElement(Position position, int penaltyValue, LayoutContext context) {
        this(position, penaltyValue, -1, context);
    }

    /**
     * Create a new BreakElement for the given {@code position}, {@code penaltyValue}
     * and {@code breakClass}. (Used principally to generate break-possibilities in
     * ranges of content that must be kept together within the context corresponding
     * to the {@code breakClass}; expected to be one of {@link Constants#EN_AUTO},
     * {@link Constants#EN_LINE}, {@link Constants#EN_COLUMN} or {@link Constants#EN_PAGE})
     * @param position  the corresponding {@link Position}
     * @param penaltyValue  the penalty value
     * @param breakClass    the break class
     * @param context       the {@link LayoutContext}
     */
    public BreakElement(Position position, int penaltyValue, int breakClass,
                        LayoutContext context) {
        this(position, 0, penaltyValue, breakClass, context);
    }

    /**
     * Constructor for hard breaks.
     *
     * @param position the Position instance needed by the addAreas stage of the LMs.
     * @param penaltyWidth the penalty width
     * @param penaltyValue the penalty value for the penalty element to be constructed
     * @param breakClass the break class of this penalty (one of {@link Constants#EN_AUTO},
     * {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE})
     * @param context the layout context which contains the pending conditional elements
     */
    public BreakElement(Position position, int penaltyWidth, int penaltyValue,
                int breakClass, LayoutContext context) {
        super(position);
        this.penaltyWidth = penaltyWidth;
        this.penaltyValue = penaltyValue;
        this.breakClass = breakClass;
        this.pendingBeforeMarks = context.getPendingBeforeMarks();
        this.pendingAfterMarks = context.getPendingAfterMarks();
    }

    private static String getBreakClassName(int breakClass) {
        return AbstractBreaker.getBreakClassName(breakClass);
    }

    /** {@inheritDoc} */
    public boolean isConditional() {
        return false; //Does not really apply here
    }

    /** {@inheritDoc} */
    /*
    public boolean isPenalty() {
        return true; //not entirely true but a BreakElement will generate a penalty later
    }*/

    /** @return the penalty width */
    public int getPenaltyWidth() {
        return this.penaltyWidth;
    }

    /** @return the penalty value */
    public int getPenaltyValue() {
        return this.penaltyValue;
    }

    /**
     * Sets the penalty value.
     * @param p the new penalty value
     */
    public void setPenaltyValue(int p) {
        this.penaltyValue = p;
    }

    /** {@inheritDoc} */
    public boolean isForcedBreak() {
        return penaltyValue == -KnuthElement.INFINITE;
    }

    /**
     * Returns the break class of this penalty.
     *
     * @return one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN},
     * {@link Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE},
     * {@link Constants#EN_ODD_PAGE}
     */
    public int getBreakClass() {
        return breakClass;
    }

    /**
     * Sets the break class.
     *
     * @param breakClass one of {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN},
     * {@link Constants#EN_PAGE}, {@link Constants#EN_EVEN_PAGE},
     * {@link Constants#EN_ODD_PAGE}
     */
    public void setBreakClass(int breakClass) {
        this.breakClass = breakClass;
    }

    /** @return the pending border and padding elements at the before edge */
    public List getPendingBeforeMarks() {
        return this.pendingBeforeMarks;
    }

    /** @return the pending border and padding elements at the after edge */
    public List getPendingAfterMarks() {
        return this.pendingAfterMarks;
    }

    /**
     * Clears all pending marks associated with this break element. This is used in break
     * cases where we only know very late if the break is actually after all the content
     * of an FO has been generated.
     */
    public void clearPendingMarks() {
        this.pendingBeforeMarks = null;
        this.pendingAfterMarks = null;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("BreakPossibility[p:");
        sb.append(KnuthPenalty.valueOf(this.penaltyValue));
        if (isForcedBreak()) {
            sb.append(" (forced break, ")
                    .append(getBreakClassName(this.breakClass))
                    .append(")");
        } else if (this.penaltyValue >= 0 && this.breakClass != -1) {
            sb.append(" (keep constraint, ")
                    .append(getBreakClassName(this.breakClass))
                    .append(")");
        }
        sb.append("; w:");
        sb.append(penaltyWidth);
        sb.append("]");
        return sb.toString();
    }

}
