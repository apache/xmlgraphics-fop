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
import org.apache.fop.area.Area;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.layoutmgr.LayoutInfo;

import org.w3c.dom.Document;

import java.awt.geom.Point2D;
import java.util.List;

public class InstreamForeignObject extends FObj {

    int breakBefore;
    int breakAfter;
    int scaling;
    int width;
    int height;
    int contwidth;
    int contheight;
    boolean wauto;
    boolean hauto;
    boolean cwauto;
    boolean chauto;
    int spaceBefore;
    int spaceAfter;
    int startIndent;
    int endIndent;

    Viewport areaCurrent;

    /**
     * constructs an instream-foreign-object object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param propertyList the explicit properties of this object
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        LeafNodeLayoutManager lm = new LeafNodeLayoutManager(this);
        lm.setCurrentArea(getInlineArea());
        list.add(lm);
    }

    /**
     * Get the inline area created by this element.
     */
    protected InlineArea getInlineArea() {
        if (children == null) {
            return areaCurrent;
        }

        if (this.children.size() != 1) {
            // error
        }
        FONode fo = (FONode)children.get(0);
        if(!(fo instanceof XMLObj)) {
            // error
        }
        XMLObj child = (XMLObj)fo;

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

        // viewport size is determined by block-progression-dimension
        // and inline-progression-dimension

        // if replaced then use height then ignore block-progression-dimension
        //int h = this.properties.get("height").getLength().mvalue();

        // use specified line-height then ignore dimension in height direction
        boolean hasLH = properties.get("line-height").getSpecifiedValue() != null;
        int bpd = 0;
        boolean bpdauto = false;
        if(hasLH) {
            bpd = properties.get("line-height").getLength().mvalue();
        } else {
            // this property does not apply when the line-height applies
            // isn't the block-progression-dimension always in the same
            // direction as the line height?
            bpdauto = properties.get("block-progression-dimension").getLength().isAuto();
            bpd = properties.get("block-progression-dimension").getLength().mvalue();
        }

        //boolean ipdauto = properties.get("inline-progression-dimension").getLength().isAuto();
        //int ipd = properties.get("inline-progression-dimension").getLength().mvalue();

        // if auto then use the intrinsic size of the content scaled
        // to the content-height and content-width

        Point2D size = child.getDimension(new Point2D.Float(-1, -1));

        Document doc = child.getDocument();
        String ns = child.getDocumentNamespace();

        children = null;
        ForeignObject foreign = new ForeignObject(doc, ns);

        areaCurrent = new Viewport(foreign);
        areaCurrent.setWidth((int)size.getX() * 1000);
        areaCurrent.setHeight((int)size.getY() * 1000);
        areaCurrent.setOffset(0);
        areaCurrent.info = new LayoutInfo();
        areaCurrent.info.alignment = properties.get("vertical-align").getEnum();
        areaCurrent.info.lead = areaCurrent.getHeight();

        return areaCurrent;
    }

    public boolean generatesInlineAreas() {
        return true;
    }

