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
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.*;

// Java
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.awt.geom.Rectangle2D;

public class ExternalGraphic extends FObj {
    String url;
    int breakAfter;
    int breakBefore;
    int align;
    int startIndent;
    int endIndent;
    int spaceBefore;
    int spaceAfter;
    int viewWidth = -1;
    int viewHeight = -1;
    boolean clip = false;
    Rectangle2D placement = null;

    public ExternalGraphic(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        InlineArea area = getInlineArea();
        if(area != null) {
            LeafNodeLayoutManager lm = new LeafNodeLayoutManager(this);
            lm.setCurrentArea(area);
            lm.setAlignment(properties.get("vertical-align").getEnum());
            lm.setLead(viewHeight);
            list.add(lm);
        }
    }

    protected InlineArea getInlineArea() {
        setup();
        if(url == null) {
            return null;
        }
        Image imArea = new Image(url);
        Viewport vp = new Viewport(imArea);
        vp.setWidth(viewWidth);
        vp.setHeight(viewHeight);
        vp.setClip(clip);
        vp.setContentPosition(placement);
        vp.setOffset(0);

        return vp;
    }

    public void setup() {
        url = this.properties.get("src").getString();
        if(url == null) {
            return;
        }
        url = ImageFactory.getURL(url);

        // assume lr-tb for now
        Length ipd = properties.get("inline-progression-dimension.optimum").getLength();
        if(!ipd.isAuto()) {
            viewWidth = ipd.mvalue();
        } else {
            ipd = properties.get("width").getLength();
            if(!ipd.isAuto()) {
                viewWidth = ipd.mvalue();
            }
        }
        Length bpd = properties.get("block-progression-dimension.optimum").getLength();
        if(!bpd.isAuto()) {
            viewHeight = bpd.mvalue();
        } else {
            bpd = properties.get("height").getLength();
            if(!bpd.isAuto()) {
                viewHeight = bpd.mvalue();
            }
        }

        // if we need to load this image to get its size
        FopImage fopimage = null;

        int cwidth = -1;
        int cheight = -1;
        Length ch = properties.get("content-height").getLength();
        if(!ch.isAuto()) {
            /*if(ch.scaleToFit()) {
                if(viewHeight != -1) {
                    cheight = viewHeight;
                }
            } else {*/
            cheight = ch.mvalue();
        }
        Length cw = properties.get("content-width").getLength();
        if(!cw.isAuto()) {
            /*if(cw.scaleToFit()) {
                if(viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {*/
            cwidth = cw.mvalue();
        }

        int scaling = properties.get("scaling").getEnum();
        if((scaling == Scaling.UNIFORM) || (cwidth == -1) || cheight == -1) {
            ImageFactory fact = ImageFactory.getInstance();
            fopimage = fact.getImage(url, userAgent);
            if(fopimage == null) {
                // error
                url = null;
                return;
            }
            // load dimensions
            if(!fopimage.load(FopImage.DIMENSIONS, userAgent)) {
                // error
                url = null;
                return;
            }
            if(cwidth == -1) {
                cwidth = (int)(fopimage.getWidth() * 1000);
            }
            if(cheight == -1) {
                cheight = (int)(fopimage.getHeight() * 1000);
            }
            if(scaling == Scaling.UNIFORM) {
                // adjust the larger
                double rat1 = cwidth / (fopimage.getWidth() * 1000f);
                double rat2 = cheight / (fopimage.getHeight() * 1000f);
                if(rat1 < rat2) {
                    // reduce cheight
                    cheight = (int)(rat1 * fopimage.getHeight() * 1000);
                } else {
                    cwidth = (int)(rat2 * fopimage.getWidth() * 1000);
                }
            }
        }

        if(viewWidth == -1) {
            viewWidth = cwidth;
        }
        if(viewHeight == -1) {
            viewHeight = cheight;
        }

        if(cwidth > viewWidth || cheight > viewHeight) {
            int overflow = properties.get("overflow").getEnum();
            if(overflow == Overflow.HIDDEN) {
                clip = true;
            } else if(overflow == Overflow.ERROR_IF_OVERFLOW) {
                getLogger().error("Image: " + url + " overflows the viewport");
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
        // this.properties.get("content-type");
        // this.properties.get("dominant-baseline");
        setupID();
        // this.properties.get("keep-with-next");
        // this.properties.get("keep-with-previous");
        // this.properties.get("line-height");
        // this.properties.get("line-height-shift-adjustment");
        // this.properties.get("scaling-method");
    }

}

