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

package org.apache.fop.fo.flow;

// XML
import java.awt.geom.Rectangle2D;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.xml.sax.Attributes;

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
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        getFOTreeControl().getFOInputHandler().image(this);
    }

    /**
     * Setup this image.
     * This gets the sizes for the image and the dimensions and clipping.
     */
    public void setup() {
        url = this.propertyList.get(PR_SRC).getString();
        if (url == null) {
            return;
        }
        url = ImageFactory.getURL(url);

        // assume lr-tb for now and just use the .optimum value of the range
        Length ipd = propertyList.get(PR_INLINE_PROGRESSION_DIMENSION).
                                    getLengthRange().getOptimum().getLength();
        if (!ipd.isAuto()) {
            viewWidth = ipd.getValue();
        } else {
            ipd = propertyList.get(PR_WIDTH).getLength();
            if (!ipd.isAuto()) {
                viewWidth = ipd.getValue();
            }
        }
        Length bpd = propertyList.get(PR_BLOCK_PROGRESSION_DIMENSION | CP_OPTIMUM).getLength();
        if (!bpd.isAuto()) {
            viewHeight = bpd.getValue();
        } else {
            bpd = propertyList.get(PR_HEIGHT).getLength();
            if (!bpd.isAuto()) {
                viewHeight = bpd.getValue();
            }
        }

        // if we need to load this image to get its size
        FopImage fopimage = null;

        int cwidth = -1;
        int cheight = -1;
        Length ch = propertyList.get(PR_CONTENT_HEIGHT).getLength();
        if (!ch.isAuto()) {
            /*if (ch.scaleToFit()) {
                if (viewHeight != -1) {
                    cheight = viewHeight;
                }
            } else {*/
            cheight = ch.getValue();
        }
        Length cw = propertyList.get(PR_CONTENT_WIDTH).getLength();
        if (!cw.isAuto()) {
            /*if (cw.scaleToFit()) {
                if (viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {*/
            cwidth = cw.getValue();
        }

        int scaling = propertyList.get(PR_SCALING).getEnum();
        if ((scaling == Scaling.UNIFORM) || (cwidth == -1) || cheight == -1) {
            ImageFactory fact = ImageFactory.getInstance();
            fopimage = fact.getImage(url, getUserAgent());
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
            int overflow = propertyList.get(PR_OVERFLOW).getEnum();
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
        int da = propertyList.get(PR_DISPLAY_ALIGN).getEnum();
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

        int ta = propertyList.get(PR_TEXT_ALIGN).getEnum();
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
     * @return the ViewHeight (in millipoints??)
     */
    public int getViewHeight() {
        return viewHeight;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveExternalGraphic(this);
    }

    public String getURL() {
        return url;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public boolean getClip() {
        return clip;
    }

    public Rectangle2D getPlacement() {
        return placement;
    }

}
