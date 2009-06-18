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
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Iterator;

public class ListBlock extends FObj {

    int align;
    int alignLast;
    int breakBefore;
    int breakAfter;
    int lineHeight;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int spaceBetweenListRows = 0;
    ColorType backgroundColor;

    public ListBlock(FONode parent) {
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
            // this.properties.get("provisional-distance-between-starts");
            // this.properties.get("provisional-label-separation");

            this.align = this.properties.get("text-align").getEnum();
            this.alignLast = this.properties.get("text-align-last").getEnum();
            this.lineHeight =
                this.properties.get("line-height").getLength().mvalue();
            this.startIndent =
                this.properties.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.properties.get("end-indent").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();
            this.spaceBetweenListRows = 0;    // not used at present
            this.backgroundColor =
                this.properties.get("background-color").getColorType();

            this.marker = 0;

            if (area instanceof BlockArea) {
                area.end();
            }

            if (spaceBefore != 0) {
                area.addDisplaySpace(spaceBefore);
            }

            if (this.isInTableCell) {
                startIndent += forcedStartOffset;
                endIndent += area.getAllocationWidth() - forcedWidth
                             - forcedStartOffset;
            }

            // initialize id
            String id = this.properties.get("id").getString();
            area.getIDReferences().initializeID(id, area);
        }

        BlockArea blockArea =
            new BlockArea(propMgr.getFontState(area.getFontInfo()),
                          area.getAllocationWidth(), area.spaceLeft(),
                          startIndent, endIndent, 0, align, alignLast,
                          lineHeight);
        blockArea.setGeneratedBy(this);
        this.areasGenerated++;
        if (this.areasGenerated == 1)
            blockArea.isFirst(true);
            // for normal areas this should be the only pair
        blockArea.addLineagePair(this, this.areasGenerated);

        // markers
        //if (this.hasMarkers())
            //blockArea.addMarkers(this.getMarkers());


        blockArea.setPage(area.getPage());
        blockArea.setBackgroundColor(backgroundColor);
        blockArea.start();

        blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
        blockArea.setIDReferences(area.getIDReferences());

        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            if (!(children.get(i) instanceof ListItem)) {
                log.error("children of list-blocks must be list-items");
                return new Status(Status.OK);
            }
            ListItem listItem = (ListItem)children.get(i);
            Status status;
            if ((status = listItem.layout(blockArea)).isIncomplete()) {
                if (status.getCode() == Status.AREA_FULL_NONE && i > 0) {
                    status = new Status(Status.AREA_FULL_SOME);
                }
                this.marker = i;
                blockArea.end();
                area.addChild(blockArea);
                area.increaseHeight(blockArea.getHeight());
                area.setAbsoluteHeight(blockArea.getAbsoluteHeight());
                return status;
            }
        }

        blockArea.end();
        area.addChild(blockArea);
        area.increaseHeight(blockArea.getHeight());
        area.setAbsoluteHeight(blockArea.getAbsoluteHeight());

        if (spaceAfter != 0) {
            area.addDisplaySpace(spaceAfter);
        }

        if (area instanceof BlockArea) {
            area.start();
        }

        blockArea.isLast(true);
        return new Status(Status.OK);
    }

    public boolean generatesInlineAreas() {
        return false;
    }


}
