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
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class for handling files input from command line
 * either with XML and XSLT files (and optionally xsl
 * parameters) or FO File input alone
 */
public class InputHandler implements ErrorListener {
    private File sourcefile = null;  // either FO or XML/XSLT usage
    private File stylesheet = null;  // for XML/XSLT usage
    private Vector xsltParams = null; // for XML/XSLT usage

    protected Log log = LogFactory.getLog(InputHandler.class);
    
    /**
     * Constructor for XML->XSLT->FO input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @param params Vector of command-line parameters (name, value, 
     *      name, value, ...) for XSL stylesheet, null if none
     */
    public InputHandler(File xmlfile, File xsltfile, Vector params) {
        sourcefile  = xmlfile;
        stylesheet = xsltfile;
        xsltParams = params;
    }

    /**
     * Constructor for FO input
     * @param fofile the file to read the FO document.
     */
    public InputHandler(File fofile) {
        sourcefile = fofile;
    }

    /**
     * Generate a document, given an initialized Fop object
     * @param fop -- Fop object
     * @throws FOPException in case of an error during processing
     */
    public void render(Fop fop) throws FOPException {

        // if base URL was not explicitly set in FOUserAgent, obtain here
        if (fop.getUserAgent().getBaseURL() == null) {
            String baseURL = null;

            try {
                baseURL =
                    new File(sourcefile.getAbsolutePath()).
                        getParentFile().toURL().toExternalForm();
            } catch (Exception e) {
                baseURL = "";
            }
            fop.getUserAgent().setBaseURL(baseURL);
        }

        try {
            // Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer;
            
            if (stylesheet == null) {   // FO Input
                transformer = factory.newTransformer();
            } else {    // XML/XSLT input
                transformer = factory.newTransformer(new StreamSource(
                    stylesheet));
            
                // Set the value of parameters, if any, defined for stylesheet
                if (xsltParams != null) { 
                    for (int i = 0; i < xsltParams.size(); i += 2) {
                        transformer.setParameter((String) xsltParams.elementAt(i),
                            (String) xsltParams.elementAt(i + 1));
                    }
                }
            }
            transformer.setErrorListener(this);

            // Create a SAXSource from the input Source file
            Source src = new StreamSource(sourcefile);

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (Exception e) {
            throw new FOPException(e);
        }
    }
    
    /**
     * Implementation of the ErrorListener interface.
     */
    public void warning(TransformerException exc) {
        log.warn(exc.toString());
    }

    /**
     * Implementation of the ErrorListener interface.
     */
    public void error(TransformerException exc) {
        log.error(exc.toString());
    }

    /**
     * Implementation of the ErrorListener interface.
     */
    public void fatalError(TransformerException exc)
            throws TransformerException {
        throw exc;
    }
}
