/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LMiter implements ListIterator {


    private ListIterator baseIter;
    private FObj curFO;
    protected List listLMs;
    protected int curPos = 0;

    public LMiter(ListIterator bIter) {
        baseIter = bIter;
        listLMs = new ArrayList(10);
    }

    public boolean hasNext() {
        return (curPos < listLMs.size()) ? true : preLoadNext();
    }

    protected boolean preLoadNext() {
        // skip over child FObj's that don't add lms
        while (baseIter != null && baseIter.hasNext()) {
            Object theobj = baseIter.next();
            if(theobj instanceof FObj) {
                FObj fobj = (FObj) theobj;
                //listLMs.add(fobj.getLayoutManager());
                fobj.addLayoutManager(listLMs);
                if(curPos < listLMs.size()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasPrevious() {
        return (curPos > 0);
    }

    public Object previous() throws NoSuchElementException {
        if (curPos > 0) {
            return listLMs.get(--curPos);
        } else
            throw new NoSuchElementException();
    }

    public Object next() throws NoSuchElementException {
        if (curPos < listLMs.size()) {
            return listLMs.get(curPos++);
        } else
            throw new NoSuchElementException();
    }

    public void remove() throws NoSuchElementException {
        if (curPos > 0) {
            listLMs.remove(--curPos);
            // Note: doesn't actually remove it from the base!
        } else
            throw new NoSuchElementException();

    }


    public void add(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("LMiter doesn't support add");
    }

    public void set(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("LMiter doesn't support set");
    }

    public int nextIndex() {
        return curPos;
    }

    public int previousIndex() {
        return curPos - 1;
    }

}
