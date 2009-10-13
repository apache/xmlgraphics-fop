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

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import org.apache.fop.Version;
import org.apache.fop.fonts.FontInfo;

/**
 * This class enables to transcode an input to a pdf document.
 *
 * <p>Two transcoding hints (<code>KEY_WIDTH</code> and
 * <code>KEY_HEIGHT</code>) can be used to respectively specify the image
 * width and the image height. If only one of these keys is specified,
 * the transcoder preserves the aspect ratio of the original image.
 *
 * <p>The <code>KEY_BACKGROUND_COLOR</code> defines the background color
 * to use for opaque image formats, or the background color that may
 * be used for image formats that support alpha channel.
 *
 * <p>The <code>KEY_AOI</code> represents the area of interest to paint
 * in device space.
 *
 * <p>Three additional transcoding hints that act on the SVG
 * processor can be specified:
 *
 * <p><code>KEY_LANGUAGE</code> to set the default language to use (may be
 * used by a &lt;switch> SVG element for example),
 * <code>KEY_USER_STYLESHEET_URI</code> to fix the URI of a user
 * stylesheet, and <code>KEY_PIXEL_TO_MM</code> to specify the pixel to
 * millimeter conversion factor.
 *
 * <p><code>KEY_AUTO_FONTS</code> to disable the auto-detection of fonts installed in the system.
 * The PDF Transcoder cannot use AWT's font subsystem and that's why the fonts have to be
 * configured differently. By default, font auto-detection is enabled to match the behaviour
 * of the other transcoders, but this may be associated with a price in the form of a small
 * performance penalty. If font auto-detection is not desired, it can be disable using this key.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public class PDFTranscoder extends AbstractFOPTranscoder
        implements Configurable {

    /** Graphics2D instance that is used to paint to */
    protected PDFDocumentGraphics2D graphics = null;

    /**
     * Constructs a new {@link PDFTranscoder}.
     */
    public PDFTranscoder() {
        super();
        this.handler = new FOPErrorHandler();
    }

    /**
     * {@inheritDoc}
     */
    protected UserAgent createUserAgent() {
        return new AbstractFOPTranscoder.FOPTranscoderUserAgent() {
            // The PDF stuff wants everything at 72dpi
            public float getPixelUnitToMillimeter() {
                return super.getPixelUnitToMillimeter();
                //return 25.4f / 72; //72dpi = 0.352778f;
            }
        };
    }

    /**
     * Transcodes the specified Document as an image in the specified output.
     *
     * @param document the document to transcode
     * @param uri the uri of the document or null if any
     * @param output the ouput where to transcode
     * @exception TranscoderException if an error occured while transcoding
     */
    protected void transcode(Document document, String uri,
                             TranscoderOutput output)
        throws TranscoderException {

        graphics = new PDFDocumentGraphics2D(isTextStroked());
        graphics.getPDFDocument().getInfo().setProducer("Apache FOP Version "
                + Version.getVersion()
                + ": PDF Transcoder for Batik");
        if (hints.containsKey(KEY_DEVICE_RESOLUTION)) {
            graphics.setDeviceDPI(getDeviceResolution());
        }

        setupImageInfrastructure(uri);

        try {
            Configuration effCfg = getEffectiveConfiguration();

            if (effCfg != null) {
                PDFDocumentGraphics2DConfigurator configurator
                        = new PDFDocumentGraphics2DConfigurator();
                configurator.configure(graphics, effCfg);
            } else {
                graphics.setupDefaultFontInfo();
            }
        } catch (Exception e) {
            throw new TranscoderException(
                "Error while setting up PDFDocumentGraphics2D", e);
        }

        super.transcode(document, uri, output);

        if (getLogger().isTraceEnabled()) {
            getLogger().trace("document size: " + width + " x " + height);
        }

        // prepare the image to be painted
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx,
                    document.getDocumentElement());
        float widthInPt = UnitProcessor.userSpaceToSVG(width, SVGLength.SVG_LENGTHTYPE_PT,
                    UnitProcessor.HORIZONTAL_LENGTH, uctx);
        int w = (int)(widthInPt + 0.5);
        float heightInPt = UnitProcessor.userSpaceToSVG(height, SVGLength.SVG_LENGTHTYPE_PT,
                UnitProcessor.HORIZONTAL_LENGTH, uctx);
        int h = (int)(heightInPt + 0.5);
        if (getLogger().isTraceEnabled()) {
            getLogger().trace("document size: " + w + "pt x " + h + "pt");
        }

        // prepare the image to be painted
        //int w = (int)(width + 0.5);
        //int h = (int)(height + 0.5);

        try {
            OutputStream out = output.getOutputStream();
            if (!(out instanceof BufferedOutputStream)) {
                out = new BufferedOutputStream(out);
            }
            graphics.setupDocument(out, w, h);
            graphics.setSVGDimension(width, height);

            if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR)) {
                graphics.setBackgroundColor
                    ((Color)hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
            }
            graphics.setGraphicContext
                (new org.apache.xmlgraphics.java2d.GraphicContext());
            graphics.preparePainting();

            graphics.transform(curTxf);
            graphics.setRenderingHint
                (RenderingHintsKeyExt.KEY_TRANSCODING,
                 RenderingHintsKeyExt.VALUE_TRANSCODING_VECTOR);

            this.root.paint(graphics);

            graphics.finish();
        } catch (IOException ex) {
            throw new TranscoderException(ex);
        }
    }

    /** {@inheritDoc} */
    protected BridgeContext createBridgeContext() {
        //For compatibility with Batik 1.6
        return createBridgeContext("1.x");
    }

    /** {@inheritDoc} */
    public BridgeContext createBridgeContext(String version) {
        FontInfo fontInfo = graphics.getFontInfo();
        if (isTextStroked()) {
            fontInfo = null;
        }
        BridgeContext ctx = new PDFBridgeContext(userAgent, fontInfo,
                getImageManager(), getImageSessionContext());
        return ctx;
    }

}
