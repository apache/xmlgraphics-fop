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

// Java
import java.util.List;

import org.xml.sax.Locator;

import org.apache.commons.logging.Log;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.LayoutManager;


/**
 * The retrieve-marker formatting object.
 * This will create a layout manager that will retrieve
 * a marker based on the information.
 */
public class RetrieveMarker extends FObjMixed {
    // The value of properties relevant for fo:retrieve-marker.
    private String retrieveClassName;
    private int retrievePosition;
    private int retrieveBoundary;
    // End of property values

    private PropertyList propertyList;

    /**
     * Create a retrieve marker object.
     *
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public RetrieveMarker(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        retrieveClassName = pList.get(PR_RETRIEVE_CLASS_NAME).getString();
        retrievePosition = pList.get(PR_RETRIEVE_POSITION).getEnum();
        retrieveBoundary = pList.get(PR_RETRIEVE_BOUNDARY).getEnum();
    }
    
    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    protected PropertyList createPropertyList(PropertyList parent, 
            FOEventHandler foEventHandler) throws FOPException {
        // TODO: A special RetrieveMarkerPropertyList would be more memory
        // efficient. Storing a StaticPropertyList like this will keep all
        // the parent PropertyLists alive.
        propertyList = new StaticPropertyList(this, parent);
        return propertyList;
    }

    public PropertyList getPropertyList() {
        return propertyList;
    }

    /**
     * Return the "retrieve-class-name" property.
     */
    public String getRetrieveClassName() {
        return retrieveClassName;
    }

    /**
     * Return the "retrieve-position" property.
     */
    public int getRetrievePosition() {
        return retrievePosition;
    }

    /**
     * Return the "retrieve-boundry" property.
     */
    public int getRetrieveBoundary() {
        return retrieveBoundary;
    }

    public void bindMarker(Marker marker) {
        // assert(marker != null);
        try {
            marker.rebind(getPropertyList());
        } catch (FOPException exc) {
            Log log = getLogger();
            log.error("fo:retrieve-marker unable to rebind property values", exc);
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:retrieve-marker";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_RETRIEVE_MARKER;
    }
}
