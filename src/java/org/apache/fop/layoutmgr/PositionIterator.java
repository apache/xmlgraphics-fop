/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** A position iterator. */
public abstract class PositionIterator implements Iterator {

    private Iterator parentIter;
    private Object nextObj;
    private LayoutManager childLM;
    private boolean bHasNext;

    /**
     * Construct position iterator.
     * @param pIter an iterator to use as parent
     */
    protected PositionIterator(Iterator pIter) {
        parentIter = pIter;
        lookAhead();
        //checkNext();
    }

    /** @return layout manager of next child layout manager or null */
    public LayoutManager getNextChildLM() {
        // Move to next "segment" of iterator, ie: new childLM
        if (childLM == null && nextObj != null) {
            childLM = getLM(nextObj);
            bHasNext = true;
        }
        return childLM;
    }

    /**
     * @param nextObj next object from which to obtain position
     * @return layout manager
     */
    protected abstract LayoutManager getLM(Object nextObj);

    /**
     * @param nextObj next object from which to obtain position
     * @return position of next object
     */
    protected abstract Position getPos(Object nextObj);

    private void lookAhead() {
        if (parentIter.hasNext()) {
            bHasNext = true;
            nextObj = parentIter.next();
        } else {
            endIter();
        }
    }

    /** @return true if not at end of sub-sequence with same child layout manager */
    protected boolean checkNext() {
        LayoutManager lm = getLM(nextObj);
        if (childLM == null) {
            childLM = lm;
        } else if (childLM != lm && lm != null) {
            // End of this sub-sequence with same child LM
            bHasNext = false;
            childLM = null;
            return false;
        }
        return true;
    }

    /** end (reset) iterator */
    protected void endIter() {
        bHasNext = false;
        nextObj = null;
        childLM = null;
    }

    /** {@inheritDoc} */
    public boolean hasNext() {
        return (bHasNext && checkNext());
    }


    /** {@inheritDoc} */
    public Object next() throws NoSuchElementException {
        if (bHasNext) {
            Object retObj = getPos(nextObj);
            lookAhead();
            return retObj;
        } else {
            throw new NoSuchElementException("PosIter");
        }
    }

    /** @return peek at next object */
    public Object peekNext() {
        return nextObj;
    }

    /** {@inheritDoc} */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("PositionIterator doesn't support remove");
    }
}

