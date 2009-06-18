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

// Java
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;

public class ExternalGraphic extends FObj {
    String url;
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

    public ExternalGraphic(FONode parent) {
        super(parent);
    }

    public void addLayoutManager(List list) {
        LeafNodeLayoutManager lm = new LeafNodeLayoutManager(this);
        lm.setCurrentArea(getInlineArea());
        list.add(lm);
    }

    protected InlineArea getInlineArea() {
        setup();
        if(url == null) {
            return null;
        }
        url = ImageFactory.getURL(url);
        // if we need to load this image to get its size
        ImageFactory fact = ImageFactory.getInstance();
        FopImage fopimage = fact.getImage(url, userAgent);
        // if(fopimage == null) {
        // error
        // }
        // if(!fopimage.load(FopImage.DIMENSIONS)) {
        // error
        // }
        Image imArea = new Image(url);
        Viewport vp = new Viewport(imArea);
        return vp;
    }

    public void setup() {

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
        url = this.properties.get("src").getString();
        // this.properties.get("text-align");
        // this.properties.get("width");
    }

}

