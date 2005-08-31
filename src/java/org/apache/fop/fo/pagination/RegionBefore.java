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

// Java
import java.awt.Rectangle;

// FOP
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.fo.FONode;

/**
 * The fo:region-before element.
 */
public class RegionBefore extends RegionBA {
    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RegionBefore(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-before";
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getViewportRectangle(FODimension)
     */
    public Rectangle getViewportRectangle (FODimension reldims, FODimension pageViewPortRect) {
        // Depends on extent, precedence and writing mode
        // This should return rectangle in writing-mode coordinates relative
        // to the page-reference area rectangle
        // This means the origin is (start, before) and the dimensions are (ipd,bpd)
        // Before is always 0, start depends on extent
        // ipd depends on precedence, bpd=extent
        /* Special rules apply to resolving extent.
         * In the property subsystem the extent property is configured to 
         * using BLOCK_WIDTH as its percent base.
         * However, depending on the writing mode extent import resolved either
         * against the page width or the page height.
         */
        Rectangle vpRect;
        SimplePercentBaseContext pageWidthContext 
            = new SimplePercentBaseContext(null, LengthBase.CUSTOM_BASE, pageViewPortRect.ipd);
        SimplePercentBaseContext pageHeightContext
            = new SimplePercentBaseContext(null, LengthBase.CUSTOM_BASE, pageViewPortRect.bpd);
        SimplePercentBaseContext neighbourContext;
        if (getWritingMode() == EN_LR_TB || getWritingMode() == EN_RL_TB) {
            neighbourContext = pageWidthContext;
            vpRect = new Rectangle(0, 0, reldims.ipd, getExtent().getValue(pageHeightContext));
        } else {
            neighbourContext = pageHeightContext;
            vpRect = new Rectangle(0, 0, getExtent().getValue(pageWidthContext), reldims.ipd);
        }
        if (getPrecedence() == EN_FALSE) {
            adjustIPD(vpRect, getWritingMode(), neighbourContext);
        }
        return vpRect;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:region-before";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_REGION_BEFORE;
    }
}

