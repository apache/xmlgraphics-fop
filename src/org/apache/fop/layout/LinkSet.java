/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

import org.apache.fop.layout.inline.InlineArea;

// Java
import java.util.List;
import java.awt.Rectangle;

/**
 * a set of rectangles on a page that are linked to a common
 * destination
 */
public class LinkSet {

    /**
     * the destination of the links
     */
    private String destination;

    /**
     * the set of rectangles
     */
    private List rects = new java.util.ArrayList();

    private int xoffset = 0;
    private int yoffset = 0;

    /* the maximum Y offset value encountered for this LinkSet */
    private int maxY = 0;

    protected int startIndent;
    protected int endIndent;

    private int linkType;

    private Area area;

    public static final int INTERNAL = 0;    // represents internal link
    public static final int EXTERNAL = 1;    // represents external link

    // property required for alignment adjustments
    private int contentRectangleWidth = 0;

    public LinkSet(String destination, Area area, int linkType) {
        this.destination = destination;
        this.area = area;
        this.linkType = linkType;
    }

    public void addRect(Rectangle r, LineArea lineArea,
                        InlineArea inlineArea) {
        LinkedRectangle linkedRectangle = new LinkedRectangle(r, lineArea,
                inlineArea);
        linkedRectangle.setY(this.yoffset);
        if (this.yoffset > maxY) {
            maxY = this.yoffset;
        }
        rects.add(linkedRectangle);
    }

    public void setYOffset(int y) {
        this.yoffset = y;
    }

    public void setXOffset(int x) {
        this.xoffset = x;
    }

    public void setContentRectangleWidth(int contentRectangleWidth) {
        this.contentRectangleWidth = contentRectangleWidth;
    }

    public void applyAreaContainerOffsets(AreaContainer ac, Area area) {
        int height = area.getAbsoluteHeight();
        BlockArea ba = (BlockArea)area;
        for (int i = 0; i < rects.size(); i++ ) {
            LinkedRectangle r = (LinkedRectangle)rects.get(i);
            r.setX(r.getX() + ac.getXPosition() + area.getTableCellXOffset());
            r.setY(ac.getYPosition() - height + (maxY - r.getY())
                   - ba.getHalfLeading());
        }
    }

    // intermediate implementation for joining all sublinks on same line
    public void mergeLinks() {
        int numRects = rects.size();
        if (numRects <= 1) return;

        LinkedRectangle curRect =
            new LinkedRectangle((LinkedRectangle)rects.get(0));
        List nv = new java.util.ArrayList();

        for (int ri = 1; ri < numRects; ri++) {
            LinkedRectangle r = (LinkedRectangle)rects.get(ri);

            // yes, I'm really happy with comparing refs...
            if (r.getLineArea() == curRect.getLineArea()) {
                curRect.setWidth(r.getX() + r.getWidth() - curRect.getX());
            } else {
                nv.add(curRect);
                curRect = new LinkedRectangle(r);
            }

            if (ri == numRects - 1) {
                nv.add(curRect);
            }
        }

        rects = nv;
    }

    public void align() {
        for (int i = 0; i < rects.size(); i++ ) {
            LinkedRectangle r = (LinkedRectangle)rects.get(i);
            r.setX(r.getX() + r.getLineArea().getStartIndent()
                   + r.getInlineArea().getXOffset());
        }
    }

    public String getDest() {
        return this.destination;
    }

    public List getRects() {
        return this.rects;
    }

    public int getEndIndent() {
        return endIndent;
    }

    public int getStartIndent() {
        return startIndent;
    }

    public Area getArea() {
        return area;
    }

    public int getLinkType() {
        return linkType;
    }

}
