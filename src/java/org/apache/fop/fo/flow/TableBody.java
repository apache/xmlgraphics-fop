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

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.AddLMVisitor;

import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.LMVisited;


/**
 * Class modelling the fo:table-body object. See Sec. 6.7.8 of the XSL-FO
 * Standard.
 */
public class TableBody extends FObj implements LMVisited {

    private int spaceBefore;
    private int spaceAfter;
    private ColorType backgroundColor;

    /**
     * @param parent FONode that is the parent of the object
     */
    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        setupID();
        getFOInputHandler().startBody(this);
    }

    private void setup() {
        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        // Common Aural Properties
        CommonAural mAurProps = propMgr.getAuralProps();

        // Common Border, Padding, and Background Properties
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // Common Relative Position Properties
        CommonRelativePosition mRelProps =
          propMgr.getRelativePositionProps();

        setupID();

        this.spaceBefore = this.propertyList.get(
                             PR_SPACE_BEFORE | CP_OPTIMUM).getLength().getValue();
        this.spaceAfter = this.propertyList.get(
                            PR_SPACE_AFTER | CP_OPTIMUM).getLength().getValue();
        this.backgroundColor =
          this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

    }

    /**
     * @return true (TableBody contains Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveTableBody(this);
    }

    protected void endOfNode() throws SAXParseException {
        getFOInputHandler().endBody(this);
    }

    public String getName() {
        return "fo:table-body";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_BODY;
    }
}

