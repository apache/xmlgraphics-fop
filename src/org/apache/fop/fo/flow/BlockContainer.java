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
import org.apache.fop.layoutmgr.BlockContainerLayoutManager;

import org.xml.sax.Attributes;

import java.util.List;

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

    public BlockContainer(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.span = this.properties.get("span").getEnum();
        setupID();
    }

    public void addLayoutManager(List list) {
        BlockContainerLayoutManager blm = new BlockContainerLayoutManager(this);
        blm.setOverflow(properties.get("overflow").getEnum());
        list.add(blm);
    }

    public void setup() {

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
            setupID();
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("overflow");
            // this.properties.get("reference-orientation");
            // this.properties.get("span");
            // this.properties.get("width");
            // this.properties.get("writing-mode");

            this.backgroundColor =
                this.properties.get("background-color").getColorType();

            this.width = this.properties.get("width").getLength().mvalue();
            this.height = this.properties.get("height").getLength().mvalue();
            span = this.properties.get("span").getEnum();

    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    public boolean generatesInlineAreas() {
        return false;
    }

    protected boolean containsMarkers() {
        return true;
    }

    public int getSpan() {
        return this.span;
    }

}

