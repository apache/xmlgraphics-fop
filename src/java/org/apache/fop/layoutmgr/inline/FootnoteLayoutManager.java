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
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.layoutmgr.FootnoteBodyLayoutManager;
import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.NonLeafPosition;
import org.apache.fop.layoutmgr.Position;
import org.apache.fop.layoutmgr.PositionIterator;

/**
 * Layout manager for fo:footnote.
 */
public class FootnoteLayoutManager extends InlineStackingLayoutManager {

    /**
     * logging instance
     */
    private static Log log = LogFactory.getLog(FootnoteLayoutManager.class);

    private Footnote footnote;
    private InlineStackingLayoutManager citationLM;
    private FootnoteBodyLayoutManager bodyLM;
    /** Represents the footnote citation **/
    private KnuthElement forcedAnchor;

    /**
     * Create a new footnote layout manager.
     * @param node footnote to create the layout manager for
     */
    public FootnoteLayoutManager(Footnote node) {
        super(node);
        footnote = node;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        // create an InlineStackingLM handling the fo:inline child of fo:footnote
        citationLM = new InlineLayoutManager(footnote.getFootnoteCitation());

        // create a FootnoteBodyLM handling the fo:footnote-body child of fo:footnote
        bodyLM = new FootnoteBodyLayoutManager(footnote.getFootnoteBody());
    }

    /** {@inheritDoc} */
    @Override
    public List getNextKnuthElements(LayoutContext context,
                                           int alignment) {
        // for the moment, this LM is set as the citationLM's parent
        // later on, when this LM will have nothing more to do, the citationLM's parent
        // will be set to the fotnoteLM's parent
        citationLM.setParent(this);
        citationLM.initialize();
        bodyLM.setParent(this);
        bodyLM.initialize();

        // get Knuth elements representing the footnote citation
        List returnedList = new LinkedList();
        while (!citationLM.isFinished()) {
            List partialList = citationLM.getNextKnuthElements(context, alignment);
            if (partialList != null) {
                returnedList.addAll(partialList);
            }
        }
        if (returnedList.size() == 0) {
            //Inline part of the footnote is empty. Need to send back an auxiliary
            //zero-width, zero-height inline box so the footnote gets painted.
            KnuthSequence seq = new InlineKnuthSequence();
            //Need to use an aux. box, otherwise, the line height can't be forced to zero height.
            forcedAnchor = new KnuthInlineBox(0, null, null, true);
            seq.add(forcedAnchor);
            returnedList.add(seq);
        }
        setFinished(true);

        addAnchor(returnedList);

        // "wrap" the Position stored in each list inside returnedList
        ListIterator listIterator = returnedList.listIterator();
        ListIterator elementIterator = null;
        KnuthSequence list = null;
        ListElement element = null;
        while (listIterator.hasNext()) {
            list = (KnuthSequence) listIterator.next();
            elementIterator = list.listIterator();
            while (elementIterator.hasNext()) {
                element = (KnuthElement) elementIterator.next();
                element.setPosition(notifyPos(new NonLeafPosition(this, element.getPosition())));
            }
        }

        return returnedList;
    }

    /** {@inheritDoc} */
    @Override
    public List getChangedKnuthElements(List oldList, int alignment, int depth) {
        List returnedList = super.getChangedKnuthElements(oldList, alignment, depth);
        addAnchor(returnedList);
        return returnedList;
    }


    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        // "Unwrap" the NonLeafPositions stored in posIter and put
        // them in a new list, that will be given to the citationLM
        LinkedList<Position> positionList = new LinkedList<Position>();
        Position pos;
        while (posIter.hasNext()) {
            pos = posIter.next();
            if (pos != null && pos.getPosition() != null) {
                positionList.add(pos.getPosition());
            }
        }

        // FootnoteLM does not create any area,
        // so the citationLM child will add directly to the FootnoteLM parent area
        citationLM.setParent(getParent());

        // make the citationLM add its areas
        LayoutContext childContext = new LayoutContext(context);
        PositionIterator childPosIter = new PositionIterator(positionList.listIterator());
        LayoutManager childLM;
        while ((childLM = childPosIter.getNextChildLM()) != null) {
            childLM.addAreas(childPosIter, childContext);
            childContext.setLeadingSpace(childContext.getTrailingSpace());
            childContext.setFlags(LayoutContext.RESOLVE_LEADING_SPACE, true);
        }
    }

    /**
     * Find the last box in the sequence, and add a reference to the FootnoteBodyLM
     * @param citationList the list of elements representing the footnote citation
     */
    private void addAnchor(List citationList) {
        KnuthInlineBox lastBox = null;
        // the list of elements is searched backwards, until we find a box
        ListIterator citationIterator = citationList.listIterator(citationList.size());
        while (citationIterator.hasPrevious() && lastBox == null) {
            Object obj = citationIterator.previous();
            if (obj instanceof KnuthElement) {
                // obj is an element
                KnuthElement element = (KnuthElement)obj;
                if (element instanceof KnuthInlineBox) {
                    lastBox = (KnuthInlineBox) element;
                }
            } else {
                // obj is a sequence of elements
                KnuthSequence seq = (KnuthSequence)obj;
                ListIterator nestedIterator = seq.listIterator(seq.size());
                while (nestedIterator.hasPrevious() && lastBox == null) {
                    KnuthElement element = (KnuthElement)nestedIterator.previous();
                    if (element instanceof KnuthInlineBox && !element.isAuxiliary()
                            || element == forcedAnchor) {
                        lastBox = (KnuthInlineBox) element;
                    }
                }
            }
        }
        if (lastBox != null) {
            lastBox.setFootnoteBodyLM(bodyLM);
        } else {
            //throw new IllegalStateException("No anchor box was found for a footnote.");
        }
    }
}
