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

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ToBeImplementedElement;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;

/**
 * Class modelling the fo:table-and-caption property. See Sec. 6.7.2 of the
 * XSL-FO Standard.
 */
public class TableAndCaption extends ToBeImplementedElement {

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableAndCaption(FONode parent) {
        super(parent);
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Margin Properties-Block
        CommonMarginBlock mProps = propMgr.getMarginProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps = propMgr.getRelativePositionProps();

        // this.propertyList.get("caption-side");
        setupID();
        // this.propertyList.get("keep-together");
        // this.propertyList.get("keep-with-next");
        // this.propertyList.get("keep-with-previous");

    }

    /**
     * @return false (TableAndCaption doesn't generate inline areas)
     */
    public boolean generatesInlineAreas() {
        return false;
    }

    public String getName() {
        return "fo:table-and-caption";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_AND_CAPTION;
    }
}

