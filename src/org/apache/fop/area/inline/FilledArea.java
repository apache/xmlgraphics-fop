/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import java.util.List;
import java.util.ArrayList;

/**
 * Filled area.
 * This inline area contains some inline areas.
 * When the renderer gets the child areas to render
 * the inline areas are repeated to fill the ipd of
 * this inline parent.
 * This extends InlineParent so that the renderer will render
 * this as a normal inline parent.
 */
public class FilledArea extends InlineParent {
    private int unitWidth;

    /**
     * Create a new filled area.
     */
    public FilledArea() {
    }

    /**
     * Set the unit width for the areas to fill the full width.
     *
     * @param w the unit width
     */
    public void setUnitWidth(int w) {
        unitWidth = w;
    }

    /**
     * Get the child areas for this filed area.
     * This copies the references of the inline areas so that
     * it fills the total width of the area a whole number of times
     * for the unit width.
     *
     * @return the list of child areas copied to fill the width
     */
    public List getChildAreas() {
        int units = (int)(getWidth() / unitWidth);
        ArrayList newList = new ArrayList();
        for (int count = 0; count < units; count++) {
            newList.addAll(inlines);
        }
        return newList;
    }
}

