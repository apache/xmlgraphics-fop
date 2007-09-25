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

import org.apache.fop.layoutmgr.inline.KnuthParagraph;

/**
 * This class represents a List Element for a Knuth Paragraph
 */
public class ParagraphListElement extends ListElement {

    private KnuthParagraph para;
    
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
        return returnList;
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

}
