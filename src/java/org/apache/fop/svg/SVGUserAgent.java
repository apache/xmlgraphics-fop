/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

/* $Id: SVGUserAgent.java,v 1.2 2004/02/27 17:43:22 jeremias Exp $ */
 
package org.apache.fop.svg;

import org.apache.fop.apps.FOFileHandler;
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
    private Logger logger;
    private float pixelUnitToMillimeter = 0.0f;

    /**
     * Creates a new SVGUserAgent.
     * @param log an Avalon logging instance
     * @param pixelUnitToMM The pixel to millimeter conversion factor 
     *                  currently in effect
     * @param at the current transform
     */
    public SVGUserAgent(Logger log, float pixelUnitToMM, AffineTransform at) {
        logger = log;
        pixelUnitToMillimeter = pixelUnitToMM;
        currentTransform = at;
    }

    /**
     * Returns the logger associated with this user agent.
     * @return Logger the logger
     */
    protected final Logger getLogger() {
        return logger;
    }

    /**
     * Displays an error message.
     * @param message the message to display
     */
    public void displayError(String message) {
        logger.error(message);
    }

    /**
     * Displays an error resulting from the specified Exception.
     * @param ex the exception to display
     */
    public void displayError(Exception ex) {
        logger.error("SVG Error" + ex.getMessage(), ex);
    }

    /**
     * Displays a message in the User Agent interface.
     * The given message is typically displayed in a status bar.
     * @param message the message to display
     */
    public void displayMessage(String message) {
        logger.info(message);
    }

    /**
     * Shows an alert dialog box.
     * @param message the message to display
     */
    public void showAlert(String message) {
        logger.warn(message);
    }

    /**
     * Returns a customized the pixel to mm factor.
     * @return the pixel unit to millimeter conversion factor
     */
    public float getPixelUnitToMillimeter() {
        return pixelUnitToMillimeter;
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
        return FOFileHandler.getParserClassName();
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

