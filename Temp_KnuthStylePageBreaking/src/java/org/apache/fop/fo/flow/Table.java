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

import org.xml.sax.Locator;

import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.StaticPropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAural;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.CommonRelativePosition;
import org.apache.fop.fo.properties.KeepProperty;
import org.apache.fop.fo.properties.LengthPairProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class modelling the fo:table object.
 */
public class Table extends FObj {
    // The value of properties relevant for fo:table.
    private CommonAccessibility commonAccessibility;
    private CommonAural commonAural;
    private CommonBorderPaddingBackground commonBorderPaddingBackground;
    private CommonMarginBlock commonMarginBlock;
    private CommonRelativePosition commonRelativePosition;
    private LengthRangeProperty blockProgressionDimension;
    // private ToBeImplementedProperty borderAfterPrecedence;
    // private ToBeImplementedProperty borderBeforePrecedence;
    private int borderCollapse;
    // private ToBeImplementedProperty borderEndPrecedence;
    private LengthPairProperty borderSeparation;
    // private ToBeImplementedProperty borderStartPrecedence;
    private int breakAfter;
    private int breakBefore;
    private String id;
    private LengthRangeProperty inlineProgressionDimension;
    private int intrusionDisplace;
    //private Length height;
    private KeepProperty keepTogether;
    private KeepProperty keepWithNext;
    private KeepProperty keepWithPrevious;
    private int tableLayout;
    private int tableOmitFooterAtBreak;
    private int tableOmitHeaderAtBreak;
    //private Length width;
    private int writingMode;
    // End of property values

    private static final int MINCOLWIDTH = 10000; // 10pt

    /** collection of columns in this table */
    protected List columns = null;
    private TableBody tableHeader = null;
    private TableBody tableFooter = null;
  
    /** used for validation */
    private boolean tableColumnFound = false;
    private boolean tableHeaderFound = false;   
    private boolean tableFooterFound = false;   
    private boolean tableBodyFound = false; 
   
    /** 
     * Default table-column used when no columns are specified. It is used
     * to handle inheritance (especially visibility) and defaults properly. */
    private TableColumn defaultColumn;
    
