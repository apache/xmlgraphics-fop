/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.xslt;

import org.apache.xalan.xslt.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import org.w3c.dom.Document;

/**
 * Handles xslt tranformations via Xalan1 (non-trax)
 */

public class Xalan1Transform {

    /**
     * Cache of compiled stylesheets (filename, StylesheetRoot)
     */
    private static Hashtable _stylesheetCache = new Hashtable();

    public static StylesheetRoot getStylesheet(String xsltFilename, boolean cache)
            throws org.xml.sax.SAXException {
        if (cache && _stylesheetCache.containsKey(xsltFilename)) {
            return (StylesheetRoot)_stylesheetCache.get(xsltFilename);
        }

        // Use XSLTProcessor to instantiate an XSLTProcessor.
        XSLTProcessor processor = XSLTProcessorFactory.getProcessor();

        XSLTInputSource xslSheet = new XSLTInputSource(xsltFilename);

        // Perform the transformation.
        StylesheetRoot compiledSheet = processor.processStylesheet(xslSheet);
        if (cache) {
            _stylesheetCache.put(xsltFilename, compiledSheet);
        }
        return compiledSheet;
    }

    public static void transform(String xmlSource, String xslURL,
                                 String outputFile) throws java.io.IOException,
                                 java.net.MalformedURLException,
                                 org.xml.sax.SAXException {
        try {
            javax.xml.parsers.DocumentBuilder docBuilder =
                javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(new FileInputStream(xmlSource));
            transform(doc, xslURL, outputFile);
        } catch (javax.xml.parsers.ParserConfigurationException ex) {
            throw new org.xml.sax.SAXException(ex);
        }

    }

    public static void transform(Document xmlSource, String xslURL,
                                 String outputFile) throws java.io.IOException,
                                 java.net.MalformedURLException,
                                 org.xml.sax.SAXException {

        XSLTResultTarget xmlResult = new XSLTResultTarget(outputFile);

        StylesheetRoot stylesheet = getStylesheet(xslURL, true);

        // Perform the transformation.
        stylesheet.process(XSLTProcessorFactory.getProcessor(), xmlSource,
                           xmlResult);
    }

    public static void transform(String xmlSource, String xslURL,
                                 java.io.Writer outputFile) throws java.io.IOException,
                                 java.net.MalformedURLException,
                                 org.xml.sax.SAXException {

        XSLTInputSource source = new XSLTInputSource(xmlSource);
        XSLTResultTarget xmlResult = new XSLTResultTarget(outputFile);

        StylesheetRoot stylesheet = getStylesheet(xslURL, true);

        // Perform the transformation.
        stylesheet.process(XSLTProcessorFactory.getProcessor(), source,
                           xmlResult);
    }

    public static void transform(Document xmlSource, InputStream xsl,
                                 Document outputDoc) throws java.io.IOException,
                                 java.net.MalformedURLException,
                                 org.xml.sax.SAXException {

        XSLTInputSource source = new XSLTInputSource(xmlSource);
        XSLTInputSource xslSheet = new XSLTInputSource(xsl);
        XSLTResultTarget xmlResult = new XSLTResultTarget(outputDoc);


        // Perform the transformation.
        XSLTProcessor processor = XSLTProcessorFactory.getProcessor();

        processor.process(source, xslSheet, xmlResult);
    }

}
