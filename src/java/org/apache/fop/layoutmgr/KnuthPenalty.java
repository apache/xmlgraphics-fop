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

import org.apache.fop.fo.Constants;

/**
 * An instance of this class represents information about a feasible
 * breaking point; it does not represent any piece of content.
 *
 * A KnuthPenalty is a feasible breaking point unless its value is infinity;
 * a KnuthPenalty whose value is -infinity represents a forced break.
 *
 * A KnuthPenalty is suppressed, and its width is ignored, if it is not a
 * chosen breaking point; for example, a KnuthPenalty representing a
 * hyphenation point has a width (the "-" width), which must be ignored if
 * that point is not chosen as a breaking point.
 *
 * Besides the inherited methods and attributes, this class has two more
 * attributes and the methods used to get them: the penalty value, which is
 * a kind of "aesthetic cost" (the higher the value, the more unsightly the
 * breaking point), and a boolean that marks KnuthPenalties which should not
 * be chosen as breaking points for consecutive lines.
 */
public class KnuthPenalty extends KnuthElement {

    /** Used for flagged penalties. See Knuth algorithm. */
    public static final int FLAGGED_PENALTY = 50;
    /** Dummy, zero-width penalty */
    public static final KnuthPenalty DUMMY_ZERO_PENALTY
            = new KnuthPenalty(0, 0, false, null, true);

    private int penalty;
    private boolean penaltyFlagged;
    private int breakClass = -1;

    /**
     * Create a new KnuthPenalty.
     *
     * @param width the width of this penalty
     * @param penalty the penalty value of this penalty
     * @param penaltyFlagged is this penalty flagged?
     * @param pos the Position stored in this penalty
     * @param auxiliary is this penalty auxiliary?
     */
    public KnuthPenalty(int width, int penalty, boolean penaltyFlagged, Position pos,
                        boolean auxiliary) {
        super(width, pos, auxiliary);
        this.penalty = penalty;
        this.penaltyFlagged = penaltyFlagged;
    }

    /**
     * Create a new KnuthPenalty.
     *
     * @param width the width of this penalty
     * @param penalty the penalty value of this penalty
     * @param penaltyFlagged is this penalty flagged?
     * @param breakClass the break class of this penalty (one of
     * {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE})
     * @param pos the Position stored in this penalty
     * @param isAuxiliary is this penalty auxiliary?
     */
    public KnuthPenalty(int width, int penalty, boolean penaltyFlagged, int breakClass,
                        Position pos, boolean isAuxiliary) {
        this(width, penalty, penaltyFlagged, pos, isAuxiliary);
        this.breakClass = breakClass;
    }

    private static String getBreakClassName(int breakClass) {
        return AbstractBreaker.getBreakClassName(breakClass);
    }

    /**
     * Get the penalty's value as a {@link java.lang.String}.
     * (Mainly used in {@link #toString()} methods, to improve readability
     * of the trace logs.)
     *
     * TODO: shouldn't be penalty a class of its own?
     *
     * @param penaltyValue  the penalty value
     * @return  the penalty value as a {@link java.lang.String}
     */
    protected static String valueOf(int penaltyValue) {
        String result = (penaltyValue < 0) ? "-" : "";
        int tmpValue = Math.abs(penaltyValue);
        result += (tmpValue == KnuthElement.INFINITE)
                ? "INFINITE"
                : String.valueOf(tmpValue);
        return result;
    }

    /** {@inheritDoc} */
    public boolean isPenalty() {
        return true;
    }

    /**
     * @return the penalty value of this penalty.
     */
    public int getPenalty() {
        return penalty;
    }

    /**
     * Sets a new penalty value.
     * @param penalty the new penalty value
     */
    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    /** @return true is this penalty is a flagged one. */
    public boolean isPenaltyFlagged() {
        return penaltyFlagged;
    }

    /** {@inheritDoc} */
    public boolean isForcedBreak() {
        return penalty == -KnuthElement.INFINITE;
    }

    /**
     * @return the break class of this penalty (EN_AUTO, EN_COLUMN, EN_PAGE, EN_EVEN_PAGE,
     * EN_ODD_PAGE)
     */
    public int getBreakClass() {
        return breakClass;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer buffer = new StringBuffer(64);
        if (isAuxiliary()) {
            buffer.append("aux. ");
        }
        buffer.append("penalty");
        buffer.append(" p=");
        buffer.append(valueOf(this.penalty));
        if (this.penaltyFlagged) {
            buffer.append(" [flagged]");
        }
        buffer.append(" w=");
        buffer.append(getWidth());
        if (isForcedBreak()) {
            buffer.append(" (forced break, ")
                    .append(getBreakClassName(this.breakClass))
                    .append(")");
        } else if (this.penalty >= 0 && this.breakClass != -1) {
            //penalty corresponding to a keep constraint
            buffer.append(" (keep constraint, ")
                    .append(getBreakClassName(this.breakClass))
                    .append(")");
        }
        return buffer.toString();
    }
}
