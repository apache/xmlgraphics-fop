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

// Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * XSLTInputHandler basically takes an XML file and transforms it with an XSLT
 * file and the resulting XSL-FO document is input for FOP.
 */
public class XSLTInputHandler extends InputHandler {

    private TraxInputHandler traxInputHandler;

    /**
     * Constructor for files as input
     * @param xmlfile XML file
     * @param xsltfile XSLT file
     * @throws FOPException if initializing the Transformer fails
     */
    public XSLTInputHandler(File xmlfile, File xsltfile) throws FOPException {
        this.traxInputHandler = new TraxInputHandler(xmlfile, xsltfile);
    }

    /**
     * Constructor with URIs/URLs as input.
     * @param xmlURL XML URL
     * @param xsltURL XSLT URL
     * @throws FOPException if initializing the Transformer fails
     */
    public XSLTInputHandler(String xmlURL, String xsltURL) throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlURL, xsltURL);
    }

    /**
     * Constructor with InputSources as input.
     * @param xmlSource XML InputSource
     * @param xsltSource XSLT InputSource
     * @throws FOPException if initializing the Transformer fails
     */
    public XSLTInputHandler(InputSource xmlSource, InputSource xsltSource)
                throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlSource, xsltSource);
    }

    /**
     * Get the InputSource.
     * @return the InputSource
     * @deprecated Use TraxInputHandler run(Driver driver) instead.
     */
    public InputSource getInputSource() {
        return traxInputHandler.getInputSource();
    }

    /**
     * Get the parser, actually an XML filter.
     * @see org.apache.fop.apps.InputHandler#getParser()
     * @deprecated Use TraxInputHandler run(Driver driver) instead.
     */
    public XMLReader getParser() throws FOPException {
        return traxInputHandler.getParser();
    }

    /**
     * @see org.apache.fop.apps.InputHandler#run(Driver)
     */
    public void run(Driver driver) throws FOPException {
        traxInputHandler.run(driver);
    }

    /**
     * Sets an XSLT parameter.
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public void setParameter(String name, Object value) {
        traxInputHandler.setParameter(name, value);
    }

}


