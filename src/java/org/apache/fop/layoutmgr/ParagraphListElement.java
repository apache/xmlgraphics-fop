/*
 * Copyright 2007 The Apache Software Foundation.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.fop.layoutmgr.inline.KnuthInlineBox;
import org.apache.fop.layoutmgr.inline.KnuthParagraph;
import org.apache.fop.layoutmgr.list.LineBreakingListElement;

/**
 * This class represents a List Element for a Knuth Paragraph
 */
public class ParagraphListElement extends LineBreakingListElement {

    private KnuthParagraph para;
    private boolean lineBreakingStarting = true;
    private boolean lineBreakingFinished = false;
    
    /**
     * @param para The Knuth Paragraph
     * @param position The position of this List Element
     */
    public ParagraphListElement(KnuthParagraph para, Position position) {
        super(position);
        this.para = para;
    }
    
    /**
     * @return the Knuth Paragraph
     */
    public KnuthParagraph getPara() {
        return para;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.ListElement#isUnresolvedElement()
     */
    public boolean isUnresolvedElement() {
        return false;
    }
    
    public LinkedList doLineBreaking() {
        LinkedList returnList = para.getLineLayoutManager().createLineBreaks(para);
        wrapPositions(returnList);
        lineBreakingStarting = false;
        lineBreakingFinished = true;
        return returnList;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#lineBreakingIsStarting()
     */
    public boolean lineBreakingIsStarting() {
        return lineBreakingStarting;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#lineBreakingIsFinished()
     */
    public boolean lineBreakingIsFinished() {
        return lineBreakingFinished;
    }

    /**
     * Wrap the position in each element in list in a position stack
     * which is a copy of the position stack of this paragraph list element.
     * @param list
     */
    private void wrapPositions(LinkedList list) {
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            ListElement elt = (ListElement) iter.next();
            Position pos = getPosition();
            pos = pos.getLM().rewrapPosition(pos, elt.getPosition());
            elt.setPosition(pos);
        }
    }

    public boolean getFootnoteKnuthElements(LayoutManager lm, LayoutContext context, int alignment) {
        boolean bFootnotesPresent = false;
        if (para != null) {
            ListIterator paraListIterator = para.listIterator();
            while (paraListIterator.hasNext()) {
                ListElement element = (ListElement) paraListIterator.next();
                if (element instanceof KnuthInlineBox
                        && ((KnuthInlineBox) element).getFootnoteBodyLM() != null) {
                    bFootnotesPresent = true;
                    FootnoteBodyLayoutManager fblm = ((KnuthInlineBox) element).getFootnoteBodyLM();
                    fblm.initialize();
                    fblm.setParent(lm);
                    fblm.getKnuthElements(context, alignment);
                }
            }
        }
        return bFootnotesPresent;
    }
    
}
