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

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
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

    /** Holds the writing mode */
    protected int wm;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    protected Region(FONode parent) {
        super(parent);
        layoutMaster = (SimplePageMaster) parent;
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws SAXParseException {
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        // clip = pList.get(PR_CLIP);
        displayAlign = pList.get(PR_DISPLAY_ALIGN).getEnum();
        overflow = pList.get(PR_OVERFLOW).getEnum();
        regionName = pList.get(PR_REGION_NAME).getString();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingMode = pList.getWritingMode();
        
        // regions may have name, or default
        if (null == regionName) {
            setRegionName(getDefaultRegionName());
        } else if (regionName.equals("")) {
            setRegionName(getDefaultRegionName());
        } else {
            setRegionName(regionName);
            // check that name is OK. Not very pretty.
            if (isReserved(getRegionName())
                    && !getRegionName().equals(getDefaultRegionName())) {
                throw new SAXParseException("region-name '" + regionName
                        + "' for " + this.getName()
                        + " is not permitted.", locator);
            }
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

        // regions may have name, or default
        if (null == this.propertyList.get(PR_REGION_NAME)) {
            setRegionName(getDefaultRegionName());
        } else if (getPropString(PR_REGION_NAME).equals("")) {
            setRegionName(getDefaultRegionName());
        } else {
            setRegionName(getPropString(PR_REGION_NAME));
            // check that name is OK. Not very pretty.
            if (isReserved(getRegionName())
                    && !getRegionName().equals(getDefaultRegionName())) {
                throw new SAXParseException("region-name '" + regionName
                        + "' for " + this.getName()
                        + " is not permitted.", locator);
            }
        }

        this.wm = getPropEnum(PR_WRITING_MODE);
        this.overflow = getPropEnum(PR_OVERFLOW);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            invalidChildError(loc, nsURI, localName);
    }

    public abstract Rectangle getViewportRectangle(FODimension pageRefRect);

    /**
     * Returns the default region name (xsl-region-before, xsl-region-start,
     * etc.)
     * @return the default region name
     */
    protected abstract String getDefaultRegionName();

     /**
     * Sets the name of the region.
     * @param name the name
     */
    private void setRegionName(String name) {
        this.regionName = name;
    }

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
     * @see org.apache.fop.fo.FObj#generatesReferenceAreas()
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
     * Return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground; 
    }

    /**
     * Return the "region-name" property.
     */
    public String getRegionName() {
        return this.regionName;
    }

    /**
     * Return the "writing-mode" property.
     */
    public int getWritingMode() {
        return writingMode;
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
}
