/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import java.util.List;
import java.util.ListIterator;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.LMVisited;

/**
 * Visitor pattern for the purpose of adding
 * Layout Managers to nodes in the FOTree.
 * Each method is responsible to return a LayoutManager 
 * responsible for laying out this FObj's content.
 */
public class AddLMVisitor {

    /** The List object to which methods in this class should add Layout
     *  Managers */
    protected List currentLMList;

    /** A List object which can be used to save and restore the currentLMList if
     * another List should temporarily be used */
    protected List saveLMList;

    /**
     *
     * @param fobj the FObj object for which a layout manager should be created
     * @param lmList the list to which the newly created layout manager(s)
     * should be added
     */
    public void addLayoutManager(FObj fobj, List lmList) {
        /* Store the List in a global variable so that it can be accessed by the
           Visitor methods */
        currentLMList = lmList;
        if (fobj instanceof LMVisited) {
            ((LMVisited) fobj).acceptVisitor(this);
        } else {
            fobj.addLayoutManager(currentLMList);
        }
    }

    /**
     * Accessor for the currentLMList.
     * @return the currentLMList.
     */
    public List getCurrentLMList() {
        return currentLMList;
    }

    /**
     * Accessor for the saveLMList.
     * @return the saveLMList.
     */
    public List getSaveLMList() {
        return saveLMList;
    }

    /**
     * @param node Wrapper object to process
     */
    public void serveWrapper(Wrapper node) {
        ListIterator baseIter;
        baseIter = node.getChildNodes();
        if (baseIter == null) return;
        while (baseIter.hasNext()) {
            FObj child = (FObj) baseIter.next();
            if (child instanceof LMVisited) {
                ((LMVisited) child).acceptVisitor(this);
            } else {
                child.addLayoutManager(currentLMList);
            }
        }
    }
}
