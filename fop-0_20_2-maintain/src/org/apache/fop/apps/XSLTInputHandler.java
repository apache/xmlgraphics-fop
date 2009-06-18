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

// Imported java.io classes
import java.io.File;

/**
 * XSLTInputHandler takes an XML input, transforms it with XSLT
 * and provides the resulting xsl:fo document as input for the
 * FOP driver.
 * Use TraxInputHandler instead.
 */
public class XSLTInputHandler extends InputHandler {
    private TraxInputHandler traxInputHandler;

    public XSLTInputHandler(File xmlfile, File xsltfile)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlfile, xsltfile);
    }

    public XSLTInputHandler(String xmlURL, String xsltURL)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlURL, xsltURL);
    }

    public XSLTInputHandler(InputSource xmlSource, InputSource xsltSource)
      throws FOPException {
        traxInputHandler = new TraxInputHandler(xmlSource, xsltSource);
    }

    /**
     * Get the InputSource.
     * Use TraxInputHandler run(Driver driver) instead.
     * @deprecated
     */
    public InputSource getInputSource() {
        return traxInputHandler.getInputSource();
    }

    /**
     * Get the parser, actually an XML filter.
     * Use TraxInputHandler run(Driver driver) instead.
     * @deprecated
     */
    public XMLReader getParser() throws FOPException {
        return traxInputHandler.getParser();
    }

    public void run(Driver driver) throws FOPException {
        traxInputHandler.run(driver);
    }

    public void setParameter(String name, Object value) {
        traxInputHandler.setParameter(name, value);
    }
}

