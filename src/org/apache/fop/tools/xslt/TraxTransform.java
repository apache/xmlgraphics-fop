/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.tools.xslt;

import javax.xml.transform.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;

import java.util.Hashtable;
import org.w3c.dom.Document;

/**
 * Handles xslt tranformations via Trax (xalan2)
 */

public class TraxTransform {

    /**
     * Cache of compiled stylesheets (filename, StylesheetRoot)
     */
    private static Hashtable _stylesheetCache = new Hashtable();

    public static Transformer getTransformer(String xsltFilename,
                                             boolean cache) {
        try {
            if (cache && _stylesheetCache.containsKey(xsltFilename)) {
                Templates cachedStylesheet =
                    (Templates)_stylesheetCache.get(xsltFilename);
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
                _stylesheetCache.put(xsltFilename, compiledSheet);
            }
            return compiledSheet.newTransformer();
        } catch (TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
        return null;

    }

    public static void transform(String xmlSource, String xslURL,
                                 String outputFile) {
        transform(new javax.xml.transform.stream.StreamSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(outputFile));
    }

    public static void transform(Document xmlSource, String xslURL,
                                 String outputFile) {

        transform(new javax.xml.transform.dom.DOMSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(outputFile));

    }

    public static void transform(String xmlSource, String xslURL,
                                 Writer output) {
        transform(new javax.xml.transform.stream.StreamSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xslURL),
                  new javax.xml.transform.stream.StreamResult(output));
    }

    public static void transform(Document xmlSource, InputStream xsl,
                                 Document outputDoc) {
        transform(new javax.xml.transform.dom.DOMSource(xmlSource),
                  new javax.xml.transform.stream.StreamSource(xsl),
                  new javax.xml.transform.dom.DOMResult(outputDoc));
    }

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
