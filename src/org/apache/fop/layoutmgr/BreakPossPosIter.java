/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;


import java.util.List;

public class BreakPossPosIter extends PositionIterator {
    private int m_iterCount ;

    BreakPossPosIter(List bpList, int startPos, int endPos) {
	super(bpList.listIterator(startPos));
	m_iterCount = endPos - startPos;
    }

    // Check position < endPos

    protected boolean checkNext() {
	return (m_iterCount > 0 && super.checkNext());
    }

    public Object next() {
	--m_iterCount;
	return super.next();
    }

    protected BPLayoutManager getLM(Object nextObj) {
	return ((BreakPoss)nextObj).getLayoutManager();
    }

    protected BreakPoss.Position getPos(Object nextObj) {
	return ((BreakPoss)nextObj).getPosition();
    }

}
