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
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;

/**
 * Marker formatting object.
 */
public class Marker extends FObjMixed {
    // The value of properties relevant for fo:marker.
    private String markerClassName;
    // End of property values

    /**
     * Create a marker fo.
     * @param parent the parent fo node
     */
    public Marker(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws SAXParseException {
        markerClassName = pList.get(PR_MARKER_CLASS_NAME).getString();
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (#PCDATA|%inline;|%block;)*
     * Additionally: "An fo:marker may contain any formatting objects that 
     * are permitted as a replacement of any fo:retrieve-marker that retrieves
     * the fo:marker's children."
     * @todo implement "additional" constraint, possibly within fo:retrieve-marker
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (!isBlockOrInlineItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * Return the "marker-class-name" property.
     */
    public String getMarkerClassName() {
        return markerClassName;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:marker";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_MARKER;
    }
}
