/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.properties.DisplayAlign;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.FopImage;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layoutmgr.AbstractLayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.Length;

// Java
import java.util.List;
import java.awt.geom.Rectangle2D;

/**
 * External graphic formatting object.
 * This FO node handles the external graphic. It creates an image
 * inline area that can be added to the area tree.
 */
public class ExternalGraphic extends FObj {
    private String url;
    private int breakAfter;
    private int breakBefore;
    private int align;
    private int startIndent;
    private int endIndent;
    private int spaceBefore;
    private int spaceAfter;
    private int viewWidth = -1;
    private int viewHeight = -1;
    private boolean clip = false;
    private Rectangle2D placement = null;

    /**
     * Create a new External graphic node.
     *
     * @param parent the parent of this node
     */
    public ExternalGraphic(FONode parent) {
        super(parent);
    }

    /**
     * Add the layout manager for this to the list.
     * This adds a leafnode layout manager that deals with the
     * created viewport/image area.
     *
     * @param list the list to add the layout manager to
     */
    public void addLayoutManager(List list) {
        InlineArea area = getInlineArea();
        if (area != null) {
            setupID();
            LeafNodeLayoutManager lm = new LeafNodeLayoutManager(this);
            lm.setCurrentArea(area);
            lm.setAlignment(properties.get("vertical-align").getEnum());
            lm.setLead(viewHeight);
            list.add(lm);
        }
    }

    /**
     * Get the inline area for this external grpahic.
     * This creates the image area and puts it inside a viewport.
     *
     * @return the viewport containing the image area
     */
    protected InlineArea getInlineArea() {
        setup();
        if (url == null) {
            return null;
        }
        Image imArea = new Image(url);
        Viewport vp = new Viewport(imArea);
        vp.setWidth(viewWidth);
        vp.setHeight(viewHeight);
        vp.setClip(clip);
        vp.setContentPosition(placement);
        vp.setOffset(0);

        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();
        AbstractLayoutManager.addBorders(vp, bap);
        AbstractLayoutManager.addBackground(vp, bProps);

        return vp;
    }

    /**
     * Setup this image.
     * This gets the sizes for the image and the dimensions and clipping.
     */
    public void setup() {
        url = this.properties.get("src").getString();
        if (url == null) {
            return;
        }
        url = ImageFactory.getURL(url);

        // assume lr-tb for now
        Length ipd = properties.get("inline-progression-dimension.optimum").getLength();
        if (!ipd.isAuto()) {
            viewWidth = ipd.mvalue();
        } else {
            ipd = properties.get("width").getLength();
            if (!ipd.isAuto()) {
                viewWidth = ipd.mvalue();
            }
        }
        Length bpd = properties.get("block-progression-dimension.optimum").getLength();
        if (!bpd.isAuto()) {
            viewHeight = bpd.mvalue();
        } else {
            bpd = properties.get("height").getLength();
            if (!bpd.isAuto()) {
                viewHeight = bpd.mvalue();
            }
        }

        // if we need to load this image to get its size
        FopImage fopimage = null;

        int cwidth = -1;
        int cheight = -1;
        Length ch = properties.get("content-height").getLength();
        if (!ch.isAuto()) {
            /*if (ch.scaleToFit()) {
                if (viewHeight != -1) {
                    cheight = viewHeight;
                }
            } else {*/
            cheight = ch.mvalue();
        }
        Length cw = properties.get("content-width").getLength();
        if (!cw.isAuto()) {
            /*if (cw.scaleToFit()) {
                if (viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {*/
            cwidth = cw.mvalue();
        }

        int scaling = properties.get("scaling").getEnum();
        if ((scaling == Scaling.UNIFORM) || (cwidth == -1) || cheight == -1) {
            ImageFactory fact = ImageFactory.getInstance();
            fopimage = fact.getImage(url, userAgent);
            if (fopimage == null) {
                // error
                url = null;
                return;
            }
            // load dimensions
            if (!fopimage.load(FopImage.DIMENSIONS, userAgent)) {
                // error
                url = null;
                return;
            }
            if (cwidth == -1) {
                cwidth = (int)(fopimage.getWidth() * 1000);
            }
            if (cheight == -1) {
                cheight = (int)(fopimage.getHeight() * 1000);
            }
            if (scaling == Scaling.UNIFORM) {
                // adjust the larger
                double rat1 = cwidth / (fopimage.getWidth() * 1000f);
                double rat2 = cheight / (fopimage.getHeight() * 1000f);
                if (rat1 < rat2) {
                    // reduce cheight
                    cheight = (int)(rat1 * fopimage.getHeight() * 1000);
                } else {
                    cwidth = (int)(rat2 * fopimage.getWidth() * 1000);
                }
            }
        }

        if (viewWidth == -1) {
            viewWidth = cwidth;
        }
        if (viewHeight == -1) {
            viewHeight = cheight;
        }

        if (cwidth > viewWidth || cheight > viewHeight) {
            int overflow = properties.get("overflow").getEnum();
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                getLogger().error("Image: " + url
                                  + " overflows the viewport, clipping to viewport");
                clip = true;
            }
        }

        int xoffset = 0;
        int yoffset = 0;
        int da = properties.get("display-align").getEnum();
        switch(da) {
            case DisplayAlign.BEFORE:
            break;
            case DisplayAlign.AFTER:
                yoffset = viewHeight - cheight;
            break;
            case DisplayAlign.CENTER:
                yoffset = (viewHeight - cheight) / 2;
            break;
            case DisplayAlign.AUTO:
            default:
            break;
        }

        int ta = properties.get("text-align").getEnum();
        switch(ta) {
            case TextAlign.CENTER:
                xoffset = (viewWidth - cwidth) / 2;
            break;
            case TextAlign.END:
                xoffset = viewWidth - cwidth;
            break;
            case TextAlign.START:
            break;
            case TextAlign.JUSTIFY:
            default:
            break;
        }
        placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);

        // Common Accessibility Properties
        AccessibilityProps mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        AuralProps mAurProps = propMgr.getAuralProps();

        // Common Margin Properties-Inline
        MarginInlineProps mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        RelativePositionProps mRelProps = propMgr.getRelativePositionProps();

        // this.properties.get("alignment-adjust");
        // this.properties.get("alignment-baseline");
        // this.properties.get("baseline-shift");
        // this.properties.get("content-type");
        // this.properties.get("dominant-baseline");
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("scaling-method");
    }

}

