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

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.io.File;
import java.net.URL;


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
        setBaseURL(fofile);
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
     * @see org.apache.fop.apps.InputHandler#getParser()
     */
    public XMLReader getParser() throws FOPException {
        return createParser();
    }

    /**
     * Returns the fully qualified classname of the standard XML parser for FOP
     * to use.
     * @return the XML parser classname
     */
    public String getParserClassName() {
        try {
            return createParser().getClass().getName();
        } catch (FOPException e) {
            return null;
        }
    }
}

