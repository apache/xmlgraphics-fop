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

/**
 * An instance of this class represents an unbreakable piece of content with
 * fixed width: for example an image, a syllable (but only if letter spacing
 * is constant), ...
 * 
 * A KnuthBox is never a feasible breaking point.
 * 
 * The represented piece of content is never suppressed.
 * 
 * Besides the inherited methods and attributes, this class has some more
 * attributes to store information about the content height and its vertical
 * positioning, and the methods used to get them.
 */
public class KnuthBox extends KnuthElement {
    private int lead;
    private int total;
    private int middle;

    /**
     * Create a new KnuthBox.
     *
     * @param w    the width of this box
     * @param l    the height of this box above the main baseline
     * @param t    the total height of this box
     * @param m    the height of this box above and below the middle baseline
     * @param pos  the Position stored in this box
     * @param bAux is this box auxiliary?
     */
    public KnuthBox(int w, int l, int t, int m, Position pos, boolean bAux) {
        super(w, pos, bAux);
        lead = l;
        total = t;
        middle = m;
    }

    public boolean isBox() {
        return true;
    }

    /**
     * Return the height of this box above the main baseline.
     */
    public int getLead() {
        return lead;
    }

    /**
     * Return the total height of this box.
     */
    public int getTotal() {
        return total;
    }

    /**
     * Return the height of this box above and below the middle baseline.
     */
    public int getMiddle() {
        return middle;
    }
}
