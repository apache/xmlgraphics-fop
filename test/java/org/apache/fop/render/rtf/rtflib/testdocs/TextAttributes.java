/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.render.rtf.rtflib.testdocs;

import java.io.IOException;

import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;

/**  Generates a simple RTF test document for the jfor rtflib package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class TextAttributes extends TestDocument {
    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect)
    throws IOException {
        final RtfParagraph para = sect.newParagraph();
        para.newText("This is normal\n");
        para.newText("This is bold\n", new RtfAttributes().set(RtfText.ATTR_BOLD));
        para.newText("This is italic\n", new RtfAttributes().set(RtfText.ATTR_ITALIC));
        para.newText("This is underline\n", new RtfAttributes().set(RtfText.ATTR_UNDERLINE));

        // RTF font sizes are in half-points
        para.newText("This is size 48\n", new RtfAttributes().set(RtfText.ATTR_FONT_SIZE, 96));

        para.newText(
            "This is bold and italic\n",
            new RtfAttributes().set(RtfText.ATTR_BOLD).set(RtfText.ATTR_ITALIC)
          );

        final RtfAttributes attr = new RtfAttributes();
        attr.set(RtfText.ATTR_BOLD).set(RtfText.ATTR_ITALIC);
        attr.set(RtfText.ATTR_UNDERLINE);
        attr.set(RtfText.ATTR_FONT_SIZE, 72);
        para.newText("This is bold, italic, underline and size 36\n", attr);

        para.newText("This is back to normal\n");
    }
}