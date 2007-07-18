/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.Color;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the fo:page-number object.
 */
public class PageNumber extends FObj {
    // The value of properties relevant for fo:page-number.
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private int dominantBaseline;
    // private ToBeImplementedProperty letterSpacing;
    private SpaceProperty lineHeight;
    /** Holds the text decoration values. May be null */
    private CommonTextDecoration textDecoration;
    // private ToBeImplementedProperty textShadow;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonMarginInline commonMarginInline;
    //     private CommonRelativePosition commonRelativePosition;
    //     private KeepProperty keepWithNext;
    //     private KeepProperty keepWithPrevious;
    //     private int scoreSpaces;
    //     private Length textAltitude;
    //     private Length textDepth;
    //     private int textTransform;
    //     private int visibility;
    //     private SpaceProperty wordSpacing;
    //     private int wrapOption;
    //  End of property values

    // Properties which are not explicitely listed but are still applicable 
    private Color color;
    
    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumber(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        // letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        textDecoration = pList.getTextDecorationProps();
        // textShadow = pList.get(PR_TEXT_SHADOW);

        // implicit properties
        color = pList.get(Constants.PR_COLOR).getColor(getUserAgent());
    }

    /**
     * {@inheritDoc}
     */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startPageNumber(this);
    }

    /**
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endPageNumber(this);
    }

    /**
     * {@inheritDoc}
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /** @return the Common Font Properties. */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /** @return the "color" property. */
    public Color getColor() {
        return color;
    }

    /** @return the Common Border, Padding, and Background Properties. */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the "text-decoration" property. */
    public CommonTextDecoration getTextDecoration() {
        return textDecoration; 
    }

    /**
     * @return the "alignment-adjust" property
     */
    public Length getAlignmentAdjust() {
        return alignmentAdjust;
    }
    
    /**
     * @return the "alignment-baseline" property
     */
    public int getAlignmentBaseline() {
        return alignmentBaseline;
    }
    
    /**
     * @return the "baseline-shift" property
     */
    public Length getBaselineShift() {
        return baselineShift;
    }
    
    /**
     * @return the "dominant-baseline" property
     */
    public int getDominantBaseline() {
        return dominantBaseline;
    }
    
    /**
     * @return the "line-height" property
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }
    
    /** {@inheritDoc} */
    public String getLocalName() {
        return "page-number";
    }

    /** {@inheritDoc} */
    public int getNameId() {
        return FO_PAGE_NUMBER;
    }
}
