/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.CommonTextDecoration;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the fo:page-number object.
 */
public class PageNumber extends FObj {
    // The value of properties relevant for fo:page-number.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonMarginInline commonMarginInline;
    private CommonRelativePosition commonRelativePosition;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private int dominantBaseline;
    private String id;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // private ToBeImplementedProperty letterSpacing;
    private SpaceProperty lineHeight;
    private int scoreSpaces;
    private Length textAltitude;
    /** Holds the text decoration values. May be null */
    private CommonTextDecoration textDecoration;
    private Length textDepth;
    // private ToBeImplementedProperty textShadow;
    private int textTransform;
    private int visibility;
    private SpaceProperty wordSpacing;
    private int wrapOption;
    //  End of property values

    // Properties which are not explicitely listed but are still applicable 
    private ColorType color;
    
    /**
     * @param parent FONode that is the parent of this object
     */
    public PageNumber(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonRelativePosition = pList.getRelativePositionProps();
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        id = pList.get(PR_ID).getString();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        // letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        scoreSpaces = pList.get(PR_SCORE_SPACES).getEnum();
        textAltitude = pList.get(PR_TEXT_ALTITUDE).getLength();
        textDecoration = pList.getTextDecorationProps();
        textDepth = pList.get(PR_TEXT_DEPTH).getLength();
        // textShadow = pList.get(PR_TEXT_SHADOW);
        textTransform = pList.get(PR_TEXT_TRANSFORM).getEnum();
        visibility = pList.get(PR_VISIBILITY).getEnum();
        wordSpacing = pList.get(PR_WORD_SPACING).getSpace();
        wrapOption = pList.get(PR_WRAP_OPTION).getEnum();

        // implicit properties
        color = pList.get(Constants.PR_COLOR).getColorType();
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().startPageNumber(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        getFOEventHandler().endPageNumber(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
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
    public ColorType getColor() {
        return color;
    }

    /** @return the Common Border, Padding, and Background Properties. */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the "id" property. */
    public String getId() {
        return id;
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
    
   /** @see org.apache.fop.fo.FONode#getName() */
    public String getName() {
        return "fo:page-number";
    }

    /** @see org.apache.fop.fo.FObj#getNameId() */
    public int getNameId() {
        return FO_PAGE_NUMBER;
    }
}
