/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.area;

import java.util.List;

/**
 * The span-reference-area.
 * This is a block-area with 0 border and padding that is stacked
 * within the main-reference-area
 * This object holds one or more normal-flow-reference-area children
 * based on the column-count trait in effect for this span.
 * See fo:region-body definition in the XSL Rec for more information. 
 */
public class Span extends Area {
    // the list of flow reference areas in this span area
    private List flowAreas;
    private int height;
    private int colCount;
    private int colGap;
    private int colWidth; // width for each normal flow, calculated value

    /**
     * Create a span area with the number of columns for this span area.
     *
     * @param colCount the number of columns in the span
     * @param colGap the column gap between each column 
     * @param ipd the total ipd of the span 
     */
    public Span(int colCount, int colGap, int ipd) {
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
        this.colCount = colCount;
        this.colGap = colGap;
        this.ipd = ipd;
        createNormalFlows();
    }

    /**
     * Create the normal flows for this Span
     */
    private void createNormalFlows() {
        flowAreas = new java.util.ArrayList(colCount);        
        colWidth = (ipd - ((colCount - 1) * colGap)) / colCount;

        for (int i=0; i< colCount; i++) {
            NormalFlow newFlow = new NormalFlow(colWidth);
            newFlow.setIPD(getIPD());
            flowAreas.add(newFlow);
        }
    }

    /**
     * Get the column count for this span area.
     *
     * @return the number of columns defined for this span area
     */
    public int getColumnCount() {
        return colCount;
    }

    /**
     * Get the width of a single column within this Span
     *
     * @return the width of a single column
     */
    public int getColumnWidth() {
        return colWidth;
    }

    /**
     * Get the height of this span area.
     *
     * @return the height of this span area
     */
    public int getHeight() {
        return height;
    }


    /**
     * Get the normal flow area for a particular column.
     *
     * @param colRequested the zero-based column number of the flow
     * @return the flow area for the requested column
     */
    public NormalFlow getNormalFlow(int colRequested) {
        if (colRequested >= 0 && colRequested < colCount) {
            return (NormalFlow) flowAreas.get(colRequested);
        } else { // internal error
            throw new IllegalArgumentException("Invalid column number " + 
                    colRequested + " requested; only 0-" + (colCount-1) +
                    " available.");
        }
    }
}

