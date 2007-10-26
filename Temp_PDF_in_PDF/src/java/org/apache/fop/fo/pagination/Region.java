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

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;

/**
 * This is an abstract base class for pagination regions
 */
public abstract class Region extends FObj {
    // The value of properties relevant for fo:region
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    // private ToBeImplementedProperty clip
    private int displayAlign;
    private int overflow;
    private String regionName;
    private Numeric referenceOrientation;
    private int writingMode;
    // End of property values
    
    private SimplePageMaster layoutMaster;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected Region(FONode parent) {
        super(parent);
        layoutMaster = (SimplePageMaster) parent;
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        // clip = pList.get(PR_CLIP);
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        regionName = pList.get(PR_REGION_NAME).getString();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingMode = pList.getWritingMode();
        
        // regions may have name, or default
        if (regionName.equals("")) {
            regionName = getDefaultRegionName();
        } else {
            // check that name is OK. Not very pretty.
            if (isReserved(getRegionName())
                    && !getRegionName().equals(getDefaultRegionName())) {
                throw new ValidationException("region-name '" + regionName
                        + "' for " + this.getName()
                        + " is not permitted.", locator);
            }
        }
        
        //TODO do we need context for getBPPaddingAndBorder() and getIPPaddingAndBorder()?
        if (getUserAgent().validateStrictly()
                && (getCommonBorderPaddingBackground().getBPPaddingAndBorder(false, null) != 0 
                || getCommonBorderPaddingBackground().getIPPaddingAndBorder(false, null) != 0)) {
            throw new PropertyException("Border and padding for region \""
                    + regionName + "\" must be '0' (See 6.4.13 in XSL 1.0).");
        }
    }

    /**
     * {@inheritDoc} String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @param pageRefRect reference dimension of the page area.
     * @param spm the simple page master this region belongs to.
     * @return the rectangle for the viewport area
     */
    public abstract Rectangle getViewportRectangle(FODimension pageRefRect
                                                   , SimplePageMaster spm);

    /**
     * Returns the default region name (xsl-region-before, xsl-region-start,
     * etc.)
     * @return the default region name
     */
    protected abstract String getDefaultRegionName();

    /**
     * Checks to see if a given region name is one of the reserved names
     *
     * @param name a region name to check
     * @return true if the name parameter is a reserved region name
     */
    protected boolean isReserved(String name) /*throws FOPException*/ {
        return (name.equals("xsl-region-before")
                || name.equals("xsl-region-start")
                || name.equals("xsl-region-end")
                || name.equals("xsl-region-after")
                || name.equals("xsl-before-float-separator")
                || name.equals("xsl-footnote-separator"));
    }

    /**
     * {@inheritDoc}
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Returns a sibling region for this region.
     * @param regionId the Constants ID of the FO representing the region
     * @return the requested region
     */
    protected Region getSiblingRegion(int regionId) {
        // Ask parent for region
        return layoutMaster.getRegion(regionId);
    }

    /**
     * @return the Background Properties (border and padding are not used here).
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground; 
    }

    /** @return the "region-name" property. */
    public String getRegionName() {
        return regionName;
    }

    /** @return the "writing-mode" property. */
    public int getWritingMode() {
        return writingMode;
    }

    /** @return the "overflow" property. */
    public int getOverflow() {
        return overflow;
    }
    
    /** @return the display-align property. */
    public int getDisplayAlign() {
        return displayAlign;
    }
       
    /** @return the "reference-orientation" property. */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }
}
