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

import org.apache.fop.fo.Constants;

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
        this(position, 0, penaltyValue, -1, context);
    }
    
    /**
     * Constructor for hard breaks.
     * @param position the Position instance needed by the addAreas stage of the LMs.
     * @param penaltyWidth the penalty width
     * @param penaltyValue the penalty value for the penalty element to be constructed
     * @param breakClass the break class of this penalty (one of the break-* constants)
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
    
    /** @see org.apache.fop.layoutmgr.UnresolvedListElement#isConditional() */
    public boolean isConditional() {
        return false; //Does not really apply here
    }

    /** @see org.apache.fop.layoutmgr.ListElement#isPenalty() */
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
    
    /** @see org.apache.fop.layoutmgr.ListElement#isForcedBreak() */
    public boolean isForcedBreak() {
        return penaltyValue == -KnuthElement.INFINITE;
    }
    
    /** @return the break class of this penalty (one of the break-* constants) */
    public int getBreakClass() {
        return breakClass;
    }
    
    /**
     * Sets the break class.
     * @param breakClass the new break class
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
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("BreakPossibility[p:");
        sb.append(this.penaltyValue);
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
        sb.append("; w:");
        sb.append(penaltyWidth);
        sb.append("]");
        return sb.toString();
    }

}
