/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.batik.bridge.*;
import org.apache.batik.swing.svg.*;
import org.apache.batik.swing.gvt.*;
import org.apache.batik.gvt.*;
import org.apache.batik.gvt.renderer.*;
import org.apache.batik.gvt.filter.*;
import org.apache.batik.gvt.event.*;

import org.apache.avalon.framework.logger.Logger;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.css.*;
import org.w3c.dom.svg.SVGLength;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Dimension;

public class SVGUserAgent implements UserAgent {
    AffineTransform currentTransform = null;
    Logger log;

    /**
     * Creates a new SVGUserAgent.
     */
    public SVGUserAgent(AffineTransform at) {
        currentTransform = at;
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    /**
     * Displays an error message.
     */
    public void displayError(String message) {
        log.error(message);
    }

    /**
     * Displays an error resulting from the specified Exception.
     */
    public void displayError(Exception ex) {
        log.error("SVG Error" + ex.getMessage(), ex);
    }

    /**
     * Displays a message in the User Agent interface.
     * The given message is typically displayed in a status bar.
     */
    public void displayMessage(String message) {
        log.info(message);
    }

    /**
     * Returns a customized the pixel to mm factor.
     */
    public float getPixelToMM() {
        // this is set to 72dpi as the values in fo are 72dpi
        return 0.35277777777777777778f; // 72 dpi
        // return 0.26458333333333333333333333333333f;    // 96dpi
    }

    /**
     * Returns the language settings.
     */
    public String getLanguages() {
        return "en"; // userLanguages;
    }

    public String getMedia() {
        return "";
    }

    /**
     * Returns the user stylesheet uri.
     * @return null if no user style sheet was specified.
     */
    public String getUserStyleSheetURI() {
        return null; // userStyleSheetURI;
    }

    /**
     * Returns the class name of the XML parser.
     */
    public String getXMLParserClassName() {
        return org.apache.fop.apps.Driver.getParserClassName();
    }

    /**
     * Opens a link in a new component.
     * @param doc The current document.
     * @param uri The document URI.
     */
    public void openLink(SVGAElement elt) {
    }


    public Point getClientAreaLocationOnScreen() {
        return new Point(0, 0);
    }

    public void setSVGCursor(java.awt.Cursor cursor) {}

    public AffineTransform getTransform() {
        return currentTransform;
    }

    public Dimension2D getViewportSize() {
        return new Dimension(100, 100);
    }

    public EventDispatcher getEventDispatcher() {
        return null;
    }

    public boolean supportExtension(String str) {
        return false;
    }

    public boolean hasFeature(String str) {
        return false;
    }

    public boolean isXMLParserValidating() {
        return true;
    }

    public void registerExtension(BridgeExtension be) {}

    public void handleElement(Element elt, Object data) {}


}

