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

// Java
import java.io.File;
import java.net.URL;

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

// JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;

/**
 * Manages input if it is an XSL-FO file.
 */
public class FOFileHandler extends InputHandler {
    
    private File fofile = null;
    private URL foURL = null;

    /**
     * Create a FOFileHandler for a file.
     * @param fofile the file to read the FO document.
     */
    public FOFileHandler(File fofile) {
        this.fofile = fofile;
        try {
            baseURL =
                new File(fofile.getAbsolutePath()).getParentFile().toURL().toExternalForm();
        } catch (Exception e) {
            baseURL = "";
        }
    }

    /**
     * Create a FOFileHandler for an URL.
     * @param url the URL to read the FO document.
     */
    public FOFileHandler(URL url) {
        this.foURL = url;
    }

    /**
     * @see org.apache.fop.apps.InputHandler#getInputSource()
     */
    public InputSource getInputSource () {
        if (fofile != null) {
            return super.fileInputSource(fofile);
        }
        return super.urlInputSource(foURL);
    }

    /**
     * @see org.apache.fop.apps.InputHandler#render(Fop)
     */
    public void render(Fop fop) throws FOPException {

        // temporary until baseURL removed from inputHandler objects
        if (fop.getUserAgent().getBaseURL() == null) {
            fop.getUserAgent().setBaseURL(getBaseURL());
        }

        try {
            // Setup JAXP using identity transformer (no stylesheet here)
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            
            // Setup input stream
            Source src = new SAXSource(getInputSource());

            // Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(fop.getDefaultHandler());
            
            // Start XSLT transformation and FOP processing
            transformer.transform(src, res);

        } catch (Exception e) {
            throw new FOPException(e);
        }
    }
}
