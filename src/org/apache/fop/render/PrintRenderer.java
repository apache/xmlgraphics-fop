/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */
package org.apache.fop.render;

// FOP
import org.apache.fop.render.pdf.FontSetup;
import org.apache.fop.layout.FontInfo;

// Java
import java.io.IOException;
import java.io.OutputStream;

/** Abstract base class of "Print" type renderers.  */
public abstract class PrintRenderer extends AbstractRenderer {

    /** Font configuration */
    protected FontInfo fontInfo;

    /**
     * Set up the font info
     *
     * @param fontInfo  font info to set up
     */
    public void setupFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        FontSetup.setup(fontInfo, null);
    }

    /** @see org.apache.fop.render.Renderer */
    public void startRenderer(OutputStream outputStream)
        throws IOException { }

    /** @see org.apache.fop.render.Renderer */
    public void stopRenderer()
        throws IOException { }

}
