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

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fonts.Font;


/**
 * Class modelling the fo:page-number-citation object. See Sec. 6.6.11 of the
 * XSL-FO Standard.
 * This inline fo is replaced with the text for a page number.
 * The page number used is the page that contains the start of the
 * block referenced with the ref-id attribute.
 */
public class PageNumberCitation extends FObj {
    /** Fontstate for this object **/
    protected Font fontState;

    private float red;
    private float green;
    private float blue;
    private int wrapOption;
    private String pageNumber;
    private String refId;
    private boolean unresolved = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumberCitation(FONode parent) {
        super(parent);
    }

    /**
     * @param str string to be measured
     * @return width (in millipoints ??) of the string
     */
    public int getStringWidth(String str) {
        int width = 0;
        for (int count = 0; count < str.length(); count++) {
            width += fontState.getCharWidth(str.charAt(count));
        }
        return width;
    }

    public void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        this.fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps =
          propMgr.getRelativePositionProps();

        // this.propertyList.get("alignment-adjust");
        // this.propertyList.get("alignment-baseline");
        // this.propertyList.get("baseline-shift");
        // this.propertyList.get("dominant-baseline");
        setupID();
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");
        // this.propertyList.get("letter-spacing");
        // this.propertyList.get("line-height");
        // this.propertyList.get("line-height-shift-adjustment");
        // this.propertyList.get("ref-id");
        // this.propertyList.get("score-spaces");
        // this.propertyList.get("text-decoration");
        // this.propertyList.get("text-shadow");
        // this.propertyList.get("text-transform");
        // this.propertyList.get("word-spacing");

        ColorType c = this.propertyList.get(PR_COLOR).getColorType();
        this.red = c.getRed();
        this.green = c.getGreen();
        this.blue = c.getBlue();

        this.wrapOption = this.propertyList.get(PR_WRAP_OPTION).getEnum();
        this.refId = this.propertyList.get(PR_REF_ID).getString();

        if (this.refId.equals("")) {
            //throw new FOPException("page-number-citation must contain \"ref-id\"");
        }

    }

    public String getRefId() {
        return refId;
    }

    public boolean getUnresolved() {
        return unresolved;
    }

    public void setUnresolved(boolean isUnresolved) {
        unresolved = isUnresolved;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.servePageNumberCitation(this);
    }

    public Font getFontState() {
        return fontState;
    }

    public String getName() {
        return "fo:page-number-citation";
    }
}
