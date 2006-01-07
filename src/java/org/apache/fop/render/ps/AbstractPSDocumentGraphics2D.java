/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.OutputStream;
import java.io.IOException;

//FOP
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
public abstract class AbstractPSDocumentGraphics2D extends PSGraphics2D {

    protected static final Integer ZERO = new Integer(0);
    
    protected int width;
    protected int height;
    
    protected float viewportWidth;
    protected float viewportHeight;

    protected int pagecount;
    protected boolean pagePending;

    protected Shape initialClip;
    protected AffineTransform initialTransform;

    
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
    AbstractPSDocumentGraphics2D(boolean textAsShapes) {
        super(textAsShapes);

        if (!textAsShapes) {
            fontInfo = new FontInfo();
            FontSetup.setup(fontInfo, null, null);
        }
    }

    /**
     * Setup the document.
     * @param stream the output stream to write the document
     * @param width the width of the page
     * @param height the height of the page
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public void setupDocument(OutputStream stream, int width, int height) throws IOException {
        this.width = width;
        this.height = height;
        this.pagecount = 0;
        this.pagePending = false;

        //Setup for PostScript generation
        setPSGenerator(new PSGenerator(stream));
        
        writeFileHeader();
    }
    
    protected abstract void writeFileHeader() throws IOException;

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
    public AbstractPSDocumentGraphics2D(boolean textAsShapes, OutputStream stream,
                                 int width, int height) throws IOException {
        this(textAsShapes);
        setupDocument(stream, width, height);
    }

    /**
     * Set the dimensions of the SVG document that will be drawn.
     * This is useful if the dimensions of the SVG document are different
     * from the PostScript document that is to be created.
     * The result is scaled so that the SVG fits correctly inside the
     * PostScript document.
     * @param w the width of the page
     * @param h the height of the page
     * @throws IOException in case of an I/O problem
     */
    public void setSVGDimension(float w, float h) throws IOException {
        this.viewportWidth = w;
        this.viewportHeight = h;
        /*
        if (w != this.width || h != this.height) {
            gen.concatMatrix(width / w, 0, 0, height / h, 0, 0);
        }*/
    }

    /**
     * Set the background of the PostScript document.
     * This is used to set the background for the PostScript document
     * Rather than leaving it as the default white.
     * @param col the background colour to fill
     */
    public void setBackgroundColor(Color col) {
        /**(todo) Implement this */
        /*
        Color c = col;
        PDFColor currentColour = new PDFColor(c.getRed(), c.getGreen(), c.getBlue());
        currentStream.write("q\n");
        currentStream.write(currentColour.getColorSpaceOut(true));

        currentStream.write("0 0 " + width + " " + height + " re\n");

        currentStream.write("f\n");
        currentStream.write("Q\n");
        */
    }
    
    public int getPageCount() {
        return this.pagecount;
    }

    public void nextPage() throws IOException {
        closePage();
    }

    protected void closePage() throws IOException {
        if (!this.pagePending) {
            return; //ignore
        }
        //Finish page
        writePageTrailer();
        this.pagePending = false;         
    }
    
    /**
     * Writes the page header for a page.
     * @throws IOException In case an I/O error occurs
     */
    protected abstract void writePageHeader() throws IOException;
    
    /**
     * Writes the page trailer for a page.
     * @throws IOException In case an I/O error occurs
     */
    protected abstract void writePageTrailer() throws IOException;
    

    /** {@inheritDoc} */
    protected void preparePainting() {
        if (this.pagePending) {
            return;
        }
        try {
            startPage();
        } catch (IOException ioe) {
            handleIOException(ioe);
        }
    }

    protected void startPage() throws IOException {
        if (this.pagePending) {
            throw new IllegalStateException("Close page first before starting another");
        }
        //Start page
        this.pagecount++;
        
        if (this.initialTransform == null) {
            //Save initial transformation matrix
            this.initialTransform = getTransform();
            this.initialClip = getClip();      
        } else {
            //Reset transformation matrix
            setTransform(this.initialTransform);
            setClip(this.initialClip);
        }
          
        writePageHeader();
        if ((this.viewportWidth != this.width 
                || this.viewportHeight != this.height)
                && (this.viewportWidth > 0) && (this.viewportHeight > 0)){
            gen.concatMatrix(this.width / this.viewportWidth, 0, 
                       0, -1 * (this.height / this.viewportHeight), 
                       0, this.height);
        } else {
            gen.concatMatrix(1, 0, 0, -1, 0, this.height);
        }
        gen.writeDSCComment(DSCConstants.END_PAGE_SETUP);
        this.pagePending = true;
    }

    /**
     * The rendering process has finished.
     * This should be called after the rendering has completed as there is
     * no other indication it is complete.
     * This will then write the results to the output stream.
     * @throws IOException an io exception if there is a problem
     *         writing to the output stream
     */
    public void finish() throws IOException {
        if (this.pagePending) {
            closePage();
        }
        
        //Finish document
        gen.writeDSCComment(DSCConstants.TRAILER);
        gen.writeDSCComment(DSCConstants.PAGES, new Integer(this.pagecount));
        gen.writeDSCComment(DSCConstants.EOF);
        gen.flush();
    }
    
    /**
     * This constructor supports the create method
     * @param g the PostScript document graphics to make a copy of
     */
    public AbstractPSDocumentGraphics2D(AbstractPSDocumentGraphics2D g) {
        super(g);
    }
    

}

