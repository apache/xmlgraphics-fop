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

// Java
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
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
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.PageNumberLayoutManager;

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
    // private ToBeImplementedProperty alignmentAdjust;
    // private ToBeImplementedProperty alignmentBaseline;
    private Length baselineShift;
    // private ToBeImplementedProperty dominantBaseline;
    private String id;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // private ToBeImplementedProperty letterSpacing;
    private Length lineHeight;
    // private ToBeImplementedProperty scoreSpaces;
    // private ToBeImplementedProperty textAltitude;
    private int textDecoration;
    // private ToBeImplementedProperty textDepth;
    // private ToBeImplementedProperty textShadow;
    private int textTransform;
    // private ToBeImplementedProperty visibility;
    private SpaceProperty wordSpacing;
    private int wrapOption;
    //  End of property values

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
        // alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST);
        // alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE);
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        // dominantBaseline = pList.get(PR_DOMINANT_BASELINE);
        id = pList.get(PR_ID).getString();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        // letterSpacing = pList.get(PR_LETTER_SPACING);
        lineHeight = pList.get(PR_LINE_HEIGHT).getLength();
        // scoreSpaces = pList.get(PR_SCORE_SPACES);
        // textAltitude = pList.get(PR_TEXT_ALTITUDE);
        textDecoration = pList.get(PR_TEXT_DECORATION).getEnum();
        // textDepth = pList.get(PR_TEXT_DEPTH);
        // textShadow = pList.get(PR_TEXT_SHADOW);
        textTransform = pList.get(PR_TEXT_TRANSFORM).getEnum();
        // visibility = pList.get(PR_VISIBILITY);
        wordSpacing = pList.get(PR_WORD_SPACING).getSpace();
        wrapOption = pList.get(PR_WRAP_OPTION).getEnum();
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

    /**
     * Return the Common Font Properties.
     */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /**
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the "text-decoration" property.
     */
    public int getTextDecoration() {
        return textDecoration; 
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {
        PageNumberLayoutManager lm = new PageNumberLayoutManager(this);
        list.add(lm);
    }

    /**
     * @see org.apache.fop.fo.FONode#getName()
     */
    public String getName() {
        return "fo:page-number";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_PAGE_NUMBER;
    }
}
