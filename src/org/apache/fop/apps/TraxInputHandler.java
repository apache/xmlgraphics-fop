/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;


// Imported TraX classes
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;


// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;



// Imported java.io classes
import java.io.InputStream;
import java.io.IOException;
import java.io.File;

/**
 * XSLTInputHandler basically takes an xmlfile and transforms it with an xsltfile
 * and the resulting xsl:fo document is input for Fop.
 */
public class TraxInputHandler extends InputHandler {

    File xmlfile, xsltfile;

    public TraxInputHandler(File xmlfile, File xsltfile) {
        this.xmlfile = xmlfile;
        this.xsltfile = xsltfile;
    }

    /**
     * overwrites the method of the super class to return the xmlfile
     */
    public InputSource getInputSource() {
        return fileInputSource(xmlfile);
    }

    /**
     * overwrites this method of the super class and returns an XMLFilter instead of a
     * simple XMLReader which allows chaining of transformations
     *
     */
    public XMLReader getParser() throws FOPException {
        return this.getXMLFilter(xmlfile, xsltfile);
    }

    /**
     * Creates from the transformer an instance of an XMLFilter which
     * then can be used in a chain with the XMLReader passed to Driver. This way
     * during the conversion of the xml file + xslt stylesheet the resulting
     * data is fed into Fop. This should help to avoid memory problems
     * @param xmlfile The xmlfile containing the text data
     * @param xsltfile An xslt stylesheet
     * @return XMLFilter an XMLFilter which can be chained together with other XMLReaders or XMLFilters
     */
    public static XMLFilter getXMLFilter(File xmlfile,
                                         File xsltfile) throws FOPException {
        try {
            // Instantiate  a TransformerFactory.
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Determine whether the TransformerFactory supports The use uf SAXSource
            // and SAXResult
            if (tFactory.getFeature(SAXSource.FEATURE)
                    && tFactory.getFeature(SAXResult.FEATURE)) {
                // Cast the TransformerFactory to SAXTransformerFactory.
                SAXTransformerFactory saxTFactory =
                    ((SAXTransformerFactory)tFactory);
                // Create an XMLFilter for each stylesheet.
                XMLFilter xmlfilter =
                    saxTFactory.newXMLFilter(new StreamSource(xsltfile));

                // Create an XMLReader.
                XMLReader parser = createParser();
                if (parser == null) {
                    throw new FOPException("Unable to create SAX parser");
                }

                // xmlFilter1 uses the XMLReader as its reader.
                xmlfilter.setParent(parser);
                return xmlfilter;
            } else {
                throw new FOPException("Your parser doesn't support the features SAXSource and SAXResult."
                                       + "\nMake sure you are using a xsl parser which supports TrAX");
            }
        } catch (Exception ex) {
            if (ex instanceof FOPException) {
                throw (FOPException)ex;
            }
            throw new FOPException(ex);
        }
    }

}

