/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.layoutmgr.InlineLayoutManager;

/**
 * Class modelling the commonalities of several inline-level
 * formatting objects.
 */
public abstract class InlineLevel extends FObjMixed {
    protected CommonBorderPaddingBackground commonBorderPaddingBackground;
    protected CommonAccessibility commonAccessibility;
    protected CommonMarginInline commonMarginInline;
    protected CommonAural commonAural;
    protected CommonFont commonFont;
    protected ColorType color;
    protected Length lineHeight;
    protected int visibility;
    // End of property values

    /**
     * @param parent FONode that is the parent of this object
     */
    protected InlineLevel(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonAccessibility = pList.getAccessibilityProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonAural = pList.getAuralProps();
        commonFont = pList.getFontProps();
        color = pList.get(PR_COLOR).getColorType();
        lineHeight = pList.get(PR_LINE_HEIGHT).getLength();
        visibility = pList.get(PR_VISIBILITY).getEnum();
    }

    /**
     * Return the Common Margin Properties-Inline.
     */
    public CommonMarginInline getCommonMarginInline() {
        return commonMarginInline;
    }

    /**
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    } 

    /**
     * Return the Common Font Properties.
     */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * Return the "color" property.
     */
    public ColorType getColor() {
        return color;
    }
}

