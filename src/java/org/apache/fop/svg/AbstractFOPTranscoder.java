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
package org.apache.fop.svg;

import org.xml.sax.EntityResolver;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.DOMImplementation;

/**
 * This is the common base class of all of FOP's transcoders.
 */
public abstract class AbstractFOPTranscoder extends SVGAbstractTranscoder
            implements LogEnabled {

    /**
     * The key to specify whether to stroke text instead of using text 
     * operations.
     */
    public static final TranscodingHints.Key KEY_STROKE_TEXT = new BooleanKey();

    /** The value to turn on text stroking. */
    public static final Boolean VALUE_FORMAT_ON = Boolean.TRUE;

    /** The value to turn off text stroking. */
    public static final Boolean VALUE_FORMAT_OFF = Boolean.FALSE;

    /**
     * The user agent dedicated to this Transcoder.
     */
    protected UserAgent userAgent = createUserAgent();

    private Logger logger;
    private EntityResolver resolver;

    /**
     * Constructs a new FOP-style transcoder.
     */
    public AbstractFOPTranscoder() {
        hints.put(KEY_DOCUMENT_ELEMENT_NAMESPACE_URI,
                  SVGConstants.SVG_NAMESPACE_URI);
        hints.put(KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
        hints.put(KEY_DOM_IMPLEMENTATION,
                  SVGDOMImplementation.getDOMImplementation());
    }
    
    /**
     * Creates and returns the default user agent for this transcoder. Override
     * this method if you need non-default behaviour.
     * @return UserAgent the newly created user agent
     */
    protected UserAgent createUserAgent() {
        return new FOPTranscoderUserAgent();
    }
    
    /**
     * @see org.apache.avalon.framework.logger.LogEnabled#enableLogging(Logger)
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the EntityResolver that should be used when building SVG documents.
     * @param resolver the resolver
     */
    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }
    
    /**
     * Returns the logger associated with this transcoder. It returns a 
     * ConsoleLogger if no logger has been explicitly set.
     * @return Logger the logger for the transcoder.
     */
    protected final Logger getLogger() {
        if (this.logger == null) {
            this.logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        }
        return this.logger;
    }
    
    /**
     * Creates a <tt>DocumentFactory</tt> that is used to create an SVG DOM
     * tree. The specified DOM Implementation is ignored and the Batik
     * SVG DOM Implementation is automatically used.
     *
     * @param domImpl the DOM Implementation (not used)
     * @param parserClassname the XML parser classname
     * @return the document factory
     */
    protected DocumentFactory createDocumentFactory(DOMImplementation domImpl,
            String parserClassname) {
        final FOPSAXSVGDocumentFactory factory 
                = new FOPSAXSVGDocumentFactory(parserClassname);
        if (this.resolver != null) {
            factory.setAdditionalEntityResolver(this.resolver);
        }
        return factory;
    }

    // --------------------------------------------------------------------
    // FOP's default error handler (for transcoders)
    // --------------------------------------------------------------------

    /**
     * This is the default transcoder error handler for FOP. It logs error
     * to an Avalon Logger instead of to System.out. The remaining behaviour 
     * is the same as Batik's DefaultErrorHandler.
     */    
    protected class FOPErrorHandler implements ErrorHandler {
        
        /**
         * @see org.apache.batik.transcoder.ErrorHandler#error(TranscoderException)
         */
        public void error(TranscoderException te)
                throws TranscoderException {
            getLogger().error(te.getMessage());
        }

        /**
         * @see org.apache.batik.transcoder.ErrorHandler#fatalError(TranscoderException)
         */
        public void fatalError(TranscoderException te)
                throws TranscoderException {
            throw te;
        }

        /**
         * @see org.apache.batik.transcoder.ErrorHandler#warning(TranscoderException)
         */
        public void warning(TranscoderException te)
                throws TranscoderException {
            getLogger().warn(te.getMessage());
        }

    }

    // --------------------------------------------------------------------
    // UserAgent implementation
    // --------------------------------------------------------------------

    /**
     * A user agent implementation for FOP's Transcoders.
     */
    protected class FOPTranscoderUserAgent extends SVGAbstractTranscoderUserAgent {

        /**
         * Displays the specified error message using the <tt>ErrorHandler</tt>.
         * @param message the message to display
         */
        public void displayError(String message) {
            try {
                getErrorHandler().error(new TranscoderException(message));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified error using the <tt>ErrorHandler</tt>.
         * @param e the exception to display
         */
        public void displayError(Exception e) {
            try {
                getErrorHandler().error(new TranscoderException(e));
            } catch (TranscoderException ex) {
                throw new RuntimeException();
            }
        }

        /**
         * Displays the specified message using the <tt>ErrorHandler</tt>.
         * @param message the message to display
         */
        public void displayMessage(String message) {
            getLogger().info(message);
        }

        /**
         * Returns the pixel to millimeter conversion factor specified in the
         * <tt>TranscodingHints</tt> or 0.3528 if any.
         * @return the pixel unit to millimeter factor
         */
        public float getPixelUnitToMillimeter() {
            Object key = ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER;
            if (getTranscodingHints().containsKey(key)) {
                return ((Float)getTranscodingHints().get(key)).floatValue();
            } else {
                // return 0.3528f; // 72 dpi
                return 0.26458333333333333333333333333333f;    // 96dpi
            }
        }

        /**
         * Get the media for this transcoder. Which is always print.
         * @return PDF media is "print"
         */
        public String getMedia() {
            return "print";
        }

    }
    
}
