/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

// Java
import java.util.List;
import java.util.ListIterator;
import java.util.ArrayList;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.table.TableLayoutManager;
import org.apache.fop.layoutmgr.table.Body;
import org.apache.fop.layoutmgr.table.Column;
import org.apache.fop.fo.properties.LengthRangeProperty;

/**
 * Class modelling the fo:table object.
 */
public class Table extends FObj {
    private static final int MINCOLWIDTH = 10000; // 10pt

    /** collection of columns in this table */
    protected ArrayList columns = null;
    private TableBody tableHeader = null;
    private TableBody tableFooter = null;
    private boolean omitHeaderAtBreak = false;
    private boolean omitFooterAtBreak = false;

    private int breakBefore;
    private int breakAfter;
    private int spaceBefore;
    private int spaceAfter;
    private ColorType backgroundColor;
    private LengthRangeProperty ipd;
    private int height;

    private boolean bAutoLayout = false;
    private int contentWidth = 0; // Sum of column widths
    /** Optimum inline-progression-dimension */
    private int optIPD;
    /** Minimum inline-progression-dimension */
    private int minIPD;
    /** Maximum inline-progression-dimension */
    private int maxIPD;

    /**
     * @param parent FONode that is the parent of this object
     */
    public Table(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.breakBefore = getPropEnum(PR_BREAK_BEFORE);
        this.breakAfter = getPropEnum(PR_BREAK_AFTER);
        this.spaceBefore = getPropLength(PR_SPACE_BEFORE | CP_OPTIMUM);
        this.spaceAfter = getPropLength(PR_SPACE_AFTER | CP_OPTIMUM);
        this.backgroundColor =
          this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();
        this.ipd = this.propertyList.get(
                     PR_INLINE_PROGRESSION_DIMENSION).getLengthRange();
        this.height = getPropLength(PR_HEIGHT);
        this.bAutoLayout = (getPropEnum(PR_TABLE_LAYOUT) == TableLayout.AUTO);

        this.omitHeaderAtBreak = getPropEnum(PR_TABLE_OMIT_HEADER_AT_BREAK)
            == TableOmitHeaderAtBreak.TRUE;
        this.omitFooterAtBreak = getPropEnum(PR_TABLE_OMIT_FOOTER_AT_BREAK)
            == TableOmitFooterAtBreak.TRUE;
        getFOEventHandler().startTable(this);
    }

    protected void endOfNode() throws SAXParseException {
        getFOEventHandler().endTable(this);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) throws SAXParseException {
        if (child.getName().equals("fo:table-column")) {
            if (columns == null) {
                columns = new ArrayList();
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

    private ArrayList getColumns() {
        return columns;
    }

    private TableBody getTableHeader() {
        return tableHeader;
    }

    private TableBody getTableFooter() {
        return tableFooter;
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     * @todo see if can/should move much of this logic into TableLayoutManager
     *      and/or TableBody and TableColumn FO subclasses.
     */
    public void addLayoutManager(List list) {
        TableLayoutManager tlm = new TableLayoutManager(this);
        ArrayList columns = getColumns();
        if (columns != null) {
            ArrayList columnLMs = new ArrayList();
            ListIterator iter = columns.listIterator();
            while (iter.hasNext()) {
                columnLMs.add(getTableColumnLayoutManager((TableColumn)iter.next()));
            }
            tlm.setColumns(columnLMs);
        }
        if (getTableHeader() != null) {
            tlm.setTableHeader(getTableBodyLayoutManager(getTableHeader()));
        }
        if (getTableFooter() != null) {
            tlm.setTableFooter(getTableBodyLayoutManager(getTableFooter()));
        }
        list.add(tlm);
    }

    public Column getTableColumnLayoutManager(TableColumn node) {
         Column clm = new Column(node);
         return clm;
    }
    
    public Body getTableBodyLayoutManager(TableBody node) {
         Body blm = new Body(node);
         return blm;
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
