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
import org.xml.sax.Attributes;

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
 * Common base class for the <a href="http://www.w3.org/TR/xsl/#fo_page-number-citation">
 * <code>fo:page-number-citation</code></a> and
 * <a href="http://www.w3.org/TR/xsl/#fo_page-number-citation-last">
 * <code>fo:page-number-citation-last</code></a> objects.
 */
public abstract class AbstractPageNumberCitation extends FObj {

    // The value of properties relevant for fo:page-number-citation(-last).
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private int dominantBaseline;
    private String ptr;  // used for accessibility
    // private ToBeImplementedProperty letterSpacing;
    private SpaceProperty lineHeight;
    private String refId;
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
    // End of property values

    // Properties which are not explicitely listed but are still applicable
    private Color color;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public AbstractPageNumberCitation(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
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
        ptr = pList.get(PR_X_PTR).getString();   // used for accessibility
        refId = pList.get(PR_REF_ID).getString();
        textDecoration = pList.getTextDecorationProps();
        // textShadow = pList.get(PR_TEXT_SHADOW);

        // implicit properties
        color = pList.get(Constants.PR_COLOR).getColor(getUserAgent());
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator, Attributes attlist, PropertyList pList) throws FOPException {
        super.processNode(elementName, locator, attlist, pList);
        if (!inMarker() && (refId == null || "".equals(refId))) {
            missingPropertyError("ref-id");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /** @return the {@link CommonFont} */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /** @return the "color" property. */
    public Color getColor() {
        return color;
    }

    /** @return the "text-decoration" property. */
    public CommonTextDecoration getTextDecoration() {
        return textDecoration;
    }

    /** @return the "foi:ptr" property.  */
    public String getPtr() {
        return ptr;
    }
    
    /** @return the "alignment-adjust" property */
    public Length getAlignmentAdjust() {
        return alignmentAdjust;
    }

    /** @return the "alignment-baseline" property */
    public int getAlignmentBaseline() {
        return alignmentBaseline;
    }

    /** @return the "baseline-shift" property */
    public Length getBaselineShift() {
        return baselineShift;
    }

    /** @return the "dominant-baseline" property */
    public int getDominantBaseline() {
        return dominantBaseline;
    }

    /** @return the {@link CommonBorderPaddingBackground} */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the "line-height" property */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @return the "ref-id" property. */
    public String getRefId() {
        return refId;
    }

}
