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
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.CharIterator;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.NullCharIterator;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.SpaceProperty;

 /**
  * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_block">
  * <code>fo:block object</code></a>.
  */
public class Block extends FObjMixed implements BreakPropertySet,
        CommonAccessibilityHolder {

    // used for FO validation
    private boolean blockOrInlineItemFound = false;
    private boolean initialPropertySetFound = false;

    // The value of properties relevant for fo:block.
    private CommonAccessibility commonAccessibility;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonFont commonFont;
    private CommonHyphenation commonHyphenation;
    private CommonMarginBlock commonMarginBlock;
    private CommonRelativePosition commonRelativePosition;
    private int breakAfter;
    private int breakBefore;
    private Color color;
    private int hyphenationKeep;
    private Numeric hyphenationLadderCount;
    private int intrusionDisplace;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private Length lastLineEndIndent;
    private int linefeedTreatment;
    private SpaceProperty lineHeight;
    private int lineHeightShiftAdjustment;
    private int lineStackingStrategy;
    private Numeric orphans;
    private int whiteSpaceTreatment;
    private int span;
    private int textAlign;
    private int textAlignLast;
    private Length textIndent;
    private int whiteSpaceCollapse;
    private Numeric widows;
    private int wrapOption;
    private int disableColumnBalancing;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private Length textDepth;
    //     private Length textAltitude;
    //     private int visibility;
    // End of property values

    /**
     * Base constructor
     *
     * @param parent FONode that is the parent of this object
     *
     */
    public Block(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonFont = pList.getFontProps();
        commonHyphenation = pList.getHyphenationProps();
        commonMarginBlock = pList.getMarginBlockProps();
        commonRelativePosition = pList.getRelativePositionProps();

        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        color = pList.get(PR_COLOR).getColor(getUserAgent());
        hyphenationKeep = pList.get(PR_HYPHENATION_KEEP).getEnum();
        hyphenationLadderCount = pList.get(PR_HYPHENATION_LADDER_COUNT).getNumeric();
        intrusionDisplace = pList.get(PR_INTRUSION_DISPLACE).getEnum();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        lastLineEndIndent = pList.get(PR_LAST_LINE_END_INDENT).getLength();
        linefeedTreatment = pList.get(PR_LINEFEED_TREATMENT).getEnum();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        lineHeightShiftAdjustment = pList.get(PR_LINE_HEIGHT_SHIFT_ADJUSTMENT).getEnum();
        lineStackingStrategy = pList.get(PR_LINE_STACKING_STRATEGY).getEnum();
        orphans = pList.get(PR_ORPHANS).getNumeric();
        whiteSpaceTreatment = pList.get(PR_WHITE_SPACE_TREATMENT).getEnum();
        span = pList.get(PR_SPAN).getEnum();
        textAlign = pList.get(PR_TEXT_ALIGN).getEnum();
        textAlignLast = pList.get(PR_TEXT_ALIGN_LAST).getEnum();
        textIndent = pList.get(PR_TEXT_INDENT).getLength();
        whiteSpaceCollapse = pList.get(PR_WHITE_SPACE_COLLAPSE).getEnum();
        widows = pList.get(PR_WIDOWS).getNumeric();
        wrapOption = pList.get(PR_WRAP_OPTION).getEnum();
        disableColumnBalancing = pList.get(PR_X_DISABLE_COLUMN_BALANCING).getEnum();
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startBlock(this);
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endBlock(this);
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /** @return the {@link CommonMarginBlock} */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /** @return the {@link CommonBorderPaddingBackground} */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the {@link CommonFont} */
    public CommonFont getCommonFont() {
        return commonFont;
    }

    /** @return the {@link CommonHyphenation} */
    public CommonHyphenation getCommonHyphenation() {
        return commonHyphenation;
    }

    /** @return the "break-after" property. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "hyphenation-ladder-count" property.  */
    public Numeric getHyphenationLadderCount() {
        return hyphenationLadderCount;
    }

    /** @return the "keep-with-next" property.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" property.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" property.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the "orphans" property.  */
    public int getOrphans() {
        return orphans.getValue();
    }

    /** @return the "widows" property.  */
    public int getWidows() {
        return widows.getValue();
    }

    /** @return the "line-stacking-strategy" property.  */
    public int getLineStackingStrategy() {
        return lineStackingStrategy;
    }

    /** @return the "color" property */
    public Color getColor() {
        return color;
    }

    /** @return the "line-height" property */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @return the "span" property */
    public int getSpan() {
        return this.span;
    }

    /** @return the "text-align" property */
    public int getTextAlign() {
        return textAlign;
    }

    /** @return the "text-align-last" property */
    public int getTextAlignLast() {
        return textAlignLast;
    }

    /** @return the "text-indent" property */
    public Length getTextIndent() {
        return textIndent;
    }

    /** @return the "last-line-end-indent" property */
    public Length getLastLineEndIndent() {
        return lastLineEndIndent;
    }

    /** @return the "wrap-option" property */
    public int getWrapOption() {
        return wrapOption;
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* initial-property-set? (#PCDATA|%inline;|%block;)*
     * <br><i>Additionally: "An fo:bidi-override that is a descendant of an fo:leader
     *  or of the fo:inline child of an fo:footnote may not have block-level
     *  children, unless it has a nearer ancestor that is an
     *  fo:inline-container."</i>
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if ("marker".equals(localName)) {
                if (blockOrInlineItemFound || initialPropertySetFound) {
                   nodesOutOfOrderError(loc, "fo:marker",
                        "initial-property-set? (#PCDATA|%inline;|%block;)");
                }
            } else if ("initial-property-set".equals(localName)) {
                if (initialPropertySetFound) {
                    tooManyNodesError(loc, "fo:initial-property-set");
                } else if (blockOrInlineItemFound) {
                    nodesOutOfOrderError(loc, "fo:initial-property-set",
                        "(#PCDATA|%inline;|%block;)");
                } else {
                    initialPropertySetFound = true;
                }
            } else if (isBlockOrInlineItem(nsURI, localName)) {
                blockOrInlineItemFound = true;
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** @return the "linefeed-treatment" property */
    public int getLinefeedTreatment() {
        return linefeedTreatment;
    }

    /** @return the "white-space-treatment" property */
    public int getWhitespaceTreatment() {
        return whiteSpaceTreatment;
    }

    /** @return the "white-space-collapse" property */
    public int getWhitespaceCollapse() {
        return whiteSpaceCollapse;
    }

    /** @return the {@link CommonRelativePosition} */
    public CommonRelativePosition getCommonRelativePosition() {
        return this.commonRelativePosition;
    }

    /** @return the "hyphenation-keep" property */
    public int getHyphenationKeep() {
        return this.hyphenationKeep;
    }

    /** @return the "intrusion-displace" property */
    public int getIntrusionDisplace() {
        return this.intrusionDisplace;
    }

    /** @return the "line-height-shift-adjustment" property */
    public int getLineHeightShiftAdjustment() {
        return this.lineHeightShiftAdjustment;
    }

     /**
     * @return the "fox:disable-column-balancing" property, one of
     * {@link org.apache.fop.fo.Constants#EN_TRUE},
     * {@link org.apache.fop.fo.Constants#EN_FALSE}
     */
     public int getDisableColumnBalancing() {
         return disableColumnBalancing;
     }


    /** {@inheritDoc} */
    public CharIterator charIterator() {
        return NullCharIterator.getInstance();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "block";
    }

     /**
      * {@inheritDoc}
      * @return {@link org.apache.fop.fo.Constants#FO_BLOCK}
      */
    public int getNameId() {
        return FO_BLOCK;
    }

}
