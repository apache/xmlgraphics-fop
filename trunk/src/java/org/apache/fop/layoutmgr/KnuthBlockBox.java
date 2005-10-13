/*
 * Copyright 2004-2005 The Apache Software Foundation.
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

import org.apache.fop.traits.MinOptMax;

import java.util.LinkedList;

public class KnuthBlockBox extends KnuthBox {
    
    private MinOptMax ipdRange;
    private int bpd;
    private LinkedList footnoteList;
    private LinkedList elementLists = null;

    public KnuthBlockBox(int w, MinOptMax range, int bpdim, Position pos, boolean bAux) {
        super(w, pos, bAux);
        ipdRange = (MinOptMax) range.clone();
        bpd = bpdim;
        footnoteList = new LinkedList();
    }

    public KnuthBlockBox(int w, LinkedList list, Position pos, boolean bAux) {
        super(w, pos, bAux);
        ipdRange = new MinOptMax(0);
        bpd = 0;
        footnoteList = new LinkedList(list);
    }

    public LinkedList getFootnoteBodyLMs() {
        return footnoteList;
    }

    public boolean hasAnchors() {
        return (footnoteList.size() > 0);
    }

    public void addElementList(LinkedList list) {
        if (elementLists == null) {
            elementLists = new LinkedList();
        }
        elementLists.add(list);
    }

    public LinkedList getElementLists() {
        return elementLists;
    }

    public MinOptMax getIPDRange() {
        return (MinOptMax) ipdRange.clone();
    }

    public int getBPD() {
        return bpd;
    }
}