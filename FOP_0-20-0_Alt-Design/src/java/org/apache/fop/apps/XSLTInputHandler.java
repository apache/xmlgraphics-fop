/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.apps;

// Imported java.io classes
import java.io.File;
import java.util.Vector;

// Imported TraX classes
import javax.xml.transform.Source;
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
     */
    public XSLTInputHandler(File xmlfile, File xsltfile, Vector params) {
        this.xmlSource  = new StreamSource(xmlfile);
        this.xsltSource = new StreamSource(xsltfile);
        setBaseURL(xmlfile);
        xsltParams = params;
    }

    /**
     * Constructor for files as input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(File xmlfile, File xsltfile) {
        this.xmlSource  = new StreamSource(xmlfile);
        this.xsltSource = new StreamSource(xsltfile);
        setBaseURL(xmlfile);
    }

    /**
     * Constructor with URIs/URLs as input.
     * @param xmlURL XML URL
     * @param xsltURL XSLT URL
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(String xmlURL, String xsltURL) {
        this.xmlSource  = new StreamSource(xmlURL);
        this.xsltSource = new StreamSource(xsltURL);
    }

    /**
     * Constructor with InputSources as input.
     * @param xmlSource XML InputSource
     * @param xsltSource XSLT InputSource
     * @deprecated Use JAXP instead.
     */
    public XSLTInputHandler(InputSource xmlSource, InputSource xsltSource) {
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
    public XMLFilter getXMLFilter(Source xsltSource, Vector inParams)
    throws FOPException {
        try {
            // Instantiate  a TransformerFactory.
            TransformerFactory tFactory = TransformerFactory.newInstance();
            // Determine whether the TransformerFactory supports the use of
            // SAXSource and SAXResult
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

