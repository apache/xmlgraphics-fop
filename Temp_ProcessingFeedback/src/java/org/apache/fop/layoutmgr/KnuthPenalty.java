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

    private int penalty;
    private boolean bFlagged; 
    private int breakClass = -1;

    /**
     * Create a new KnuthPenalty.
     *
     * @param w the width of this penalty
     * @param p the penalty value of this penalty
     * @param f is this penalty flagged?
     * @param pos the Position stored in this penalty
     * @param bAux is this penalty auxiliary?
     */
    public KnuthPenalty(int w, int p, boolean f, Position pos, boolean bAux) {
        super(w, pos, bAux);
        penalty = p;
        bFlagged = f;
    }

    /**
     * Create a new KnuthPenalty.
     * 
     * @param w the width of this penalty
     * @param p the penalty value of this penalty
     * @param f is this penalty flagged?
     * @param iBreakClass the break class of this penalty (one of
     * {@link Constants#EN_AUTO}, {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE})
     * @param pos the Position stored in this penalty
     * @param bAux is this penalty auxiliary?
     */
    public KnuthPenalty(int w, int p, boolean f,
            int iBreakClass, Position pos, boolean bAux) {
        super(w, pos, bAux);
        penalty = p;
        bFlagged = f;
        breakClass = iBreakClass;
    }

    /** {@inheritDoc} */
    public boolean isPenalty() {
        return true;
    }

    /**
     * @return the penalty value of this penalty.
     */
    public int getP() {
        return penalty;
    }

    /**
     * Sets a new penalty value.
     * @param p the new penalty value
     */
    public void setP(int p) {
        this.penalty = p;
    }
    
    /** @return true is this penalty is a flagged one. */
    public boolean isFlagged() {
        return bFlagged;
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
    
    /**
     * Sets the break class for this penalty.
     * @param cl the break class (EN_AUTO, EN_COLUMN, EN_PAGE, EN_EVEN_PAGE, EN_ODD_PAGE)
     */
    public void setBreakClass(int cl) {
        this.breakClass = cl;
    }
    
    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        if (isAuxiliary()) {
            sb.append("aux. ");
        }
        sb.append("penalty");
        sb.append(" p=");
        if (getP() < 0) {
            sb.append("-");
        }
        if (Math.abs(getP()) == INFINITE) {
            sb.append("INFINITE");
        } else {
            sb.append(getP());
        }
        if (isFlagged()) {
            sb.append(" [flagged]");
        }
        sb.append(" w=");
        sb.append(getW());
        if (isForcedBreak()) {
            sb.append(" (forced break");
            switch (getBreakClass()) {
            case Constants.EN_PAGE:
                sb.append(", page");
                break;
            case Constants.EN_COLUMN:
                sb.append(", column");
                break;
            case Constants.EN_EVEN_PAGE:
                sb.append(", even page");
                break;
            case Constants.EN_ODD_PAGE:
                sb.append(", odd page");
                break;
            default:
            }
            sb.append(")");
        }
        return sb.toString();
    }
    
}
