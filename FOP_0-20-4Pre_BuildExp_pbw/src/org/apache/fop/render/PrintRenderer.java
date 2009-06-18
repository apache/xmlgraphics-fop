/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.render;

// FOP
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.layout.FontInfo;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Abstract base class of "Print" type renderers.
 * 
 */
public abstract class PrintRenderer extends AbstractRenderer {
    protected FontInfo fontInfo;

    /**
     * set up the font info
     * 
     * @param fontInfo font info to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        FontSetup.setup(fontInfo);
    }

    /**
     *
     */
    public void startRenderer(OutputStream outputStream)
    throws IOException {}

    /**
     *
     */
    public void stopRenderer()
    throws IOException
    {
    }

}
