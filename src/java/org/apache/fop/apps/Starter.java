/*
 * $Id: Starter.java,v 1.12 2003/02/27 10:13:07 jeremias Exp $
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

// Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;

/**
 * Abstract super class.
 * Creates a SAX Parser (defaulting to Xerces).
 */
public abstract class Starter extends AbstractLogEnabled {

    /** InputHandler associated with this Starter */
    protected InputHandler inputHandler;

    /**
     * Main constructor
     * @throws FOPException In case of a problem constructing the Starter
     */
    public Starter() throws FOPException {
    }

    /**
     * Sets the InputHandler to use.
     * @param inputHandler input handler
     */
    public void setInputHandler(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Runs the current setup.
     * @throws FOPException In case of a problem during the FOP run
     */
    public abstract void run() throws FOPException;

    /**
     * Sets the parser features on an XMLReader
     * @param parser XMLReader to set features on
     * @throws FOPException if the XMLReader doesn't support the feature that
     * need to be set
     */
    public void setParserFeatures(XMLReader parser) throws FOPException {
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
        } catch (SAXException e) {
            throw new FOPException("Error: You need a parser which allows the"
                   + " http://xml.org/sax/features/namespace-prefixes"
                   + " feature to be set to true to support namespaces", e);
        }
    }

}
