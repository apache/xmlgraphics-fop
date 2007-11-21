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

package org.apache.fop.layoutmgr.list;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.fop.layoutmgr.LayoutContext;
import org.apache.fop.layoutmgr.ListElement;
import org.apache.fop.layoutmgr.Position;


/**
 * 
 */
public class ListItemListElement extends LineBreakingListElement {
    
    private ListItemLayoutManager lm;
    private LayoutContext context;
    
    public ListItemListElement(ListItemLayoutManager lm, Position pos, LayoutContext context) {
        super(pos);
        this.lm = lm;
        this.context = context;
    }

    /**
     * @return the lm
     */
    public ListItemLayoutManager getListItemLayoutManager() {
        return lm;
    }

    /**
     * @return the context
     */
    public LayoutContext getContext() {
        return context;
    }
    
    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#doLineBreaking()
     */
    public LinkedList doLineBreaking() {
        LinkedList returnList = lm.doLineBreaking(context);
        wrapPositions(returnList);
        return returnList;
    }

    /**
     * @return the widowRowLimit
     */
    public int getWidowRowLimit() {
        return lm.getWidowRowLimit();
    }

    /**
     * @return the orphanRowLimit
     */
    public int getOrphanRowLimit() {
        return lm.getOrphanRowLimit();
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
    
    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#lineBreakingIsStarting()
     */
    public boolean lineBreakingIsStarting() {
        return lm.lineBreakingIsStarting();
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#lineBreakingIsFinished()
     */
    public boolean lineBreakingIsFinished() {
        return lm.lineBreakingIsFinished();
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#isStartOfSubsequence()
     */
    public boolean isStartOfSubsequence() {
        return lm.isStartOfSubsequence();
    }

    /* (non-Javadoc)
     * @see org.apache.fop.layoutmgr.list.LineBreakingListElement#isEndOfSubsequence()
     */
    public boolean isEndOfSubsequence() {
        return lm.isEndOfSubsequence();
    }

}
