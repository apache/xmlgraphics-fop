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

package org.apache.fop.rtf.rtflib.testdocs;

import java.io.IOException;

import org.apache.fop.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTable;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTableRow;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfTableCell;

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