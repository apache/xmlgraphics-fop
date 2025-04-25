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
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.Property;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_table-caption">
 * <code>fo:table-caption</code></a> object.
 */
public class TableCaption extends FObj implements CommonAccessibilityHolder {

    private CommonAccessibility commonAccessibility;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;

    /** used for FO validation */
    private boolean blockItemFound;

    /**
     * Create a TableCaption instance with the given {@link FONode}
     * as parent.
     * @param parent {@link FONode} that is the parent of this object
     */
    public TableCaption(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        Property keepWithNextProp = pList.get(PR_KEEP_WITH_NEXT);
        if (keepWithNextProp instanceof KeepProperty) {
            ((KeepProperty)keepWithNextProp).setWithinPage(
                    EnumProperty.getInstance(Constants.EN_ALWAYS, "ALWAYS"), true);
        }
        keepWithNext = keepWithNextProp.getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
    }

    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startTableCaption(this);
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("marker* (%block;)");
        }
        getFOEventHandler().endTableCaption(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: marker* (%block;)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("marker")) {
                if (blockItemFound) {
                   nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
                }
            } else if (!isBlockItem(nsURI, localName)) {
                invalidChildError(loc, nsURI, localName);
            } else {
                blockItemFound = true;
            }
        }
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-caption";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_TABLE_CAPTION}
     */
    public int getNameId() {
        return FO_TABLE_CAPTION;
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
}

