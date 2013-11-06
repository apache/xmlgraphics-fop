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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;

public class InlineContainer extends FObj {

    private LengthRangeProperty inlineProgressionDimension;
    private LengthRangeProperty blockProgressionDimension;
    private int overflow;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginInline commonMarginInline;
    private Numeric referenceOrientation;
    private int displayAlign;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private SpaceProperty lineHeight;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private int dominantBaseline;
    private WritingModeTraits writingModeTraits;

    /** used for FO validation */
    private boolean blockItemFound;

    /**
     * Creates a new instance.
     *
     * @param parent the parent of this inline-container
     */
    public InlineContainer(FONode parent) {
        super(parent);
    }

    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginInline = pList.getMarginInlineProps();
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingModeTraits = new WritingModeTraits(
                WritingMode.valueOf(pList.get(PR_WRITING_MODE).getEnum()));
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)+
     */
    @Override
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (blockItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(%block;)+");
                }
            } else if (!isBlockItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            } else {
                blockItemFound = true;
            }
        }
    }

    @Override
    public void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "inline-container";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_INLINE_CONTAINER}
     */
    public int getNameId() {
        return FO_INLINE_CONTAINER;
    }

    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    public int getOverflow() {
        return overflow;
    }

    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return this.commonBorderPaddingBackground;
    }

    public CommonMarginInline getCommonMarginInline() {
        return this.commonMarginInline;
    }

    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }

    public int getDisplayAlign() {
        return this.displayAlign;
    }

    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    public Length getAlignmentAdjust() {
        return alignmentAdjust;
    }

    public int getAlignmentBaseline() {
        return alignmentBaseline;
    }

    public Length getBaselineShift() {
        return baselineShift;
    }

    public int getDominantBaseline() {
        return dominantBaseline;
    }

    public WritingMode getWritingMode() {
        return writingModeTraits.getWritingMode();
    }

    public Direction getInlineProgressionDirection() {
        return writingModeTraits.getInlineProgressionDirection();
    }

    public Direction getBlockProgressionDirection() {
        return writingModeTraits.getBlockProgressionDirection();
    }

    public Direction getColumnProgressionDirection() {
        return writingModeTraits.getColumnProgressionDirection();
    }

    public Direction getRowProgressionDirection() {
        return writingModeTraits.getRowProgressionDirection();
    }

    public Direction getShiftDirection() {
        return writingModeTraits.getShiftDirection();
    }

    @Override
    public boolean isDelimitedTextRangeBoundary(int boundary) {
        return false;
    }

    @Override
    public boolean generatesReferenceAreas() {
        return true;
    }

}
