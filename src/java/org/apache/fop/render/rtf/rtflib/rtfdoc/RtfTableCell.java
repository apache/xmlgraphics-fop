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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.Writer;
import java.io.IOException;
import java.util.Iterator;

/**  A cell in an RTF table, container for paragraphs, lists, etc.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfTableCell
        extends RtfContainer
        implements IRtfParagraphContainer, IRtfListContainer, IRtfTableContainer,
            IRtfExternalGraphicContainer, IRtfTextrunContainer {
    private RtfParagraph paragraph;
    private RtfList list;
    private RtfTable table;
    private RtfExternalGraphic externalGraphic;
    private final RtfTableRow parentRow;
    private boolean setCenter;
    private boolean setRight;
    private int id;

    /** default cell width (in twips ??) */
    public static final int DEFAULT_CELL_WIDTH = 2000;

    /** cell width in twips */
    private int cellWidth;
    private int widthOffset;

    /** cell merging has three states */
    private int vMerge = NO_MERGE;
    private int hMerge = NO_MERGE;

    /** cell merging: this cell is not merged */
    public static final int NO_MERGE = 0;

    /** cell merging: this cell is the start of a range of merged cells */
    public static final int MERGE_START = 1;

    /** cell merging: this cell is part of (but not the start of) a range of merged cells */
    public static final int MERGE_WITH_PREVIOUS = 2;

    /** Create an RTF element as a child of given container */
    RtfTableCell(RtfTableRow parent, Writer w, int cellWidth, int idNum) throws IOException {
        super(parent, w);
        id = idNum;
        parentRow = parent;
        this.cellWidth = cellWidth;
        setCenter = false;
        setRight = false;

    }

    /** Create an RTF element as a child of given container */
    RtfTableCell(RtfTableRow parent, Writer w, int cellWidth, RtfAttributes attrs,
            int idNum) throws IOException {
        super(parent, w, attrs);
        id = idNum;
        parentRow = parent;
        this.cellWidth = cellWidth;
    }

    /**
     * Start a new paragraph after closing current current paragraph, list and table
     * @param attrs attributes of new RtfParagraph
     * @return new RtfParagraph object
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        closeAll();

        // in tables, RtfParagraph must have the intbl attribute
        if (attrs == null) {
            attrs = new RtfAttributes();
        }
        
        attrs.set("intbl");

        paragraph = new RtfParagraph(this, writer, attrs);

        if (paragraph.attrib.isSet("qc")) {
            setCenter = true;
            attrs.set("qc");
        } else if (paragraph.attrib.isSet("qr")) {
            setRight = true;
            attrs.set("qr");
        } else {
            attrs.set("ql");
        }
        attrs.set("intbl");


        //lines modified by Chris Scott, Westinghouse
        return paragraph;
    }

    /**
     * Start a new external graphic after closing current paragraph, list and table
     * @throws IOException for I/O problems
     * @return new RtfExternalGraphic object
     */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        externalGraphic = new RtfExternalGraphic(this, writer);
        return externalGraphic;
    }

    /**
     * Start a new paragraph with default attributes after closing current
     * paragraph, list and table
     * @return new RtfParagraph object
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }

    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrib attributes for new RtfList
     * @return new RtfList object
     * @throws IOException for I/O problems
     */
    public RtfList newList(RtfAttributes attrib) throws IOException {
        closeAll();
        list = new RtfList(this, writer, attrib);
        return list;
    }

    /**
     * Start a new nested table after closing current paragraph, list and table
     * @param tc table column info for new RtfTable
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    public RtfTable newTable(ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, tc);
        return table;
    }

    /**
     * Start a new nested table after closing current paragraph, list and table
     * @param attrs attributes of new RtfTable
     * @param tc table column info for new RtfTable
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    // Modified by Boris Poudérous on 07/22/2002
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, attrs, tc); // Added tc Boris Poudérous 07/22/2002
        return table;
    }

    /** used by RtfTableRow to write the <celldef> cell definition control words
     *  @param widthOffset sum of the widths of preceeding cells in same row
     *  @return widthOffset + width of this cell
     */
    int writeCellDef(int offset) throws IOException {
        /*
         * Don't write \clmgf or \clmrg. Instead add the widths
         * of all spanned columns and create a single wider cell,
         * because \clmgf and \clmrg won't work in last row of a
         * table (Word2000 seems to do the same).
         * Cause of this, dont't write horizontally merged cells.
         * They just exist as placeholders in TableContext class,
         * and are never written to RTF file.    
         */
        // horizontal cell merge codes
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return offset;
        }
        
        newLine();
        this.widthOffset = offset;

        // vertical cell merge codes
        if (vMerge == MERGE_START) {
            writeControlWord("clvmgf");
        } else if (vMerge == MERGE_WITH_PREVIOUS) {
            writeControlWord("clvmrg");
        }

        /**
         * Added by Boris POUDEROUS on 2002/06/26
         */
        // Cell background color processing :
        writeAttributes (attrib, ITableAttributes.CELL_COLOR);
        /** - end - */

        writeAttributes (attrib, ITableAttributes.ATTRIB_CELL_PADDING);
        writeAttributes (attrib, ITableAttributes.CELL_BORDER);
        writeAttributes (attrib, IBorderAttributes.BORDERS);

        // determine cell width
        int iCurrentWidth = this.cellWidth;
        if (attrib.getValue("number-columns-spanned") != null) {
            // Get the number of columns spanned
            int nbMergedCells = ((Integer)attrib.getValue("number-columns-spanned")).intValue();
            
            RtfTable tab = getRow().getTable();
            
            // Get the context of the current table in order to get the width of each column
            ITableColumnsInfo tableColumnsInfo
                = tab.getITableColumnsInfo();
            
            tableColumnsInfo.selectFirstColumn();

            // Reach the column index in table context corresponding to the current column cell
            // id is the index of the current cell (it begins at 1)
            // getColumnIndex() is the index of the current column in table context (it begins at 0)
            //  => so we must widthdraw 1 when comparing these two variables.
            while ((this.id - 1) != tableColumnsInfo.getColumnIndex()) {
               tableColumnsInfo.selectNextColumn();
            }

            // We widthdraw one cell because the first cell is already created
            // (it's the current cell) !
            int i = nbMergedCells - 1;
            while (i > 0) {
                tableColumnsInfo.selectNextColumn();
                iCurrentWidth += (int)tableColumnsInfo.getColumnWidth();

                i--;
            }
        }
        final int xPos = offset + iCurrentWidth;

        //these lines added by Chris Scott, Westinghouse
        //some attributes need to be writting before opening block
        if (setCenter) {
            writeControlWord("qc");
        } else if (setRight) {
            writeControlWord("qr");
        } else {
            writeControlWord("ql");
        }

        writeControlWord("cellx" + xPos);

        //TODO Why is this here, right after an alignment command is written (see above)?
        writeControlWord("ql");

        return xPos;

    }
    
    /**
     * Overriden to avoid writing any it's a merged cell.
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {
       // Never write horizontally merged cells.
       if (hMerge == MERGE_WITH_PREVIOUS) {
           return;
       }
       
       super.writeRtfContent();
    }

    /**
     * Called before writeRtfContent; overriden to avoid writing
     * any it's a merged cell.
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
        // Never write horizontally merged cells.
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return;
        }
        
        super.writeRtfPrefix();
    }

    /**
     * The "cell" control word marks the end of a cell
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        // Never write horizontally merged cells.
        if (hMerge == MERGE_WITH_PREVIOUS) {
            return;
        }
        
        if (getRow().getTable().isNestedTable()) {
            //nested table
            writeControlWordNS("nestcell");
            writeGroupMark(true);
            writeControlWord("nonesttables");
            writeControlWord("par");
            writeGroupMark(false);
        } else {
            // word97 hangs if cell does not contain at least one "par" control word
            // TODO this is what causes the extra spaces in nested table of test
            //      004-spacing-in-tables.fo,
            // but if is not here we generate invalid RTF for word97

            if (setCenter) {
                writeControlWord("qc");
            } else if (setRight) {
                writeControlWord("qr");
            } else {
                RtfElement lastChild = null;
                
                if (getChildren().size() > 0) {
                    lastChild = (RtfElement) getChildren().get(getChildren().size() - 1);
                }
                    
                
                if (lastChild != null
                        && lastChild instanceof RtfTextrun) {
                    //Don't write \ql in order to allow for example a right aligned paragraph 
                    //in a not right aligned table-cell to write its \qr.
                } else {
                    writeControlWord("ql");
                }                
            }

            if (!containsText()) {
                writeControlWord("intbl");

                //R.Marra this create useless paragraph
                //Seem working into Word97 with the "intbl" only
                //writeControlWord("par");
            }

            writeControlWord("cell");
        }
    }


    //modified by Chris Scott, Westinghouse
    private void closeCurrentParagraph() throws IOException {
        if (paragraph != null) {
            paragraph.close();
        }
    }

    private void closeCurrentList() throws IOException {
        if (list != null) {
            list.close();
        }
    }

    private void closeCurrentTable() throws IOException {
        if (table != null) {
            table.close();
        }
    }

    private void closeCurrentExternalGraphic() throws IOException {
        if (externalGraphic != null) {
            externalGraphic.close();
        }
    }

    private void closeAll()
    throws IOException {
        closeCurrentTable();
        closeCurrentParagraph();
        closeCurrentList();
        closeCurrentExternalGraphic();
    }

    /**
     * @param mergeStatus vertical cell merging status to set
     */
    public void setVMerge(int mergeStatus) { this.vMerge = mergeStatus; }

    /**
     * @return vertical cell merging status
     */
    public int getVMerge() { return this.vMerge; }

    /**
     * Set horizontal cell merging status
     * @param mergeStatus mergeStatus to set
     */
    public void setHMerge(int mergeStatus) {
        this.hMerge = mergeStatus;
    }

    /**
     * @return horizontal cell merging status
     */
    public int getHMerge() {
        return this.hMerge;
    }

    /** get cell width */
    int getCellWidth() { return this.cellWidth; }

    /**
     * Overridden so that nested tables cause extra rows to be added after the row
     * that contains this cell
     * disabled for V0.3 - nested table support is not done yet
     * @throws IOException for I/O problems
     */
    /*
    protected void writeRtfContent()
    throws IOException {
        int extraRowIndex = 0;
        RtfTableCell extraCell = null;

        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfTable) {
                // nested table - render its cells in supplementary rows after current row,
                // and put the remaining content of this cell in a new cell after nested table
                // Line added by Boris Poudérous
        parentRow.getExtraRowSet().setParentITableColumnsInfo(
                ((RtfTable)this.getParentOfClass(e.getClass())).getITableColumnsInfo());
        extraRowIndex = parentRow.getExtraRowSet().addTable((RtfTable)e,
                extraRowIndex, widthOffset);
                // Boris Poudérous added the passing of the current cell
                // attributes to the new cells (in order not to have cell without
                // border for example)
        extraCell = parentRow.getExtraRowSet().createExtraCell(extraRowIndex,
                widthOffset, this.getCellWidth(), attrib);
                extraRowIndex++;

            } else if (extraCell != null) {
                // we are after a nested table, add elements to the extra cell created for them
                extraCell.addChild(e);

            } else {
                // before a nested table, normal rendering
                e.writeRtf();
            }
        }
    }*/

    /**
     * A table cell always contains "useful" content, as it is here to take some
     * space in a row.
     * Use containsText() to find out if there is really some useful content in the cell.
     * TODO: containsText could use the original isEmpty implementation?
     * @return false (always)
     */
    public boolean isEmpty() {
        return false;
    }

    /** true if the "par" control word must be written for given RtfParagraph
     *  (which is not the case for the last non-empty paragraph of the cell)
     */
    boolean paragraphNeedsPar(RtfParagraph p) {
        // true if there is at least one non-empty paragraph after p in our children
        boolean pFound = false;
        boolean result = false;
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final Object o = it.next();
            if (!pFound) {
                // set pFound when p is found in the list
                pFound =  (o == p);
            } else {
                if (o instanceof RtfParagraph) {
                    final RtfParagraph p2 = (RtfParagraph)o;
                    if (!p2.isEmpty()) {
                        // found a non-empty paragraph after p
                        result = true;
                        break;
                    }
                } else if (o instanceof RtfTable) {
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the current RtfTextrun object.
     * Opens a new one if necessary.
     * @return The RtfTextrun object
     * @throws IOException Thrown when an IO-problem occurs
     */
    public RtfTextrun getTextrun() throws IOException {
        RtfAttributes attrs = new RtfAttributes();
        
        if (!getRow().getTable().isNestedTable()) {
            attrs.set("intbl");
        }
        
        RtfTextrun textrun = RtfTextrun.getTextrun(this, writer, attrs);

        //Suppress the very last \par, because the closing \cell applies the
        //paragraph attributes. 
        textrun.setSuppressLastPar(true);  
        
        return textrun;
    }
    
    /**
     * Get the parent row.
     * @return The parent row.
     */
    public RtfTableRow getRow() {
        RtfElement e = this;
        while (e.parent != null) {
            if (e.parent instanceof RtfTableRow) {
                return (RtfTableRow) e.parent;
            }

            e = e.parent;
        }

        return null;  
    }
}
