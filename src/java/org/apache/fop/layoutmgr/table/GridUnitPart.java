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

/**
 * Represents a non-dividable part of a grid unit. Used by the table stepper.
 */
class GridUnitPart {

    /** Primary grid unit */
    protected PrimaryGridUnit pgu;
    /** Index of the starting element of this part */
    protected int start;
    /** Index of the ending element of this part */
    protected int end;

    /**
     * Creates a new GridUnitPart.
     * @param pgu Primary grid unit
     * @param start starting element
     * @param end ending element
     */
    protected GridUnitPart(PrimaryGridUnit pgu, int start, int end) {
        this.pgu = pgu;
        this.start = start;
        this.end = end;
    }

    /** @return true if this part is the first part of a cell */
    public boolean isFirstPart() {
        return (start == 0);
    }

    /** @return true if this part is the last part of a cell */
    public boolean isLastPart() {
        return (end >= 0 && end == pgu.getElements().size() - 1);
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer("Part: ");
        sb.append(start).append("-").append(end);
        sb.append(" [").append(isFirstPart() ? "F" : "-").append(isLastPart() ? "L" : "-");
        sb.append("] ").append(pgu);
        return sb.toString();
    }

    boolean mustKeepWithPrevious() {
        return pgu.getFlag(GridUnit.KEEP_WITH_PREVIOUS_PENDING)
                || (pgu.getRow() != null && pgu.getRow().mustKeepWithPrevious());
    }

}
