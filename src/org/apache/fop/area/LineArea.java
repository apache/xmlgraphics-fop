/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.area.inline.InlineArea;

import java.util.ArrayList;
import java.util.List;

/**
 * The line area.
 * This is a line area that contains inline areas.
 */
public class LineArea extends Area {
    private int stacking = LR;
    // contains inline areas
    // has start indent and length, dominant baseline, height
    private int startIndent;
    private int length;

    private int lineHeight;
    // this is the offset for the dominant baseline
    private int baseLine;

    // this class can contain the dominant char styling info
    // this means that many renderers can optimise a bit

    private List inlineAreas = new ArrayList();

    /**
     * Set the height of this line area.
     *
     * @param height the height of the line area
     */
    public void setHeight(int height) {
        lineHeight = height;
    }

    /**
     * Get the height of this line area.
     *
     * @return the height of the line area
     */
    public int getHeight() {
        return lineHeight;
    }

    /**
     * Add a child area to this line area.
     *
     * @param childArea the inline child area to add
     */
    public void addChild(Area childArea) {
        if (childArea instanceof InlineArea) {
            addInlineArea((InlineArea)childArea);
        }
    }

    /**
     * Add an inline child area to this line area.
     *
     * @param area the inline child area to add
     */
    public void addInlineArea(InlineArea area) {
        inlineAreas.add(area);
    }

    /**
     * Get the inline child areas of this line area.
     *
     * @return the list of inline areas
     */
    public List getInlineAreas() {
        return inlineAreas;
    }

    /**
     * Set the start indent of this line area.
     * The start indent is used for offsetting the start of
     * the inline areas for alignment or other indents.
     *
     * @param si the start indent value
     */
    public void setStartIndent(int si) {
        startIndent = si;
    }

    /**
     * Get the start indent of this line area.
     * The start indent is used for offsetting the start of
     * the inline areas for alignment or other indents.
     *
     * @return the start indent value
     */
    public int getStartIndent() {
        return startIndent;
    }
}

