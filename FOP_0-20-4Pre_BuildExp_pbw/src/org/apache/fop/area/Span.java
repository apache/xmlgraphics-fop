/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Iterator;

// this is a reference area block area with 0 border and padding
public class Span extends Area {
    // the list of flow reference areas in this span area
    ArrayList flowAreas;
    int height;

    public Span(int cols) {
        flowAreas = new ArrayList(cols);
    }

    public void addFlow(Flow flow) {
        flowAreas.add(flow);
	flow.setParent(this);
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

    /**
     * Maximum available BPD for a Span is the maxBPD for its containing
     * MainReference less the content BPD of any previous spans
     */
    public MinOptMax getMaxBPD() {
	MinOptMax maxbpd = parent.getMaxBPD();
	MainReference mainref = (MainReference)parent;
	Iterator spanIter = mainref.getSpans().iterator();
	while (spanIter.hasNext()) {
	    Span s = (Span)spanIter.next();
	    if (s == this) break;
	    maxbpd = MinOptMax.subtract(maxbpd, s.getContentBPD());
	}
	return maxbpd;
    }

}
