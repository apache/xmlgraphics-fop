/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.List;
import java.util.ArrayList;

// this is a reference area block area with 0 border and padding
public class Span extends Area {
    // the list of flow reference areas in this span area
    private List flowAreas;
    private int height;

    public Span(int cols) {
        flowAreas = new ArrayList(cols);
    }

    public void addFlow(Flow flow) {
        flowAreas.add(flow);
    }

    public int getColumnCount() {
        return flowAreas.size();
    }

    public int getHeight() {
        return height;
    }

    public Flow getFlow(int count) {
        return (Flow) flowAreas.get(count);
    }

}

