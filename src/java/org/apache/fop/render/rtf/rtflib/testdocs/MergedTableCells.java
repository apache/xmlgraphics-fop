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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;

/**  Generates an RTF test document containing merged table cells
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
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
            c.setHMerge(c.MERGE_START);
            c.newParagraph().newText("cell 0,0, width 80mm, merge start, "
                    + "followed by two merged cells totalling 80mm width.");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setHMerge(c.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("THIS IS IN AN HMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setHMerge(c.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("THIS IS IN AN HMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");
        }

        // second row, start vertical merging in column 1
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(c.MERGE_START);
            c.newParagraph().newText("cell 1,0, vertical merge start, 40mm, spans three rows.");

            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText("cell 1,1, no merge, 80mm");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(c.MERGE_START);
            c.newParagraph().newText("cell 1,2, vertical merge start, 40mm, spans two rows.");
        }

        // third row, column 1 merged with previous row
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(c.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 2,0, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 2,1, no merge, 40mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 2,2, no merge, 40mm");

            c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(c.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 2,3, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");
        }

        // fourth row, column 1 merged with previous row
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(40 * MM_TO_TWIPS);
            c.setVMerge(c.MERGE_WITH_PREVIOUS);
            c.newParagraph().newText("cell 3,0, VMERGED CELL, MUST NOT APPEAR IN RTF DOCUMENT");

            r.newTableCell(10 * MM_TO_TWIPS).newParagraph().newText("cell 3,1, no merge, 10mm");
            r.newTableCell(30 * MM_TO_TWIPS).newParagraph().newText("cell 3,2, no merge, 30mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 3,3, no merge, 40mm");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText("cell 3,4, no merge, 40mm");
        }

        // fifth row, just one cell
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(160 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 4,0, width 160mm, only cell in this row");
        }
    }
}