/*
 * $Id: AWTRenderer.java,v 1.44 2003/03/07 09:46:31 jeremias Exp $
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
package org.apache.fop.render.awt;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.fop.layout.FontInfo;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.viewer.PreviewDialog;
import org.apache.fop.viewer.Translator;

/**
 * This is FOP's AWT renderer.
 */
public class AWTRenderer extends AbstractRenderer implements Printable, Pageable {

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected List pageList = new java.util.Vector();
    //protected ProgressListener progressListener = null;

    /**
     * The resource bundle used for AWT messages.
     */
    protected Translator translator = null;

    protected Map fontNames = new java.util.Hashtable();
    protected Map fontStyles = new java.util.Hashtable();
    protected Color saveColor = null;

    /**
     * Image Object and Graphics Object. The Graphics Object is the Graphics
     * object that is contained within the Image Object.
     */
    private BufferedImage pageImage = null;
    private Graphics2D graphics = null;

    /**
     * The current (internal) font name
    */
    protected String currentFontName;

    /**
     * The current font size in millipoints
     */
    protected int currentFontSize;

    /**
     * The current colour's red, green and blue component
     */
    protected float currentRed = 0;
    protected float currentGreen = 0;
    protected float currentBlue = 0;

    /**
     * The preview dialog frame used for display of the documents.
     * Also used as the AWT Component for FontSetup in generating
     * valid font measures.
     */
    protected PreviewDialog frame;
    
    public AWTRenderer() {
        translator = new Translator();
    }

    public Translator getTranslator() {
        return translator;
    }

    public int getPageCount() {
        return 0;
    }

    public void setupFontInfo(FontInfo fontInfo) {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        FontSetup.setup(fontInfo, fontImage.createGraphics());
    }

    /**
     * Sets the preview dialog frame used for display of the documents.
     * @param frame the PreviewDialog frame
     */
    public void setPreviewDialog(PreviewDialog frame) {
        this.frame = frame;
        frame.setStatus(translator.getString("Status.Build.FO.tree"));
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int aValue) {
        pageNumber = aValue;
    }

    public void setScaleFactor(double newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public BufferedImage getLastRenderedPage() {
        return pageImage;
    }

    public void startRenderer(OutputStream out)
    throws IOException {
    }

    public void stopRenderer()
    throws IOException {
        frame.setStatus(translator.getString("Status.Show"));
        frame.showPage();
    }

    // Printable Interface
    public PageFormat getPageFormat(int pos) {
        return null;
    }

    public Printable getPrintable(int pos) {
        return null;
    }

    public int getNumberOfPages() {
        return 0;
    }

    public int print(Graphics g, PageFormat format, int pos) {
        return 0;
    }
}
