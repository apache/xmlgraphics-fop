/*
 * $Id: XSLTInputHandler.java,v 1.10 2003/02/27 10:13:05 jeremias Exp $
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
import java.util.Vector;

// Imported TraX classes
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
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
 * XSLTInputHandler basically takes an XML file and transforms it with an XSLT
 * file and the resulting XSL-FO document is input for FOP.
 */
public class XSLTInputHandler extends InputHandler {

    private StreamSource xmlSource;
    private Source xsltSource;
    private Vector xsltParams = null; // not yet implemented
    
    /**
     * Constructor for files as input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @param params Vector of command-line parameters (name, value, 
     *      name, value, ...) for XSL stylesheet
     * @throws FOPException if initializing the Transformer fails
     */
    public XSLTInputHandler(File xmlfile, File xsltfile, Vector params) throws FOPException {
        this.xmlSource  = new StreamSource(xmlfile);
        this.xsltSource = new StreamSource(xsltfile);
        xsltParams = params;
    }

    /**
     * Constructor for files as input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @throws FOPException if initializing the Transformer fails
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(File xmlfile, File xsltfile) throws FOPException {
        this.xmlSource  = new StreamSource(xmlfile);
        this.xsltSource = new StreamSource(xsltfile);
    }

    /**
     * Constructor with URIs/URLs as input.
     * @param xmlURL XML URL
     * @param xsltURL XSLT URL
     * @throws FOPException if initializing the Transformer fails
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(String xmlURL, String xsltURL) throws FOPException {
        this.xmlSource  = new StreamSource(xmlURL);
        this.xsltSource = new StreamSource(xsltURL);
    }

    /**
     * Constructor with InputSources as input.
     * @param xmlSource XML InputSource
     * @param xsltSource XSLT InputSource
     * @throws FOPException if initializing the Transformer fails
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(InputSource xmlSource, InputSource xsltSource)
                throws FOPException {
        this.xmlSource  = new StreamSource(xmlSource.getByteStream(),
                                           xmlSource.getSystemId());
        this.xsltSource = new StreamSource(xsltSource.getByteStream(),
                                           xsltSource.getSystemId());
    }

    /**
     * @see org.apache.fop.apps.InputHandler#getInputSource()
     */
    public InputSource getInputSource() {
        InputSource is = new InputSource();
        is.setByteStream(xmlSource.getInputStream());
        is.setSystemId(xmlSource.getSystemId());
        return is;
    }

    /**
     * Overwrites this method of the super class and returns an XMLFilter 
     * instead of a simple XMLReader which allows chaining of transformations.
     * @see org.apache.fop.apps.InputHandler#getParser()
     */
    public XMLReader getParser() throws FOPException {
        return getXMLFilter(xsltSource, xsltParams);
    }

    /**
     * Creates from the transformer an instance of an XMLFilter which
     * then can be used in a chain with the XMLReader passed to Driver. This way
     * during the conversion of the xml file + xslt stylesheet the resulting
     * data is fed into Fop. This should help to avoid memory problems
     * @param xsltSource An xslt stylesheet
     * @return an XMLFilter which can be chained together with other 
     * XMLReaders or XMLFilters
     * @throws FOPException if setting up the XMLFilter fails
     */
    public static XMLFilter getXMLFilter(Source xsltSource, Vector inParams) throws FOPException {
        try {
            // Instantiate  a TransformerFactory.
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Determine whether the TransformerFactory supports The use of SAXSource
            // and SAXResult
            if (tFactory.getFeature(SAXSource.FEATURE)
                    && tFactory.getFeature(SAXResult.FEATURE)) {
                // Cast the TransformerFactory to SAXTransformerFactory.
                SAXTransformerFactory saxTFactory =
                    ((SAXTransformerFactory)tFactory);
                // Create an XMLFilter for each stylesheet.
                XMLFilter xmlfilter =
                    saxTFactory.newXMLFilter(xsltSource);
                    
/*              if (inParams != null) { 
                    Transformer transformer = ??? how to obtain from an XMLFilter?
                    int nParams = inParams.size();
            
                    for (int i = 0; i < nParams; i += 2) {
                        transformer.setParameter((String) inParams.elementAt(i),
                            (String) inParams.elementAt(i + 1));
                    }
                }
*/                  
                
                // Create an XMLReader.
                XMLReader parser = FOFileHandler.createParser();
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

