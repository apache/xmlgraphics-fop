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
import java.util.logging.Logger;
import java.io.File;

/**
 * Abstract super class for input handlers.
 * Should be used to abstract the various possibilities on how input
 * can be provided to FOP (but actually isn't).
 */
public abstract class InputHandler {

    protected Logger log = Logger.getLogger(Fop.fopPackage);
    /**
     * Get the input source associated with this input handler.
     * @return the input source
     */
    public abstract InputSource getInputSource();

    protected String baseURL = null;
    
    /**
     * Get the base URL associated with this input source
     * @return the input source
     */
    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Attempts to set a default base URL from the parent of the file passed
     * as an argument. Sets <code>baseURL</code> to the URL derived, or to an
     * empty string if that fails.
     * @param file
     */
    protected void setBaseURL(File file) {
        try {
            baseURL =
                new File(file.getAbsolutePath()).getParentFile().toURL().toExternalForm();
        } catch (Exception e) {
            baseURL = "";
        }
    }

    /**
     * Get the SAX parser associated with this input handler.
     * @return the SAX parser
     * @throws FOPException in case of an error determining the SAX parser
     */
    public abstract XMLReader getParser() throws FOPException;

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
            throw new Error("unexpected MalformedURLException");
        }
    }

    /**
     * creates a SAX parser, using the value of org.xml.sax.parser
     * defaulting to org.apache.xerces.parsers.SAXParser
     *
     * @return the created SAX parser
     */
    protected XMLReader createParser() throws FOPException {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        log.config("using SAX parser " + parserClassName);

        try {
            return (XMLReader)Class.forName(parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            throw new FOPException(e);
        } catch (InstantiationException e) {
            throw new FOPException("Could not instantiate "
                                   + parserClassName, e);
        } catch (IllegalAccessException e) {
            throw new FOPException("Could not access " + parserClassName, e);
        } catch (ClassCastException e) {
            throw new FOPException(parserClassName + " is not a SAX driver",
                                   e);
        }
    }

}

