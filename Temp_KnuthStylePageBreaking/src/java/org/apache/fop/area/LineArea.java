/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

    // this is the offset for the dominant baseline
    private int baseLine;

    // this class can contain the dominant char styling info
    // this means that many renderers can optimise a bit

    private List inlineAreas = new ArrayList();

    /**
     * Add a child area to this line area.
     *
     * @param childArea the inline child area to add
     */
    public void addChildArea(Area childArea) {
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

