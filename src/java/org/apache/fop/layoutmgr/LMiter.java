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
import java.util.ListIterator;
import java.util.NoSuchElementException;

/** An iterator for layout managers. */
public class LMiter implements ListIterator {

    /** list of layout managers */
    protected List listLMs;
    /** current position in iteration */
    protected int curPos = 0;
    /** The LayoutManager to which this LMiter is attached **/
    private LayoutManager lp;

    /**
     * Construct a layout manager iterator.
     * @param lp the associated layout manager (parent)
     */
    public LMiter(LayoutManager lp) {
        this.lp = lp;
        listLMs = lp.getChildLMs();
    }

    /** {@inheritDoc} */
    public boolean hasNext() {
        return (curPos < listLMs.size()) ? true : lp.createNextChildLMs(curPos);
    }

    /** {@inheritDoc} */
    public boolean hasPrevious() {
        return (curPos > 0);
    }

    /** {@inheritDoc} */
    public Object previous() throws NoSuchElementException {
        if (curPos > 0) {
            return listLMs.get(--curPos);
        } else {
            throw new NoSuchElementException();
        }
    }

    /** {@inheritDoc} */
    public Object next() throws NoSuchElementException {
        if (curPos < listLMs.size()) {
            return listLMs.get(curPos++);
        } else {
            throw new NoSuchElementException();
        }
    }

    /** {@inheritDoc} */
     public void remove() throws NoSuchElementException {
        if (curPos > 0) {
            listLMs.remove(--curPos);
            // Note: doesn't actually remove it from the base!
        } else {
            throw new NoSuchElementException();
        }
    }


    /** {@inheritDoc} */
   public void add(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("LMiter doesn't support add");
    }

    /** {@inheritDoc} */
    public void set(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("LMiter doesn't support set");
    }

    /** {@inheritDoc} */
    public int nextIndex() {
        return curPos;
    }

    /** {@inheritDoc} */
    public int previousIndex() {
        return curPos - 1;
    }

}
