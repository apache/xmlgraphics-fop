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

import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.fo.FObj;

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
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveInstreamForeignObject(this);
    }

    public String getName() {
        return "fo:instream-foreign-object";
    }
}
