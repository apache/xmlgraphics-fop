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
    private int columnCount;

    /**
     * Create a span area with the number of columns for this span area.
     *
     * @param cols the number of columns in the span
     * @param ipd the ipd of the span 
     */
    public Span(int cols, int ipd) {
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
        columnCount = cols;
        this.ipd = ipd;
        flowAreas = new java.util.ArrayList(cols);
        addAdditionalNormalFlow(); // one normal flow is required
    }

    /**
     * Create a new normal flow and add it to this span area
     *
     * @return the newly made NormalFlow object
     */
    public NormalFlow addAdditionalNormalFlow() {
        if (flowAreas.size() >= columnCount) { // internal error
            throw new IllegalStateException("Maximum number of flow areas (" +
                    columnCount + ") for this span reached.");
        }
        NormalFlow newFlow = new NormalFlow();
        newFlow.setIPD(getIPD());
        flowAreas.add(newFlow);
        return newFlow;
    }

    /**
     * Get the column count for this span area.
     *
     * @return the number of columns defined for this span area
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Get the count of normal flows for this span area.
     *
     * @return the number of normal flows attached to this span
     */
    public int getNormalFlowCount() {
        return flowAreas.size();
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
     * @param count the column number for the flow
     * @return the flow area for the requested column
     */
    public NormalFlow getNormalFlow(int columnNumber) {
        return (NormalFlow) flowAreas.get(columnNumber);
    }

}

