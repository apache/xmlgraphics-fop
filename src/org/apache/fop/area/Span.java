/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.List;
import java.util.ArrayList;

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
        flowAreas = new ArrayList(cols);
    }

    /**
     * Add the flow area to this span area.
     *
     * @param flow the flow area to add
     */
    public void addFlow(Flow flow) {
        flowAreas.add(flow);
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

