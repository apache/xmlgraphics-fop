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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTableCell;

/**  Generates an RTF document to test nested tables with the jfor rtflib package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class NestedTable extends TestDocument {
    private static final int MM_TO_TWIPS = (int)(1440f / 25.4f);

    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("This document demonstrates pseudo-nested "
                + "tables created using merged table cells");

        firstTestTable(sect);
        RtfParagraph p = sect.newParagraph();
        p.newText("Test continues on next page.");
        p.newPageBreak();
        secondTestTable(sect);

        p = sect.newParagraph();
        p.newText("Test continues on next page.");
        p.newPageBreak();
        thirdTestTable(sect);

        sect.newParagraph().newText("End of nested tables test document");
    }

    private void firstTestTable(RtfSection sect)
    throws IOException {

        sect.newParagraph().newText("First test: table with one nested table in cell 1,1");
        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());
        // first row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(160 * MM_TO_TWIPS);
            c.newParagraph().newText("cell 0,0, width 160mm, only cell in this row.");
        }

        // second row contains nested table
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,0, width 40mm, to the left of nested table.");

            final RtfTableCell c = r.newTableCell(80 * MM_TO_TWIPS);
            c.newParagraph().newText("cell 1,1, width 80mm, this text is "
                    + "followed by a nested table in the same cell, followed "
                    + "by text that says 'AFTER NESTED TABLE'.");
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 1);
            c.newParagraph().newText("AFTER NESTED TABLE");

            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,2, width 40mm, to the right of nested table.");
        }

        // third row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,0, width 80mm, this row has two cells.");
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,1, width 80mm, last cell.");
        }

    }

    private void secondTestTable(RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("Second test: table with two nested tables in cell 1,1");
        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());
        // first row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(160 * MM_TO_TWIPS);
            c.newParagraph().newText("second test table: cell 0,0, width 160mm, "
                    + "only cell in this row.");
        }

        // second row contains nested table
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,0, width 40mm, to the left of nested tables.");

            final RtfTableCell c = r.newTableCell(80 * MM_TO_TWIPS);
            c.newParagraph().newText("cell 1,1, width 80mm, this text is "
                    + "followed by a nested table in the same cell, followed "
                    + "by text that says 'BETWEEN', then another table, then 'AFTER'.");
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 2);
            c.newParagraph().newText("BETWEEN");
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 3);
            c.newParagraph().newText("AFTER");

            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,2, width 40mm, to the right of nested table.");
        }

        // third row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,0, width 80mm, this row has two cells.");
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,1, width 80mm, last cell.");
        }
    }

    private void thirdTestTable(RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("Third test: table with two nested tables "
                + "in cell 1,1 and one nested table in cell 0,1");
        final RtfTable tbl = sect.newTable(new DummyTableColumnsInfo());
        // first row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            RtfTableCell c = r.newTableCell(80 * MM_TO_TWIPS);
            c.newParagraph().newText("third test table: cell 0,0, width 40mm, "
                    + "the cell to its right contains a nested table with no other text.");
            c = r.newTableCell(80 * MM_TO_TWIPS);
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 4);
        }

        // second row contains nested table
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,0, width 40mm, to the left of nested tables.");

            final RtfTableCell c = r.newTableCell(80 * MM_TO_TWIPS);
            c.newParagraph().newText("cell 1,1, width 80mm, this text is "
                    + "followed by a nested table in the same cell, followed "
                    + "by text that says 'BETWEEN', then another table, then 'AFTER'.");
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 5);
            c.newParagraph().newText("BETWEEN");
            fillNestedTable(c.newTable(new DummyTableColumnsInfo()), 6);
            c.newParagraph().newText("AFTER");

            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 1,2, width 40mm, to the right of nested table.");
        }

        // third row, normal
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,0, width 80mm, this row has two cells.");
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText
                    ("cell 2,1, width 80mm, last cell.");
        }
    }

    /** fill the nested table */
    private void fillNestedTable(RtfTable tbl, int index)
    throws IOException {
        final String id = "TABLE " + index;
        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(80 * MM_TO_TWIPS).newParagraph().newText(
            id + ":nested cell 0,0. Nested table contains 3 rows with 1,2 and 3 cells respectively"
            );
        }

        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText(id + ":nested cell 1,0, 40mm.");
            r.newTableCell(40 * MM_TO_TWIPS).newParagraph().newText(id + ":nested cell 1,1, 40mm.");
        }

        {
            RtfTableRow r = tbl.newTableRow();
            r.newTableCell(30 * MM_TO_TWIPS).newParagraph().newText(id + ":nested cell 2,0, 30mm.");
            r.newTableCell(30 * MM_TO_TWIPS).newParagraph().newText(id + ":nested cell 2,1, 30mm.");
            r.newTableCell(20 * MM_TO_TWIPS).newParagraph().newText(id + ":nested cell 2,2, 20mm.");
        }
    }
}