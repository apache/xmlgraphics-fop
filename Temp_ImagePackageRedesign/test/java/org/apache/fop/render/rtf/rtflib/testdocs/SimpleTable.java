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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;

/**  Generates a simple RTF test document for the jfor rtflib package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */
class SimpleTable extends TestDocument {
    /** generate the body of the test document */
    static final int MAX_ROW = 2;
    static final int MAX_COL = 3;
    static final int INCH_TO_TWIPS = 1440;
    static final int C1W = 4;

    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());
        tbl.newTableRow().newTableCell(C1W * INCH_TO_TWIPS).newParagraph().newText
                ("Here's a table row with just one cell, width " + C1W + "''");

        for (int row = 0; row < MAX_ROW; row++) {
            final RtfTableRow r = tbl.newTableRow();

            for (int col = 0; col < MAX_COL; col++) {
                final float widthInInches = col / 2f + 1f;
                final int widthInTwips = (int)(widthInInches * INCH_TO_TWIPS);
                final RtfTableCell c = r.newTableCell(widthInTwips);
                c.newParagraph().newText("(" + row + "," + col + "), width "
                        + widthInInches  + "''");
                if (row == 0 && col == 1) {
                    for (int i = 0; i < 4; i++) {
                        c.newParagraph().newText("additional paragraph " + i + " of cell 0,1");
                    }
                }
            }
        }

        sect.newParagraph().newText("This paragraph follows the table.");
    }
}