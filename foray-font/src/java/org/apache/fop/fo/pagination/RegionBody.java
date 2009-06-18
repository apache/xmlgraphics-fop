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

// Java
import java.awt.Rectangle;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.LengthBase;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.SimplePercentBaseContext;
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
            attributeError("If overflow property is set to \"scroll\"," 
                    + " a column-count other than \"1\" may not be specified.");
        }
    }

    /**
     * Return the Common Margin Properties-Block.
     * @return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * Return the "column-count" property.
     * @return the "column-count" property.
     */
    public int getColumnCount() {
        return columnCount.getValue();
    }

    /**
     * Return the "column-gap" property.
     * @return the "column-gap" property.
     */
    public int getColumnGap() {
        return columnGap.getValue();
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getViewportRectangle(FODimension, SimplePageMaster)
     */
    public Rectangle getViewportRectangle (FODimension reldims, SimplePageMaster spm) {
        /* Special rules apply to resolving margins in the page context.
         * Contrary to normal margins in this case top and bottom margin
         * are resolved relative to the height. In the property subsystem
         * all margin properties are configured to using BLOCK_WIDTH.
         * That's why we 'cheat' here and setup a context for the height but
         * use the LengthBase.BLOCK_WIDTH.
         * Also the values are resolved relative to the page size 
         * and reference orientation.
         */
        SimplePercentBaseContext pageWidthContext;
        SimplePercentBaseContext pageHeightContext;
        if (spm.getReferenceOrientation() % 180 == 0) {
            pageWidthContext = new SimplePercentBaseContext(null, 
                                                            LengthBase.CONTAINING_BLOCK_WIDTH,
                                                            spm.getPageWidth().getValue());
            pageHeightContext = new SimplePercentBaseContext(null,
                                                             LengthBase.CONTAINING_BLOCK_WIDTH,
                                                             spm.getPageHeight().getValue());
        } else {
            // invert width and height since top left are rotated by 90 (cl or ccl)
            pageWidthContext = new SimplePercentBaseContext(null, 
                                                            LengthBase.CONTAINING_BLOCK_WIDTH,
                                                            spm.getPageHeight().getValue());
            pageHeightContext = new SimplePercentBaseContext(null,
                                                             LengthBase.CONTAINING_BLOCK_WIDTH,
                                                             spm.getPageWidth().getValue());
        }

        int start;
        int end;
        if (spm.getWritingMode() == EN_LR_TB) { // Left-to-right
            start = commonMarginBlock.marginLeft.getValue(pageWidthContext);
            end = commonMarginBlock.marginRight.getValue(pageWidthContext);
        } else { // all other supported modes are right-to-left
            start = commonMarginBlock.marginRight.getValue(pageWidthContext);
            end = commonMarginBlock.marginLeft.getValue(pageWidthContext);
        }
        int before = commonMarginBlock.spaceBefore.getOptimum(pageHeightContext)
                        .getLength().getValue(pageHeightContext);
        int after = commonMarginBlock.spaceAfter.getOptimum(pageHeightContext)
                        .getLength().getValue(pageHeightContext);
        return new Rectangle(start, before,
                    reldims.ipd - start - end,
                    reldims.bpd - before - after);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "region-body";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_REGION_BODY;
    }
}
