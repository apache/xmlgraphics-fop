/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.xslt;

import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;

/**
 * Handles xslt tranformations via Trax (xalan2)
 */
public class TraxTransform {

    /**
     * Cache of compiled stylesheets (filename, StylesheetRoot)
     */
    private static Map stylesheetCache = new java.util.Hashtable();

    /**
     * Gets a Trax transformer
     * @param xsltFilename Filename of the XSLT file
     * @param cache True, if caching of the stylesheet is allowed
     * @return Transformer the Trax transformer
     */
    public static Transformer getTransformer(String xsltFilename,
                                             boolean cache) {
        try {
            if (cache && stylesheetCache.containsKey(xsltFilename)) {
                Templates cachedStylesheet =
                    (Templates)stylesheetCache.get(xsltFilename);
                return cachedStylesheet.newTransformer();
            }

            Source xslSheet =
                new javax.xml.transform.stream.StreamSource(xsltFilename);


            /*
             * System.out.println("****************************");
             * System.out.println("trax compile \nin: " + xsltFilename);
             * System.out.println("****************************");
             */
            TransformerFactory factory = TransformerFactory.newInstance();

            Templates compiledSheet = factory.newTemplates(xslSheet);
            if (cache) {
                stylesheetCache.put(xsltFilename, compiledSheet);
            }
            return compiledSheet.newTransformer();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    /**
     * Transforms an XML file using XSLT.
     * @param xmlSource Filename of the source XML file
     * @param xslURL Filename of the XSLT filename
     * @param outputFile Target filename
     */
    public static void transform(String xmlSource, String xslURL,
                                 String outputFile) {
        transform(new javax.xml.transform.stream.StreamSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(outputFile));
    }

    /**
     * Transforms an XML file using XSLT.
     * @param xmlSource Source DOM Document
     * @param xslURL Filename of the XSLT filename
     * @param outputFile Target filename
     */
    public static void transform(Document xmlSource, String xslURL,
                                 String outputFile) {

        transform(new javax.xml.transform.dom.DOMSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(outputFile));

    }

    /**
     * Transforms an XML file using XSLT.
     * @param xmlSource Filename of the source XML file
     * @param xslURL Filename of the XSLT filename
     * @param output Target Writer instance
     */
    public static void transform(String xmlSource, String xslURL,
                                 Writer output) {
        transform(new javax.xml.transform.stream.StreamSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(output));
    }

    /**
     * Transforms an XML file using XSLT.
     * @param xmlSource Source DOM Document
     * @param xsl Filename of the XSLT filename
     * @param outputDoc Target DOM document
     */
    public static void transform(Document xmlSource, InputStream xsl,
                                 Document outputDoc) {
        transform(new javax.xml.transform.dom.DOMSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xsl),
                  new javax.xml.transform.dom.DOMResult(outputDoc));
    }

    /**
     * Transforms an XML file using XSLT.
     * @param xmlSource XML Source
     * @param xslSource XSLT Source
     * @param result Target Result
     */
    public static void transform(Source xmlSource, Source xslSource,
                                 Result result) {
        try {
            Transformer transformer;
            if (xslSource.getSystemId() == null) {
                TransformerFactory factory = TransformerFactory.newInstance();
                transformer = factory.newTransformer(xslSource);
            } else {
                transformer = getTransformer(xslSource.getSystemId(), true);
            }
            transformer.transform(xmlSource, result);
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        } catch (TransformerException ex) {
            ex.printStackTrace();
        }

    }

}
