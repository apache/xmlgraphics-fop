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

package org.apache.fop.render.intermediate;

import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Abstract base class for XML-writing IFPainter implementations.
 */
public abstract class AbstractXMLWritingIFPainter extends AbstractIFPainter {

    private static final Attributes EMPTY_ATTS = new AttributesImpl();

    /** Constant for the "CDATA" attribute type. */
    protected static final String CDATA = "CDATA";

    /**
     * Default SAXTransformerFactory that can be used by subclasses.
     */
    protected SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /** Main SAX ContentHandler to receive the generated SAX events. */
    protected ContentHandler handler;

    /** {@inheritDoc} */
    public void setResult(Result result) throws IFException {
        if (result instanceof SAXResult) {
            SAXResult saxResult = (SAXResult)result;
            this.handler = saxResult.getHandler();
        } else {
            this.handler = createContentHandler(result);
        }
    }

    /**
     * Returns the main namespace used for generated XML content.
     * @return the main namespace
     */
    protected abstract String getMainNamespace();

    /**
     * Creates a ContentHandler for the given JAXP Result instance.
     * @param result the JAXP Result instance
     * @return the requested SAX ContentHandler
     * @throws IFException if an error occurs setting up the output
     */
    protected ContentHandler createContentHandler(Result result) throws IFException {
        try {
            TransformerHandler tHandler = tFactory.newTransformerHandler();
            Transformer transformer = tHandler.getTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            tHandler.setResult(result);
            return tHandler;
        } catch (TransformerConfigurationException tce) {
            throw new IFException(
                    "Error while setting up the serializer for SVG output", tce);
        }
    }

    /* ---=== helper methods ===--- */

    private static final String BASE_FORMAT = "0.################";

    private static class DecimalFormatThreadLocal extends ThreadLocal {

        protected synchronized Object initialValue() {
            DecimalFormat df = new DecimalFormat(BASE_FORMAT, new DecimalFormatSymbols(Locale.US));
            return df;
        }
    };

    //DecimalFormat is not thread-safe!
    private static final ThreadLocal DECIMAL_FORMAT = new DecimalFormatThreadLocal();

    private static String format(double value) {
        DecimalFormat df = (DecimalFormat)DECIMAL_FORMAT.get();
        return df.format(value);
    }

    /**
     * Converts an {@code AffineTransform} instance to an SVG style transform method.
     * @param transform the transformation matrix
     * @param sb the StringBuffer to write the transform method to
     * @return the StringBuffer passed to this method
     */
    protected StringBuffer toString(AffineTransform transform, StringBuffer sb) {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        if (matrix[0] == 1 && matrix[3] == 1 && matrix[1] == 0 && matrix[2] == 0) {
            sb.append("translate(");
            sb.append(format(matrix[4]));
            if (matrix[5] != 0) {
                sb.append(',').append(format(matrix[5]));
            }
        } else {
            sb.append("matrix(");
            for (int i = 0; i < 6; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(format(matrix[i]));
            }
        }
        sb.append(')');
        return sb;
    }


    /**
     * Convenience method to generate a startElement SAX event.
     * @param localName the local name of the element
     * @param atts the attributes
     * @throws SAXException if a SAX exception occurs
     */
    protected void startElement(String localName, Attributes atts) throws SAXException {
        handler.startElement(getMainNamespace(), localName, localName, atts);
    }

    /**
     * Convenience method to generate a startElement SAX event.
     * @param localName the local name of the element
     * @throws SAXException if a SAX exception occurs
     */
    protected void startElement(String localName) throws SAXException {
        handler.startElement(getMainNamespace(), localName, localName, EMPTY_ATTS);
    }

    /**
     * Convenience method to generate a endElement SAX event.
     * @param localName the local name of the element
     * @throws SAXException if a SAX exception occurs
     */
    protected void endElement(String localName) throws SAXException {
        handler.endElement(getMainNamespace(), localName, localName);
    }

    /**
     * Convenience method to generate an empty element.
     * @param localName the local name of the element
     * @param atts the attributes
     * @throws SAXException if a SAX exception occurs
     */
    protected void element(String localName, Attributes atts) throws SAXException {
        handler.startElement(getMainNamespace(), localName, localName, atts);
        handler.endElement(getMainNamespace(), localName, localName);
    }

    /**
     * Converts an array of integer coordinates into a space-separated string.
     * @param coordinates the coordinates
     * @return the space-separated array of coordinates
     */
    protected String toString(int[] coordinates) {
        if (coordinates == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0, c = coordinates.length; i < c; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Integer.toString(coordinates[i]));
        }
        return sb.toString();
    }
}
