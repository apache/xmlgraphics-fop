/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;


import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class PositionIterator implements Iterator 
{
    Iterator m_parentIter;
    Object m_nextObj;
    BPLayoutManager m_childLM;
    boolean m_bHasNext;

    PositionIterator(Iterator parentIter) {
	m_parentIter = parentIter;
	lookAhead();
	//checkNext();
    }

    BPLayoutManager getNextChildLM() {
	// Move to next "segment" of iterator, ie: new childLM
	if (m_childLM == null && m_nextObj != null) {
	    m_childLM = getLM(m_nextObj);
	    m_bHasNext = true;
	}
	return m_childLM;
    }

    abstract protected BPLayoutManager getLM(Object nextObj);

    abstract protected BreakPoss.Position getPos(Object nextObj);

    private void lookAhead() {
	if (m_parentIter.hasNext()) {
	    m_bHasNext = true;
	    m_nextObj = m_parentIter.next();
	}
	else {
	    endIter();
	}
    }

    protected boolean checkNext() {
	BPLayoutManager lm = getLM(m_nextObj);
	if (m_childLM==null) {
	    m_childLM = lm;
	}
	else if (m_childLM != lm) {
	    // End of this sub-sequence with same child LM
	    m_bHasNext = false;
	    m_childLM = null;
	    return false;
	}
	return true;
    }

    protected void endIter() {
	m_bHasNext = false;
	m_nextObj = null;
	m_childLM = null;
    }

    public boolean hasNext() {
	return (m_bHasNext && checkNext());
    }


    public Object next() throws NoSuchElementException {
	if (m_bHasNext) {
	    Object retObj = getPos(m_nextObj);
	    lookAhead();
	    return retObj;
	}
	else {
	    throw new NoSuchElementException("PosIter");
	}
    }

    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException("PositionIterator doesn't support remove");
    }
}

