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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**  Container for RtfTableCell elements
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 *  @author Roberto Marra roberto@link-u.com
 */

public class RtfTableRow extends RtfContainer implements ITableAttributes {
    private RtfTableCell cell;
    private RtfExtraRowSet extraRowSet;
    private int id;
    private int highestCell = 0;


    /** Create an RTF element as a child of given container */
    RtfTableRow(RtfTable parent, Writer w, int idNum) throws IOException {
        super(parent, w);
        id = idNum;
    }

    /** Create an RTF element as a child of given container */
    RtfTableRow(RtfTable parent, Writer w, RtfAttributes attrs, int idNum) throws IOException {
        super(parent, w, attrs);
        id = idNum;
    }

    /**
     * Close current cell if any and start a new one
     * @param cellWidth width of new cell
     * @return new RtfTableCell
     * @throws IOException for I/O problems
     */
    public RtfTableCell newTableCell(int cellWidth) throws IOException {
        highestCell++;
        cell = new RtfTableCell(this, writer, cellWidth, highestCell);
        return cell;
    }

    /**
     * Close current cell if any and start a new one
     * @param attrs attributes of new cell
     * @param cellWidth width of new cell
     * @return new RtfTableCell
     * @throws IOException for I/O problems
     */
    public RtfTableCell newTableCell(int cellWidth, RtfAttributes attrs) throws IOException {
        highestCell++;
        cell = new RtfTableCell(this, writer, cellWidth, attrs, highestCell);
        return cell;
    }

    /**
     * Added by Boris POUDEROUS on 07/02/2002
     * in order to add an empty cell that is merged with the cell above.
     * This cell is placed before or after the nested table.
     * @param attrs attributes of new cell
     * @param cellWidth width of new cell
     * @return new RtfTableCell
     * @throws IOException for I/O problems
     */
    public RtfTableCell newTableCellMergedVertically(int cellWidth,
           RtfAttributes attrs) throws IOException {
        highestCell++;
        cell = new RtfTableCell (this, writer, cellWidth, attrs, highestCell);
        cell.setVMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
        return cell;
    }

    /**
     * Added by Boris POUDEROUS on 07/02/2002
     * in order to add an empty cell that is merged with the previous cell.
     * @param attrs attributes of new cell
     * @param cellWidth width of new cell
     * @return new RtfTableCell
     * @throws IOException for I/O problems
     */
    public RtfTableCell newTableCellMergedHorizontally (int cellWidth,
           RtfAttributes attrs) throws IOException {
        highestCell++;
        // Added by Normand Masse
        // Inherit attributes from base cell for merge
        RtfAttributes wAttributes = (RtfAttributes)attrs.clone();
        wAttributes.unset("number-columns-spanned");

        cell = new RtfTableCell(this, writer, cellWidth, wAttributes, highestCell);
        cell.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
        return cell;
    }

