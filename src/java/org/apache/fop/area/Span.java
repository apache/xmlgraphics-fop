/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * The span reference area.
 * This is a reference area block area with 0 border and padding
 * The span reference areas are stacked inside the main reference area.
 */
public class Span extends Area {
    // the list of flow reference areas in this span area
    private List flowAreas;
    private int height;

    /**
     * Create a span area with the number of columns for this span area.
     *
     * @param cols the number of columns in the span
     */
    public Span(int cols) {
        flowAreas = new java.util.ArrayList(cols);
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
    }

    /**
     * Add a flow area to this span area.
     *
     * @param flow the flow area to add
     */
    public void addFlow(Flow flow) {
        flowAreas.add(flow);
    }

    /**
     * Create a new flow and add it to this span area
     *
     * @return the newly made Flow object
     */
    public Flow addNewFlow() {
        Flow newFlow = new Flow();
        newFlow.setIPD(getIPD());
        flowAreas.add(newFlow);
        return newFlow;
    }

    /**
     * Get the column count for this span area.
     *
     * @return the number of columns in this span area
     */
    public int getColumnCount() {
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
     * Get the flow area for a particular column.
     *
     * @param count the column number for the flow
     * @return the flow area for the requested column
     */
    public Flow getFlow(int count) {
        return (Flow) flowAreas.get(count);
    }

}

