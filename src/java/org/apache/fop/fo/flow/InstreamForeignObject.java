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

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.DisplayAlign;
import org.apache.fop.fo.properties.TextAlign;

/**
 * The instream-foreign-object flow formatting object.
 * This is an atomic inline object that contains
 * xml data.
 */
public class InstreamForeignObject extends FObj {

    /**
     * constructs an instream-foreign-object object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    public int computeXOffset (int ipd, int cwidth) {
        int xoffset = 0;
        int ta = propertyList.get(PR_TEXT_ALIGN).getEnum();
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

    public int computeYOffset(int bpd, int cheight) {
        int yoffset = 0;
        int da = propertyList.get(PR_DISPLAY_ALIGN).getEnum();
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

            // this.propertyList.get("alignment-adjust");
            // this.propertyList.get("alignment-baseline");
            // this.propertyList.get("baseline-shift");
            // this.propertyList.get("block-progression-dimension");
            // this.propertyList.get("content-height");
            // this.propertyList.get("content-type");
            // this.propertyList.get("content-width");
            // this.propertyList.get("display-align");
            // this.propertyList.get("dominant-baseline");
            // this.propertyList.get("height");
            setupID();
            // this.propertyList.get("inline-progression-dimension");
            // this.propertyList.get("keep-with-next");
            // this.propertyList.get("keep-with-previous");
            // this.propertyList.get("line-height");
            // this.propertyList.get("line-height-shift-adjustment");
            // this.propertyList.get("overflow");
            // this.propertyList.get("scaling");
            // this.propertyList.get("scaling-method");
            // this.propertyList.get("text-align");
            // this.propertyList.get("width");

            /* retrieve properties *
            int align = this.propertyList.get("text-align").getEnum();
            int valign = this.propertyList.get("vertical-align").getEnum();
            int overflow = this.propertyList.get("overflow").getEnum();

            this.breakBefore = this.propertyList.get("break-before").getEnum();
            this.breakAfter = this.propertyList.get("break-after").getEnum();
            this.width = this.propertyList.get("width").getLength().mvalue();
            this.height = this.propertyList.get("height").getLength().mvalue();
            this.contwidth =
                this.propertyList.get("content-width").getLength().mvalue();
            this.contheight =
                this.propertyList.get("content-height").getLength().mvalue();
            this.wauto = this.propertyList.get("width").getLength().isAuto();
            this.hauto = this.propertyList.get("height").getLength().isAuto();
            this.cwauto =
                this.propertyList.get("content-width").getLength().isAuto();
            this.chauto =
                this.propertyList.get("content-height").getLength().isAuto();

            this.startIndent =
                this.propertyList.get("start-indent").getLength().mvalue();
            this.endIndent =
                this.propertyList.get("end-indent").getLength().mvalue();
            this.spaceBefore =
                this.propertyList.get("space-before.optimum").getLength().mvalue();
            this.spaceAfter =
                this.propertyList.get("space-after.optimum").getLength().mvalue();

            this.scaling = this.propertyList.get("scaling").getEnum();

*/

/**
 * This is a hook for an FOTreeVisitor subclass to be able to access
 * this object.
 * @param fotv the FOTreeVisitor subclass that can access this object.
 * @see org.apache.fop.fo.FOTreeVisitor
 */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveInstreamForeignObject(this);
    }

}
