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

// XML
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
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
    public void bind(PropertyList pList) throws SAXParseException {
        super.bind(pList);
        commonMarginBlock = pList.getMarginBlockProps();
        columnCount = pList.get(PR_COLUMN_COUNT).getNumeric();
        columnGap = pList.get(PR_COLUMN_GAP).getLength();
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
        /*
        * Use space-before and space-after which will use corresponding
        * absolute margin properties if specified. For indents:
        * try to get corresponding absolute margin property using the
        * writing-mode on the page (not on the region-body!). If that's not
        * set but indent is explicitly set, it will return that.
        */
        int start = commonMarginBlock.startIndent.getValue();
        int end = commonMarginBlock.endIndent.getValue();
        int before = commonMarginBlock.spaceBefore.getOptimum().getLength().getValue();
        int after = commonMarginBlock.spaceAfter.getOptimum().getLength().getValue();
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
