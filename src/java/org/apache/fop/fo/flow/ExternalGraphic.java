/*
 * $Id: ExternalGraphic.java,v 1.32 2003/03/05 20:38:21 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.properties.DisplayAlign;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.image.FopImage;
import org.apache.fop.datatypes.Length;

// Java
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
            viewWidth = ipd.getValue();
        } else {
            ipd = properties.get("width").getLength();
            if (!ipd.isAuto()) {
                viewWidth = ipd.getValue();
            }
        }
        Length bpd = properties.get("block-progression-dimension.optimum").getLength();
        if (!bpd.isAuto()) {
            viewHeight = bpd.getValue();
        } else {
            bpd = properties.get("height").getLength();
            if (!bpd.isAuto()) {
                viewHeight = bpd.getValue();
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
            cheight = ch.getValue();
        }
        Length cw = properties.get("content-width").getLength();
        if (!cw.isAuto()) {
            /*if (cw.scaleToFit()) {
                if (viewWidth != -1) {
                    cwidth = viewWidth;
                }
            } else {*/
            cwidth = cw.getValue();
        }

        int scaling = properties.get("scaling").getEnum();
        if ((scaling == Scaling.UNIFORM) || (cwidth == -1) || cheight == -1) {
            ImageFactory fact = ImageFactory.getInstance();
            fopimage = fact.getImage(url, getUserAgent());
            if (fopimage == null) {
                // error
                url = null;
                return;
            }
            // load dimensions
            if (!fopimage.load(FopImage.DIMENSIONS, getUserAgent())) {
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

    /**
     * @see org.apache.fop.fo.FObj#handleAttrs
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        getFOTreeControl().getFOInputHandler().image(this);
    }
}
