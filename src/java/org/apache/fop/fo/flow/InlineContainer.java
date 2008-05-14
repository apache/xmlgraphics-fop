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

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_inline-container">
 * <code>fo:inline-container</code></a> object.
 */
public class InlineContainer extends FObj {
    
    // The value of properties relevant for fo:inline-container.
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private LengthRangeProperty blockProgressionDimension;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginInline commonMarginInline;
    private int clip;
    private int dominantBaseline;
    private LengthRangeProperty inlineProgressionDimension;
    private KeepProperty keepTogether;
    private SpaceProperty lineHeight;
    private int overflow;
    private Numeric referenceOrientation;
    private int writingMode;
    // Unused but valid items, commented out for performance:
    //     private CommonRelativePosition commonRelativePosition;
    //     private int displayAlign;
    //     private Length height;
    //     private KeepProperty keepWithNext;
    //     private KeepProperty keepWithPrevious;
    //     private Length width;
    // End of property values

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * Base constructor
     * 
     * @param parent {@link FONode} that is the parent of this object
     */
    public InlineContainer(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginInline = pList.getMarginInlineProps();
        clip = pList.get(PR_CLIP).getEnum();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingMode = pList.get(PR_WRITING_MODE).getEnum();
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (blockItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
                }
            } else if (!isBlockItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            } else {
                blockItemFound = true;
            }
        }
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
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
    
    /** @return the "block-progression-dimension" property */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }
    
    /** @return the "clip" property */
    public int getClip() {
        return clip;
    }
    
    /**@return Returns the {@link CommonBorderPaddingBackground} */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return this.commonBorderPaddingBackground;
    }

    /** @return Returns the {@link CommonMarginInline} */
    public CommonMarginInline getCommonMarginInline() {
        return this.commonMarginInline;
    }

    /** @return the "dominant-baseline" property */
    public int getDominantBaseline() {
        return dominantBaseline;
    }
    
    /** @return the "keep-together" property */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }
    
    /** @return the "inline-progression-dimension" property */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }
    
    /** @return the "line-height" property */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** @return the "overflow" property */
    public int getOverflow() {
        return overflow;
    }
    
    /** @return the "reference-orientation" property */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }
    
    /** @return the "writing-mode" property */
    public int getWritingMode() {
        return writingMode;
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
    
}