    /**
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
        newLine();
        writeGroupMark(true);
    }

    /**
     * Overridden to write trowd and cell definitions before writing our cells
     * @throws IOException for I/O problems
     */
    protected void writeRtfContent() throws IOException {

        // create new extra row set to allow our cells to put nested tables
        // in rows that will be rendered after this one
        extraRowSet = new RtfExtraRowSet(writer);

        // render the row and cells definitions
        writeControlWord("trowd");

        //check for keep-together
        if (attrib != null && attrib.isSet(ITableAttributes.ROW_KEEP_TOGETHER)) {
            writeControlWord(ROW_KEEP_TOGETHER);
        }

        writePaddingAttributes();

        final RtfTable parentTable = (RtfTable) parent;
        adjustBorderProperties(parentTable);

        writeAttributes(attrib, new String[]{ITableAttributes.ATTR_HEADER});
        writeAttributes(attrib, ITableAttributes.ROW_BORDER);
        writeAttributes(attrib, ITableAttributes.CELL_BORDER);
        writeAttributes(attrib, IBorderAttributes.BORDERS);

        if (attrib.isSet(ITableAttributes.ROW_HEIGHT)) {
            writeOneAttribute(
                    ITableAttributes.ROW_HEIGHT,
                    attrib.getValue(ITableAttributes.ROW_HEIGHT));
        }

        /**
         * Added by Boris POUDEROUS on 07/02/2002
         * in order to get the indexes of the cells preceding a cell that
         * contains a nested table.
         * Thus, the cells of the extra row will be merged with the cells above.
         */
        boolean nestedTableFound = false;
        int index = 0; // Used to store the index of the cell that contains a nested table
        int numberOfCellsBeforeNestedTable = 0;

        java.util.Vector indexesFound = new java.util.Vector();
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();

            if (e instanceof RtfTableCell) {
                if (!nestedTableFound) {
                    ++numberOfCellsBeforeNestedTable;
                }
                for (Iterator it2 = ((RtfTableCell)e).getChildren().iterator(); it2.hasNext();) {
                      final RtfElement subElement = (RtfElement)it2.next();
                      if (subElement instanceof RtfTable) {
                          nestedTableFound = true;
                          indexesFound.addElement(new Integer(index));
                        } else if (subElement instanceof RtfParagraph) {
                           for (Iterator it3
                                = ((RtfParagraph)subElement).getChildren().iterator(); 
                                it3.hasNext();) {
                                final RtfElement subSubElement = (RtfElement)it3.next();
                                if (subSubElement instanceof RtfTable) {
                                    nestedTableFound = true;
                                    indexesFound.addElement(new Integer(index));
                                  }
                             }
                        }
                   }
              }

            index++;
          }
         /** - end - */

        // write X positions of our cells
        int xPos = 0;
        
        final Object leftIndent = attrib.getValue(ITableAttributes.ATTR_ROW_LEFT_INDENT);
        if (leftIndent != null) {
            xPos = ((Integer)leftIndent).intValue();
        }
        
        index = 0;            // Line added by Boris POUDEROUS on 07/02/2002
        for (Iterator it = getChildren().iterator(); it.hasNext();) {
            final RtfElement e = (RtfElement)it.next();
            if (e instanceof RtfTableCell) {
                /**
                 * Added by Boris POUDEROUS on 2002/07/02
                 */
                // If one of the row's child cells contains a nested table :
                if (!indexesFound.isEmpty()) {
                    for (int i = 0; i < indexesFound.size(); i++) {
                        // If the current cell index is equals to the index of the cell that
                        // contains a nested table => NO MERGE
                        if (index == ((Integer)indexesFound.get(i)).intValue()) {
                            break;

                        // If the current cell index is lower than the index of the cell that
                        // contains a nested table => START VERTICAL MERGE
                        } else if (index < ((Integer)indexesFound.get(i)).intValue()) {
                            ((RtfTableCell)e).setVMerge(RtfTableCell.MERGE_START);
                            break;

                        // If the current cell index is greater than the index of the cell that
                        // contains a nested table => START VERTICAL MERGE
                        } else if (index > ((Integer)indexesFound.get(i)).intValue()) {
                            ((RtfTableCell)e).setVMerge(RtfTableCell.MERGE_START);
                            break;
                          }
                      }
                  }
                /** - end - */

                // Added by Normand Masse
                // Adjust the cell's display attributes so the table's/row's borders
                // are drawn properly.
                RtfTableCell cell = (RtfTableCell)e;
                if (index == 0) {
                    if (!cell.getRtfAttributes().isSet(ITableAttributes.CELL_BORDER_LEFT)) {
                        cell.getRtfAttributes().set(ITableAttributes.CELL_BORDER_LEFT,
                            (String)attrib.getValue(ITableAttributes.ROW_BORDER_LEFT));
                    }
                }

                if (index == this.getChildCount() - 1) {
                    if (!cell.getRtfAttributes().isSet(ITableAttributes.CELL_BORDER_RIGHT)) {
                        cell.getRtfAttributes().set(ITableAttributes.CELL_BORDER_RIGHT,
                            (String)attrib.getValue(ITableAttributes.ROW_BORDER_RIGHT));
                    }
                }

                if (isFirstRow()) {
                    if (!cell.getRtfAttributes().isSet(ITableAttributes.CELL_BORDER_TOP)) {
                        cell.getRtfAttributes().set(ITableAttributes.CELL_BORDER_TOP,
                            (String)attrib.getValue(ITableAttributes.ROW_BORDER_TOP));
                    }
                }

                if ((parentTable != null) && (parentTable.isHighestRow(id))) {
                    if (!cell.getRtfAttributes().isSet(ITableAttributes.CELL_BORDER_BOTTOM)) {
                        cell.getRtfAttributes().set(ITableAttributes.CELL_BORDER_BOTTOM,
                            (String)attrib.getValue(ITableAttributes.ROW_BORDER_BOTTOM));
                    }
                }

                xPos = cell.writeCellDef(xPos);
            }
          index++; // Added by Boris POUDEROUS on 2002/07/02
        }

