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

package org.apache.fop.fo.pagination;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.LengthProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fonts.Font;
import org.apache.fop.fo.properties.CommonMarginInline;

/**
 * Class modelling the fo:title object. See Sec. 6.4.20 in the XSL-FO Standard.
 */
public class Title extends FObjMixed {

    /**
     * @param parent FONode that is the parent of this object
     */
    public Title(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: (#PCDATA|%inline;)*
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) {
        if (!isInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Font Properties
        Font fontState = propMgr.getFontState(getFOInputHandler().getFontInfo());

        // Common Margin Properties-Inline
        CommonMarginInline mProps = propMgr.getMarginInlineProps();

        Property prop;
        prop = this.propertyList.get(PR_BASELINE_SHIFT);
        if (prop instanceof LengthProperty) {
            Length bShift = prop.getLength();
        } else if (prop instanceof EnumProperty) {
            int bShift = prop.getEnum();
        }
        ColorType col = this.propertyList.get(PR_COLOR).getColorType();
        Length lHeight = this.propertyList.get(PR_LINE_HEIGHT).getLength();
        int lShiftAdj = this.propertyList.get(
                          PR_LINE_HEIGHT_SHIFT_ADJUSTMENT).getEnum();
        int vis = this.propertyList.get(PR_VISIBILITY).getEnum();
        Length zIndex = this.propertyList.get(PR_Z_INDEX).getLength();

    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveTitle(this);
    }

    public String getName() {
        return "fo:title";
    }
}

