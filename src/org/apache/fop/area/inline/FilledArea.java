/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.area.MinOptMax;

import java.util.List;
import java.util.ArrayList;

/**
 * Filled area.
 * This inline area contains some inline areas.
 * When the renderer gets the child areas to render
 * the inline areas are repeated to fill the ipd of
 * this inline parent.
 */
public class FilledArea extends InlineParent {
    private int unitWidth;

    public FilledArea() {
    }

    public void setUnitWidth(int w) {
        unitWidth = w;
    }

    public List getChildAreas() {
        int units = (int)(getWidth() / unitWidth);
        ArrayList newList = new ArrayList();
        for (int count = 0; count < units; count++) {
            newList.addAll(inlines);
        }
        return newList;
    }
}

