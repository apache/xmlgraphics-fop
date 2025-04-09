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

package org.apache.fop.fo.flow.table;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_table-and-caption">
 * <code>fo:table-and-caption</code></a> property.
 * TODO needs implementation
 */
public class TableAndCaption extends FObj implements CommonAccessibilityHolder {

    private static final String TABLE_CAPTION = "fo:table-caption";
    private static final String TABLE = "fo:table";
    private CommonAccessibility commonAccessibility;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;

    /** used for FO validation */
    private boolean tableCaptionFound;
    private boolean tableFound;

    /**
     * Create a TableAndCaption instance with the given {@link FONode}
     * as parent.
     * @param parent FONode that is the parent of this object
     */
    public TableAndCaption(FONode parent) {
        super(parent);
    }

    @Override
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * {@inheritDoc}
     */
    public void endOfNode() throws FOPException {
        if (!tableFound) {
            missingChildElementError("marker* table-caption? table");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* table-caption? table
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {

        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (tableCaptionFound) {
                    nodesOutOfOrderError(loc, "fo:marker", TABLE_CAPTION);
                } else if (tableFound) {
                    nodesOutOfOrderError(loc, "fo:marker", TABLE);
                }
            } else if (localName.equals("table-caption")) {
                if (tableCaptionFound) {
                    tooManyNodesError(loc, TABLE_CAPTION);
                } else if (tableFound) {
                    nodesOutOfOrderError(loc, TABLE_CAPTION, TABLE);
                } else {
                    tableCaptionFound = true;
                }
            } else if (localName.equals("table")) {
                if (tableFound) {
                    tooManyNodesError(loc, TABLE);
                } else {
                    tableFound = true;
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-and-caption";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_TABLE_AND_CAPTION}
     */
    public int getNameId() {
        return FO_TABLE_AND_CAPTION;
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }
}

