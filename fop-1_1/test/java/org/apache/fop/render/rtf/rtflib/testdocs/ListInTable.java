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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfList;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;

/**  Generates a simple RTF test document for the jfor rtflib package.
 */

class ListInTable extends TestDocument {
    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("There must be a table below where the "
                + "second cell contains a bulleted list mixed with normal paragraphs");

        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());
        final RtfTableRow row = tbl.newTableRow();
        row.newTableCell(RtfTableCell.DEFAULT_CELL_WIDTH).newParagraph().newText("cell A, simple");

        final RtfTableCell c = row.newTableCell(RtfTableCell.DEFAULT_CELL_WIDTH);
        c.newParagraph().newText("cell B, contains this paragraph followed by "
                + "a list and another paragraph");
        fillList(c.newList(null), 1, 3);
        c.newParagraph().newText("Normal paragraph, follows the list.");

        row.newTableCell(RtfTableCell.DEFAULT_CELL_WIDTH).newParagraph().newText("cell C, simple");
    }

    private void fillList(RtfList list, int listIndex, int nItems)
    throws IOException {
        for (int i = 0; i < nItems; i++) {
            final RtfListItem item = list.newListItem();
            for (int j = 0; j <= i; j++) {
                final RtfParagraph para = item.newParagraph();
                para.newText("List " + listIndex + ", item " + i + ", paragraph " + j);
                if (i == 0 && j == 0) {
                    final String txt = "This item takes more than one line to check word-wrapping.";
                    para.newText(". " + "This list must have " + nItems
                            + " items. " + txt + " " + txt + " " + txt);
                }
            }
        }
    }
}
