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

import org.apache.fop.fo.flow.table.PrimaryGridUnit;

/**
 * Represents a non-divisible part of a grid unit. Used by the table stepper.
 */
class CellPart {

    /** Primary grid unit */
    protected PrimaryGridUnit pgu;
    /** Index of the starting element of this part */
    protected int start;
    /** Index of the ending element of this part */
    protected int end;

    private int condBeforeContentLength;
    private int length;
    private int condAfterContentLength;
    private int bpBeforeNormal;
    private int bpBeforeFirst;
    private int bpAfterNormal;
    private int bpAfterLast;
    private boolean isLast;

    /**
     * Creates a new CellPart.
     *
     * @param pgu Primary grid unit
     * @param start starting element
     * @param end ending element
     * @param last true if this cell part is the last one for the cell
     * @param condBeforeContentLength length of the additional content that will have to
     * be displayed if this part will be the first one on the page
     * @param length length of the content represented by this cell part
     * @param condAfterContentLength length of the additional content that will have to be
     * displayed if this part will be the last one on the page
     * @param bpBeforeNormal width of border- and padding-before in the normal case
     * @param bpBeforeFirst width of (possibly optional) border- and padding-before if
     * this part will be the first one on the page
     * @param bpAfterNormal width of border- and padding-after in the normal case
     * @param bpAfterLast width of (possibly optional) border- and padding-after if this
     * part will be the last one on the page
     */
    protected CellPart(PrimaryGridUnit pgu, int start, int end, boolean last,
            int condBeforeContentLength, int length, int condAfterContentLength,
            int bpBeforeNormal, int bpBeforeFirst,
            int bpAfterNormal, int bpAfterLast) {
        this.pgu = pgu;
        this.start = start;
        this.end = end;
        this.isLast = last;
        this.condBeforeContentLength = condBeforeContentLength;
        this.length = length;
        this.condAfterContentLength = condAfterContentLength;
        this.bpBeforeNormal = bpBeforeNormal;
        this.bpBeforeFirst = bpBeforeFirst;
        this.bpAfterNormal = bpAfterNormal;
        this.bpAfterLast = bpAfterLast;
    }

    /** @return true if this part is the first part of a cell */
    public boolean isFirstPart() {
        return (start == 0);
    }

    /** @return true if this part is the last part of a cell */
    boolean isLastPart() {
        return isLast;
    }

    int getBorderPaddingBefore(boolean firstOnPage) {
        if (firstOnPage) {
            return bpBeforeFirst;
        } else {
            return bpBeforeNormal;
        }
    }

    int getBorderPaddingAfter(boolean lastOnPage) {
        if (lastOnPage) {
            return bpAfterLast;
        } else {
            return bpAfterNormal;
        }
    }

    int getConditionalBeforeContentLength() {
        return condBeforeContentLength;
    }

    int getLength() {
        return length;
    }

    int getConditionalAfterContentLength() {
        return condAfterContentLength;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer("Part: ");
        sb.append(start).append("-").append(end);
        sb.append(" [").append(isFirstPart() ? "F" : "-").append(isLastPart() ? "L" : "-");
        sb.append("] ").append(pgu);
        return sb.toString();
    }

}
