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
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.ValidationPercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.EnumNumber;
import org.apache.fop.fo.properties.EnumProperty;
import org.apache.fop.fo.properties.NumberProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.PropertyMaker;
import org.apache.fop.fo.properties.StructurePointerPropertySet;
import org.apache.fop.layoutmgr.table.CollapsingBorderModel;

/**
 * Common base class for table-related FOs
 */
public abstract class TableFObj extends FObj implements StructurePointerPropertySet {

    private Numeric borderAfterPrecedence;
    private Numeric borderBeforePrecedence;
    private Numeric borderEndPrecedence;
    private Numeric borderStartPrecedence;
    private String ptr;

    ConditionalBorder borderBefore;             // CSOK: VisibilityModifier
    ConditionalBorder borderAfter;              // CSOK: VisibilityModifier
    BorderSpecification borderStart;            // CSOK: VisibilityModifier
    BorderSpecification borderEnd;              // CSOK: VisibilityModifier

    CollapsingBorderModel collapsingBorderModel; // CSOK: VisibilityModifier

    /**
     * Create a TableFObj instance that is a child
     * of the given {@link FONode}
     *
     * @param parent the parent {@link FONode}
     */
    public TableFObj(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE).getNumeric();
        borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE).getNumeric();
        borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE).getNumeric();
        borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE).getNumeric();
        if (getNameId() != FO_TABLE //Separate check for fo:table in Table.java
                && getNameId() != FO_TABLE_CELL
                && getCommonBorderPaddingBackground().hasPadding(
                        ValidationPercentBaseContext.getPseudoContext())) {
            TableEventProducer eventProducer = TableEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.paddingNotApplicable(this, getName(), getLocator());
        }
    }

    /**
     * Return the value for the "border-precedence" property
     * for the given side.
     *
     * @param side  the side for which to return the border precedence
     * @return the "border-precedence" value for the given side
     */
    public Numeric getBorderPrecedence(int side) {
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            return borderBeforePrecedence;
        case CommonBorderPaddingBackground.AFTER:
            return borderAfterPrecedence;
        case CommonBorderPaddingBackground.START:
            return borderStartPrecedence;
        case CommonBorderPaddingBackground.END:
            return borderEndPrecedence;
        default:
            return null;
        }
    }

    /**
     * Convenience method to returns a reference
     * to the base {@link Table} instance.
     *
     * @return  the base table instance
     *
     */
    public Table getTable() {
        // Overridden in Table; for any other Table-node, recursive call to
        // parent.getTable()
        return ((TableFObj) parent).getTable();
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public abstract CommonBorderPaddingBackground getCommonBorderPaddingBackground();

    /**
     * {@link PropertyMaker} subclass for the column-number property
     */
    public static class ColumnNumberPropertyMaker extends PropertyMaker {

        /**
         * Constructor
         *
         * @param propId    the id of the property for which the maker should
         *                  be created
         */
        public ColumnNumberPropertyMaker(int propId) {
            super(propId);
        }

        /** {@inheritDoc} */
        public Property make(PropertyList propertyList)
                throws PropertyException {
            FObj fo = propertyList.getFObj();

            return NumberProperty.getInstance(((ColumnNumberManagerHolder) fo.getParent())
                    .getColumnNumberManager().getCurrentColumnNumber());
        }


        /**
         * {@inheritDoc}
         * Check the value of the column-number property.
         */
        public Property make(PropertyList propertyList, String value, FObj fo)
                    throws PropertyException {

            Property p = super.make(propertyList, value, fo);

            int columnIndex = p.getNumeric().getValue();
            int colSpan = propertyList.get(Constants.PR_NUMBER_COLUMNS_SPANNED)
                                .getNumeric().getValue();

            // only check whether the column-number is occupied in case it was
            // specified on a fo:table-cell or fo:table-column
            int foId = propertyList.getFObj().getNameId();
            if (foId == FO_TABLE_COLUMN || foId == FO_TABLE_CELL) {
                ColumnNumberManagerHolder parent
                        = (ColumnNumberManagerHolder) propertyList.getParentFObj();
                ColumnNumberManager columnIndexManager = parent.getColumnNumberManager();
                int lastIndex = columnIndex - 1 + colSpan;
                for (int i = columnIndex; i <= lastIndex; ++i) {
                    if (columnIndexManager.isColumnNumberUsed(i)) {
                        /* if column-number is already in use by another
                         * cell/column => error!
                         */
                        TableEventProducer eventProducer
                                = TableEventProducer.Provider.get(
                                    fo.getUserAgent().getEventBroadcaster());
                        eventProducer.cellOverlap(
                                this, propertyList.getFObj().getName(),
                                i, propertyList.getFObj().getLocator());
                    }
                }
            }
            return p;
        }

        /**
         * {@inheritDoc}
         * If the value is not positive, return a property whose value
         * is the next column number.
         */
        public Property convertProperty(Property p,
                                        PropertyList propertyList, FObj fo)
                    throws PropertyException {
            if (p instanceof EnumProperty) {
                return EnumNumber.getInstance(p);
            }
            Number val = p.getNumber();
            if (val != null) {
                int i = Math.round(val.floatValue());
                int foId = propertyList.getFObj().getNameId();
                if (i <= 0) {
                    if (foId == FO_TABLE_CELL || foId == FO_TABLE_COLUMN) {
                        ColumnNumberManagerHolder parent
                            = (ColumnNumberManagerHolder) propertyList.getParentFObj();
                        ColumnNumberManager columnIndexManager
                            = parent.getColumnNumberManager();
                        i = columnIndexManager.getCurrentColumnNumber();
                    } else {
                        /* very exceptional case:
                         * negative column-number specified on
                         * a FO that is not a fo:table-cell or fo:table-column
                         */
                        i = 1;
                    }
                    TableEventProducer eventProducer = TableEventProducer.Provider.get(
                            fo.getUserAgent().getEventBroadcaster());
                    eventProducer.forceNextColumnNumber(this, propertyList.getFObj().getName(),
                            val, i, propertyList.getFObj().getLocator());
                }
                return NumberProperty.getInstance(i);
            }
            return convertPropertyDatatype(p, propertyList, fo);
        }

    }

    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator, Attributes attlist,
            PropertyList pList) throws FOPException {
        super.processNode(elementName, locator, attlist, pList);
        Table table = getTable();
        if (!inMarker() && !table.isSeparateBorderModel()) {
            collapsingBorderModel = CollapsingBorderModel.getBorderModelFor(table
                    .getBorderCollapse());
            setCollapsedBorders();
        }
    }

    @Override
    public void setPtr(String ptr) {
        this.ptr = ptr;
    }

    /** {@inheritDoc} */
    public String getPtr() {
        return ptr;
    }

    /**
     * Prepares the borders of this element if the collapsing-border model is in use.
     * Conflict resolution with parent elements is done where applicable.
     */
    protected void setCollapsedBorders() {
        createBorder(CommonBorderPaddingBackground.START);
        createBorder(CommonBorderPaddingBackground.END);
        createBorder(CommonBorderPaddingBackground.BEFORE);
        createBorder(CommonBorderPaddingBackground.AFTER);
    }

    /**
     * Creates a BorderSpecification from the border set on the given side. If no border
     * is set, a BorderSpecification with border-style none is created.
     *
     * @param side one of CommonBorderPaddingBackground.BEFORE|AFTER|START|END
     */
    private void createBorder(int side) {
        BorderSpecification borderSpec = new BorderSpecification(
                getCommonBorderPaddingBackground().getBorderInfo(side), getNameId());
        switch (side) {
        case CommonBorderPaddingBackground.BEFORE:
            borderBefore = new ConditionalBorder(borderSpec, collapsingBorderModel);
            break;
        case CommonBorderPaddingBackground.AFTER:
            borderAfter = new ConditionalBorder(borderSpec, collapsingBorderModel);
            break;
        case CommonBorderPaddingBackground.START:
            borderStart = borderSpec;
            break;
        case CommonBorderPaddingBackground.END:
            borderEnd = borderSpec;
            break;
        default: assert false;
        }
    }
}
