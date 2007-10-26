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

package org.apache.fop.layoutmgr.table;

import java.util.List;

import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.Position;

/**
 * This class represents a Position specific to TableContentLayoutManager. Used for normal
 * content cases.
 */
class TableContentPosition extends Position {

    /** The position is the first of the row group. */
    public static final int FIRST_IN_ROWGROUP = 1;
    /** The position is the last of the row group. */
    public static final int LAST_IN_ROWGROUP = 2;

    /** the list of GridUnitParts making up this position */
    protected List gridUnitParts;
    /** effective row this position belongs to */
    protected EffRow row;
    /** flags for the position */
    protected int flags;

    /**
     * Creates a new TableContentPosition.
     * @param lm applicable layout manager
     * @param gridUnitParts the list of GridUnitPart instances
     * @param row effective row this position belongs to
     */
    protected TableContentPosition(LayoutManager lm, List gridUnitParts,
            EffRow row) {
        super(lm);
        this.gridUnitParts = gridUnitParts;
        this.row = row;
    }

    /**
     * Returns a flag for this GridUnit.
     * @param which the requested flag
     * @return the value of the flag
     */
    public boolean getFlag(int which) {
        return (flags & (1 << which)) != 0;
    }

    /**
     * Sets a flag on a GridUnit.
     * @param which the flag to set
     * @param value the new value for the flag
     */
    public void setFlag(int which, boolean value) {
        if (value) {
            flags |= (1 << which); //set flag
        } else {
            flags &= ~(1 << which); //clear flag
        }
    }

    public boolean generatesAreas() {
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TableContentPosition:");
        sb.append(getIndex());
        sb.append("[");
        sb.append(row.getIndex()).append("/");
        sb.append(getFlag(FIRST_IN_ROWGROUP) ? "F" : "-");
        sb.append(getFlag(LAST_IN_ROWGROUP) ? "L" : "-").append("]");
        sb.append("(");
        sb.append(gridUnitParts);
        sb.append(")");
        return sb.toString();
    }
}
