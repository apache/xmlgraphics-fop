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

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.layoutmgr.BlockContainerLayoutManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * Class modelling the fo:block-container object.
 * @todo implement validateChildNode()
 */
public class BlockContainer extends FObj {
    // The value of properties relevant for fo:block-container.
    private CommonAbsolutePosition commonAbsolutePosition ;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private LengthRangeProperty blockProgressionDimension;
    private int breakAfter;
    private int breakBefore;
    // private ToBeImplementedProperty clip;
    private int displayAlign;
    private Length height;
    private String id;
    private LengthRangeProperty inlineProgressionDimension;
    // private ToBeImplementedProperty intrusionDisplace;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int overflow;
    private Numeric referenceOrientation;
    private int span;
    private Length width;
    private int writingMode;
    // private ToBeImplementedProperty zIndex;
    // End of property values

    private ColorType backgroundColor;
    private int position;

    private int top;
    private int bottom;
    private int left;
    private int right;
    private int _width;
    private int _height;


    /**
     * @param parent FONode that is the parent of this object
     */
    public BlockContainer(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) {
        commonAbsolutePosition = pList.getAbsolutePositionProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        // clip = pList.get(PR_CLIP);
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        height = pList.get(PR_HEIGHT).getLength();
        id = pList.get(PR_ID).getString();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        // intrusionDisplace = pList.get(PR_INTRUSION_DISPLACE);
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        span = pList.get(PR_SPAN).getEnum();
        width = pList.get(PR_WIDTH).getLength();
        writingMode = pList.get(PR_WRITING_MODE).getEnum();
        // zIndex = pList.get(PR_Z_INDEX);
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws SAXParseException {
        checkId(id);
        getFOEventHandler().startBlockContainer(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.span = getPropEnum(PR_SPAN);
        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        this._width = getPropLength(PR_WIDTH);
        this._height = getPropLength(PR_HEIGHT);
        getFOEventHandler().startBlockContainer(this);
    }
    
    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws SAXParseException {
        getFOEventHandler().endBlockContainer(this);
    }

    /**
     * @return true (BlockContainer can generate Reference Areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Return the Common Absolute Position Properties.
     */
    public CommonAbsolutePosition getCommonAbsolutePosition() {
        return commonAbsolutePosition;
    }
    
    /**
     * Return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * Return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the "inline-progression-dimension" property.
     */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /**
     * Return the "overflow" property.
     */
    public int getOverflow() {
        return overflow;
    }

    /**
     * Return the "reference-orientation" property.
     */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }

    /**
     * Return the "span" property.
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * Return the "writing-mode" property.
     */
    public int getWritingMode() {
        return writingMode;
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {    
        BlockContainerLayoutManager blm = new BlockContainerLayoutManager(this);
        blm.setOverflow(getPropEnum(PR_OVERFLOW));
        list.add(blm);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:block-container";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BLOCK_CONTAINER;
    }
}

