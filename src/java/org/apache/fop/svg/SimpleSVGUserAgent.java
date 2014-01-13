/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.svg;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import org.apache.batik.bridge.UserAgentAdapter;

/**
 * A simple SVG user agent.
 * This is an implementation of the Batik SVG user agent. It ignores any message output sent
 * by Batik.
 */
public class SimpleSVGUserAgent extends UserAgentAdapter {

    private AffineTransform currentTransform = null;
    private float pixelUnitToMillimeter = 0.0f;

    /**
     * Creates a new user agent.
     * @param pixelUnitToMM the pixel to millimeter conversion factor currently in effect
     * @param at the current transform
     */
    public SimpleSVGUserAgent(float pixelUnitToMM, AffineTransform at) {
        pixelUnitToMillimeter = pixelUnitToMM;
        currentTransform = at;
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
     * @return the media for FO documents is "print"
     */
    public String getMedia() {
        return "print";
    }

    /**
     * Returns the user stylesheet URI.
     * @return null if no user style sheet was specified.
     */
    public String getUserStyleSheetURI() {
        return null; // userStyleSheetURI;
    }


    private static final String XML_PARSER_CLASS_NAME;

    static {
        String result;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            result = factory.newSAXParser().getXMLReader().getClass().getName();
        } catch (SAXException e) {
            result = null;
        } catch (ParserConfigurationException e) {
            result = null;
        }
        XML_PARSER_CLASS_NAME = result;
    }

    /**
     * Returns the class name of the XML parser.
     * @return the XML parser class name
     */
    public String getXMLParserClassName() {
        return XML_PARSER_CLASS_NAME;
    }

    /**
     * Is the XML parser validating.
     * @return true if the XML parser is validating
     */
    public boolean isXMLParserValidating() {
        return false;
    }

    /**
     * Get the transform of the SVG document.
     * @return the transform
     */
    public AffineTransform getTransform() {
        return currentTransform;
    }

    /** {@inheritDoc} */
    public void setTransform(AffineTransform at) {
        this.currentTransform = at;
    }

    /**
     * Get the default viewport size for an SVG document.
     * This returns a default value of 100x100.
     * @return the default viewport size
     */
    public Dimension2D getViewportSize() {
        return new Dimension(100, 100);
    }

}

