/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMImplementation;

import org.xml.sax.EntityResolver;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.transcoder.ErrorHandler;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.BooleanKey;
import org.apache.batik.transcoder.keys.FloatKey;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;

/**
 * This is the common base class of all of FOP's transcoders.
 */
public abstract class AbstractFOPTranscoder extends SVGAbstractTranscoder {

    /**
     * The key is used to specify the resolution for on-the-fly images generated
     * due to complex effects like gradients and filters.
     */
    public static final TranscodingHints.Key KEY_DEVICE_RESOLUTION = new FloatKey();

    /**
     * The key to specify whether to stroke text instead of using text
     * operations.
     */
    public static final TranscodingHints.Key KEY_STROKE_TEXT = new BooleanKey();

    /**
     * The key is used to specify whether the available fonts should be automatically
     * detected. The alternative is to configure the transcoder manually using a configuration
     * file.
     */
    public static final TranscodingHints.Key KEY_AUTO_FONTS = new BooleanKey();

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
    private Configuration cfg = null;
    private ImageManager imageManager;
    private ImageSessionContext imageSessionContext;

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
     * Sets the logger.
     * @param logger the logger
     */
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

    /** {@inheritDoc} */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    /**
     * Returns the effective configuration for the transcoder.
     * @return the effective configuration
     */
    protected Configuration getEffectiveConfiguration() {
        Configuration effCfg = this.cfg;
        if (effCfg == null) {
            //By default, enable font auto-detection if no cfg is given
            boolean autoFonts = true;
            if (hints.containsKey(KEY_AUTO_FONTS)) {
                autoFonts = ((Boolean)hints.get(KEY_AUTO_FONTS)).booleanValue();
            }
            if (autoFonts) {
                DefaultConfiguration c = new DefaultConfiguration("cfg");
                DefaultConfiguration fonts = new DefaultConfiguration("fonts");
                c.addChild(fonts);
                DefaultConfiguration autodetect = new DefaultConfiguration("auto-detect");
                fonts.addChild(autodetect);
                effCfg = c;
            }
        }
        return effCfg;
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

    /**
     * Indicates whether text should be stroked rather than painted using text operators. Stroking
     * text (also referred to as "painting as shapes") can used in situations where the quality of
     * text output is not satisfying. The downside of the work-around: The generated file will
     * likely become bigger and you will lose copy/paste functionality for certain output formats
     * such as PDF.
     * @return true if text should be stroked rather than painted using text operators
     */
    protected boolean isTextStroked() {
        boolean stroke = false;
        if (hints.containsKey(KEY_STROKE_TEXT)) {
            stroke = ((Boolean)hints.get(KEY_STROKE_TEXT)).booleanValue();
        }
        return stroke;
    }

    /**
     * Returns the device resolution that has been set up.
     * @return the device resolution (in dpi)
     */
    protected float getDeviceResolution() {
        if (hints.containsKey(KEY_DEVICE_RESOLUTION)) {
            return ((Float)hints.get(KEY_DEVICE_RESOLUTION)).floatValue();
        } else {
            return 72;
        }
    }

    /**
     * Returns the ImageManager to be used by the transcoder.
     * @return the image manager
     */
    protected ImageManager getImageManager() {
        return this.imageManager;
    }

    /**
     * Returns the ImageSessionContext to be used by the transcoder.
     * @return the image session context
     */
    protected ImageSessionContext getImageSessionContext() {
        return this.imageSessionContext;
    }

    /**
     * Sets up the image infrastructure (the image loading framework).
     * @param baseURI the base URI of the current document
     */
    protected void setupImageInfrastructure(final String baseURI) {
        final ImageContext imageContext = new ImageContext() {
            public float getSourceResolution() {
                return 25.4f / userAgent.getPixelUnitToMillimeter();
            }
        };
        this.imageManager = new ImageManager(imageContext);
        this.imageSessionContext = new AbstractImageSessionContext() {

            public ImageContext getParentContext() {
                return imageContext;
            }

            public float getTargetResolution() {
                return getDeviceResolution();
            }

            public Source resolveURI(String uri) {
                System.out.println("resolve " + uri);
                try {
                    ParsedURL url = new ParsedURL(baseURI, uri);
                    InputStream in = url.openStream();
                    StreamSource source = new StreamSource(in, url.toString());
                    return source;
                } catch (IOException ioe) {
                    userAgent.displayError(ioe);
                    return null;
                }
            }

        };
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
         * {@inheritDoc}
         */
        public void error(TranscoderException te)
                throws TranscoderException {
            getLogger().error(te.getMessage());
        }

        /**
         * {@inheritDoc}
         */
        public void fatalError(TranscoderException te)
                throws TranscoderException {
            throw te;
        }

        /**
         * {@inheritDoc}
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
