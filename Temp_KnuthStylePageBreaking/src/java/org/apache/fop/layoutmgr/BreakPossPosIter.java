/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * Position iterator for break possibilities.
 */
public class BreakPossPosIter extends PositionIterator {

    private int iterCount;

    /**
     * Main constructor
     * @param bpList List of break possibilities
     * @param startPos starting position
     * @param endPos ending position
     */
    public BreakPossPosIter(List bpList, int startPos, int endPos) {
        super((bpList.size() > 0 ? bpList.listIterator(startPos) : bpList.listIterator()));
        iterCount = endPos - startPos;
    }

    // Check position < endPos
    
    /**
     * @see org.apache.fop.layoutmgr.PositionIterator#checkNext()
     */
    protected boolean checkNext() {
        if (iterCount > 0) {
            return super.checkNext();
        } else {
            endIter();
            return false;
        }
    }

    /**
     * @see java.util.Iterator#next()
     */
    public Object next() {
        --iterCount;
        return super.next();
    }

    public BreakPoss getBP() {
        return (BreakPoss) peekNext();
    }

    protected LayoutManager getLM(Object nextObj) {
        return ((BreakPoss) nextObj).getLayoutManager();
    }

    protected Position getPos(Object nextObj) {
        return ((BreakPoss) nextObj).getPosition();
    }

}
