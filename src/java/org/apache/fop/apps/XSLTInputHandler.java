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
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

// Imported SAX classes
import org.xml.sax.InputSource;

/**
 * XSLTInputHandler basically takes an XML file and transforms it with an XSLT
 * file and the resulting XSL-FO document is input for FOP.
 */
public class XSLTInputHandler extends InputHandler {
    private StreamSource xmlSource;
    private Source xsltSource;
    private Vector xsltParams = null;
    
    /**
     * Constructor for files as input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @param params Vector of command-line parameters (name, value, 
     *      name, value, ...) for XSL stylesheet, null if none
     * @throws FOPException if initializing the Transformer fails
     */
    public XSLTInputHandler(File xmlfile, File xsltfile, Vector params) {
        this.xmlSource  = new StreamSource(xmlfile);
        this.xsltSource = new StreamSource(xsltfile);
        try {
            baseURL =
                new File(xmlfile.getAbsolutePath()).getParentFile().toURL().toExternalForm();
        } catch (Exception e) {
            baseURL = "";
        }
        xsltParams = params;
    }

    /**
     * @see org.apache.fop.apps.InputHandler#render(Fop)
     */
    public void render(Fop fop) 
        throws FOPException {

        // temporary until baseURL removed from inputHandler objects
        if (fop.getUserAgent().getBaseURL() == null) {
            fop.getUserAgent().setBaseURL(getBaseURL());
        }

        try {
            // Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(xsltSource);
            
            // Set the value of parameters, if any, defined for stylesheet
            if (xsltParams != null) { 
                for (int i = 0; i < xsltParams.size(); i += 2) {
                    transformer.setParameter((String) xsltParams.elementAt(i),
                        (String) xsltParams.elementAt(i + 1));
                }
            }

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());

            // Start XSLT transformation and FOP processing
            transformer.transform(xmlSource, res);

        } catch (Exception e) {
            throw new FOPException(e);
        }
    }
}
