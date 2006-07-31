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

package org.apache.fop.layoutmgr.inline;

import java.util.LinkedList;
import java.util.List;

import org.apache.fop.fo.flow.Float;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.FloatBodyLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.Position;

/**
 * Layout manager for fo:float.
 */
public class FloatLayoutManager extends AbstractLayoutManager 
                                   implements InlineLevelLayoutManager {

    private Float floatNode;
    private FloatBodyLayoutManager bodyLM;
    /** Represents the float citation **/
    private KnuthElement forcedAnchor;

    /**
     * Create a new float layout manager.
     * @param node float to create the layout manager for
     */
    public FloatLayoutManager(Float node) {
        super(node);
        floatNode = node;
    }
    
    /** @see org.apache.fop.layoutmgr.LayoutManager#initialize() */
    public void initialize() {
        // create a FloatBodyLM handling the fo:float-body child of fo:float
        bodyLM = new FloatBodyLayoutManager(floatNode);
    }

    /** @see org.apache.fop.layoutmgr.LayoutManager */
    public LinkedList getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        // this is the only method that must be implemented:
        // all other methods will never be called, as the returned elements
        // contain Positions created by the citationLM, so its methods will
        // be called instead

        bodyLM.setParent(this);
        bodyLM.initialize();

        // get Knuth elements representing the float citation
        LinkedList returnedList = new LinkedList();
        //Inline part of the float is empty. Need to send back an auxiliary
        //zero-width, zero-height inline box so the float gets painted.
        KnuthSequence seq = new InlineKnuthSequence();
        //Need to use an aux. box, otherwise, the line height can't be forced to zero height.
        forcedAnchor = new KnuthInlineBox(0, null, null, true);
        ((KnuthInlineBox) forcedAnchor).setFloatBodyLM(bodyLM);
        seq.add(forcedAnchor);
        returnedList.add(seq);
        setFinished(true);

        return returnedList;
    }

    /** @see org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager */
    public List addALetterSpaceTo(List oldList) {
        log.warn("null implementation of addALetterSpaceTo() called!");
        return oldList;
    }

    /**
     * Remove the word space represented by the given elements
     *
     * @param oldList the elements representing the word space
     */
    public void removeWordSpace(List oldList) {
        // do nothing
        log.warn(this.getClass().getName() + " should not receive a call to removeWordSpace(list)");
    }

    /** @see org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager */
    public void getWordChars(StringBuffer sbChars, Position pos) {
        log.warn("null implementation of getWordChars() called!");
    }

    /** @see org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager */
    public void hyphenate(Position pos, HyphContext hc) {
        log.warn("null implementation of hyphenate called!");
    }

    /** @see org.apache.fop.layoutmgr.inline.InlineLevelLayoutManager */
    public boolean applyChanges(List oldList) {
        log.warn("null implementation of applyChanges() called!");
        return false;
    }

    /**
     * @see org.apache.fop.layoutmgr.LayoutManager#getChangedKnuthElements(java.util.List, int)
     */
    public LinkedList getChangedKnuthElements(List oldList,
                                              int alignment) {
        log.warn("null implementation of getChangeKnuthElement() called!");
        return null;
    }
}
