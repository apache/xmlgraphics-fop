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

import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;

/**  Generates a simple RTF test document for the jfor rtflib package.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */
public class ParagraphAlignment extends TestDocument {

    /**
     * Constructor
     */
    public ParagraphAlignment() {
    }

    /**
     * Generate the document.
     * @param rda RtfDocumentArea
     * @param sect RtfSection
     * @throws java.io.IOException for I/O errors
     */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect) throws java.io.IOException
    {
        RtfAttributes attr = new RtfAttributes ();
        attr.set(RtfText.ALIGN_CENTER);
        RtfParagraph p = sect.newParagraph (attr);
        p.newLineBreak();
        p.newLineBreak();
        p.newText ("Centered title");
        p.newLineBreak();
        p.close();

        attr = new RtfAttributes ();
        attr.set(RtfText.ALIGN_LEFT);
        p = sect.newParagraph (attr);
        p.newLineBreak();
        p.newText ("This is the left aligned text.");
        p.newLineBreak();
        p.close();

        attr = new RtfAttributes ();
        attr.set(RtfText.ALIGN_RIGHT);
        p = sect.newParagraph (attr);
        p.newLineBreak();
        p.newText ("This is the right aligned text.");
        p.newLineBreak();
        p.close();
    }
}
