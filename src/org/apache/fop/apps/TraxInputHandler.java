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
package org.apache.fop.apps;

// Imported java.io classes
import java.io.File;

// Imported TraX classes
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.XMLFilter;

/**
 * XSLTInputHandler basically takes an XML file and transforms it with an 
 * XSLT file and the resulting XSL-FO document is input for FOP.
 */
public class TraxInputHandler extends InputHandler {

    private File xmlfile, xsltfile;

    /**
     * Main constructor
     * @param xmlfile XML file 
     * @param xsltfile XSLT file
     */
    public TraxInputHandler(File xmlfile, File xsltfile) {
        this.xmlfile = xmlfile;
        this.xsltfile = xsltfile;
    }

    /**
     * @see org.apache.fop.apps.InputHandler#getInputSource()
     */
    public InputSource getInputSource() {
        return fileInputSource(xmlfile);
    }

    /**
     * Overwrites this method of the super class and returns an XMLFilter 
     * instead of a simple XMLReader which allows chaining of transformations.
     * @see org.apache.fop.apps.InputHandler#getParser()
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
     * @return an XMLFilter which can be chained together with other 
     * XMLReaders or XMLFilters
     * @throws FOPException if setting up the XMLFilter fails
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
                throw new FOPException("Your parser doesn't support the "
                        + "features SAXSource and SAXResult."
                        + "\nMake sure you are using an XSLT engine which "
                        + "supports TrAX");
            }
        } catch (FOPException fe) {
            throw fe;
        } catch (Exception ex) {
            throw new FOPException(ex);
        }
    }

}

