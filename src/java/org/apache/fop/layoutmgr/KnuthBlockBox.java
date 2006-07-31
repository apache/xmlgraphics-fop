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

import org.apache.fop.traits.MinOptMax;

import java.util.LinkedList;

/**
 * Knuth box used to represent a line in block-progression-dimension (i.e. the width is its height).
 */
public class KnuthBlockBox extends KnuthBox {
    
    private MinOptMax ipdRange;
    /**
     * Natural width of the line represented by this box. In addition to ipdRange because
     * it isn't possible to get the opt value stored in a MinOptMax object.
     */
    private int bpd;
    /** FootnoteBodyLayoutManagers corresponding to the footnotes cited on this line. */
    private LinkedList footnoteLMList;
    /** FloatBodyLayoutManagers corresponding to the floats cited on this line. */
    private LinkedList floatLMList;
    /**
     * The Knuth sequences corresponding to the footnotes cited on this line. This is a List
     * of List of KnuthElement objects.
     */
    private LinkedList footnoteElementLists = null;
    /**
     * The Knuth sequences corresponding to the floats cited on this line. This is a List
     * of List of KnuthElement objects.
     */
    private LinkedList floatElementLists = null;

    /**
     * Creates a new box.
     * @param w block progression dimension of this box
     * @param range min, opt, max inline progression dimension of this box
     * @param bpdim natural width of the line represented by this box.
     * @param pos the Position stored in this box
     * @param bAux is this box auxiliary?
     */
    public KnuthBlockBox(int w, MinOptMax range, int bpdim, Position pos, boolean bAux) {
        super(w, pos, bAux);
        ipdRange = (MinOptMax) range.clone();
        bpd = bpdim;
        footnoteLMList = new LinkedList();
        floatLMList = new LinkedList();
    }

    /**
     * Creates a new box.
     * @param w block progression dimension of this box
     * @param footnoteLMList footnotes cited by elements in this box. The list contains
     * the corresponding FootnoteBodyLayoutManagers 
     * @param floatLMList floats cited by elements in this box. The list contains the
     * corresponding FloatBodyLayoutManagers 
     * @param pos the Position stored in this box
     * @param bAux is this box auxiliary?
     */
    public KnuthBlockBox(int w,
                         LinkedList footnoteLMList,
                         LinkedList floatLMList,
                         Position pos,
                         boolean bAux) {
        super(w, pos, bAux);
        ipdRange = new MinOptMax(0);
        bpd = 0;
        this.footnoteLMList = new LinkedList(footnoteLMList);
        this.floatLMList = new LinkedList(floatLMList);
    }

    /**
     * @return the LMs for the footnotes cited in this box.
     */
    public LinkedList getFootnoteBodyLMs() {
        return footnoteLMList;
    }

    /**
     * @return the LMs for the floats cited in this box.
     */
    public LinkedList getFloatBodyLMs() {
        return floatLMList;
    }

    /**
     * @return true if this box contains footnote citations.
     */
    public boolean hasFootnoteAnchors() {
        return (footnoteLMList.size() > 0);
    }

    /**
     * @return true if this box contains float citations.
     */
    public boolean hasFloatAnchors() {
        return (floatLMList.size() > 0);
    }

    /**
     * Adds a footnote to this box's list of footnotes.
     * @param list KnuthElement instances corresponding to the footnote body
     */
    public void addFootnoteElementList(LinkedList list) {
        if (footnoteElementLists == null) {
            footnoteElementLists = new LinkedList();
        }
        footnoteElementLists.add(list);
    }

    /**
     * Returns the list of footnotes cited by this box.
     * @return a list of KnuthElement sequences corresponding to the footnote bodies
     */
    public LinkedList getFootnoteElementLists() {
        return footnoteElementLists;
    }

    /**
     * Adds a float to this box's list of floats.
     * @param list KnuthElement instances corresponding to the float body
     */
    public void addFloatElementList(LinkedList list) {
        if (floatElementLists == null) {
            floatElementLists = new LinkedList();
        }
        floatElementLists.add(list);
    }

    /**
     * Returns the list of floats cited by this box.
     * @return a list of KnuthElement sequences corresponding to the float bodies
     */
    public LinkedList getFloatElementLists() {
        return floatElementLists;
    }

    /**
     * @return the inline progression dimension of this box.
     */
    public MinOptMax getIPDRange() {
        return (MinOptMax) ipdRange.clone();
    }

    /**
     * Returns the natural width (without stretching nor shrinking) of the line
     * represented by this box.
     * @return the line width
     */
    public int getBPD() {
        return bpd;
    }
}
