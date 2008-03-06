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

package org.apache.fop.fo.flow;

import java.util.Iterator;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableFObj;

/**
 * Class modelling the fo:retrieve-marker object.
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
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        if (findAncestor(FO_STATIC_CONTENT) < 0) {
            invalidChildError(locator, getParent().getName(), FO_URI, getName(), 
                "rule.retrieveMarkerDescendatOfStaticContent");
        }

        retrieveClassName = pList.get(PR_RETRIEVE_CLASS_NAME).getString();
        retrievePosition = pList.get(PR_RETRIEVE_POSITION).getEnum();
        retrieveBoundary = pList.get(PR_RETRIEVE_BOUNDARY).getEnum();
        
        if (retrieveClassName == null || retrieveClassName.equals("")) {
            missingPropertyError("retrieve-class-name");
        }
        
        propertyList = pList.getParentPropertyList();
    }
    
    /**
     * {@inheritDoc}
     * XSL Content Model: empty
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
            invalidChildError(loc, nsURI, localName);
    }

    /**
     * @return the "retrieve-class-name" property.
     */
    public String getRetrieveClassName() {
        return retrieveClassName;
    }

    /**
     * @return the "retrieve-position" property (enum value).
     */
    public int getRetrievePosition() {
        return retrievePosition;
    }

    /**
     * @return the "retrieve-boundary" property (enum value).
     */
    public int getRetrieveBoundary() {
        return retrieveBoundary;
    }
    
    private PropertyList createPropertyListFor(FObj fo, PropertyList parent) {
        return getFOEventHandler().getPropertyListMaker().make(fo, parent);
    }

    private void cloneSingleNode(FONode child, FONode newParent,
                            Marker marker, PropertyList parentPropertyList)
        throws FOPException {

        if (child != null) {
            FONode newChild = child.clone(newParent, true);
            if (child instanceof FObj) {
                Marker.MarkerPropertyList pList;
                PropertyList newPropertyList = createPropertyListFor(
                            (FObj) newChild, parentPropertyList);
                
                pList = marker.getPropertyListFor(child);
                newChild.processNode(
                        child.getLocalName(),
                        getLocator(),
                        pList,
                        newPropertyList);
                if (newChild instanceof TableFObj) {
                    // TODO calling startOfNode (and endOfNode, below) on other fobjs may
                    // have undesirable side-effects. This is really ugly and will need to
                    // be addressed sooner or later
                    ((TableFObj) newChild).startOfNode();
                }
                addChildTo(newChild, (FObj) newParent);
                if (newChild.getNameId() == FO_TABLE) {
                    Table t = (Table) child;
                    cloneSubtree(t.getColumns().listIterator(),
                            newChild, marker, newPropertyList);
                    cloneSingleNode(t.getTableHeader(),
                            newChild, marker, newPropertyList);
                    cloneSingleNode(t.getTableFooter(),
                            newChild, marker, newPropertyList);
                }
                cloneSubtree(child.getChildNodes(), newChild,
                        marker, newPropertyList);
                if (newChild instanceof TableFObj) {
                    // TODO this is ugly
                    ((TableFObj) newChild).endOfNode();
                }
            } else if (child instanceof FOText) {
                FOText ft = (FOText) newChild;
                ft.bind(parentPropertyList);
                addChildTo(newChild, (FObj) newParent);
            }
            if (newChild instanceof FObjMixed) {
                handleWhiteSpaceFor((FObjMixed) newChild);
            }
        }
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
                              Marker marker, PropertyList parentPropertyList)
        throws FOPException {
        if (parentIter != null) {
            FONode child;
            while (parentIter.hasNext()) {
                child = (FONode) parentIter.next();
                cloneSingleNode(child, newParent, 
                        marker, parentPropertyList);
            }
        }
    }

    private void cloneFromMarker(Marker marker)
        throws FOPException {
        // clean up remnants from a possible earlier layout
        if (firstChild != null) {
            currentTextNode = null;
            firstChild = null;
        }
        cloneSubtree(marker.getChildNodes(), this, 
                        marker, propertyList);
        handleWhiteSpaceFor(this);
    }

    /**
     * Clone the subtree of the given marker
     * 
     * @param marker the marker that is to be cloned
     */
    public void bindMarker(Marker marker) {
        if (marker.getChildNodes() != null) {
            try {
                cloneFromMarker(marker);
            } catch (FOPException exc) {
                log.error("fo:retrieve-marker unable to clone "
                        + "subtree of fo:marker (marker-class-name="
                        + marker.getMarkerClassName() + ")", exc);
                return;
            }
        } else if (log.isInfoEnabled()) {
            log.info("Empty marker retrieved...");
        }
        return;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "retrieve-marker";
    }

    /**
     * {@inheritDoc}
     */
    public int getNameId() {
        return FO_RETRIEVE_MARKER;
    }    
}