/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */

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