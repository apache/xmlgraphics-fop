/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 2003 The Apache Software Foundation. All rights reserved.
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
package org.apache.fop.render.ps;

import java.io.IOException;

/**
 * This class is a wrapper for the <tt>AbstractPSDocumentGraphics2D</tt> that
 * is used to create EPS (Encapsulated PostScript) files instead of PS file.
 *
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 * @see org.apache.fop.render.ps.PSGraphics2D
 * @see org.apache.fop.render.ps.AbstractPSDocumentGraphics2D
 */
public class EPSDocumentGraphics2D extends AbstractPSDocumentGraphics2D {

    /**
     * Create a new EPSDocumentGraphics2D.
     * This is used to create a new EPS document, the height,
     * width and output stream can be setup later.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     */
    public EPSDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);
    }

    protected void writeFileHeader() throws IOException {
        final Long pagewidth = new Long(this.width);
        final Long pageheight = new Long(this.height);

        //PostScript Header
        gen.writeln(DSCConstants.PS_ADOBE_30 + " " + DSCConstants.EPSF_30);
        gen.writeDSCComment(DSCConstants.CREATOR, 
                    new String[] {"FOP EPS Transcoder for SVG"});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, 
                    new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(0));
        gen.writeDSCComment(DSCConstants.BBOX, new Object[]
                {ZERO, ZERO, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(2));
        gen.writeDSCComment(DSCConstants.END_COMMENTS);
        
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        PSProcSets.writeFOPStdProcSet(gen);
        PSProcSets.writeFOPEPSProcSet(gen);
        if (document != null) {
            PSProcSets.writeFontDict(gen, document);
        }
        gen.writeDSCComment(DSCConstants.END_PROLOG);
    }

    protected void writePageHeader() throws IOException {
        Integer pageNumber = new Integer(this.pagecount);
        gen.writeDSCComment(DSCConstants.PAGE, new Object[] 
                {pageNumber.toString(), pageNumber});
        gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                {ZERO, ZERO, new Integer(width), new Integer(height)});
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);
        if (this.document != null) {         
            gen.writeln("FOPFonts begin");
        }
    }
    
    protected void writePageTrailer() throws IOException {
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeDSCComment(DSCConstants.END_PAGE);
    }

}
