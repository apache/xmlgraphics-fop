/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

/**
 * The main body reference area.
 * This area that contains the flow via the span areas.
 */
public class MainReference extends Area {
    private List spanAreas = new ArrayList();
    private int columnGap;
    private int width;

    /**
     * Add a span area to this area.
     *
     * @param span the span area to add
     */
    public void addSpan(Span span) {
        spanAreas.add(span);
    }

    /**
     * Get the span areas from this area.
     *
     * @return the list of span areas
     */
    public List getSpans() {
        return spanAreas;
    }

    /**
     * Get the column gap in millipoints.
     *
     * @return the column gap in millioints
     */
    public int getColumnGap() {
        return columnGap;
    }

    /**
     * Get the width of this reference area.
     *
     * @return the width
     */
    public int getWidth() {
        return width;
    }

}

