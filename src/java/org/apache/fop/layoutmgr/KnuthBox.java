/*
 * Copyright 2004 The Apache Software Foundation.
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

public class KnuthBox extends KnuthElement {
    private int lead;
    private int total;
    private int middle;

    public KnuthBox(int w, int l, int t, int m, Position pos, boolean bAux) {
        super(KNUTH_BOX, w, pos, bAux);
        lead = l;
        total = t;
        middle = m;
    }

    public int getLead() {
        return lead;
    }

    public int getTotal() {
        return total;
    }

    public int getMiddle() {
        return middle;
    }
}