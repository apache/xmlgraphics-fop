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
import org.apache.fop.apps.FOPException;
import org.apache.fop.image.*;

// Java
import java.util.Enumeration;
import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;

public class ExternalGraphic extends FObj {

    int breakAfter;
    int breakBefore;
    int align;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    String src;
    int height;
    int width;
    String id;

    ImageArea imageArea;

    public ExternalGraphic(FObj parent) {
        super(parent);
        this.name = "fo:external-graphic";
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

            // Common Margin Properties-Inline
            MarginInlineProps mProps = propMgr.getMarginInlineProps();

            // Common Relative Position Properties
            RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

            // this.properties.get("alignment-adjust");
            // this.properties.get("alignment-baseline");
            // this.properties.get("baseline-shift");
            // this.properties.get("block-progression-dimension");
            // this.properties.get("content-height");
            // this.properties.get("content-type");
            // this.properties.get("content-width");
            // this.properties.get("display-align");
            // this.properties.get("dominant-baseline");
            // this.properties.get("height");
            // this.properties.get("id");
            // this.properties.get("inline-progression-dimension");
            // this.properties.get("keep-with-next");
            // this.properties.get("keep-with-previous");
            // this.properties.get("line-height");
            // this.properties.get("line-height-shift-adjustment");
            // this.properties.get("overflow");
            // this.properties.get("scaling");
            // this.properties.get("scaling-method");
            // this.properties.get("src");
            // this.properties.get("text-align");
            // this.properties.get("width");

            // FIXME
            this.align = this.properties.get("text-align").getEnum();

            this.startIndent =
                this.properties.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.properties.get("end-indent").getLength().mvalue();

            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();

            this.src = this.properties.get("src").getString();

            this.width = this.properties.get("width").getLength().mvalue();

            this.height = this.properties.get("height").getLength().mvalue();

            this.id = this.properties.get("id").getString();

            area.getIDReferences().createID(id);
            /*
             * if (area instanceof BlockArea) {
             * area.end();
             * }
             * if (this.isInTableCell) {
             * startIndent += forcedStartOffset;
             * endIndent = area.getAllocationWidth() - forcedWidth -
             * forcedStartOffset;
             * }
             */
            this.marker = 0;
        }

        try {
            FopImage img = FopImageFactory.Make(src);
            // if width / height needs to be computed
            if ((width == 0) || (height == 0)) {
                // aspect ratio
                double imgWidth = img.getWidth();
                double imgHeight = img.getHeight();
                if ((width == 0) && (height == 0)) {
                    width = (int)((imgWidth * 1000d));
                    height = (int)((imgHeight * 1000d));
                } else if (height == 0) {
                    height = (int)((imgHeight * ((double)width)) / imgWidth);
                } else if (width == 0) {
                    width = (int)((imgWidth * ((double)height)) / imgHeight);
                }
            }

            // scale image if it doesn't fit in the area/page
            // Need to be more tested...
            double ratio = ((double)width) / ((double)height);
            int areaWidth = area.getAllocationWidth() - startIndent
                            - endIndent;
            int pageHeight = area.getPage().getBody().getMaxHeight()
                             - spaceBefore;
            if (height > pageHeight) {
                height = pageHeight;
                width = (int)(ratio * ((double)height));
            }
            if (width > areaWidth) {
                width = areaWidth;
                height = (int)(((double)width) / ratio);
            }

            if (area.spaceLeft() < (height + spaceBefore)) {
                return new Status(Status.AREA_FULL_NONE);
            }

            this.imageArea =
                new ImageArea(propMgr.getFontState(area.getFontInfo()), img,
                              area.getAllocationWidth(), width, height,
                              startIndent, endIndent, align);

            if ((spaceBefore != 0) && (this.marker == 0)) {
                area.addDisplaySpace(spaceBefore);
            }

            if (marker == 0) {
                // configure id
                area.getIDReferences().configureID(id, area);
            }

            imageArea.start();
            imageArea.end();
            // area.addChild(imageArea);
            // area.increaseHeight(imageArea.getHeight());

            if (spaceAfter != 0) {
                area.addDisplaySpace(spaceAfter);
            }
            if (breakBefore == BreakBefore.PAGE
                    || ((spaceBefore + imageArea.getHeight())
                        > area.spaceLeft())) {
                return new Status(Status.FORCE_PAGE_BREAK);
            }

            if (breakBefore == BreakBefore.ODD_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_ODD);
            }

            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_EVEN);
            }


            if (area instanceof BlockArea) {
                BlockArea ba = (BlockArea)area;
                LineArea la = ba.getCurrentLineArea();
                if (la == null) {
                    return new Status(Status.AREA_FULL_NONE);
                }
                la.addPending();
                if (imageArea.getContentWidth() > la.getRemainingWidth()) {
                    la = ba.createNextLineArea();
                    if (la == null) {
                        return new Status(Status.AREA_FULL_NONE);
                    }
                }
                la.addInlineArea(imageArea);
            } else {
                area.addChild(imageArea);
                area.increaseHeight(imageArea.getContentHeight());
            }
            imageArea.setPage(area.getPage());

            if (breakAfter == BreakAfter.PAGE) {
                this.marker = BREAK_AFTER;
                return new Status(Status.FORCE_PAGE_BREAK);
            }

            if (breakAfter == BreakAfter.ODD_PAGE) {
                this.marker = BREAK_AFTER;
                return new Status(Status.FORCE_PAGE_BREAK_ODD);
            }

            if (breakAfter == BreakAfter.EVEN_PAGE) {
                this.marker = BREAK_AFTER;
                return new Status(Status.FORCE_PAGE_BREAK_EVEN);
            }





        } catch (MalformedURLException urlex) {
            // bad URL
            log.error("Error while creating area : "
                                   + urlex.getMessage());
        } catch (FopImageException imgex) {
            // image error
            log.error("Error while creating area : "
                                   + imgex.getMessage());
        }

        // if (area instanceof BlockArea) {
        // area.start();
        // }

        return new Status(Status.OK);
    }

}

