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

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

// java
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.URL;


/**
 * Manages input if it is an XSL-FO file.
 */
public class FOInputHandler extends InputHandler {
    
    private File fofile = null;
    private URL foURL = null;

    /**
     * Create a FOInputHandler for a file.
     * @param fofile the file to read the FO document.
     */
    public FOInputHandler(File fofile) {
        this.fofile = fofile;
    }

    /**
     * Create a FOInputHandler for an URL.
     * @param url the URL to read the FO document.
     */
    public FOInputHandler(URL url) {
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
     * Creates <code>XMLReader</code> object using default
     * <code>SAXParserFactory</code>
     * @return the created <code>XMLReader</code>
     * @throws FOPException if the parser couldn't be created or configured for proper operation.
     */
    protected static XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(
                "http://xml.org/sax/features/namespace-prefixes", true);
            return factory.newSAXParser().getXMLReader();
        } catch (SAXNotSupportedException se) {
            throw new FOPException("Error: You need a parser which allows the"
                   + " http://xml.org/sax/features/namespace-prefixes"
                   + " feature to be set to true to support namespaces", se);
        } catch (SAXException se) {
            throw new FOPException("Couldn't create XMLReader", se);
        } catch (ParserConfigurationException pce) {
            throw new FOPException("Couldn't create XMLReader", pce);
        }
    }

    /**
     * Returns the fully qualified classname of the standard XML parser for FOP
     * to use.
     * @return the XML parser classname
     */
    public static final String getParserClassName() {
        try {
            return createParser().getClass().getName();
        } catch (FOPException e) {
            return null;
        }
    }
}

