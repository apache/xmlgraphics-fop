/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Author : Seshadri G

package org.apache.fop.render.mif;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.render.AbstractRenderer;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.fo.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.svg.*;
import org.apache.fop.mif.*;
import org.apache.fop.layout.*;
import org.apache.fop.image.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.svg.SVGLength;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.Hashtable;

/**
 * Renderer that renders areas to MIF
 *
 */
public class MIFRenderer extends AbstractRenderer {

    private String currentFontName;
    private String currentFontSize;
    private int pageHeight;
    private int pageWidth;

    /**
     * the current vertical position in millipoints from bottom
     */
    protected int currentYPosition = 0;

    /**
     * the current horizontal position in millipoints from left
     */
    protected int currentXPosition = 0;

    /**
     * the horizontal position of the current area container
     */
    private int currentAreaContainerXPosition = 0;


    /**
     * the MIF Document being created
     */
    protected MIFDocument mifDoc;


    /* is a table currently open? */
    private boolean inTable = false;

    /**
     * create the MIF renderer
     */
    public MIFRenderer() {
        this.mifDoc = new MIFDocument();
    }

    public void startRenderer(OutputStream outputStream)
    throws IOException {}

    /**
     * set up the given FontInfo
     */
    public void setupFontInfo(FontInfo fontInfo) {

        FontSetup.setup(fontInfo);
        // FontSetup.addToFontFormat(this.mifDoc, fontInfo);

    }

    /**
     * set the producer of the rendering
     */
    public void setProducer(String producer) {}


    /**
    */
    public void stopRenderer()
    throws IOException {
        log.info("writing out MIF");
    }

}

