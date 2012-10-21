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

/**
 * An iterator over {@link Position} instances, that is wrapped around
 * another 'parent' {@link Iterator}. The parent can be either another
 * {@code PositionIterator}, or an iterator over {@link KnuthElement}s,
 * for example.<br/>
 * The {@link #next()} method always returns a {@link Position}. The
 * {@link #getPos(Object)} method can be overridden in subclasses
 * to take care of obtaining the {@link LayoutManager} or {@link Position}
 * from the object returned by the parent iterator's {@code next()} method.
 */
public class PositionIterator implements Iterator<Position> {

    private Iterator parentIter;
    private Object nextObj;
    private LayoutManager childLM;
    private boolean hasNext;

    /**
     * Construct position iterator.
     * @param parentIter an iterator to use as parent
     */
    public PositionIterator(Iterator parentIter) {
        this.parentIter = parentIter;
        lookAhead();
        //checkNext();
    }

    /** @return layout manager of next child layout manager or null */
    public LayoutManager getNextChildLM() {
        // Move to next "segment" of iterator, ie: new childLM
        if (childLM == null && nextObj != null) {
            childLM = getLM(nextObj);
            hasNext = true;
        }
        return childLM;
    }

    /**
     * @param nextObj next object from which to obtain position
     * @return layout manager
     */
    protected LayoutManager getLM(Object nextObj) {
        return getPos(nextObj).getLM();
    }

    /**
     * Default implementation assumes that the passed
     * {@code nextObj} is itself a {@link Position}, and just returns it.
     * Subclasses for which this is not the case, <em>must</em> provide a
     * suitable override this method.
     * @param nextObj next object from which to obtain position
     * @return position of next object.
     */
    protected Position getPos(Object nextObj) {
        if (nextObj instanceof Position) {
            return (Position)nextObj;
        }
        throw new IllegalArgumentException(
                "Cannot obtain Position from the given object.");
    }

    private void lookAhead() {
        if (parentIter.hasNext()) {
            hasNext = true;
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
            hasNext = false;
            childLM = null;
            return false;
        }
        return true;
    }

    /** end (reset) iterator */
    protected void endIter() {
        hasNext = false;
        nextObj = null;
        childLM = null;
    }

    /** {@inheritDoc} */
    public boolean hasNext() {
        return (hasNext && checkNext());
    }


    /** {@inheritDoc} */
    public Position next() throws NoSuchElementException {
        if (hasNext) {
            Position retPos = getPos(nextObj);
            lookAhead();
            return retPos;
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
