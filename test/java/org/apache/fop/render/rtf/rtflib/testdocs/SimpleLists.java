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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListItem;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListStyle;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListStyleNumber;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;

/**  Generates a simple RTF test document for the jfor rtflib package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class SimpleLists extends TestDocument {
    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        sect.newParagraph().newText("First paragraph of the 'SimpleLists' RTF test document.");
        sect.newParagraph().newText("First bulleted list with 5 items.");
        makeList(sect, 1, 5, null);
        sect.newParagraph().newText("Normal paragraph between lists 1 and 2.");
        makeList(sect, 2, 3, null);
        sect.newParagraph().newText("Normal paragraph after list 2.");

        sect.newParagraph().newText("Now a numbered list (4 items):");
        makeList(sect, 3, 4, new RtfListStyleNumber());
    }

    private void makeList(RtfSection sect, int listIndex, int nItems, RtfListStyle ls)
    throws IOException {
        final RtfList list = sect.newList(null);
        if (ls != null) {
            list.setRtfListStyle(ls);
        }
        for (int i = 0; i < nItems; i++) {
            final RtfListItem item = list.newListItem();
            for (int j = 0; j <= i; j++) {
                final RtfParagraph para = item.newParagraph();
                para.newText("List " + listIndex + ", item " + i + ", paragraph " + j);
                if (i == 0 && j == 0) {
                    final String txt = "This item takes more than one line to check word-wrapping.";
                    para.newText(". " + "This list should have " + nItems
                            + " items. " + txt + " " + txt + " " + txt);
                }
            }
        }
    }
}
