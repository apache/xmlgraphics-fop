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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.testdocs;

import java.io.IOException;

import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;

/**  Generates an RTF test document containing merged table cells
 */

class MergedTableCells extends TestDocument {
    static final int MM_TO_TWIPS = (int)(1440f / 25.4f);

    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("This document contains a table with some merged cells.");

        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());

        // first row, test horizontal merging
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(80 * MM_TO_TWIPS);
            c.setHMerge(RtfTableCell.MERGE_START);
            c.newParagraph().newText("cell 0,0, width 80mm, merge start, "
                    + "followed by two merged cells totalling 80mm width.");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("THIS IS IN AN HMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setHMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("THIS IS IN AN HMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");
        }

        // second row, start vertical merging in column 1
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(RtfTableCell.MERGE_START);
            c.newParagraph().newText("cell 1,0, vertical merge start, 40mm, spans three rows.");

            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText("cell 1,1, no merge, 80mm");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(RtfTableCell.MERGE_START);
            c.newParagraph().newText("cell 1,2, vertical merge start, 40mm, spans two rows.");
        }

        // third row, column 1 merged with previous row
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 2,0, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 2,1, no merge, 40mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 2,2, no merge, 40mm");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 2,3, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");
        }

        // fourth row, column 1 merged with previous row
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(RtfTableCell.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 3,0, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            r.newTableCell(10 * MM_TO_TWIPS).newParagraph().newText("cell 3,1, no merge, 10mm");
            r.newTableCell(30 * MM_TO_TWIPS).newParagraph().newText("cell 3,2, no merge, 30mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 3,3, no merge, 40mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 3,4, no merge, 40mm");
        }

        // fifth row, just one cell
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(160 * MM_TO_TWIPS).newParagraph().newText(
                    "cell 4,0, width 160mm, only cell in this row");
        }
    }
}
