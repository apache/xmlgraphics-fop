/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;


import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class PositionIterator implements Iterator {
    Iterator parentIter;
    Object nextObj;
    LayoutManager childLM;
    boolean bHasNext;

    PositionIterator(Iterator pIter) {
        parentIter = pIter;
        lookAhead();
        //checkNext();
    }

    public LayoutManager getNextChildLM() {
        // Move to next "segment" of iterator, ie: new childLM
        if (childLM == null && nextObj != null) {
            childLM = getLM(nextObj);
            bHasNext = true;
        }
        return childLM;
    }

    protected abstract LayoutManager getLM(Object nextObj);

    protected abstract Position getPos(Object nextObj);

    private void lookAhead() {
        if (parentIter.hasNext()) {
            bHasNext = true;
            nextObj = parentIter.next();
        } else {
            endIter();
        }
    }

    protected boolean checkNext() {
        LayoutManager lm = getLM(nextObj);
        if (childLM == null) {
            childLM = lm;
        } else if (childLM != lm) {
            // End of this sub-sequence with same child LM
            bHasNext = false;
            childLM = null;
            return false;
        }
        return true;
    }

    protected void endIter() {
        bHasNext = false;
        nextObj = null;
        childLM = null;
    }

    public boolean hasNext() {
        return (bHasNext && checkNext());
    }


    public Object next() throws NoSuchElementException {
        if (bHasNext) {
            Object retObj = getPos(nextObj);
            lookAhead();
            return retObj;
        } else {
            throw new NoSuchElementException("PosIter");
        }
    }

    protected Object peekNext() {
        return nextObj;
    }

    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("PositionIterator doesn't support remove");
    }
}

