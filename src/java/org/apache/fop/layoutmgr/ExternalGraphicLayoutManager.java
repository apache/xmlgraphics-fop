/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.layoutmgr;

// Java
import java.awt.geom.Rectangle2D;

// FOP
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;

/**
 * LayoutManager for the fo:external-graphic formatting object
 */
public class ExternalGraphicLayoutManager extends LeafNodeLayoutManager {

    ExternalGraphic graphic = null;

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
     * Constructor
     *
     * @param node the fo:external-graphic formatting object that creates the area
     */
    public ExternalGraphicLayoutManager(ExternalGraphic node) {
        super(node);

        graphic = node;
        setup();
        InlineArea area = getExternalGraphicInlineArea();
        setCurrentArea(area);
        setAlignment(graphic.getProperty(PR_VERTICAL_ALIGN).getEnum());
        setLead(viewHeight);
    }

    /**
     * Setup this image.
     * This gets the sizes for the image and the dimensions and clipping.
     * @todo see if can simplify property handling logic
     */
    private void setup() {
        url = ImageFactory.getURL(graphic.getURL());

        // assume lr-tb for now and just use the .optimum value of the range
        Length ipd = graphic.getPropertyList().get(PR_INLINE_PROGRESSION_DIMENSION).
                                    getLengthRange().getOptimum().getLength();
        if (!ipd.isAuto()) {
            viewWidth = ipd.getValue();
        } else {
            ipd = graphic.getPropertyList().get(PR_WIDTH).getLength();
            if (!ipd.isAuto()) {
                viewWidth = ipd.getValue();
            }
        }
        Length bpd = graphic.getPropertyList().get(PR_BLOCK_PROGRESSION_DIMENSION | CP_OPTIMUM).getLength();
        if (!bpd.isAuto()) {
            viewHeight = bpd.getValue();
        } else {
            bpd = graphic.getPropertyList().get(PR_HEIGHT).getLength();
            if (!bpd.isAuto()) {
                viewHeight = bpd.getValue();
            }
        }

        // if we need to load this image to get its size
        FopImage fopimage = null;

        int cwidth = -1;
        int cheight = -1;
        Length ch = graphic.getPropertyList().get(PR_CONTENT_HEIGHT).getLength();
        if (!ch.isAuto()) {
            /*if (ch.scaleToFit()) {
                if (viewHeight != -1) {
                    cheight = viewHeight;
                }
            } else {*/
            cheight = ch.getValue();
        }
        Length cw = graphic.getPropertyList().get(PR_CONTENT_WIDTH).getLength();
        if (!cw.isAuto()) {
            /*if (cw.scaleToFit()) {
                if (viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {*/
            cwidth = cw.getValue();
        }

        int scaling = graphic.getPropertyList().get(PR_SCALING).getEnum();
        if ((scaling == Scaling.UNIFORM) || (cwidth == -1) || cheight == -1) {
            ImageFactory fact = ImageFactory.getInstance();
            fopimage = fact.getImage(url, graphic.getUserAgent());
            if (fopimage == null) {
                // error
                url = null;
                return;
            }
            // load dimensions
            if (!fopimage.load(FopImage.DIMENSIONS)) {
                // error
                url = null;
                return;
            }
            if (cwidth == -1 && cheight == -1) {
                cwidth = (int)(fopimage.getWidth() * 1000);
                cheight = (int)(fopimage.getHeight() * 1000);
            } else if (cwidth == -1) {
                cwidth = (int)(fopimage.getWidth() * cheight) / fopimage.getHeight();
            } else if (cheight == -1) {
                cheight = (int)(fopimage.getHeight() * cwidth) / fopimage.getWidth();
            } else {
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
            int overflow = graphic.getPropertyList().get(PR_OVERFLOW).getEnum();
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                graphic.getLogger().error("Image: " + url
                                  + " overflows the viewport, clipping to viewport");
                clip = true;
            }
        }

        int xoffset = 0;
        int yoffset = 0;
        int da = graphic.getPropertyList().get(PR_DISPLAY_ALIGN).getEnum();
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

        int ta = graphic.getPropertyList().get(PR_TEXT_ALIGN).getEnum();
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
    }

     /**
      * Get the inline area for this external grpahic.
      * This creates the image area and puts it inside a viewport.
      *
      * @return the viewport containing the image area
      */
     public InlineArea getExternalGraphicInlineArea() {
         Image imArea = new Image(graphic.getURL());
         Viewport vp = new Viewport(imArea);
         vp.setWidth(viewWidth);
         vp.setHeight(viewHeight);
         vp.setClip(clip);
         vp.setContentPosition(placement);
         vp.setOffset(0);

         // Common Border, Padding, and Background Properties
         CommonBorderAndPadding bap = graphic.getPropertyManager().getBorderAndPadding();
         CommonBackground bProps = graphic.getPropertyManager().getBackgroundProps();
         TraitSetter.addBorders(vp, bap);
         TraitSetter.addBackground(vp, bProps);

         return vp;
     }
}

