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
import org.apache.fop.image.ImageArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.render.pdf.FontSetup;

import org.apache.fop.svg.SVGArea;

import org.apache.log.Logger;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Abstract base class for all renderers.
 * 
 */
public abstract class AbstractRenderer implements Renderer {
    protected Logger log;

    public void setLogger(Logger logger) {
        log = logger;
    }

}