        newLine();
        
        // now children can write themselves, we have the correct RTF prefix code
        super.writeRtfContent();
    }

    private void adjustBorderProperties(RtfTable parentTable) {
        // if we have attributes, manipulate border properties
        if (attrib != null && parentTable != null) {

            //if table is only one row long
            if (isFirstRow() && parentTable.isHighestRow(id)) {
                attrib.unset(ITableAttributes.ROW_BORDER_HORIZONTAL);
            //or if row is the first row
            } else if (isFirstRow()) {
                attrib.unset(ITableAttributes.ROW_BORDER_BOTTOM);
            //or if row is the last row
            } else if (parentTable.isHighestRow(id)) {
                attrib.unset(ITableAttributes.ROW_BORDER_TOP);
            //else the row is an inside row
            } else {
                attrib.unset(ITableAttributes.ROW_BORDER_BOTTOM);
                attrib.unset(ITableAttributes.ROW_BORDER_TOP);
            }
        }
    }

    /**
     * Overridden to write RTF suffix code, what comes after our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        writeControlWord("row");

        // write extra rows if any
        extraRowSet.writeRtf();
        writeGroupMark(false);
    }

    RtfExtraRowSet getExtraRowSet() {
        return extraRowSet;
    }

    private void writePaddingAttributes()
    throws IOException {
        // Row padding attributes generated in the converter package
        // use RTF 1.6 definitions - try to compute a reasonable RTF 1.5 value
        // out of them if present
        // how to do vertical padding with RTF 1.5?
        if (attrib != null && !attrib.isSet(ATTR_RTF_15_TRGAPH)) {
            int gaph = -1;
            try {
                // set (RTF 1.5) gaph to the average of the (RTF 1.6) left and right padding values
                final Integer leftPadStr = (Integer)attrib.getValue(ATTR_ROW_PADDING_LEFT);
                if (leftPadStr != null) {
                    gaph = leftPadStr.intValue();
                }
                final Integer rightPadStr = (Integer)attrib.getValue(ATTR_ROW_PADDING_RIGHT);
                if (rightPadStr != null) {
                    gaph = (gaph + rightPadStr.intValue()) / 2;
                }
            } catch (Exception e) {
                final String msg = "RtfTableRow.writePaddingAttributes: " + e.toString();
//                getRtfFile().getLog().logWarning(msg);
            }
            if (gaph >= 0) {
                attrib.set(ATTR_RTF_15_TRGAPH, gaph);
            }
        }

        // write all padding attributes
        writeAttributes(attrib, ATTRIB_ROW_PADDING);
    }

    /**
     * @return true if the row is the first in the table
     */
    public boolean isFirstRow() {
        return (id == 1);
    }

    /**
     * @param id cell id to check
     * @return true if the cell is the highest cell
     */
    public boolean isHighestCell(int id) {
        return (highestCell == id) ? true : false;
    }
}
