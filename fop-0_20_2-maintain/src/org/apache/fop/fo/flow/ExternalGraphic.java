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
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.Status;
import org.apache.fop.fo.properties.BreakBefore;
import org.apache.fop.fo.properties.BreakAfter;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.AccessibilityProps;
import org.apache.fop.layout.AuralProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.MarginInlineProps;
import org.apache.fop.layout.RelativePositionProps;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.LineArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.image.ImageArea;
import org.apache.fop.image.FopImageException;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.FopImageFactory;

// Java
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

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new ExternalGraphic(parent, propertyList,
                                       systemId, line, column);
        }
    }

    public static FObj.Maker maker() {
        return new ExternalGraphic.Maker();
    }

    public ExternalGraphic(FObj parent, PropertyList propertyList,
                           String systemId, int line, int column) {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:external-graphic";
    }

    public int layout(Area area) throws FOPException {

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

            try {
                area.getIDReferences().createID(id);
            }
            catch(FOPException e) {
                if (!e.isLocationSet()) {
                    e.setLocation(systemId, line, column);
                }
                throw e;
            }
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
                return Status.AREA_FULL_NONE;
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
                return Status.FORCE_PAGE_BREAK;
            }

            if (breakBefore == BreakBefore.ODD_PAGE) {
                return Status.FORCE_PAGE_BREAK_ODD;
            }

            if (breakBefore == BreakBefore.EVEN_PAGE) {
                return Status.FORCE_PAGE_BREAK_EVEN;
            }


            if (area instanceof BlockArea) {
                BlockArea ba = (BlockArea)area;
                LineArea la = ba.getCurrentLineArea();
                if (la == null) {
                    return Status.AREA_FULL_NONE;
                }
                la.addPending();
                if (imageArea.getContentWidth() > la.getRemainingWidth()) {
                    la = ba.createNextLineArea();
                    if (la == null) {
                        return Status.AREA_FULL_NONE;
                    }
                }
                la.addInlineArea(imageArea, this.getLinkSet());
            } else {
                area.addChild(imageArea);
                area.increaseHeight(imageArea.getContentHeight());
            }
            imageArea.setPage(area.getPage());

            if (breakAfter == BreakAfter.PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK;
            }

            if (breakAfter == BreakAfter.ODD_PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK_ODD;
            }

            if (breakAfter == BreakAfter.EVEN_PAGE) {
                this.marker = BREAK_AFTER;
                return Status.FORCE_PAGE_BREAK_EVEN;
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

        return Status.OK;
    }

}

