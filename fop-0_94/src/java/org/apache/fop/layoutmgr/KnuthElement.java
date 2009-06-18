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

/**
 * This is the super class for KnuthBox, KnuthGlue and KnuthPenalty.
 * 
 * It stores information common to all sub classes, and the methods to get it:
 * the width, a Position and a boolean marking KnuthElements used for some
 * special feature (for example, the additional elements used to represent
 * a space when text alignment is right, left or center).
 */
public abstract class KnuthElement extends ListElement {

    /** The value used as an infinite indicator. */
    public static final int INFINITE = 1000;

    private int width;
    private boolean bIsAuxiliary;

    /**
     * Create a new KnuthElement.
     * This class being abstract, this can be called only by subclasses.
     *
     * @param w    the width of this element
     * @param pos  the Position stored in this element
     * @param bAux is this an auxiliary element?
     */
    protected KnuthElement(int w, Position pos, boolean bAux) {
        super(pos);
        width = w;
        bIsAuxiliary = bAux;
    }

    /** @return true if this element is an auxiliary one. */
    public boolean isAuxiliary() {
        return bIsAuxiliary;
    }

    /** @return the width of this element. */
    public int getW() {
        return width;
    }

    /** @return the penalty value of this element, if applicable. */ 
    public int getP() {
        throw new RuntimeException("Element is not a penalty");
    }

    /** @return the stretch value of this element, if applicable. */ 
    public int getY() {
        throw new RuntimeException("Element is not a glue");
    }

    /** @return the shrink value of this element, if applicable. */ 
    public int getZ() {
        throw new RuntimeException("Element is not a glue");
    }
    
    /** @see org.apache.fop.layoutmgr.ListElement#isUnresolvedElement() */
    public boolean isUnresolvedElement() {
        return false;
    }

}
