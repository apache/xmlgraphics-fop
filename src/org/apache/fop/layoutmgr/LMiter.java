/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LMiter implements ListIterator {


    private ListIterator m_baseIter;
    private FObj m_curFO;
    private ArrayList m_listLMs;
    private int m_curPos=0;

    public LMiter(ListIterator baseIter) {
	m_baseIter = baseIter;
	m_listLMs = new ArrayList(10);
    }

    public boolean hasNext() {
	return (m_curPos < m_listLMs.size())? true :
	    preLoadNext();
    }

    private boolean preLoadNext() {
	if (m_baseIter.hasNext()) {
	    FObj fobj = (FObj)m_baseIter.next();
	    //m_listLMs.add(fobj.getLayoutManager());
	    fobj.addLayoutManager(m_listLMs);
	    return true;
	}
	else return false;
    }

    public boolean hasPrevious() {
	return (m_curPos > 0);
    }

    public Object previous() throws NoSuchElementException {
	if (m_curPos > 0) {
	    return m_listLMs.get(--m_curPos);
	}
	else throw new NoSuchElementException();
    }

    public Object next() throws NoSuchElementException {
	if (m_curPos < m_listLMs.size()) {
	    return m_listLMs.get(m_curPos++);
	}
	else throw new NoSuchElementException();
    }
    
    public void remove() throws NoSuchElementException {
	if (m_curPos > 0) {
	    m_listLMs.remove(--m_curPos);
	    // Note: doesn't actually remove it from the base!
	}
	else throw new NoSuchElementException();
	
    }


    public void add(Object o) throws UnsupportedOperationException  {
	throw new UnsupportedOperationException("LMiter doesn't support add");
    }

    public void set(Object o) throws UnsupportedOperationException {
	throw new UnsupportedOperationException("LMiter doesn't support set");
    }

    public int nextIndex() {
	return m_curPos;
    }

    public int previousIndex() {
	return m_curPos - 1;
    }

}
