/*
 * $Id$
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

