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

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.flow.table.TableFObj;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.apps.FOPException;

import java.util.Iterator;

/**
 * Abstract base class for the <a href="http://www.w3.org/TR/xsl/#fo_retrieve-marker">
 * <code>fo:retrieve-marker</code></a> and
 * <a href="http://www.w3.org/TR/xsl/#fo_retrieve-table-marker">
 * <code>fo:retrieve-table-marker</code></a> formatting objects.

 */
public abstract class AbstractRetrieveMarker extends FObjMixed {

    private PropertyList propertyList;

    /**
     * Create a new AbstractRetrieveMarker instance that
     * is a child of the given {@link FONode}
     *
     * @param parent    the parent {@link FONode}
     */
    public AbstractRetrieveMarker(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     * Store a reference to the parent {@link PropertyList}
     * to be used when the retrieve-marker is resolved.
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        this.propertyList = pList.getParentPropertyList();
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
     * @param parentPropertyList the parent PropertyList
     * @throws FOPException in case there was an error
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
                getFOValidationEventProducer().markerCloningFailed(this,
                        marker.getMarkerClassName(), exc, getLocator());
            }
        } else if (log.isDebugEnabled()) {
            log.debug("Empty marker retrieved...");
        }
    }

}
