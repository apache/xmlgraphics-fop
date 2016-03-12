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

import java.util.List;

/**
 * A dedicated {@link PositionIterator} that is backed by an iterator
 * over a list of {@link KnuthElement}s.
 */
public class KnuthPossPosIter extends PositionIterator {

    private int iterCount;

    /**
     * Main constructor
     * @param elementList List of Knuth elements
     * @param startPos starting position, inclusive
     * @param endPos ending position, exclusive
     */
    public KnuthPossPosIter(List elementList, int startPos, int endPos) {
        super(elementList.listIterator(startPos));
        iterCount = endPos - startPos;
    }

    /**
     * Auxiliary constructor
     * @param elementList List of Knuth elements
     */
    public KnuthPossPosIter(List elementList) {
        this(elementList, 0, elementList.size());
    }

    // Check position < endPos

    /** {@inheritDoc} */
    @Override
    protected boolean checkNext() {
        if (iterCount > 0) {
            return super.checkNext();
        } else {
            endIter();
            return false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Position next() {
        --iterCount;
        return super.next();
    }

    /**
     * Peek at next, returning as ListElement.
     * @return peek at next as ListElement
     */
    public ListElement getKE() {
        return (ListElement) peekNext();
    }

    /** {@inheritDoc} */
    @Override
    protected LayoutManager getLM(Object nextObj) {
        return ((ListElement) nextObj).getLayoutManager();
    }

    /** {@inheritDoc} */
    @Override
    protected Position getPos(Object nextObj) {
        return ((ListElement) nextObj).getPosition();
    }
}
