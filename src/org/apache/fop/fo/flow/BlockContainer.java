/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;


import org.xml.sax.Attributes;

public class BlockContainer extends FObj {

    ColorType backgroundColor;
    int position;

    int top;
    int bottom;
    int left;
    int right;
    int width;
    int height;

    int span;

    AreaContainer areaContainer;

    PageSequence pageSequence;

    public BlockContainer(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.span = this.properties.get("span").getEnum();
    }

    public Status layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AbsolutePositionProps mAbsProps = propMgr.getAbsolutePositionProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin-Block Properties
            MarginProps mProps = propMgr.getMarginProps();

            // this.properties.get("block-progression-dimension");
            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("clip");
            // this.properties.get("display-align");
            // this.properties.get("height");
            // this.properties.get("id");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("overflow");
            // this.properties.get("reference-orientation");
            // this.properties.get("span");
            // this.properties.get("width");
            // this.properties.get("writing-mode");

            this.marker = 0;

            this.backgroundColor =
                this.properties.get("background-color").getColorType();

            this.position = this.properties.get("position").getEnum();
            this.top = this.properties.get("top").getLength().mvalue();
            this.bottom = this.properties.get("bottom").getLength().mvalue();
            this.left = this.properties.get("left").getLength().mvalue();
            this.right = this.properties.get("right").getLength().mvalue();
            this.width = this.properties.get("width").getLength().mvalue();
            this.height = this.properties.get("height").getLength().mvalue();
            span = this.properties.get("span").getEnum();

            // initialize id
            String id = this.properties.get("id").getString();
            area.getIDReferences().initializeID(id, area);
        }

        boolean prevChildMustKeepWithNext = false;

        AreaContainer container = (AreaContainer)area;
        if ((this.width == 0) && (this.height == 0)) {
            width = right - left;
            height = bottom - top;
        }

        this.areaContainer =
            new AreaContainer(propMgr.getFontState(container.getFontInfo()),
                              container.getXPosition() + left,
                              container.getYPosition() - top, width, height,
                              position);

        areaContainer.setPage(area.getPage());
        areaContainer.setBackgroundColor(backgroundColor);
        areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
        areaContainer.start();

        areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
        areaContainer.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FObj fo = (FObj)children.get(i);
            Status status;
            if ((status = fo.layout(areaContainer)).isIncomplete()) {
                /*
                 * if ((prevChildMustKeepWithNext) && (status.laidOutNone())) {
                 * this.marker = i - 1;
                 * FObj prevChild = (FObj) children.elementAt(this.marker);
                 * prevChild.removeAreas();
                 * prevChild.resetMarker();
                 * return new Status(Status.AREA_FULL_SOME);
                 * // should probably return AREA_FULL_NONE if first
                 * // or perhaps an entirely new status code
                 * } else {
                 * this.marker = i;
                 * return status;
                 * }
                 */
            }
            if (status.getCode() == Status.KEEP_WITH_NEXT) {
                prevChildMustKeepWithNext = true;
            }
        }
        area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

        areaContainer.end();
        if (position == Position.ABSOLUTE)
            areaContainer.setHeight(height);
        area.addChild(areaContainer);

        return new Status(Status.OK);
    }

    /**
     * Return the content width of the boxes generated by this block
     * container FO.
     */
    public int getContentWidth() {
        if (areaContainer != null)
            return areaContainer.getContentWidth();    // getAllocationWidth()??
        else
            return 0;    // not laid out yet
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    public boolean generatesInlineAreas() {
        return false;
    }

    public int getSpan() {
        return this.span;
    }

}
