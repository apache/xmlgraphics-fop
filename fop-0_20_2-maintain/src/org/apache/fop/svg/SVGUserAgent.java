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

import org.apache.batik.bridge.UserAgentAdapter;

import org.apache.avalon.framework.logger.Logger;

// Java
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.Dimension;

public class SVGUserAgent extends UserAgentAdapter {
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

    public float getPixelUnitToMillimeter() {
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
     */
    public String getXMLParserClassName() {
        return org.apache.fop.apps.Driver.getParserClassName();
    }

    public AffineTransform getTransform() {
        return currentTransform;
    }

    public Dimension2D getViewportSize() {
        return new Dimension(100, 100);
    }

    public boolean isXMLParserValidating() {
        return true;
    }

}

