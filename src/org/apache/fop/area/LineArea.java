/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layoutmgr.LayoutInfo;
import org.apache.fop.fo.properties.VerticalAlign;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

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

    public MinOptMax getContentBPD() {
        return new MinOptMax(lineHeight);
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

    public void verticalAlign(int lh, int lead, int follow) {
        int maxHeight = lh;
        List inlineAreas = getInlineAreas();

        // get smallest possible offset to before edge
        // this depends on the height of no and middle alignments
        int before = lead;
        int after = follow;
        int halfLeading = (lineHeight - lead - follow) / 2;
        before += halfLeading;
        for (Iterator iter = inlineAreas.iterator(); iter.hasNext();) {
            InlineArea inline = (InlineArea) iter.next();
            LayoutInfo info = inline.info;
            int al;
            int ld = inline.getHeight();
            if (info != null) {
                al = info.alignment;
                ld = info.lead;
            } else {
                al = VerticalAlign.BASELINE;
            }
            if (al == VerticalAlign.BASELINE) {
                if (ld > before) {
                    before = ld;
                }
                if (inline.getHeight() > before) {
                    before = inline.getHeight();
                }
            } else if (al == VerticalAlign.MIDDLE) {
                if (inline.getHeight() / 2 + lead / 2 > before) {
                    before = inline.getHeight() / 2 + lead / 2;
                }
                if (inline.getHeight() / 2 - lead / 2 > after) {
                    after = inline.getHeight() / 2 - lead / 2;
                }
            } else if (al == VerticalAlign.TOP) {
            } else if (al == VerticalAlign.BOTTOM) {
            }
        }
        // then align all before, no and middle alignment
        for (Iterator iter = inlineAreas.iterator(); iter.hasNext();) {
            InlineArea inline = (InlineArea) iter.next();
            LayoutInfo info = inline.info;
            int al;
            int ld = inline.getHeight();
            boolean bloffset = false;
            if (info != null) {
                al = info.alignment;
                ld = info.lead;
                bloffset = info.blOffset;
            } else {
                al = VerticalAlign.BASELINE;
            }
            if (al == VerticalAlign.BASELINE) {
                // the offset position for text is the baseline
                if (bloffset) {
                    inline.setOffset(before);
                } else {
                    inline.setOffset(before - ld);
                }
                if (inline.getHeight() - ld > after) {
                    after = inline.getHeight() - ld;
                }
            } else if (al == VerticalAlign.MIDDLE) {
                inline.setOffset(before - inline.getHeight() / 2 -
                                 lead / 2);
            } else if (al == VerticalAlign.TOP) {
                inline.setOffset(0);
                if (inline.getHeight() - before > after) {
                    after = inline.getHeight() - before;
                }
            } else if (al == VerticalAlign.BOTTOM) {
                if (inline.getHeight() - before > after) {
                    after = inline.getHeight() - before;
                }
            }
        }

        // after alignment depends on maximum height of before
        // and middle alignments
        for (Iterator iter = inlineAreas.iterator(); iter.hasNext();) {
            InlineArea inline = (InlineArea) iter.next();
            LayoutInfo info = inline.info;
            int al;
            if (info != null) {
                al = info.alignment;
            } else {
                al = VerticalAlign.BASELINE;
            }
            if (al == VerticalAlign.BASELINE) {
            } else if (al == VerticalAlign.MIDDLE) {
            } else if (al == VerticalAlign.TOP) {
            } else if (al == VerticalAlign.BOTTOM) {
                inline.setOffset(before + after - inline.getHeight());
            }
        }
        if (before + after > maxHeight) {
            setHeight(before + after);
        } else {
            setHeight(maxHeight);
        }
    }
}

