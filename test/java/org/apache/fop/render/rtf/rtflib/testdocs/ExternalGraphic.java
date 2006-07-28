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
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfExternalGraphic;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfParagraph;

import java.io.IOException;
/**
 * Generate a test document containing external graphics.
 *
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 */
class ExternalGraphic extends TestDocument {
    private String file = "file:///tmp/jfor-images/logo.";

    //////////////////////////////////////////////////
    // @@ Construction
    //////////////////////////////////////////////////

    /**
     * Default constructor.
     */
    public ExternalGraphic () {

    }
    /** generate the body of the test document */
    protected void generateDocument (RtfDocumentArea rda, RtfSection sect) throws IOException {
        RtfParagraph p = sect.newParagraph ();
        p.newLineBreak();
        p.newLineBreak();
        p.newLineBreak();
        p.newText ("EMF image with 150 % height");
        p.newLineBreak();
        RtfExternalGraphic imageA = p.newImage ();
        imageA.setURL (file + "emf");
        imageA.setHeight ("150%");
        p.newLineBreak();
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newText ("PNG image with 150 % width");
        p.newLineBreak();
        RtfExternalGraphic imageB = sect.newImage ();
        imageB.setURL (file + "png");
        imageB.setWidth ("150%");
        p.newLineBreak();
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newLineBreak();
        p.newText ("JPG image with width = 200px and height = 20 px");
        p.newLineBreak();
        RtfExternalGraphic imageC = sect.newImage ();
        imageC.setURL (file + "jpg");
        imageC.setWidth ("200");
        imageC.setHeight ("20");
        p.newLineBreak();
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newLineBreak();
        p.newText ("GIF image with width = 200px and scaling = 'uniform', that means the image "
                + "size will adjusted automatically");
        p.newLineBreak();
        RtfExternalGraphic imageD = sect.newImage ();
        imageD.setURL (file + "gif");
        imageD.setWidth ("200");
        imageD.setScaling ("uniform");
        p.newLineBreak();
        p.close();

        p = sect.newParagraph();
        p.newLineBreak();
        p.newLineBreak();
        p.newText ("GIF image");
        p.newLineBreak();
        RtfExternalGraphic imageE = sect.newImage ();
        imageE.setURL (file + "gif");
        p.newLineBreak();
        p.close();

    }



}
