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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.SpaceProperty;

/**
 * Class modelling the fo:inline-container object.
 */
public class InlineContainer extends FObj {
    
    // The value of properties relevant for fo:inline-container.
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    // private ToBeImplementedProperty clip;
    private int dominantBaseline;
    private SpaceProperty lineHeight;
    // Unused but valid items, commented out for performance:
    //     private CommonBorderPaddingBackground commonBorderPaddingBackground;
    //     private CommonMarginInline commonMarginInline;
    //     private CommonRelativePosition commonRelativePosition;
    //     private LengthRangeProperty blockProgressionDimension;
    //     private int displayAlign;
    //     private Length height;
    //     private LengthRangeProperty inlineProgressionDimension;
    //     private KeepProperty keepTogether;
    //     private KeepProperty keepWithNext;
    //     private KeepProperty keepWithPrevious;
    //     private int overflow;
    //     private Numeric referenceOrientation;
    //     private Length width;
    //     private int writingMode;
    // End of property values

    /** used for FO validation */
    private boolean blockItemFound = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public InlineContainer(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        // clip = pList.get(PR_CLIP);
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        lineHeight = pList.get(PR_LINE_HEIGHT).getSpace();
    }

    /**
     * {@inheritDoc}
     * XSL Content Model: marker* (%block;)+
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

    /**
     * {@inheritDoc}
     */
    protected void endOfNode() throws FOPException {
        if (!blockItemFound) {
            missingChildElementError("marker* (%block;)+");
        }
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
     * @return the "line-height" property.
     */
    public SpaceProperty getLineHeight() {
        return lineHeight;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "inline-container";
    }
    
    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_INLINE_CONTAINER;
    }
    
}
