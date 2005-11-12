/*
 * Copyright 2003-2005 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

import java.io.IOException;

import org.apache.fop.Version;

/**
 * This class is a wrapper for the <tt>AbstractPSDocumentGraphics2D</tt> that
 * is used to create EPS (Encapsulated PostScript) files instead of PS file.
 *
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
                    new String[] {"Apache FOP " + Version.getVersion() 
                        + ": EPS Transcoder for SVG"});
        gen.writeDSCComment(DSCConstants.CREATION_DATE, 
                    new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(0));
        gen.writeDSCComment(DSCConstants.BBOX, new Object[]
                {ZERO, ZERO, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.LANGUAGE_LEVEL, new Integer(gen.getPSLevel()));
        gen.writeDSCComment(DSCConstants.END_COMMENTS);
        
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        PSProcSets.writeFOPStdProcSet(gen);
        PSProcSets.writeFOPEPSProcSet(gen);
        if (fontInfo != null) {
            PSFontUtils.writeFontDict(gen, fontInfo);
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
        if (fontInfo != null) {         
            gen.writeln("FOPFonts begin");
        }
    }
    
    protected void writePageTrailer() throws IOException {
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeDSCComment(DSCConstants.END_PAGE);
    }

}
