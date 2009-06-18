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

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

import org.xml.sax.Locator;

import org.apache.commons.logging.Log;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;


/**
 * The retrieve-marker formatting object.
 * This will create a layout manager that will retrieve
 * a marker based on the information.
 */
public class RetrieveMarker extends FObj {
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
        if (findAncestor(FO_STATIC_CONTENT) < 0) {
            invalidChildError(locator, FO_URI, "retrieve-marker", 
                "An fo:retrieve-marker is permitted only as the " +
                " descendant of an fo:static-content.");
        }

        retrieveClassName = pList.get(PR_RETRIEVE_CLASS_NAME).getString();
        retrievePosition = pList.get(PR_RETRIEVE_POSITION).getEnum();
        retrieveBoundary = pList.get(PR_RETRIEVE_BOUNDARY).getEnum();
        
        if (retrieveClassName == null || retrieveClassName.equals("")) {
            missingPropertyError("retrieve-class-name");
        }        
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

    /**
     * Clone the FO nodes in the parent iterator,
     * attach the new nodes to the new parent,
     * and map the new nodes to the existing property lists.
     * FOText nodes are also in the new map, with a null value.
     * Clone the subtree by a recursive call to this method.
     * @param parentIter the iterator over the children of the old parent
     * @param newParent the new parent for the cloned nodes
     * @param marker the marker that contains the old property list mapping
     * @param descPLists the map of the new nodes to property lists
     */
    private void cloneSubtree(Iterator parentIter, FONode newParent,
                              Marker marker, Map descPLists)
        throws FOPException {
        if (parentIter == null) return;
        while (parentIter.hasNext()) {
            FONode child = (FONode) parentIter.next();
            FONode newChild = child.clone(newParent, true);
            descPLists.put(newChild, marker.getPList(child));
            cloneSubtree(child.getChildNodes(), newChild, marker, descPLists);
        }
    }

    /**
     * Clone the subtree of marker,
     * and attach the new subtree to this node.
     * The property lists are not cloned;
     * the existing property lists of the direct children
     * are reparented to the property list of this node.
     * @param marker the marker that is to be cloned
     * @param descPLists the map of the new nodes to property lists
     */
    private void cloneFromMarker(Marker marker, Map descPLists)
        throws FOPException {
        // release child nodes from a possible earlier layout
        childNodes = new ArrayList();
        Iterator markerIter = marker.getChildNodes();
        cloneSubtree(markerIter, this, marker, descPLists);
        // reparent the property lists of the direct children
        for (Iterator iter = getChildNodes(); iter.hasNext(); ) {
            FONode child = (FONode) iter.next();
            Marker.MarkerPropertyList pList
                = (Marker.MarkerPropertyList) descPLists.get(child);
            if (pList != null) {
                pList.setParentPropertyList(propertyList);
            }
        }
    }

    /**
     * Bind the new nodes to the property values in this context
     * @param descPLists the map of the new nodes to property lists
     */
    private void bindChildren(Map descPLists) throws FOPException {
        for (Iterator i = descPLists.keySet().iterator(); i.hasNext(); ) {
            FONode desc = (FONode) i.next();
            PropertyList descPList;
            if (desc instanceof FObj) {
                descPList = (PropertyList) descPLists.get(desc);
                ((FObj) desc).bind(descPList);
            } else if (desc instanceof FOText) {
                descPList = (PropertyList) descPLists.get(desc.getParent());
                if (descPList == null) {
                    descPList = propertyList;
                }
                ((FOText) desc).bind(descPList);
            }
        }
    }

    /**
     * Clone the subtree of marker
     * and bind the nodes to the property values in this context.
     * The property lists are not cloned,
     * but the subtree is attached to the property list of this node.
     * This is only needed for the binding of the FO nodes.
     * After that a subsequent retrieve-marker
     * may reparent the property lists.
     * @param marker the marker that is to be cloned
     */
    public void bindMarker(Marker marker) {
        // assert(marker != null);
        // catch empty marker
        if (marker.getChildNodes() == null) {
            return;
        }
        HashMap descPLists = new HashMap();
        try {
            cloneFromMarker(marker, descPLists);
        } catch (FOPException exc) {
            Log log = getLogger();
            log.error("fo:retrieve-marker unable to clone subtree of fo:marker", exc);
            return;
        }
        try {
            bindChildren(descPLists);
        } catch (FOPException exc) {
            Log log = getLogger();
            log.error("fo:retrieve-marker unable to rebind property values", exc);
        }
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "retrieve-marker";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_RETRIEVE_MARKER;
    }
}
