/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.svg;

import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.image.*;
import org.apache.fop.svg.SVGArea;
import org.apache.fop.svg.SVGUtilities;

import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMImplementation;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.dom.util.XMLSupport;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.OutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.swing.ImageIcon;

import org.apache.fop.render.AbstractRenderer;

public class SVGRenderer extends AbstractRenderer {
    static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
    Document svgDocument;
    Element svgRoot;
    Element currentPageG = null;
    Element lastLink = null;

    float totalWidth = 0;
    float totalHeight = 0;

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected int pageNumber = 0;

    protected Hashtable fontNames = new Hashtable();
    protected Hashtable fontStyles = new Hashtable();
    protected Color saveColor = null;

    protected IDReferences idReferences = null;

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

    public SVGRenderer() {
    }

    public void setupFontInfo(FontInfo fontInfo) {
        // create a temp Image to test font metrics on
        BufferedImage fontImage =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        org.apache.fop.render.awt.FontSetup.setup(fontInfo, fontImage.createGraphics());
    }

    public void setProducer(String producer) {
    }

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
