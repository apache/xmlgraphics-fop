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

/**
 * This is the super class for KnuthBox, KnuthGlue and KnuthPenalty.
 * 
 * It stores information common to all sub classes, and the methods to get it:
 * the width, a Position and a boolean marking KnuthElements used for some
 * special feature (for example, the additional elements used to represent
 * a space when text alignment is right, left or center).
 */
public abstract class KnuthElement {

    public static final int KNUTH_BOX = 0;
    public static final int KNUTH_GLUE = 1;
    public static final int KNUTH_PENALTY = 2;

    public static final int INFINITE = 1000;

    private int type;
    private int width;
    private Position position;
    private boolean bIsAuxiliary;

    /**
     * Create a new KnuthElement.
     * This class being abstract, this can be called only by subclasses.
     *
     * @param t    the type of this element (one of the KNUTH_* constants)
     * @param w    the width of this element
     * @param pos  the Position stored in this element
     * @param bAux is this an auxiliary element?
     */
    protected KnuthElement(int t, int w, Position pos, boolean bAux) {
        type = t;
        width = w;
        position = pos;
        bIsAuxiliary = bAux;
    }

    /**
     * Return true if this element is a KnuthBox.
     */
    public boolean isBox() {
        return (type == KNUTH_BOX);
    }

    /**
     * Return true if this element is a KnuthGlue.
     */
    public boolean isGlue() {
        return (type == KNUTH_GLUE);
    }

    /**
     * Return true if this element is a KnuthPenalty.
     */
    public boolean isPenalty() {
        return (type == KNUTH_PENALTY);
    }

    /**
     * Return true if this element is an auxiliary one.
     */
    public boolean isAuxiliary() {
        return bIsAuxiliary;
    }

    /**
     * Return the width of this element.
     */
    public int getW() {
        return width;
    }

    /**
     * Return the Position stored in this element.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Change the Position stored in this element.
     */
    public void setPosition(Position pos) {
        position = pos;
    }

    /**
     * Return the LayoutManager responsible for this element.
     */
    public LayoutManager getLayoutManager() {
        if (position != null) {
            return position.getLM();
        } else {
            return null;
        }
    }
}
