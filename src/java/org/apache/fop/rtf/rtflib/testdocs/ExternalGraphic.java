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

import org.apache.fop.rtf.rtflib.rtfdoc.RtfDocumentArea;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfSection;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfExternalGraphic;
import org.apache.fop.rtf.rtflib.rtfdoc.RtfParagraph;

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
