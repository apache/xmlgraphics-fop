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

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

// Java
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.io.File;

/**
 * Abstract super class for input handlers.
 * Should be used to abstract the various possibilities on how input
 * can be provided to FOP (but actually isn't).
 */
public abstract class InputHandler {

    /**
     * Get the input source associated with this input handler.
     * @return the input source
     */
    public abstract InputSource getInputSource();

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
     * Creates <code>XMLReader</code> object using default
     * <code>SAXParserFactory</code>
     * @return the created <code>XMLReader</code>
     * @throws FOPException if the parser couldn't be created or configured for proper operation.
     */
    protected static XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newSAXParser().getXMLReader();
        } catch (SAXException se) {
            throw new FOPException("Coudn't create XMLReader", se);
        } catch (ParserConfigurationException pce) {
            throw new FOPException("Coudn't create XMLReader", pce);
        }
    }
    
    /**
     * Runs this InputHandler through the Driver.
     * @param driver Driver instance to use
     * @throws FOPException if processing this InputHandler fails
     */
    public abstract void run(Driver driver) throws FOPException;
    
}

