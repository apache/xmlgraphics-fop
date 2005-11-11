/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Iterator;


import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.PropertyListMaker;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.Property;

/**
 * Marker formatting object.
 */
public class Marker extends FObjMixed {
    // The value of properties relevant for fo:marker.
    private String markerClassName;
    // End of property values

    private PropertyListMaker savePropertyListMaker;
    private HashMap descPLists = new HashMap();

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
    public void bind(PropertyList pList) throws FOPException {
        if (findAncestor(FO_FLOW) < 0) {
            invalidChildError(locator, FO_URI, "marker", 
                "An fo:marker is permitted only as the descendant " +
                "of an fo:flow");
        }
        
        markerClassName = pList.get(PR_MARKER_CLASS_NAME).getString();
        
        if (markerClassName == null || markerClassName.equals("")) {
            missingPropertyError("marker-class-name");
        }        
    }
    
    /**
     * retrieve the property list of foNode
     * @param foNode the FO node whose property list is requested
     * @return the MarkerPropertyList of foNode
     */
    protected MarkerPropertyList getPList(FONode foNode) {
        return (MarkerPropertyList) descPLists.get(foNode);
    }

    protected PropertyList createPropertyList(PropertyList parent, FOEventHandler foEventHandler) throws FOPException {
        return new MarkerPropertyList(this, parent);
    }

    /** @see org.apache.fop.fo.FONode#startOfNode() */
    protected void startOfNode() {
        FOEventHandler foEventHandler = getFOEventHandler(); 
        // Push a new property list maker which will make MarkerPropertyLists.
        savePropertyListMaker = foEventHandler.getPropertyListMaker();
        foEventHandler.setPropertyListMaker(new PropertyListMaker() {
            public PropertyList make(FObj fobj, PropertyList parentPropertyList) {
                PropertyList pList = new MarkerPropertyList(fobj, parentPropertyList);
                descPLists.put(fobj, pList);
                return pList;
            }
        });
    }

    /** @see org.apache.fop.fo.FONode#endOfNode() */
    protected void endOfNode() throws FOPException {
        super.endOfNode();
        // Pop the MarkerPropertyList maker.
        getFOEventHandler().setPropertyListMaker(savePropertyListMaker);
        savePropertyListMaker = null;
        // unparent the child property lists
        Iterator iter = getChildNodes();
        if (iter != null) {
            while (iter.hasNext()) {
                FONode child = (FONode) iter.next();
                MarkerPropertyList pList
                    = (MarkerPropertyList) descPLists.get(child);
                if (pList != null) {
                    pList.setParentPropertyList(null);
                }
            }
        }
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
        throws ValidationException {
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

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "marker";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_MARKER;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" {").append(getMarkerClassName()).append("}");
        return sb.toString();
    }
    
    /**
     * An implementation of PropertyList which only stores the explicit
     * assigned properties. It is memory efficient but slow. 
     */
    public class MarkerPropertyList extends PropertyList {
        HashMap explicit = new HashMap();
        public MarkerPropertyList(FObj fobj, PropertyList parentPropertyList) {
            super(fobj, parentPropertyList);
        }
        
        /**
         * Set the parent property list. Used to assign a new parent 
         * before re-binding all the child elements.   
         */
        public void setParentPropertyList(PropertyList parentPropertyList) {
            this.parentPropertyList = parentPropertyList;
        }

        public void putExplicit(int propId, Property value) {
            explicit.put(new Integer(propId), value);
        }

        public Property getExplicit(int propId) {
            return (Property) explicit.get(new Integer(propId));
        }
    }

}
