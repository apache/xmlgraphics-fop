/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layoutmgr;


import org.apache.fop.area.Area;

/**
 * The interface for all LayoutManagers.
 */
public interface LayoutManager {
    public boolean generateAreas();
    public boolean generatesInlineAreas();
    public Area getParentArea (Area childArea);
    public boolean addChild (Area childArea);
    public boolean splitArea(Area areaToSplit, SplitContext context);
    public void setParentLM(LayoutManager lm);
    public int getContentIPD();
}
