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
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBase;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;

/**
 * External graphic formatting object.
 * This FO node handles the external graphic. It creates an image
 * inline area that can be added to the area tree.
 */
public class ExternalGraphic extends FObj {
    
    // The value of properties relevant for fo:external-graphic.
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
    private String src;
    private int textAlign;
    private int verticalAlign; //Extra
    private Length width;
    // End of property values

    //Additional values
    private String url;
    private FopImage fopimage;
    
    /**
     * Create a new External graphic node.
     *
     * @param parent the parent of this node
     */
    public ExternalGraphic(FONode parent) {
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
        src = pList.get(PR_SRC).getString();
        textAlign = pList.get(PR_TEXT_ALIGN).getEnum();
        verticalAlign = pList.get(PR_VERTICAL_ALIGN).getEnum();
        width = pList.get(PR_WIDTH).getLength();
        
        //Additional processing: preload image
        url = ImageFactory.getURL(getSrc());
        ImageFactory fact = ImageFactory.getInstance();
        fopimage = fact.getImage(url, getUserAgent());
        if (fopimage == null) {
            getLogger().error("Image not available: " + getSrc());
        } else {
            // load dimensions
            if (!fopimage.load(FopImage.DIMENSIONS)) {
                getLogger().error("Cannot read image dimensions: " + getSrc());
            }
        }
        //TODO Report to caller so he can decide to throw an exception
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().image(this);
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
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /**
     * @return the Common Margin Properties-Inline.
     */
    public CommonMarginInline getCommonMarginInline() {
        return commonMarginInline;
    }

    /**
     * @return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * @return the "content-height" property.
     */
    public Length getContentHeight() {
        return contentHeight;
    }

    /**
     * @return the "content-width" property.
     */
    public Length getContentWidth() {
        return contentWidth;
    }

    /**
     * @return the "display-align" property.
     */
    public int getDisplayAlign() {
        return displayAlign;
    }

    /**
     * @return the "height" property.
     */
    public Length getHeight() {
        return height;
    }

    /**
     * @return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * @return the "inline-progression-dimension" property.
     */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /**
     * @return the "overflow" property.
     */
    public int getOverflow() {
        return overflow;
    }
    
    /**
     * @return the "scaling" property.
     */
    public int getScaling() {
        return scaling;
    }

    /**
     * @return the "src" property.
     */
    public String getSrc() {
        return src;
    }

    /**
     * @return Get the resulting URL based on the src property.
     */
    public String getURL() {
        return url;
    }

    /**
     * @return the "text-align" property.
     */
    public int getTextAlign() {
        return textAlign;
    }

    /**
     * @return the "width" property.
     */
    public Length getWidth() {
        return width;
    }

    /**
     * @return the "vertical-align" property.
     */
    public int getVerticalAlign() {
        return verticalAlign;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:external-graphic";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_EXTERNAL_GRAPHIC;
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
     * @see org.apache.fop.fo.IntrinsicSizeAccess#getIntrinsicWidth()
     */
    public int getIntrinsicWidth() {
        if (fopimage != null) {
            return fopimage.getIntrinsicWidth();
        } else {
            return 0;
        }
    }

    /**
     * @see org.apache.fop.fo.IntrinsicSizeAccess#getIntrinsicHeight()
     */
    public int getIntrinsicHeight() {
        if (fopimage != null) {
            return fopimage.getIntrinsicHeight();
        } else {
            return 0;
        }
    }
    
}
