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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfHyperLink;

import java.io.IOException;

/**
 * Class <code>BasicLink</code> here.
 *
 * @author <a href="mailto:mks@ANDREAS">Andreas Putz</a>
 */

public class BasicLink extends TestDocument {
    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public BasicLink() {
    }

    /** generate the body of the test document
     * @param rda RtfDocumentArea
     * @param sect RtfSection
     * @throws IOException for I/O Errors
     */
    protected void generateDocument(RtfDocumentArea rda, RtfSection sect) throws IOException {
        RtfParagraph p = sect.newParagraph();
        p.newLineBreak();
        p.newLineBreak();
        p.newLineBreak();
        p.newText("external link: ");
        RtfHyperLink link = p.newHyperLink("click here to go to the hompage", null);
        link.setExternalURL("http://www.skynamics.com");
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newText("here we will demonstrate internal link to a bookmark");
        p.newLineBreak();
        p.newText("internal link: ");
        link = p.newHyperLink("click here to go to the bookmark", null);
        link.setInternalURL("testBookmark");
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newLineBreak();
        p.newLineBreak();
        p.newPageBreak();
        p.newBookmark("testBookmark");
        p.newText("testBookmark");
    }
}
