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
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.CommonAccessibility;

/**
 * Class modelling the fo:list-item-label object. See Sec. 6.8.5 of the XSL-FO
 * Standard.
 */
public class ListItemLabel extends FObj {

    /**
     * @param parent FONode that is the parent of this object
     */
    public ListItemLabel(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        getFOInputHandler().startListLabel();
    }

    private void setup() {

        // Common Accessibility Properties
        CommonAccessibility mAccProps = propMgr.getAccessibilityProps();

        setupID();
        // this.propertyList.get("keep-together");

        /*
         * For calculating the lineage - The fo:list-item-label formatting object
         * does not generate any areas. The fo:list-item-label formatting object
         * returns the sequence of areas created by concatenating the sequences
         * of areas returned by each of the child nodes of the fo:list-item-label.
         */

    }

    /**
     * @return true (ListItemLabel may contain Markers)
     */
    protected boolean containsMarkers() {
        return true;
    }

    protected void endOfNode() throws SAXParseException {
        super.endOfNode();
        getFOInputHandler().endListLabel();
    }
    
    public String getName() {
        return "fo:list-item-label";
    }
}

