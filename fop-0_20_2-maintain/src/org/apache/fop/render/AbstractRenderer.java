/*
 * $Id$
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
package org.apache.fop.render;

// FOP
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageException;
import org.apache.fop.fo.properties.BackgroundRepeat;
import org.apache.fop.fo.properties.Position;
import org.apache.fop.layout.SpanArea;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.LineArea;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.Box;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.inline.InlineArea;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.datatypes.ColorType;

// Avalon
import org.apache.avalon.framework.logger.Logger;

// Java
import java.util.List;

/**
 * Abstract base class for all renderers.
 *
 */
public abstract class AbstractRenderer implements Renderer {
    protected Logger log;

    /**
     * the current vertical position in millipoints from bottom
     */
    protected int currentYPosition = 0;

    /**
     * the current horizontal position in millipoints from left
     */
    protected int currentXPosition = 0;

    /**
     * the horizontal position of the current area container
     */
    protected int currentAreaContainerXPosition = 0;

    protected IDReferences idReferences;

    public void setLogger(Logger logger) {
        log = logger;
    }

    public void renderSpanArea(SpanArea area) {
        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            b.render(this);    // column areas
        }

    }

    protected abstract void doFrame(Area area);

    /**
     * Renders an area's background.
     * @param x the x position of the left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     */
    protected void doBackground(Area area, int x, int y, int w, int h) {
        if (h == 0 || w == 0)
            return;

        BackgroundProps props = area.getBackground();
        if (props == null)
            return;

        if (props.backColor.alpha() == 0) {
            this.addFilledRect(x, y, w, -h, props.backColor);
        }

        // XXX: I'm ignoring area rotation here 8(
        //      is this taken care of for me elsewhere in the codebase?
        if (props.backImage != null) {
            int imgW;
            int imgH;
            try {
            // XXX: do correct unit conversion here
            imgW = props.backImage.getWidth() * 1000;
            imgH = props.backImage.getHeight() * 1000;
            }
            catch (FopImageException fie) {
            log.error("Error obtaining bg image width and height", fie);
            return;
            }

            int dx = x;
            int dy = y;
            int endX = x + w;
            int endY = y - h;
            int clipW = w % imgW;
            int clipH = h % imgH;

            boolean repeatX = true;
            boolean repeatY = true;
            switch (props.backRepeat) {
            case BackgroundRepeat.REPEAT:
            break;

            case BackgroundRepeat.REPEAT_X:
            repeatY = false;
            break;

            case BackgroundRepeat.REPEAT_Y:
            repeatX = false;
            break;

            case BackgroundRepeat.NO_REPEAT:
            repeatX = false;
            repeatY = false;
            break;

            case BackgroundRepeat.INHERIT:
            // XXX: what to do here?
            break;

            default:
            log.error("Ignoring invalid background-repeat property");
            }

            FontState fs = area.getFontState();

            while (dy > endY) { // looping through rows
            while (dx < endX) { // looping through cols
                if (dx + imgW <= endX) {
                // no x clipping
                if (dy - imgH >= endY) {
                    // no x clipping, no y clipping
                    drawImageScaled(dx, dy, imgW, imgH,
                            props.backImage, fs);
                }
                else {
                    // no x clipping, y clipping
                    drawImageClipped(dx, dy,
                             0, 0, imgW, clipH,
                             props.backImage, fs);
                }
                }
                else {
                // x clipping
                if (dy - imgH >= endY) {
                    // x clipping, no y clipping
                    drawImageClipped(dx, dy,
                             0, 0, clipW, imgH,
                             props.backImage, fs);
                }

                else {
                    // x clipping, y clipping
                    drawImageClipped(dx, dy,
                             0, 0, clipW, clipH,
                             props.backImage, fs);
                }
                }

                if (repeatX) {
                dx += imgW;
                }
                else {
                break;
                }
            } // end looping through cols

            dx = x;

            if (repeatY) {
                dy -= imgH;
            }
            else {
                break;
            }
            } // end looping through rows
        }
    }

    /**
     * Add a filled rectangle to the current stream
     * This default implementation calls addRect
     * using the same color for fill and border.
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param fill the fill color/gradient
     */
    protected abstract void addFilledRect(int x, int y, int w, int h,
                                 ColorType col);

    /**
     * Renders an image, rendered at the image's intrinsic size.
     * This by default calls drawImageScaled() with the image's
     * intrinsic width and height, but implementations may
     * override this method if it can provide a more efficient solution.
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param image the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected void drawImage(int x, int y, FopImage image, FontState fs) {
        int w;
        int h;
        try {
            // XXX: convert these units correctly
            w = image.getWidth() * 1000;
            h = image.getHeight() * 1000;
        }
        catch (FopImageException e) {
            log.error("Failed to obtain the image width and height", e);
            return;
        }
        drawImageScaled(x, y, w, h, image, fs);
    }

    /**
     * Renders an image, scaling it to the given width and height.
     * If the scaled width and height is the same intrinsic size
     * of the image, the image is not scaled.
     *
     * @param x the x position of left edge in millipoints
     * @param y the y position of top edge in millipoints
     * @param w the width in millipoints
     * @param h the height in millipoints
     * @param image the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected abstract void drawImageScaled(int x, int y, int w, int h,
                        FopImage image,
                        FontState fs);

    /**
     * Renders an image, clipping it as specified.
     *
     * @param x the x position of left edge in millipoints.
     * @param y the y position of top edge in millipoints.
     * @param clipX the left edge of the clip in millipoints
     * @param clipY the top edge of the clip in millipoints
     * @param clipW the clip width in millipoints
     * @param clipH the clip height in millipoints
     * @param fill the image to be rendered
     * @param fs the font state to use when rendering text
     *           in non-bitmapped images.
     */
    protected abstract void drawImageClipped(int x, int y,
                         int clipX, int clipY,
                         int clipW, int clipH,
                         FopImage image,
                         FontState fs);

    /**
     * Render an image area.
     *
     * @param area the image area to render
     */
    public void renderImageArea(ImageArea area) {
        // adapted from contribution by BoBoGi
        int x = this.currentXPosition + area.getXOffset();
        int y = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= h;

        FopImage img = area.getImage();

        if (img == null) {
            log.error("Error while loading image: area.getImage() is null");
        } else {
            drawImageScaled(x, y, w, h, img, area.getFontState());
        }

        this.currentXPosition += w;
    }

    public void renderBodyAreaContainer(BodyAreaContainer area) {
        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust for negative postscript one
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        int rx = this.currentAreaContainerXPosition;
        int ry = this.currentYPosition;
        // XXX: (mjg@recaldesign.com) I had to use getAllocationWidth()
        // and getMaxHeight() as the content width and height are
        // always 0. Is this supposed to be the case?
        // IMHO, the bg should cover the entire area anyway, not
        // just the parts with content, which makes this correct.
        // Probably want to check this for the other region
        // areas as well.
        int w = area.getAllocationWidth();
            int h = area.getMaxHeight();

        doBackground(area, rx, ry, w, h);

        // floats & footnotes stuff
        renderAreaContainer(area.getBeforeFloatReferenceArea());
        renderAreaContainer(area.getFootnoteReferenceArea());

        // main reference area
        List children = area.getMainReferenceArea().getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            b.render(this);    // span areas
        }

        if (area.getPosition() != Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else {
            this.currentYPosition -= area.getHeight();
        }
    }

    /**
     * render region area container
     *
     * @param area the region area container to render
     */
    public void renderRegionAreaContainer(AreaContainer area) {
        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // Y position is computed assuming positive Y axis, adjust for negative postscript one
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        int rx = this.currentAreaContainerXPosition;
        int ry = this.currentYPosition;
        int w = area.getAllocationWidth();
        int h = area.getMaxHeight();

        doBackground(area, rx, ry, w, h);

        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            b.render(this);    // span areas
        }

        if (area.getPosition() != Position.STATIC) {
            this.currentYPosition = saveY;
            this.currentAreaContainerXPosition = saveX;
        } else {
            this.currentYPosition -= area.getHeight();
        }
    }

    /**
     * render area container
     *
     * @param area the area container to render
     */
    public void renderAreaContainer(AreaContainer area) {

        int saveY = this.currentYPosition;
        int saveX = this.currentAreaContainerXPosition;

        if (area.getPosition() == Position.ABSOLUTE) {
            // XPosition and YPosition give the content rectangle position
            this.currentYPosition = area.getYPosition();
            this.currentAreaContainerXPosition = area.getXPosition();
        } else if (area.getPosition() == Position.RELATIVE) {
            this.currentYPosition -= area.getYPosition();
            this.currentAreaContainerXPosition += area.getXPosition();
        } else if (area.getPosition() == Position.STATIC) {
            this.currentYPosition -= area.getPaddingTop()
                                     + area.getBorderTopWidth();
            /*
             * this.currentAreaContainerXPosition +=
             * area.getPaddingLeft() + area.getBorderLeftWidth();
             */
        }

        this.currentXPosition = this.currentAreaContainerXPosition;
        doFrame(area);

        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            b.render(this);
        }
        // Restore previous origin
        this.currentYPosition = saveY;
        this.currentAreaContainerXPosition = saveX;
        if (area.getPosition() == Position.STATIC) {
            this.currentYPosition -= area.getHeight();
        }

        /**
         * **
         * if (area.getPosition() != Position.STATIC) {
         * this.currentYPosition = saveY;
         * this.currentAreaContainerXPosition = saveX;
         * } else
         * this.currentYPosition -= area.getHeight();
         * **
         */
    }

    /**
     * render block area
     *
     * @param area the block area to render
     */
    public void renderBlockArea(BlockArea area) {
        // KLease: Temporary test to fix block positioning
        // Offset ypos by padding and border widths
        this.currentYPosition -= (area.getPaddingTop()
                                  + area.getBorderTopWidth());
        doFrame(area);
        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            b.render(this);
        }
        this.currentYPosition -= (area.getPaddingBottom()
                                  + area.getBorderBottomWidth());
    }

    /**
     * render line area
     *
     * @param area area to render
     */
    public void renderLineArea(LineArea area) {
        int rx = this.currentAreaContainerXPosition + area.getStartIndent();
        int ry = this.currentYPosition;
        int w = area.getContentWidth();
        int h = area.getHeight();

        this.currentYPosition -= area.getPlacementOffset();
        this.currentXPosition = rx;

        int bl = this.currentYPosition;

        List children = area.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Box b = (Box)children.get(i);
            if (b instanceof InlineArea) {
                InlineArea ia = (InlineArea)b;
                this.currentYPosition = ry - ia.getYOffset();
            } else {
                this.currentYPosition = ry - area.getPlacementOffset();
            }
            b.render(this);
        }

        this.currentYPosition = ry - h;
        this.currentXPosition = rx;
    }

    /**
     * render region areas
     *
     * @param page the page whose regions to render
     */
    public void renderRegions(Page page) {
        page.getBody().render(this);
        if (page.getBefore() != null)
            page.getBefore().render(this);
        if (page.getAfter() != null)
            page.getAfter().render(this);
        if (page.getStart() != null)
            page.getStart().render(this);
        if (page.getEnd() != null)
            page.getEnd().render(this);
    }

    public IDReferences getIDReferences() {
        return idReferences;
    }
}