    /**
     * layout this formatting object.
     *
     * @param area the area to layout the object into
     *
     * @return the status of the layout
     *
    public Status layout(Area area) throws FOPException {

        if (this.marker == BREAK_AFTER) {
            return new Status(Status.OK);
        }

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
            // this.properties.get("text-align");
            // this.properties.get("width");

            /* retrieve properties *
            String id = this.properties.get("id").getString();
            int align = this.properties.get("text-align").getEnum();
            int valign = this.properties.get("vertical-align").getEnum();
            int overflow = this.properties.get("overflow").getEnum();

            this.breakBefore = this.properties.get("break-before").getEnum();
            this.breakAfter = this.properties.get("break-after").getEnum();
            this.width = this.properties.get("width").getLength().mvalue();
            this.height = this.properties.get("height").getLength().mvalue();
            this.contwidth =
                this.properties.get("content-width").getLength().mvalue();
            this.contheight =
                this.properties.get("content-height").getLength().mvalue();
            this.wauto = this.properties.get("width").getLength().isAuto();
            this.hauto = this.properties.get("height").getLength().isAuto();
            this.cwauto =
                this.properties.get("content-width").getLength().isAuto();
            this.chauto =
                this.properties.get("content-height").getLength().isAuto();

            this.startIndent =
                this.properties.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.properties.get("end-indent").getLength().mvalue();
            this.spaceBefore =
                this.properties.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.properties.get("space-after.optimum").getLength().mvalue();

            this.scaling = this.properties.get("scaling").getEnum();

            area.getIDReferences().createID(id);
            if (this.areaCurrent == null) {
                this.areaCurrent =
                    new ForeignObjectArea(propMgr.getFontState(area.getFontInfo()),
                                          area.getAllocationWidth());

                this.areaCurrent.start();
                areaCurrent.setWidth(this.width);
                areaCurrent.setHeight(this.height);
                areaCurrent.setContentWidth(this.contwidth);
                areaCurrent.setContentHeight(this.contheight);
                areaCurrent.setScaling(this.scaling);
                areaCurrent.setAlign(align);
                areaCurrent.setVerticalAlign(valign);
                areaCurrent.setOverflow(overflow);
                areaCurrent.setSizeAuto(wauto, hauto);
                areaCurrent.setContentSizeAuto(cwauto, chauto);

                // this means that children can get the fontstate
                areaCurrent.setPage(area.getPage());

                int numChildren = this.children.size();
                if (numChildren > 1) {
                    throw new FOPException("Only one child element is allowed in an instream-foreign-object");
                }
                /* layout foreign object *
                if (this.children.size() > 0) {
                    FONode fo = (FONode)children.get(0);
                    Status status;
                    if ((status =
                            fo.layout(this.areaCurrent)).isIncomplete()) {
                        return status;
                    }

                    /* finish off the foreign object area *
                    this.areaCurrent.end();
                }
            }

            this.marker = 0;

            if (breakBefore == BreakBefore.PAGE
                    || ((spaceBefore + areaCurrent.getEffectiveHeight())
                        > area.spaceLeft())) {
                return new Status(Status.FORCE_PAGE_BREAK);
            }

            if (breakBefore == BreakBefore.ODD_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_ODD);
            }

            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return new Status(Status.FORCE_PAGE_BREAK_EVEN);
            }
        }

        if (this.areaCurrent == null) {
            return new Status(Status.OK);
        }

        if (area instanceof BlockArea) {
            BlockArea ba = (BlockArea)area;
            LineArea la = ba.getCurrentLineArea();
            if (la == null) {
                return new Status(Status.AREA_FULL_NONE);
            }
            la.addPending();
            if (areaCurrent.getEffectiveWidth() > la.getRemainingWidth()) {
                la = ba.createNextLineArea();
                if (la == null) {
                    return new Status(Status.AREA_FULL_NONE);
                }
            }
            la.addInlineArea(areaCurrent);
        } else {
            area.addChild(areaCurrent);
            area.increaseHeight(areaCurrent.getEffectiveHeight());
        }

        if (this.isInTableCell) {
            startIndent += forcedStartOffset;
            /*
             * endIndent = areaCurrent.getEffectiveWidth() - forcedWidth -
             * forcedStartOffset;
             *
        }

        areaCurrent.setStartIndent(startIndent);
        // areaCurrent.setEndIndent(endIndent);

        /* if there is a space-before *
        if (spaceBefore != 0) {
            /* add a display space *
            // area.addDisplaySpace(spaceBefore);
        }

        /* add the SVG area to the containing area *
        // area.addChild(areaCurrent);

        areaCurrent.setPage(area.getPage());

        /* increase the height of the containing area accordingly *
        // area.increaseHeight(areaCurrent.getEffectiveHeight());

        /* if there is a space-after *
        if (spaceAfter != 0) {
            /* add a display space *
            // area.addDisplaySpace(spaceAfter);
        }


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

        areaCurrent = null;
        /* return status *
        return new Status(Status.OK);
    }
*/
}
