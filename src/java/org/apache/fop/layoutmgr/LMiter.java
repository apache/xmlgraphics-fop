/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.fop.area.AreaTreeHandler;
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
    /** The LayoutManager to which this LMiter is attached **/
    private LayoutManager lp;

    public LMiter(LayoutManager lp, ListIterator bIter) {
        this.lp = lp;
        baseIter = bIter;
        listLMs = new ArrayList(10);
    }

    public boolean hasNext() {
        return (curPos < listLMs.size()) ? true : preLoadNext();
    }

    protected boolean preLoadNext() {
        AreaTreeHandler areaTreeHandler = lp.getAreaTreeHandler();
        // skip over child FObj's that don't add lms
        while (baseIter != null && baseIter.hasNext()) {
            Object theobj = baseIter.next();
            if (theobj instanceof FObj) {
                FObj fobj = (FObj) theobj;
                //listLMs.add(fobj.getLayoutManager());
                areaTreeHandler.addLayoutManager(fobj, listLMs);
                if (curPos < listLMs.size()) {
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
        } else {
            throw new NoSuchElementException();
        }
    }

    public Object next() throws NoSuchElementException {
        if (curPos < listLMs.size()) {
            return listLMs.get(curPos++);
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() throws NoSuchElementException {
        if (curPos > 0) {
            listLMs.remove(--curPos);
            // Note: doesn't actually remove it from the base!
        } else {
            throw new NoSuchElementException();
        }
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
