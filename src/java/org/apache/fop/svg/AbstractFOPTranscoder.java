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
 
package org.apache.fop.svg;

import org.xml.sax.EntityResolver;

import org.apache.commons.logging.impl.SimpleLog;
import org.apache.commons.logging.Log;
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
            {

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

    private Log logger;
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
    
    public void setLogger(Log logger) {
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
     * SimpleLog if no logger has been explicitly set.
     * @return Logger the logger for the transcoder.
     */
    protected final Log getLogger() {
        if (this.logger == null) {
            this.logger = new SimpleLog("FOP/Transcoder");
            ((SimpleLog) logger).setLevel(SimpleLog.LOG_LEVEL_INFO);
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
     * to an Commons Logger instead of to System.out. The remaining behaviour 
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
                return 25.4f / 96; //96dpi = 0.2645833333333333333f;
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
