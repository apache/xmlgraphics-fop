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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.flow.table.TableCell;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_retrieve-table-marker">
 * <code>fo:retrieve-table-marker</code></a> formatting object.
 */
public class RetrieveTableMarker extends AbstractRetrieveMarker {

    /**
     * Create a new RetrieveTableMarker instance that is
     * a child of the given {@link FONode}.
     *
     * @param parent    the parent {@link FONode}
     */
    public RetrieveTableMarker(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     * <i>NOTE: An <code>fo:retrieve-table-marker</code> is only permitted as a descendant
     * of an <code>fo:table-header</code> or an <code>fo:table-footer</code>.</i>
     */
    public void processNode(
        String elementName, Locator locator, Attributes attlist, PropertyList pList)
        throws FOPException {
        if (findAncestor(FO_TABLE_HEADER) < 0
                && findAncestor(FO_TABLE_FOOTER) < 0) {
            invalidChildError(locator, getParent().getName(), FO_URI, getName(),
                "rule.retrieveTableMarkerDescendantOfHeaderOrFooter");
        } else {
            super.processNode(elementName, locator, attlist, pList);
        }
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        setPosition(pList.get(PR_RETRIEVE_POSITION_WITHIN_TABLE).getEnum());
        setPositionLabel((String) pList.get(PR_RETRIEVE_POSITION_WITHIN_TABLE).getObject());
        setBoundary(pList.get(PR_RETRIEVE_BOUNDARY_WITHIN_TABLE).getEnum());
        setBoundaryLabel((String) pList.get(PR_RETRIEVE_BOUNDARY_WITHIN_TABLE).getObject());
    }

    @Override
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startRetrieveTableMarker(this);
    }

    @Override
    public void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endRetrieveTableMarker(this);
    }

    /**
     * Overridden to flag the ancestor table-cell.
     *
     * @param ancestorID    ID of node name to check for (e.g., FO_ROOT)
     * @return number of levels above FO where ancestor exists,
     *         -1 if not found
     */
    @Override
    protected int findAncestor(int ancestorID) {
        int found = 1;
        FONode temp = getParent();
        while (temp != null) {
            if (temp instanceof TableCell
                    && (ancestorID == FO_TABLE_HEADER || ancestorID == FO_TABLE_FOOTER)) {
                // note that if the retrieve-table-marker is not in a table-header/footer an exception is
                // thrown, so no need to reset this flag in that case
                ((TableCell) temp).flagAsHavingRetrieveTableMarker();
            }
            if (temp.getNameId() == ancestorID) {
                return found;
            }
            found += 1;
            temp = temp.getParent();
        }
        return -1;
    }

    /**
     * Return the value for the <code>retrieve-position-within-table</code>
     * property
     * @return  the value for retrieve-position-within-table; one of
     *              {@link org.apache.fop.fo.Constants#EN_FIRST_STARTING},
     *              {@link org.apache.fop.fo.Constants#EN_FIC},
     *              {@link org.apache.fop.fo.Constants#EN_LAST_STARTING},
     *              {@link org.apache.fop.fo.Constants#EN_LAST_ENDING}.
     */
    public int getRetrievePositionWithinTable() {
        return getPosition();
    }

    /**
     * Return the value for the <code>retrieve-boundary-within-table</code>
     * property
     * @return  the value for retrieve-boundary-within-table; one of
     *              {@link org.apache.fop.fo.Constants#EN_TABLE},
     *              {@link org.apache.fop.fo.Constants#EN_TABLE_FRAGMENT},
     *              {@link org.apache.fop.fo.Constants#EN_PAGE}.
     */
    public int getRetrieveBoundaryWithinTable() {
        return getBoundary();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "retrieve-table-marker";
    }

    /**
     * {@inheritDoc}
     * @return  {@link org.apache.fop.fo.Constants#FO_RETRIEVE_TABLE_MARKER}
     */
    public int getNameId() {
        return FO_RETRIEVE_TABLE_MARKER;
    }

    /** {@inheritDoc} */
    public void clearChildNodes() {
        super.clearChildNodes();
        this.currentTextNode = null;
        this.lastFOTextProcessed = null;
    }

    @Override
    protected void restoreFOEventHandlerState() {
        getFOEventHandler().restoreState(this);
    }

}
