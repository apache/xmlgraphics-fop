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

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class modelling the fo:table-row object.
 */
public class TableRow extends TableCellContainer {
    // The value of properties relevant for fo:table-row.
    private LengthRangeProperty blockProgressionDimension;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private int breakAfter;
    private int breakBefore;
    private Length height;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    // Unused but valid items, commented out for performance:
    //     private CommonAccessibility commonAccessibility;
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int visibility;
    // End of property values

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableRow(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        blockProgressionDimension
            = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        height = pList.get(PR_HEIGHT).getLength();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        super.bind(pList);
    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
            Attributes attlist, PropertyList pList) throws FOPException {
        if (!inMarker()) {
            TableBody body = (TableBody) parent;
            pendingSpans = body.pendingSpans;
            columnNumberManager = body.columnNumberManager;
        }
        super.processNode(elementName, locator, attlist, pList);
    }

    /**
     * {@inheritDoc}
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (!inMarker()) {
            TableCell cell = (TableCell) child;
            TableBody body = (TableBody) getParent();
            addTableCellChild(cell, body.isFirst(this));
        }
        super.addChildNode(child);
    }

    /**
     * {@inheritDoc}
     */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startRow(this);
    }

    /**
     * {@inheritDoc}
     */
    public void endOfNode() throws FOPException {
        if (firstChild == null) {
            missingChildElementError("(table-cell+)");
        }
        if (!inMarker()) {
            pendingSpans = null;
            columnNumberManager = null;
        }
        getFOEventHandler().endRow(this);
    }

    /**
     * {@inheritDoc} String, String)
     * XSL Content Model: (table-cell+)
     */
    protected void validateChildNode(Locator loc, String nsURI,
                                     String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (!localName.equals("table-cell")) {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    TableBody getTablePart() {
        return (TableBody) parent;
    }

    /** {@inheritDoc} */
    boolean isTableRow() {
        return true;
    }

    /** {inheritDoc} */
    protected void setCollapsedBorders() {
        TableBody body = (TableBody) parent;
        createBorder(CommonBorderPaddingBackground.START, body);
        createBorder(CommonBorderPaddingBackground.END, body);
        createBorder(CommonBorderPaddingBackground.BEFORE);
        createBorder(CommonBorderPaddingBackground.AFTER);
    }

    /** @return the "break-after" property. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-previous" property. */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-with-next" property. */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-together" property. */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /**
     * Convenience method to check if a keep-together
     * constraint is specified.
     * @return true if keep-together is active.
     */
    public boolean mustKeepTogether() {
        return !getKeepTogether().getWithinPage().isAuto()
                || !getKeepTogether().getWithinColumn().isAuto();
    }

    /**
     * Convenience method to check if a keep-with-next
     * constraint is specified.
     * @return true if keep-with-next is active.
     */
    public boolean mustKeepWithNext() {
        return !getKeepWithNext().getWithinPage().isAuto()
                || !getKeepWithNext().getWithinColumn().isAuto();
    }

    /**
     * Convenience method to check if a keep-with-previous
     * constraint is specified.
     * @return true if keep-with-previous is active.
     */
    public boolean mustKeepWithPrevious() {
        return !getKeepWithPrevious().getWithinPage().isAuto()
                || !getKeepWithPrevious().getWithinColumn().isAuto();
    }

    /**
     * @return the "block-progression-dimension" property.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * @return the "height" property.
     */
    public Length getHeight() {
        return height;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table-row";
    }

    /** {@inheritDoc} */
    public int getNameId() {
        return FO_TABLE_ROW;
    }
}
