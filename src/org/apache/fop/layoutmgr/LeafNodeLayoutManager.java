/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.FObj;
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;


/**
 * Base LayoutManager for leaf-node FObj, ie: ones which have no children.
 * These are all inline objects. Most of them cannot be split (Text is
 * an exception to this rule.)
 */
public class LeafNodeLayoutManager extends AbstractLayoutManager {

    private InlineArea curArea;

    public LeafNodeLayoutManager(FObj fobj) {
        super(fobj);
    }


    public void setCurrentArea(InlineArea ia) {
        curArea = ia;
    }

    public void generateAreas() {
        flush();
    }

    protected void flush() {
        parentLM.addChild(curArea);
    }

    /**
     * This is a leaf-node, so this method is never called.
     */
    public void addChild(Area childArea) {}


    /**
     * This is a leaf-node, so this method is never called.
     */
    public Area getParentArea(Area childArea) {
        return null;
    }


}
