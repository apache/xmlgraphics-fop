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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.complexscripts.bidi.DelimitedTextRange;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.ValidationPercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthPairProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.TableColLength;
import org.apache.fop.traits.Direction;
import org.apache.fop.traits.WritingMode;
import org.apache.fop.traits.WritingModeTraits;
import org.apache.fop.traits.WritingModeTraitsGetter;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_table">
 * <code>fo:table</code></a> object.
 */
public class Table extends TableFObj implements ColumnNumberManagerHolder, BreakPropertySet, WritingModeTraitsGetter,
        CommonAccessibilityHolder {

    // The value of FO traits (refined properties) that apply to fo:table.
    private CommonAccessibility commonAccessibility;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private LengthRangeProperty blockProgressionDimension;
    private int borderCollapse;
    private LengthPairProperty borderSeparation;
    private int breakAfter;
    private int breakBefore;
    private LengthRangeProperty inlineProgressionDimension;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int tableLayout;
    private int tableOmitFooterAtBreak;
    private int tableOmitHeaderAtBreak;
    private WritingModeTraits writingModeTraits;
    // Unused but valid items, commented out for performance:
    //     private CommonAural commonAural;
    //     private CommonRelativePosition commonRelativePosition;
    //     private int intrusionDisplace;
    // End of FO trait values

    /** extension properties */
    private Length widowContentLimit;
    private Length orphanContentLimit;

    /** collection of columns in this table */
    private List columns = new ArrayList();

    private ColumnNumberManager columnNumberManager = new ColumnNumberManager();

    /** the table-header and -footer */
    private TableHeader tableHeader = null;
    private TableFooter tableFooter = null;

    /** used for validation */
    private boolean tableColumnFound = false;
    private boolean tableHeaderFound = false;
    private boolean tableFooterFound = false;
    private boolean tableBodyFound = false;

    private boolean hasExplicitColumns = false;
    private boolean columnsFinalized = false;
    private RowGroupBuilder rowGroupBuilder;

    /**
     * The table's property list. Used in case the table has
     * no explicit columns, as a parent property list to
     * internally generated TableColumns
     */
    private PropertyList propList;

    /**
     * Construct a Table instance with the given {@link FONode}
     * as parent.
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public Table(FONode parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    public void bind(PropertyList pList) throws FOPException {
        super.bind(pList);
        commonAccessibility = CommonAccessibility.getInstance(pList);
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        borderCollapse = pList.get(PR_BORDER_COLLAPSE).getEnum();
        borderSeparation = pList.get(PR_BORDER_SEPARATION).getLengthPair();
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        tableLayout = pList.get(PR_TABLE_LAYOUT).getEnum();
        tableOmitFooterAtBreak = pList.get(PR_TABLE_OMIT_FOOTER_AT_BREAK).getEnum();
        tableOmitHeaderAtBreak = pList.get(PR_TABLE_OMIT_HEADER_AT_BREAK).getEnum();
        writingModeTraits = new WritingModeTraits(
            WritingMode.valueOf(pList.get(PR_WRITING_MODE).getEnum()),
            pList.getExplicit(PR_WRITING_MODE) != null);

        //Bind extension properties
        widowContentLimit = pList.get(PR_X_WIDOW_CONTENT_LIMIT).getLength();
        orphanContentLimit = pList.get(PR_X_ORPHAN_CONTENT_LIMIT).getLength();

        if (!blockProgressionDimension.getOptimum(null).isAuto()) {
            TableEventProducer eventProducer = TableEventProducer.Provider.get(
                    getUserAgent().getEventBroadcaster());
            eventProducer.nonAutoBPDOnTable(this, getLocator());
            // Anyway, the bpd of a table is not used by the layout code
        }
        if (tableLayout == EN_AUTO) {
            getFOValidationEventProducer().unimplementedFeature(this, getName(),
                    "table-layout=\"auto\"", getLocator());
        }
        if (!isSeparateBorderModel()) {
            if (borderCollapse == EN_COLLAPSE_WITH_PRECEDENCE) {
                getFOValidationEventProducer().unimplementedFeature(this, getName(),
                    "border-collapse=\"collapse-with-precedence\"; defaulting to \"collapse\"", getLocator());
                borderCollapse = EN_COLLAPSE;
            }
            if (getCommonBorderPaddingBackground().hasPadding(
                            ValidationPercentBaseContext.getPseudoContext())) {
                //See "17.6.2 The collapsing border model" in CSS2
                TableEventProducer eventProducer = TableEventProducer.Provider.get(
                        getUserAgent().getEventBroadcaster());
                eventProducer.noTablePaddingWithCollapsingBorderModel(this, getLocator());
            }
        }

        /* Store reference to the property list, so
         * new lists can be created in case the table has no
         * explicit columns
         * (see addDefaultColumn())
         */
        this.propList = pList;
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        getFOEventHandler().startTable(this);
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (marker*,table-column*,table-header?,table-footer?,table-body+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if ("marker".equals(localName)) {
                if (tableColumnFound || tableHeaderFound || tableFooterFound
                        || tableBodyFound) {
                   nodesOutOfOrderError(loc, "fo:marker",
                       "(table-column*,table-header?,table-footer?,table-body+)");
                }
            } else if ("table-column".equals(localName)) {
                tableColumnFound = true;
                if (tableHeaderFound || tableFooterFound || tableBodyFound) {
                    nodesOutOfOrderError(loc, "fo:table-column",
                        "(table-header?,table-footer?,table-body+)");
                }
            } else if ("table-header".equals(localName)) {
                if (tableHeaderFound) {
                    tooManyNodesError(loc, "table-header");
                } else {
                    tableHeaderFound = true;
                    if (tableFooterFound || tableBodyFound) {
                        nodesOutOfOrderError(loc, "fo:table-header",
                            "(table-footer?,table-body+)");
                    }
                }
            } else if ("table-footer".equals(localName)) {
                if (tableFooterFound) {
                    tooManyNodesError(loc, "table-footer");
                } else {
                    tableFooterFound = true;
                    if (tableBodyFound) {
                        if (getUserAgent().validateStrictly()) {
                            nodesOutOfOrderError(loc, "fo:table-footer", "(table-body+)", true);
                        }
                        if (!isSeparateBorderModel()) {
                            TableEventProducer eventProducer = TableEventProducer.Provider.get(
                                    getUserAgent().getEventBroadcaster());
                            eventProducer.footerOrderCannotRecover(this, getName(), getLocator());
                        }
                    }
                }
            } else if ("table-body".equals(localName)) {
                tableBodyFound = true;
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    public void endOfNode() throws FOPException {
        super.endOfNode();
        getFOEventHandler().endTable(this);
    }

    /** {@inheritDoc} */
    public void finalizeNode() throws FOPException {

        if (!tableBodyFound) {
           missingChildElementError(
                   "(marker*,table-column*,table-header?,table-footer?"
                       + ",table-body+)");
        }
        if (!hasChildren()) {
            getParent().removeChild(this);
            return;
        }
        if (!inMarker()) {
            rowGroupBuilder.endTable();
            /* clean up */
            for (int i = columns.size(); --i >= 0;) {
                TableColumn col = (TableColumn) columns.get(i);
                if (col != null) {
                    col.releasePropertyList();
                }
            }
            this.propList = null;
            rowGroupBuilder = null;
        }

    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode child) throws FOPException {

        int childId = child.getNameId();

        switch (childId) {
        case FO_TABLE_COLUMN:
            hasExplicitColumns = true;
            if (!inMarker()) {
                addColumnNode((TableColumn) child);
            } else {
                columns.add(child);
            }
            break;
        case FO_TABLE_HEADER:
        case FO_TABLE_FOOTER:
        case FO_TABLE_BODY:
            if (!inMarker() && !columnsFinalized) {
                columnsFinalized = true;
                if (hasExplicitColumns) {
                    finalizeColumns();
                    rowGroupBuilder = new FixedColRowGroupBuilder(this);
                } else {
                    rowGroupBuilder = new VariableColRowGroupBuilder(this);
                }

            }
            switch (childId) {
            case FO_TABLE_FOOTER:
                tableFooter = (TableFooter) child;
                break;
            case FO_TABLE_HEADER:
                tableHeader = (TableHeader) child;
                break;
            default:
                super.addChildNode(child);
            }
            break;
        default:
            super.addChildNode(child);
        }
    }

    private void finalizeColumns() throws FOPException {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i) == null) {
                columns.set(i, createImplicitColumn(i + 1));
            }
        }
    }

    /** {@inheritDoc} */
    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /** {@inheritDoc} */
    public Table getTable() {
        return this;
    }

    /**
     * Creates the appropriate number of additional implicit columns to match the given
     * column number. Used when the table has no explicit column: the number of columns is
     * then determined by the row that has the most columns.
     *
     * @param columnNumber the table must at least have this number of column
     * @throws FOPException if there was an error creating the property list for implicit
     * columns
     */
    void ensureColumnNumber(int columnNumber) throws FOPException {
        assert !hasExplicitColumns;
        for (int i = columns.size() + 1; i <= columnNumber; i++) {
            columns.add(createImplicitColumn(i));
        }
    }

    private TableColumn createImplicitColumn(int colNumber)
                    throws FOPException {
        TableColumn implicitColumn = new TableColumn(this, true);
        PropertyList pList = new StaticPropertyList(
                                implicitColumn, this.propList);
        implicitColumn.bind(pList);
        implicitColumn.setColumnWidth(new TableColLength(1.0, implicitColumn));
        implicitColumn.setColumnNumber(colNumber);
        if (!isSeparateBorderModel()) {
            implicitColumn.setCollapsedBorders(collapsingBorderModel); // TODO
        }
        return implicitColumn;
    }

    /**
     * Adds a column to the columns List, and updates the columnIndex
     * used for determining initial values for column-number
     *
     * @param col   the column to add
     */
    private void addColumnNode(TableColumn col) {

        int colNumber = col.getColumnNumber();
        int colRepeat = col.getNumberColumnsRepeated();

        /* add nulls for non-occupied indices between
         * the last column up to and including the current one
         */
        while (columns.size() < colNumber + colRepeat - 1) {
            columns.add(null);
        }

        // in case column is repeated:
        // for the time being, add the same column
        // (colRepeat - 1) times to the columns list
        // TODO: need to force the column-number (?)
        for (int i = colNumber - 1; i < colNumber + colRepeat - 1; i++) {
            columns.set(i, col);
        }

        columnNumberManager.signalUsedColumnNumbers(colNumber, colNumber + colRepeat - 1);
    }

    boolean hasExplicitColumns() {
        return hasExplicitColumns;
    }

    /** @return true of table-layout="auto" */
    public boolean isAutoLayout() {
        return (tableLayout == EN_AUTO);
    }

    /**
     *  Returns the list of table-column elements.
     *
     * @return a list of {@link TableColumn} elements, may contain null elements
     */
    public List getColumns() {
        return columns;
    }

    /**
     * Returns the column at the given index.
     *
     * @param index index of the column to be retrieved, 0-based
     * @return the corresponding column (may be an implicitly created column)
     */
    public TableColumn getColumn(int index) {
        return (TableColumn) columns.get(index);
    }

    /**
     * Returns the number of columns of this table.
     *
     * @return the number of columns, implicit or explicit, in this table
     */
    public int getNumberOfColumns() {
        return columns.size();
    }

    /** @return the body for the table-header. */
    public TableHeader getTableHeader() {
        return tableHeader;
    }

    /** @return the body for the table-footer. */
    public TableFooter getTableFooter() {
        return tableFooter;
    }

    /** @return true if the table-header should be omitted at breaks */
    public boolean omitHeaderAtBreak() {
        return (this.tableOmitHeaderAtBreak == EN_TRUE);
    }

    /** @return true if the table-footer should be omitted at breaks */
    public boolean omitFooterAtBreak() {
        return (this.tableOmitFooterAtBreak == EN_TRUE);
    }

    /**
     * @return the "inline-progression-dimension" FO trait.
     */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /**
     * @return the "block-progression-dimension" FO trait.
     */
    public LengthRangeProperty getBlockProgressionDimension() {
        return blockProgressionDimension;
    }

    /**
     * @return the Common Margin Properties-Block.
     */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /**
     * @return the Common Border, Padding, and Background Properties.
     */
    public CommonBorderPaddingBackground getCommonBorderPaddingBackground() {
        return commonBorderPaddingBackground;
    }

    /** @return the "break-after" FO trait. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" FO trait. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-next" FO trait.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" FO trait.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" FO trait.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /**
     * Convenience method to check if a keep-together constraint is specified.
     * @return true if keep-together is active.
     */
    public boolean mustKeepTogether() {
        return !getKeepTogether().getWithinPage().isAuto()
                || !getKeepTogether().getWithinColumn().isAuto();
    }

    /** @return the "border-collapse" FO trait. */
    public int getBorderCollapse() {
        return borderCollapse;
    }

    /** @return true if the separate border model is active */
    public boolean isSeparateBorderModel() {
        return (getBorderCollapse() == EN_SEPARATE);
    }

    /** @return the "border-separation" FO trait. */
    public LengthPairProperty getBorderSeparation() {
        return borderSeparation;
    }

    /** {@inheritDoc} */
    public Direction getInlineProgressionDirection() {
        return writingModeTraits.getInlineProgressionDirection();
    }

    /** {@inheritDoc} */
    public Direction getBlockProgressionDirection() {
        return writingModeTraits.getBlockProgressionDirection();
    }

    /** {@inheritDoc} */
    public Direction getColumnProgressionDirection() {
        return writingModeTraits.getColumnProgressionDirection();
    }

    /** {@inheritDoc} */
    public Direction getRowProgressionDirection() {
        return writingModeTraits.getRowProgressionDirection();
    }

    /** {@inheritDoc} */
    public Direction getShiftDirection() {
        return writingModeTraits.getShiftDirection();
    }

    /** {@inheritDoc} */
    public WritingMode getWritingMode() {
        return writingModeTraits.getWritingMode();
    }

    /** {@inheritDoc} */
    public boolean getExplicitWritingMode() {
        return writingModeTraits.getExplicitWritingMode();
    }

    /** @return the "fox:widow-content-limit" extension FO trait */
    public Length getWidowContentLimit() {
        return widowContentLimit;
    }

    /** @return the "fox:orphan-content-limit" extension FO trait */
    public Length getOrphanContentLimit() {
        return orphanContentLimit;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "table";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_TABLE}
     */
    public int getNameId() {
        return FO_TABLE;
    }

    /** {@inheritDoc} */
    public FONode clone(FONode parent, boolean removeChildren)
        throws FOPException {
        Table clone = (Table) super.clone(parent, removeChildren);
        if (removeChildren) {
            clone.columns = new ArrayList();
            clone.columnsFinalized = false;
            clone.columnNumberManager = new ColumnNumberManager();
            clone.tableHeader = null;
            clone.tableFooter = null;
            clone.rowGroupBuilder = null;
        }
        return clone;
    }

    /** {@inheritDoc} */
    public ColumnNumberManager getColumnNumberManager() {
        return columnNumberManager;
    }

    RowGroupBuilder getRowGroupBuilder() {
        return rowGroupBuilder;
    }

    @Override
    protected Stack collectDelimitedTextRanges(Stack ranges, DelimitedTextRange currentRange) {
        // header sub-tree
        TableHeader header = getTableHeader();
        if (header != null) {
            ranges = header.collectDelimitedTextRanges(ranges);
        }
        // footer sub-tree
        TableFooter footer = getTableFooter();
        if (footer != null) {
            ranges = footer.collectDelimitedTextRanges(ranges);
        }
        // body sub-tree
        for (Iterator it = getChildNodes(); (it != null) && it.hasNext();) {
            ranges = ((FONode) it.next()).collectDelimitedTextRanges(ranges);
        }
        return ranges;
    }

    @Override
    protected boolean isBidiBoundary(boolean propagate) {
        return getExplicitWritingMode();
    }

}
