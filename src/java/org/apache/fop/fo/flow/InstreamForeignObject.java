/*
 * $Id: InstreamForeignObject.java,v 1.37 2003/03/05 20:38:21 jeremias Exp $
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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.properties.DisplayAlign;
import org.apache.fop.fo.properties.Overflow;
import org.apache.fop.fo.properties.Scaling;
import org.apache.fop.fo.properties.TextAlign;
import org.apache.fop.layoutmgr.LeafNodeLayoutManager;
import org.w3c.dom.Document;

/**
 * The instream-foreign-object flow formatting object.
 * This is an atomic inline object that contains
 * xml data.
 */
public class InstreamForeignObject extends FObj {

    private Viewport areaCurrent;

    /**
     * constructs an instream-foreign-object object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    /**
     * Add the layout manager for this into the list.
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        areaCurrent = getInlineArea();
        if (areaCurrent != null) {
            LeafNodeLayoutManager lm = new LeafNodeLayoutManager();
            lm.setUserAgent(getUserAgent());
            lm.setFObj(this);
            lm.setCurrentArea(areaCurrent);
            lm.setAlignment(properties.get("vertical-align").getEnum());
            lm.setLead(areaCurrent.getHeight());
            list.add(lm);
        }
    }

    /**
     * Get the inline area created by this element.
     *
     * @return the viewport inline area
     */
    protected Viewport getInlineArea() {
        if (children == null) {
            return areaCurrent;
        }

        if (this.children.size() != 1) {
            // error
            return null;
        }
        FONode fo = (FONode)children.get(0);
        if (!(fo instanceof XMLObj)) {
            // error
            return null;
        }
        XMLObj child = (XMLObj)fo;

        // viewport size is determined by block-progression-dimension
        // and inline-progression-dimension

        // if replaced then use height then ignore block-progression-dimension
        //int h = this.properties.get("height").getLength().mvalue();

        // use specified line-height then ignore dimension in height direction
        boolean hasLH = false;//properties.get("line-height").getSpecifiedValue() != null;

        Length len;

        int bpd = -1;
        int ipd = -1;
        boolean bpdauto = false;
        if (hasLH) {
            bpd = properties.get("line-height").getLength().getValue();
        } else {
            // this property does not apply when the line-height applies
            // isn't the block-progression-dimension always in the same
            // direction as the line height?
            len = properties.get("block-progression-dimension.optimum").getLength();
            if (!len.isAuto()) {
                bpd = len.getValue();
            } else {
                len = properties.get("height").getLength();
                if (!len.isAuto()) {
                    bpd = len.getValue();
                }
            }
        }

        len = properties.get("inline-progression-dimension.optimum").getLength();
        if (!len.isAuto()) {
            ipd = len.getValue();
        } else {
            len = properties.get("width").getLength();
            if (!len.isAuto()) {
                ipd = len.getValue();
            }
        }

        // if auto then use the intrinsic size of the content scaled
        // to the content-height and content-width
        int cwidth = -1;
        int cheight = -1;
        len = properties.get("content-width").getLength();
        if (!len.isAuto()) {
            /*if(len.scaleToFit()) {
                if(ipd != -1) {
                    cwidth = ipd;
                }
            } else {*/
            cwidth = len.getValue();
        }
        len = properties.get("content-height").getLength();
        if (!len.isAuto()) {
            /*if(len.scaleToFit()) {
                if(bpd != -1) {
                    cwidth = bpd;
                }
            } else {*/
            cheight = len.getValue();
        }

        Point2D csize = new Point2D.Float(cwidth == -1 ? -1 : cwidth / 1000f,
                                          cheight == -1 ? -1 : cheight / 1000f);
        Point2D size = child.getDimension(csize);
        if (size == null) {
            // error
            return null;
        }
        if (cwidth == -1) {
            cwidth = (int)size.getX() * 1000;
        }
        if (cheight == -1) {
            cheight = (int)size.getY() * 1000;
        }
        int scaling = properties.get("scaling").getEnum();
        if (scaling == Scaling.UNIFORM) {
            // adjust the larger
            double rat1 = cwidth / (size.getX() * 1000f);
            double rat2 = cheight / (size.getY() * 1000f);
            if (rat1 < rat2) {
                // reduce cheight
                cheight = (int)(rat1 * size.getY() * 1000);
            } else {
                cwidth = (int)(rat2 * size.getX() * 1000);
            }
        }

        if (ipd == -1) {
            ipd = cwidth;
        }
        if (bpd == -1) {
            bpd = cheight;
        }

        boolean clip = false;
        if (cwidth > ipd || cheight > bpd) {
            int overflow = properties.get("overflow").getEnum();
            if (overflow == Overflow.HIDDEN) {
                clip = true;
            } else if (overflow == Overflow.ERROR_IF_OVERFLOW) {
                getLogger().error("Instream foreign object overflows the viewport: clipping");
                clip = true;
            }
        }

        int xoffset = computeXOffset(ipd, cwidth);
        int yoffset = computeYOffset(bpd, cheight);

        Rectangle2D placement = new Rectangle2D.Float(xoffset, yoffset, cwidth, cheight);

        Document doc = child.getDOMDocument();
        String ns = child.getDocumentNamespace();

        children = null;
        ForeignObject foreign = new ForeignObject(doc, ns);

        areaCurrent = new Viewport(foreign);
        areaCurrent.setWidth(ipd);
        areaCurrent.setHeight(bpd);
        areaCurrent.setContentPosition(placement);
        areaCurrent.setClip(clip);
        areaCurrent.setOffset(0);

        return areaCurrent;
    }

    private int computeXOffset (int ipd, int cwidth) {
        int xoffset = 0;
        int ta = properties.get("text-align").getEnum();
        switch (ta) {
            case TextAlign.CENTER:
                xoffset = (ipd - cwidth) / 2;
                break;
            case TextAlign.END:
                xoffset = ipd - cwidth;
                break;
            case TextAlign.START:
                break;
            case TextAlign.JUSTIFY:
            default:
                break;
        }
        return xoffset;
    }

    private int computeYOffset(int bpd, int cheight) {
        int yoffset = 0;
        int da = properties.get("display-align").getEnum();
        switch (da) {
            case DisplayAlign.BEFORE:
                break;
            case DisplayAlign.AFTER:
                yoffset = bpd - cheight;
                break;
            case DisplayAlign.CENTER:
                yoffset = (bpd - cheight) / 2;
                break;
            case DisplayAlign.AUTO:
            default:
                break;
        }
        return yoffset;
    }

    /**
     * This flow object generates inline areas.
     * @see org.apache.fop.fo.FObj#generatesInlineAreas()
     * @return true
     */
    public boolean generatesInlineAreas() {
        return true;
    }

    /*

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
            setupID();
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

*/
}
