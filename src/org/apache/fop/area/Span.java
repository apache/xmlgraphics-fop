/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;

// this is a reference area block area with 0 border and padding
public class Span extends Area {
    // the list of flow reference areas in this span area
    ArrayList flowAreas = new ArrayList();
    int height;

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
