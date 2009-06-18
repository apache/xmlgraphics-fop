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

import java.awt.geom.Point2D;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.XMLObj;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * The instream-foreign-object flow formatting object.
 * This is an atomic inline object that contains
 * xml data.
 */
public class InstreamForeignObject extends FObj {
    
    // The value of properties relevant for fo:instream-foreign-object.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginInline commonMarginInline;
    private CommonRelativePosition commonRelativePosition;
    private Length alignmentAdjust;
    private int alignmentBaseline;
    private Length baselineShift;
    private LengthRangeProperty blockProgressionDimension;
    // private ToBeImplementedProperty clip;
    private Length contentHeight;
    private String contentType;
    private Length contentWidth;
    private int displayAlign;
    private int dominantBaseline;
    private Length height;
    private String id;
    private LengthRangeProperty inlineProgressionDimension;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private Length lineHeight;
    private int overflow;
    private int scaling;
    private int scalingMethod;
    private int textAlign;
    private int verticalAlign; // shorthand!!!
    private Length width;
    // End of property values

    //Additional value
    private Point2D intrinsicDimensions;
    
    /**
     * constructs an instream-foreign-object object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public InstreamForeignObject(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginInline = pList.getMarginInlineProps();
        commonRelativePosition = pList.getRelativePositionProps();
        alignmentAdjust = pList.get(PR_ALIGNMENT_ADJUST).getLength();
        alignmentBaseline = pList.get(PR_ALIGNMENT_BASELINE).getEnum();
        baselineShift = pList.get(PR_BASELINE_SHIFT).getLength();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        // clip = pList.get(PR_CLIP);
        contentHeight = pList.get(PR_CONTENT_HEIGHT).getLength();
        contentType = pList.get(PR_CONTENT_TYPE).getString();
        contentWidth = pList.get(PR_CONTENT_WIDTH).getLength();
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        dominantBaseline = pList.get(PR_DOMINANT_BASELINE).getEnum();
        height = pList.get(PR_HEIGHT).getLength();
        id = pList.get(PR_ID).getString();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        lineHeight = pList.get(PR_LINE_HEIGHT).getLength();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        scaling = pList.get(PR_SCALING).getEnum();
        scalingMethod = pList.get(PR_SCALING_METHOD).getEnum();
        textAlign = pList.get(PR_TEXT_ALIGN).getEnum();
        verticalAlign = pList.get(PR_VERTICAL_ALIGN).getEnum();
        width = pList.get(PR_WIDTH).getLength();
    }

    /**
     * @see org.apache.fop.fo.FONode#start
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (childNodes.size() != 1) {
            missingChildElementError("one (1) non-XSL namespace child");
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: one (1) non-XSL namespace child
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (nsURI == FO_URI) {
            invalidChildError(loc, nsURI, localName);
        } else if (childNodes != null) {
            tooManyNodesError(loc, "child element");
        }
    }

    public int computeXOffset (int ipd, int cwidth) {
        int xoffset = 0;
        switch (textAlign) {
            case EN_CENTER:
                xoffset = (ipd - cwidth) / 2;
                break;
            case EN_END:
                xoffset = ipd - cwidth;
                break;
            case EN_START:
                break;
            case EN_JUSTIFY:
            default:
                break;
        }
        return xoffset;
    }

    public int computeYOffset(int bpd, int cheight) {
        int yoffset = 0;
        switch (displayAlign) {
            case EN_BEFORE:
                break;
            case EN_AFTER:
                yoffset = bpd - cheight;
                break;
            case EN_CENTER:
                yoffset = (bpd - cheight) / 2;
                break;
            case EN_AUTO:
            default:
                break;
        }
        return yoffset;
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * Return the "line-height" property.
     */
    public Length getLineHeight() {
        return lineHeight;
    }

    /**
     * Return the "inline-progression-dimension" property.
     */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /**
     * Return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * Return the "height" property.
     */
    public Length getHeight() {
        return height;
    }

    /**
     * Return the "width" property.
     */
    public Length getWidth() {
        return width;
    }

    /**
     * Return the "content-height" property.
     */
    public Length getContentHeight() {
        return contentHeight;
    }

    /**
     * Return the "content-width" property.
     */
    public Length getContentWidth() {
        return contentWidth;
    }

    /**
     * Return the "scaling" property.
     */
    public int getScaling() {
        return scaling;
    }

    /**
     * Return the "vertical-align" property.
     */
    public int getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * Return the "overflow" property.
     */
    public int getOverflow() {
        return overflow;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:instream-foreign-object";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_INSTREAM_FOREIGN_OBJECT;
    }

    /**
     * @see org.apache.fop.fo.FObj#getLayoutDimension(org.apache.fop.datatypes.PercentBase.DimensionType)
     */
    public Number getLayoutDimension(PercentBase.LayoutDimension key) {
        if (key == PercentBase.IMAGE_INTRINSIC_WIDTH) {
            return new Integer(getIntrinsicWidth());
        } else if (key == PercentBase.IMAGE_INTRINSIC_HEIGHT) {
            return new Integer(getIntrinsicHeight());
        } else {
            return super.getLayoutDimension(key);
        }
    }
    
    /**
     * Preloads the image so the intrinsic size is available.
     */
    private void prepareIntrinsicSize() {
        if (intrinsicDimensions == null) {
            XMLObj child = (XMLObj)childNodes.get(0);
            Point2D csize = new Point2D.Float(-1, -1);
            intrinsicDimensions = child.getDimension(csize);
            if (intrinsicDimensions == null) {
                getLogger().error("Intrinsic dimensions of "
                        + " instream-foreign-object could not be determined");
            }
        }
    }

    /**
     * @see org.apache.fop.fo.IntrinsicSizeAccess#getIntrinsicWidth()
     */
    public int getIntrinsicWidth() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getX() * 1000);
        } else {
            return 0;
        }
    }

    /**
     * @see org.apache.fop.fo.IntrinsicSizeAccess#getIntrinsicHeight()
     */
    public int getIntrinsicHeight() {
        prepareIntrinsicSize();
        if (intrinsicDimensions != null) {
            return (int)(intrinsicDimensions.getY() * 1000);
        } else {
            return 0;
        }
    }
    
}