    /**
     * @param parent FONode that is the parent of this object
     */
    public Table(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = pList.getAccessibilityProps();
        commonAural = pList.getAuralProps();
        commonBorderPaddingBackground = pList.getBorderPaddingBackgroundProps();
        commonMarginBlock = pList.getMarginBlockProps();
        commonRelativePosition = pList.getRelativePositionProps();
        blockProgressionDimension = pList.get(PR_BLOCK_PROGRESSION_DIMENSION).getLengthRange();
        // borderAfterPrecedence = pList.get(PR_BORDER_AFTER_PRECEDENCE);
        // borderBeforePrecedence = pList.get(PR_BORDER_BEFORE_PRECEDENCE);
        borderCollapse = pList.get(PR_BORDER_COLLAPSE).getEnum();
        // borderEndPrecedence = pList.get(PR_BORDER_END_PRECEDENCE);
        borderSeparation = pList.get(PR_BORDER_SEPARATION).getLengthPair();
        // borderStartPrecedence = pList.get(PR_BORDER_START_PRECEDENCE);
        breakAfter = pList.get(PR_BREAK_AFTER).getEnum();
        breakBefore = pList.get(PR_BREAK_BEFORE).getEnum();
        id = pList.get(PR_ID).getString();
        inlineProgressionDimension = pList.get(PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        intrusionDisplace = pList.get(PR_INTRUSION_DISPLACE).getEnum();
        //height = pList.get(PR_HEIGHT).getLength();
        keepTogether = pList.get(PR_KEEP_TOGETHER).getKeep();
        keepWithNext = pList.get(PR_KEEP_WITH_NEXT).getKeep();
        keepWithPrevious = pList.get(PR_KEEP_WITH_PREVIOUS).getKeep();
        tableLayout = pList.get(PR_TABLE_LAYOUT).getEnum();
        tableOmitFooterAtBreak = pList.get(PR_TABLE_OMIT_FOOTER_AT_BREAK).getEnum();
        tableOmitHeaderAtBreak = pList.get(PR_TABLE_OMIT_HEADER_AT_BREAK).getEnum();
        //width = pList.get(PR_WIDTH).getLength();
        writingMode = pList.get(PR_WRITING_MODE).getEnum();
        
        //Create default column in case no table-columns will be defined.
        defaultColumn = new TableColumn(this);
        PropertyList colPList = new StaticPropertyList(defaultColumn, pList);
        colPList.setWritingMode();
        defaultColumn.bind(colPList);
        
        if (borderCollapse != EN_SEPARATE && commonBorderPaddingBackground.hasPadding()) {
            //See "17.6.2 The collapsing border model" in CSS2
            getLogger().error("Table may not have padding when using the collapsing border model.");
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
        getFOEventHandler().startTable(this);
    }
   
    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (marker*,table-column*,table-header?,table-footer?,table-body+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws ValidationException {
        if (nsURI == FO_URI) {
            if (localName.equals("marker")) {
                if (tableColumnFound || tableHeaderFound || tableFooterFound 
                        || tableBodyFound) {
                   nodesOutOfOrderError(loc, "fo:marker", 
                       "(table-column*,table-header?,table-footer?,table-body+)");
                }
            } else if (localName.equals("table-column")) {
                tableColumnFound = true;
                if (tableHeaderFound || tableFooterFound || tableBodyFound) {
                    nodesOutOfOrderError(loc, "fo:table-column", 
                        "(table-header?,table-footer?,table-body+)");
                }
            } else if (localName.equals("table-header")) {
                if (tableHeaderFound) {
                    tooManyNodesError(loc, "table-header");
                } else {
                    tableHeaderFound = true;
                    if (tableFooterFound || tableBodyFound) {
                        nodesOutOfOrderError(loc, "fo:table-header", 
                            "(table-footer?,table-body+)"); 
                    }
                }
            } else if (localName.equals("table-footer")) {
                if (tableFooterFound) {
                    tooManyNodesError(loc, "table-footer");
                } else {
                    tableFooterFound = true;
                    if (tableBodyFound) {
                        nodesOutOfOrderError(loc, "fo:table-footer", 
                            "(table-body+)");
                    }
                }
            } else if (localName.equals("table-body")) {
                tableBodyFound = true;
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws FOPException {
        if (!tableBodyFound) {
           missingChildElementError("(marker*,table-column*,table-header?,table-footer?,table-body+)");
        }

        getFOEventHandler().endTable(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws FOPException {
        if (child.getName().equals("fo:table-column")) {
            if (columns == null) {
                columns = new java.util.ArrayList();
            }
            columns.add(((TableColumn)child));
        } else if (child.getName().equals("fo:table-footer")) {
            tableFooter = (TableBody)child;
        } else if (child.getName().equals("fo:table-header")) {
            tableHeader = (TableBody)child;
        } else {
            // add bodies
            super.addChildNode(child);
        }
    }

    /** @return true of table-layout="auto" */
    public boolean isAutoLayout() {
        return (tableLayout != EN_FIXED);
    }
    
    /** @return the default table column */
    public TableColumn getDefaultColumn() {
        return this.defaultColumn;
    }
    
    public List getColumns() {
        return columns;
    }
    
    /**
     * @param index index of the table-body element.
     * @return the requested table-body element
     */
    public TableBody getBody(int index) {
        return (TableBody)childNodes.get(index);
    }

    public TableBody getTableHeader() {
        return tableHeader;
    }

    public TableBody getTableFooter() {
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
     * @return the "inline-progression-dimension" property.
     */
    public LengthRangeProperty getInlineProgressionDimension() {
        return inlineProgressionDimension;
    }

    /**
     * @return the "block-progression-dimension" property.
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

    /** @return the "break-after" property. */
    public int getBreakAfter() {
        return breakAfter;
    }

    /** @return the "break-before" property. */
    public int getBreakBefore() {
        return breakBefore;
    }

    /** @return the "keep-with-next" property.  */
    public KeepProperty getKeepWithNext() {
        return keepWithNext;
    }

    /** @return the "keep-with-previous" property.  */
    public KeepProperty getKeepWithPrevious() {
        return keepWithPrevious;
    }

    /** @return the "keep-together" property.  */
    public KeepProperty getKeepTogether() {
        return keepTogether;
    }

    /** @return the "border-collapse" property. */
    public int getBorderCollapse() {
        return borderCollapse;
    }

    /** @return true if the separate border model is active */
    public boolean isSeparateBorderModel() {
        return (getBorderCollapse() == EN_SEPARATE);
    }
    
    /** @return the "border-separation" property. */
    public LengthPairProperty getBorderSeparation() {
        return borderSeparation;
    }

    /**
     * @return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE;
    }
}
