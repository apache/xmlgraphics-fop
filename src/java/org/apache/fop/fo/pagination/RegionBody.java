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
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.AddLMVisitor;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.FOPropertyMapping;

/**
 * The fo:region-body element.
 */
public class RegionBody extends Region {

    private ColorType backgroundColor;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RegionBody(FONode parent) {
        super(parent, Region.BODY_CODE);
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
        CommonMarginBlock mProps = propMgr.getMarginProps();
        int start = getRelMargin(PropertyList.START, PR_START_INDENT);
        Rectangle vpRect;
        if (this.wm == WritingMode.LR_TB || this.wm == WritingMode.RL_TB) {
            vpRect = new Rectangle(start, mProps.spaceBefore,
                    reldims.ipd - start
                        - getRelMargin(PropertyList.END, PR_END_INDENT),
                    reldims.bpd - mProps.spaceBefore - mProps.spaceAfter);
        } else {
            vpRect = new Rectangle(start, mProps.spaceBefore,
                    reldims.bpd - mProps.spaceBefore - mProps.spaceAfter,
                    reldims.ipd - start
                        - getRelMargin(PropertyList.END, PR_END_INDENT));
        }
        return vpRect;
    }

    /**
     * Get the relative margin using parent's writing mode, not own
     * writing mode.
     */
    private int getRelMargin(int reldir, int relPropId) {
        FObj parent = (FObj) getParent();
        String sPropName = "margin-"
                + parent.getPropertyList().getAbsoluteWritingMode(reldir);
        int propId = FOPropertyMapping.getPropertyId(sPropName);
        Property prop = propertyList.getExplicitOrShorthand(propId);
        if (prop == null) {
            prop = propertyList.getExplicitOrShorthand(relPropId);
        }
        return ((prop != null) ? prop.getLength().getValue() : 0);
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getDefaultRegionName()
     */
    protected String getDefaultRegionName() {
        return "xsl-region-body";
    }

    /**
     * @see org.apache.fop.fo.pagination.Region#getRegionClassCode()
     */
    public int getRegionClassCode() {
        return Region.BODY_CODE;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveRegionBody(this);
    }

    public String getName() {
        return "fo:region-body";
    }
}
