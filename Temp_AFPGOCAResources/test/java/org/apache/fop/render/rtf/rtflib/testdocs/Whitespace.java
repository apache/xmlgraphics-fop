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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;

/**  Generates an RTF document to test the WhitespaceCollapser
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class Whitespace extends TestDocument {
    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        final RtfParagraph p1 = sect.newParagraph();
        p1.newText("\t  Each word  of this paragraph must   be "
                   + "separated\tfrom\t\n\tthe next word with exactly\t \tone");
        p1.newText("   space.");

        final RtfParagraph p2 = sect.newParagraph();
        p2.newText("");
        p2.newText("In this");
        p2.newText(" paragraph ");
        p2.newText("as well,");
        p2.newText("   there must\tbe    \t");
        p2.newText("exactly");
        p2.newText(" one space   ");
        p2.newText("between  each\tword and the  next, and no spaces at the "
                   + "beginning or end of the paragraph.");

        final RtfParagraph p3 = sect.newParagraph();
        p3.newText("The word 'boomerang' must be written after this with no funny spacing: ");
        p3.newText("boo");
        p3.newText("me");
        p3.newText("r");
        p3.newText("a");
        p3.newText("ng.");
    }
}