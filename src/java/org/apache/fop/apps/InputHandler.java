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

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

// Java
import java.net.URL;
import java.io.File;

/**
 * Abstract super class for input handlers.
 * Should be used to abstract the various possibilities on how input
 * can be provided to FOP (but actually isn't).
 */
public abstract class InputHandler {

    protected String baseURL = null;
    
    /**
     * Get the base URL associated with this input source
     * @return the input source
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Generate a document, given an initialized Fop object
     * @param fop -- Fop object
     * @throws FOPException in case of an error during processing
     */
    public void render(Fop fop) throws FOPException {}

    /**
     * Creates an InputSource from a URL.
     * @param url URL to use
     * @return the newly created InputSource
     */
    public static InputSource urlInputSource(URL url) {
        return new InputSource(url.toString());
    }

    /**
     * Creates an <code>InputSource</code> from a <code>File</code>
     * @param file the <code>File</code>
     * @return the <code>InputSource</code> created
     */
    public static InputSource fileInputSource(File file) {
        /* this code adapted from James Clark's in XT */
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1) {
            path = path.replace(fSep.charAt(0), '/');
        }
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = '/' + path;
        }
        try {
            return new InputSource(new URL("file", null, path).toString());
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("unexpected MalformedURLException");
        }
    }
}
