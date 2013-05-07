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
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_block-container">
 * <code>fo:block-container</code></a> object.
 */
public class BlockContainer extends FObj implements BreakPropertySet, WritingModeTraitsGetter {
    // The value of FO traits (refined properties) that apply to fo:block-container.
    private CommonAbsolutePosition commonAbsolutePosition;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private LengthRangeProperty blockProgressionDimension;
    private int breakAfter;
    private int breakBefore;
    // private ToBeImplementedProperty clip;
    private int displayAlign;
    private LengthRangeProperty inlineProgressionDimension;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int overflow;
    private Numeric referenceOrientation;
    private int span;
    private int disableColumnBalancing;
    private WritingModeTraits writingModeTraits;
    // Unused but valid items, commented out for performance:
    //     private int intrusionDisplace;
    //     private Numeric zIndex;
    // End of FO trait values

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * Creates a new BlockContainer instance as a child of
     * the given {@link FONode}.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public BlockContainer(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAbsolutePosition = pList.getAbsolutePositionProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        // clip = pList.get(PR_CLIP);
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        span = pList.get(PR_SPAN).getEnum();
        writingModeTraits = new WritingModeTraits(
            WritingMode.valueOf(pList.get(PR_WRITING_MODE).getEnum()));
        disableColumnBalancing = pList.get(PR_X_DISABLE_COLUMN_BALANCING).getEnum();
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startBlockContainer(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)+
     * <br><i><b>BUT</b>: "In addition an fo:block-container that does not generate an
     * absolutely positioned area may have a sequence of zero or more
     * fo:markers as its initial children."
     * The latter refers to block-containers with absolute-position="absolute"
     * or absolute-position="fixed".
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if ("marker".equals(localName)) {
                if (commonAbsolutePosition.absolutePosition == EN_ABSOLUTE
                        || commonAbsolutePosition.absolutePosition == EN_FIXED) {
                    getFOValidationEventProducer()
                            .markerBlockContainerAbsolutePosition(this, locator);
                }
                if (blockItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
                }
            } else if (!isBlockItem(FO_URI, localName)) {
                invalidChildError(loc, FO_URI, localName);
            } else {
                blockItemFound = true;
            }
        }
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }

        getFOEventHandler().endBlockContainer(this);
    }

    /** @return <code>true</code> (BlockContainer can generate Reference Areas) */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /** @return the {@link CommonAbsolutePosition} */
    public CommonAbsolutePosition getCommonAbsolutePosition() {
        return commonAbsolutePosition;
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
     * @return the "block-progression-dimension" FO trait.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /** @return the "display-align" FO trait. */
    public int getDisplayAlign() {
        return displayAlign;
    }

    /** @return the "break-after" FO trait. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" FO trait. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-next" FO trait.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" FO trait.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" FO trait.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the "inline-progression-dimension" FO trait */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /** @return the "overflow" FO trait */
    public int getOverflow() {
        return overflow;
    }

    /** @return the "reference-orientation" FO trait */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }

    /** @return the "span" FO trait */
    public int getSpan() {
        return this.span;
    }

    /**
     * @return the "fox:disable-column-balancing" property, one of
     * {@link org.apache.fop.fo.Constants#EN_TRUE},
     * {@link org.apache.fop.fo.Constants#EN_FALSE}
     */
    public int getDisableColumnBalancing() {
        return disableColumnBalancing;
    }

    /**
     * Obtain inline progression direction.
     * @return the inline progression direction
     */
    public Direction getInlineProgressionDirection() {
        return writingModeTraits.getInlineProgressionDirection();
    }

    /**
     * Obtain block progression direction.
     * @return the block progression direction
     */
    public Direction getBlockProgressionDirection() {
        return writingModeTraits.getBlockProgressionDirection();
    }

    /**
     * Obtain column progression direction.
     * @return the column progression direction
     */
    public Direction getColumnProgressionDirection() {
        return writingModeTraits.getColumnProgressionDirection();
    }

    /**
     * Obtain row progression direction.
     * @return the row progression direction
     */
    public Direction getRowProgressionDirection() {
        return writingModeTraits.getRowProgressionDirection();
    }

    /**
     * Obtain (baseline) shift direction.
     * @return the (baseline) shift direction
     */
    public Direction getShiftDirection() {
        return writingModeTraits.getShiftDirection();
    }

    /**
     * Obtain writing mode.
     * @return the writing mode
     */
    public WritingMode getWritingMode() {
        return writingModeTraits.getWritingMode();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "block-container";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_BLOCK_CONTAINER}
     */
    public int getNameId() {
        return FO_BLOCK_CONTAINER;
    }
}

