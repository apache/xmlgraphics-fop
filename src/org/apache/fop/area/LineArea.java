/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.area.inline.InlineArea;

import java.util.ArrayList;
import java.util.List;

// a line area can contain information in ranges of child inline
// areas that have properties such as
// links, background, underline, bold, id areas
public class LineArea extends Area {
    int stacking = LR;
    // contains inline areas
    // has start indent and length, dominant baseline, height
    int startIndent;
    int length;

    int lineHeight;
    // this is the offset for the dominant baseline
    int baseLine;

    // this class can contain the dominant char styling info
    // this means that many renderers can optimise a bit

    ArrayList inlineAreas = new ArrayList();

    public void setHeight(int height) {
        lineHeight = height;
    }

    public int getHeight() {
        return lineHeight;
    }

    public void addInlineArea(InlineArea area) {
        inlineAreas.add(area);
    }

    public List getInlineAreas() {
        return inlineAreas;
    }

    // store properties in array list, need better solution
    ArrayList props = null;

    public void addTrait(Trait prop) {
        if (props == null) {
            props = new ArrayList();
        }
        props.add(prop);
    }

    public List getTraitList() {
        return props;
    }
}

