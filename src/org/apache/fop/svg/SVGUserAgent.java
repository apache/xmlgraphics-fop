/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import org.apache.fop.fo.FOUserAgent;

import org.apache.batik.bridge.UserAgentAdapter;

import org.apache.avalon.framework.logger.Logger;

// Java
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Dimension;

/**
 * The SVG user agent.
 * This is an implementation of the batik svg user agent
 * for handling errors and getting user agent values.
 */
public class SVGUserAgent extends UserAgentAdapter {
    private AffineTransform currentTransform = null;
    private Logger log;
    private FOUserAgent userAgent;

    /**
     * Creates a new SVGUserAgent.
     * @param ua the FO user agent
     * @param at the current transform
     */
    public SVGUserAgent(FOUserAgent ua, AffineTransform at) {
        currentTransform = at;
        userAgent = ua;
        log = userAgent.getLogger();
    }

    /**
     * Displays an error message.
     * @param message the message to display
     */
    public void displayError(String message) {
        log.error(message);
    }

    /**
     * Displays an error resulting from the specified Exception.
     * @param ex the exception to display
     */
    public void displayError(Exception ex) {
        log.error("SVG Error" + ex.getMessage(), ex);
    }

    /**
     * Displays a message in the User Agent interface.
     * The given message is typically displayed in a status bar.
     * @param message the message to display
     */
    public void displayMessage(String message) {
        log.info(message);
    }

    /**
     * Shows an alert dialog box.
     * @param message the message to display
     */
    public void showAlert(String message) {
        log.warn(message);
    }

    /**
     * Returns a customized the pixel to mm factor.
     * @return the pixel unit to millimeter conversion factor
     */
    public float getPixelUnitToMillimeter() {
        return userAgent.getPixelUnitToMillimeter();
    }

    /**
     * Returns the language settings.
     * @return the languages supported
     */
    public String getLanguages() {
        return "en"; // userLanguages;
    }

    /**
     * Returns the media type for this rendering.
     * @return the media for fo documents is "print"
     */
    public String getMedia() {
        return "print";
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
     * @return the XML parser class name
     */
    public String getXMLParserClassName() {
        return org.apache.fop.apps.Driver.getParserClassName();
    }

    /**
     * Is the XML parser validating.
     * @return true if the xml parser is validating
     */
    public boolean isXMLParserValidating() {
        return false;
    }

    /**
     * Get the transform of the svg document.
     * @return the transform
     */
    public AffineTransform getTransform() {
        return currentTransform;
    }

    /**
     * Get the default viewport size for an svg document.
     * This returns a default value of 100x100.
     * @return the default viewport size
     */
    public Dimension2D getViewportSize() {
        return new Dimension(100, 100);
    }

}

