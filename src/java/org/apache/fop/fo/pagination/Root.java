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

package org.apache.fop.fo.pagination;

// java
import java.util.List;

// FOP
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * The fo:root formatting object. Contains page masters, page-sequences.
 */
public class Root extends FObj {
    private LayoutMasterSet layoutMasterSet;
    private List pageSequences;

    /**
     * Keeps count of page number from over PageSequence instances
     */
    private int runningPageNumberCounter = 0;

    private FOTreeControl foTreeControl = null;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public Root(FONode parent) {
        super(parent);
        // this.propertyList.get("media-usage");
        pageSequences = new java.util.ArrayList();
        if (parent != null) {
            //throw new FOPException("root must be root element");
        }
    }

    /**
     * Returns the number of pages generated (over all PageSequence instances).
     * @return the number of pages
     */
    protected int getRunningPageNumberCounter() {
        return this.runningPageNumberCounter;
    }

    /**
     * Sets the overall page number counter.
     * @param count the new page count
     */
    public void setRunningPageNumberCounter(int count) {
        this.runningPageNumberCounter = count;
    }

    /**
     * Returns the number of PageSequence instances.
     * @return the number of PageSequence instances
     */
    public int getPageSequenceCount() {
        return pageSequences.size();
    }

    /**
     * Some properties, such as 'force-page-count', require a
     * page-sequence to know about some properties of the next.
     * @param current the current PageSequence
     * @return succeeding PageSequence; null if none
     */
    public PageSequence getSucceedingPageSequence(PageSequence current) {
        int currentIndex = pageSequences.indexOf(current);
        if (currentIndex == -1) {
            return null;
        }
        if (currentIndex < (pageSequences.size() - 1)) {
            return (PageSequence)pageSequences.get(currentIndex + 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the associated LayoutMasterSet.
     * @return the LayoutMasterSet instance
     */
    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    /**
     * Sets the associated LayoutMasterSet.
     * @param layoutMasterSet the LayoutMasterSet to use
     */
    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }

    /**
     * Sets the FOTreeControl that this Root is attached to
     * @param foTreeControl the FOTreeControl implementation to which this Root
     * is attached
     */
    public void setFOTreeControl(FOTreeControl foTreeControl) {
        this.foTreeControl = foTreeControl;
    }

    /**
     * This method overrides the FONode version. The FONode version calls the
     * method by the same name for the parent object. Since Root is at the top
     * of the tree, it returns the actual foTreeControl object. Thus, any FONode
     * can use this chain to find which foTreeControl it is being built for.
     * @return the FOTreeControl implementation that this Root is attached to
     */
    public FOTreeControl getFOTreeControl() {
        return foTreeControl;
    }

    /**
     * Hook for Visitor objects accessing the FO Tree.
     * @param fotv the FOTreeVisitor object accessing this node of the FO Tree
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveRoot(this);
    }

}
