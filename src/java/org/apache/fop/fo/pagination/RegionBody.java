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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonMarginBlock;

/**
 * The fo:region-body element.
 */
public class RegionBody extends Region {
    // The value of properties relevant for fo:region-body.
    private CommonMarginBlock commonMarginBlock;
    private Numeric columnCount;
    private Length columnGap;
    // End of property values

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RegionBody(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonMarginBlock = pList.getMarginBlockProps();
        columnCount = pList.get(PR_COLUMN_COUNT).getNumeric();
        columnGap = pList.get(PR_COLUMN_GAP).getLength();
        
        if ((getColumnCount() > 1) && (getOverflow() == EN_SCROLL)) {
            /* This is an error (See XSL Rec, fo:region-body description).
             * The Rec allows for acting as if "1" is chosen in
             * these cases, but we will need to be able to change Numeric
             * values in order to do this.
             */
            attributeError("If overflow property is set to \"scroll\"," +
                    " a column-count other than \"1\" may not be specified.");
        }
    }

    /**
     * Return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * Return the "column-count" property.
     */
    public int getColumnCount() {
        return columnCount.getValue();
    }

    /**
     * Return the "column-gap" property.
     */
    public int getColumnGap() {
        return columnGap.getValue();
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getViewportRectangle(FODimension)
     */
    public Rectangle getViewportRectangle (FODimension reldims) {
        int left = commonMarginBlock.marginLeft.getValue();
        int right = commonMarginBlock.marginRight.getValue();
        int top = commonMarginBlock.marginTop.getValue();
        int bottom = commonMarginBlock.marginBottom.getValue();
        return new Rectangle(left, top,
                    reldims.ipd - left - right,
                    reldims.bpd - top - bottom);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:region-body";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_REGION_BODY;
    }
}
