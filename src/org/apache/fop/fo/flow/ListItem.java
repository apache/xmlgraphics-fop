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
import org.apache.fop.layout.*;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Iterator;

public class ListItem extends FObj {

    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    String id;
    BlockArea blockArea;

    public ListItem(FONode parent) {
        super(parent);
    }

    public Status layout(Area area) throws FOPException {
        if (this.marker == START) {

            // Common Accessibility Properties
            AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

            // Common Aural Properties
            AuralProps mAurProps = propMgr.getAuralProps();

            // Common Border, Padding, and Background Properties
            BorderAndPadding bap = propMgr.getBorderAndPadding();
            BackgroundProps bProps = propMgr.getBackgroundProps();

            // Common Margin Properties-Block
            MarginProps mProps = propMgr.getMarginProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("break-after");
            // this.properties.get("break-before");
            // this.properties.get("id");
            // this.properties.get("keep-together");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("relative-align");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.id = this.properties.get("id").getString();

            area.getIDReferences().createID(id);

            this.marker = 0;
        }

        /* not sure this is needed given we know area is from list block */
        if (area instanceof BlockArea) {
            area.end();
        }

        if (spaceBefore != 0) {
            area.addDisplaySpace(spaceBefore);
        }

        this.blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(), 0, 0,
                          0, align, alignLast, lineHeight);
        this.blockArea.setGeneratedBy(this);
        this.areasGenerated++;
        if (this.areasGenerated == 1)
            this.blockArea.isFirst(true);
            // for normal areas this should be the only pair
        this.blockArea.addLineagePair(this, this.areasGenerated);

        // markers
        //if (this.hasMarkers())
            //this.blockArea.addMarkers(this.getMarkers());

        blockArea.setPage(area.getPage());
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        if (numChildren != 2) {
            throw new FOPException("list-item must have exactly two children");
        }
        ListItemLabel label = (ListItemLabel)children.get(0);
        ListItemBody body = (ListItemBody)children.get(1);

        Status status;

        // what follows doesn't yet take into account whether the
        // body failed completely or only got some text in

        if (this.marker == 0) {
            // configure id
            area.getIDReferences().configureID(id, area);

            status = label.layout(blockArea);
            if (status.isIncomplete()) {
                return status;
            }
        }

        status = body.layout(blockArea);
        if (status.isIncomplete()) {
            blockArea.end();
            area.addChild(blockArea);
            area.increaseHeight(blockArea.getHeight());
            area.setAbsoluteHeight(blockArea.getAbsoluteHeight());
            this.marker = 1;
            return status;
        }

        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());
        area.setAbsoluteHeight(blockArea.getAbsoluteHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        /* not sure this is needed given we know area is from list block */
        if (area instanceof BlockArea) {
            area.start();
        }
        this.blockArea.isLast(true);
        return new Status(Status.OK);
    }

    /**
     * Return the content width of the boxes generated by this FO.
     */
    public int getContentWidth() {
        if (blockArea != null)
            return blockArea.getContentWidth();    // getAllocationWidth()??
        else
            return 0;                              // not laid out yet
    }

    public boolean generatesInlineAreas() {
        return false;
    }

}
