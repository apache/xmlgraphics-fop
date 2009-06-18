/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.awt;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */

import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.image.*;
import org.apache.fop.svg.*;
import org.apache.fop.render.pdf.*;
import org.apache.fop.viewer.*;
import org.apache.fop.apps.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import java.awt.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.*;
import java.beans.*;
import javax.swing.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import java.text.*;

import org.apache.fop.render.AbstractRenderer;

/**
*/
public class AWTRenderer extends AbstractRenderer implements Printable, Pageable {

    protected int pageWidth = 0;
    protected int pageHeight = 0;
    protected double scaleFactor = 100.0;
    protected int pageNumber = 0;
    protected Vector pageList = new Vector();
    protected ProgressListener progressListener = null;
    protected Translator res = null;

    protected Hashtable fontNames = new Hashtable();
    protected Hashtable fontStyles = new Hashtable();
    protected Color saveColor = null;

    protected IDReferences idReferences = null;

    /**
     * Image Object and Graphics Object. The Graphics Object is the Graphics
     * object that is contained withing the Image Object.
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
     * The parent component, used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     */
    protected Component parent;

    public AWTRenderer(Translator aRes) {
        res = aRes;
    }

    public void setProducer(String producer) {
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
     * Sets parent component which is  used to set up the font.
     * This is needed as FontSetup needs a live AWT component
     * in order to generate valid font measures.
     * @param parent the live AWT component reference
     */
    public void setComponent(Component parent) {
        this.parent = parent;
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
