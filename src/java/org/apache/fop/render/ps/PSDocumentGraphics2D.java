/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

//Java
import java.awt.Graphics;
import java.io.OutputStream;
import java.io.IOException;

//FOP
import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;

/**
 * This class is a wrapper for the <tt>PSGraphics2D</tt> that
 * is used to create a full document around the PostScript rendering from
 * <tt>PSGraphics2D</tt>.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 * @see org.apache.fop.render.ps.PSGraphics2D
 */
public class PSDocumentGraphics2D extends AbstractPSDocumentGraphics2D {

    
    /**
     * Create a new AbstractPSDocumentGraphics2D.
     * This is used to create a new PostScript document, the height,
     * width and output stream can be setup later.
     * For use by the transcoder which needs font information
     * for the bridge before the document size is known.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     */
    PSDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);

        if (!textAsShapes) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo, null);
        }
    }

    /**
     * Create a new AbstractPSDocumentGraphics2D.
     * This is used to create a new PostScript document of the given height
     * and width.
     * The resulting document is written to the stream after rendering.
     *
     * @param textAsShapes set this to true so that text will be rendered
     * using curves and not the font.
     * @param stream the stream that the final document should be written to.
     * @param width the width of the document
     * @param height the height of the document
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public PSDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws IOException {
        this(textAsShapes);
        setupDocument(stream, width, height);
    }

    public void nextPage() throws IOException {
        closePage();
    }

    protected void writeFileHeader() throws IOException {
        final Long pagewidth = new Long(this.width);
        final Long pageheight = new Long(this.height);

        //PostScript Header
        gen.writeln(DSCConstants.PS_ADOBE_30);
        gen.writeDSCComment(DSCConstants.CREATOR,
                    new String[] {"Apache FOP " + Version.getVersion() 
                        + ": PostScript Transcoder for SVG"});
        gen.writeDSCComment(DSCConstants.CREATION_DATE,
                    new Object[] {new java.util.Date()});
        gen.writeDSCComment(DSCConstants.PAGES, PSGenerator.ATEND);
        gen.writeDSCComment(DSCConstants.BBOX, new Object[]
                {ZERO, ZERO, pagewidth, pageheight});
        gen.writeDSCComment(DSCConstants.END_COMMENTS);
        
        //Defaults
        gen.writeDSCComment(DSCConstants.BEGIN_DEFAULTS);
        gen.writeDSCComment(DSCConstants.END_DEFAULTS);
        
        //Prolog
        gen.writeDSCComment(DSCConstants.BEGIN_PROLOG);
        gen.writeDSCComment(DSCConstants.END_PROLOG);
        
        //Setup
        gen.writeDSCComment(DSCConstants.BEGIN_SETUP);
        PSProcSets.writeFOPStdProcSet(gen);
        PSProcSets.writeFOPEPSProcSet(gen);
        if (fontInfo != null) {
            PSFontUtils.writeFontDict(gen, fontInfo);
        }
        gen.writeDSCComment(DSCConstants.END_SETUP);
    }

    protected void writePageHeader() throws IOException {
        Integer pageNumber = new Integer(this.pagecount);
        gen.writeDSCComment(DSCConstants.PAGE, new Object[] 
                {pageNumber.toString(), pageNumber});
        gen.writeDSCComment(DSCConstants.PAGE_BBOX, new Object[]
                {ZERO, ZERO, new Integer(width), new Integer(height)});
        gen.writeDSCComment(DSCConstants.BEGIN_PAGE_SETUP);
        gen.writeln("<<");
        gen.writeln("/PageSize [" + width + " " + height + "]");
        gen.writeln("/ImagingBBox null");
        gen.writeln(">> setpagedevice");
        if (fontInfo != null) {         
            gen.writeln("FOPFonts begin");
        }
    }
    
    protected void writePageTrailer() throws IOException {
        gen.writeln("showpage");        
        gen.writeDSCComment(DSCConstants.PAGE_TRAILER);
        gen.writeDSCComment(DSCConstants.END_PAGE);
    }
    
    /**
     * This constructor supports the create method
     * @param g the PostScript document graphics to make a copy of
     */
    public PSDocumentGraphics2D(PSDocumentGraphics2D g) {
        super(g);
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     * @return     a new graphics context that is a copy of
     * this graphics context.
     */
    public Graphics create() {
        return new PSDocumentGraphics2D(this);
    }

}

