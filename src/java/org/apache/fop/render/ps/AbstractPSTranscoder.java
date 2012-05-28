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

package org.apache.fop.render.ps;


import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import org.apache.xmlgraphics.java2d.ps.AbstractPSDocumentGraphics2D;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.fop.svg.PDFDocumentGraphics2DConfigurator;

/**
 * <p>This class enables to transcode an input to a PostScript document.</p>
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
 * <p>This work was authored by Keiron Liddle (keiron@aftexsw.com).</p>
 */
public abstract class AbstractPSTranscoder extends AbstractFOPTranscoder {

    /** the root Graphics2D instance for generating PostScript */
    protected AbstractPSDocumentGraphics2D graphics = null;

    private FontInfo fontInfo;

    /**
     * Constructs a new {@link AbstractPSTranscoder}.
     */
    public AbstractPSTranscoder() {
        super();
    }

    /**
     * Creates the root Graphics2D instance for generating PostScript.
     * @return the root Graphics2D
     */
    protected abstract AbstractPSDocumentGraphics2D createDocumentGraphics2D();

    /** {@inheritDoc} */
    protected boolean getAutoFontsDefault() {
        //Currently set to false because auto-fonts requires a lot of memory in the PostScript
        //case: All fonts (even the unsupported TTF fonts) need to be loaded and TrueType loading
        //is currently very memory-intensive. At default JVM memory settings, this would result
        //in OutOfMemoryErrors otherwise.
        return false;
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

        graphics = createDocumentGraphics2D();
        if (!isTextStroked()) {
            try {
                boolean useComplexScriptFeatures = false; //TODO - FIX ME
                this.fontInfo = PDFDocumentGraphics2DConfigurator.createFontInfo(
                        getEffectiveConfiguration(), useComplexScriptFeatures);
                graphics.setCustomTextHandler(new NativeTextHandler(graphics, fontInfo));
            } catch (FOPException fe) {
                throw new TranscoderException(fe);
            }
        }

        super.transcode(document, uri, output);

        getLogger().trace("document size: " + width + " x " + height);

        // prepare the image to be painted
        UnitProcessor.Context uctx = UnitProcessor.createContext(ctx,
                    document.getDocumentElement());
        float widthInPt = UnitProcessor.userSpaceToSVG(width, SVGLength.SVG_LENGTHTYPE_PT,
                    UnitProcessor.HORIZONTAL_LENGTH, uctx);
        int w = (int)(widthInPt + 0.5);
        float heightInPt = UnitProcessor.userSpaceToSVG(height, SVGLength.SVG_LENGTHTYPE_PT,
                UnitProcessor.HORIZONTAL_LENGTH, uctx);
        int h = (int)(heightInPt + 0.5);
        getLogger().trace("document size: " + w + "pt x " + h + "pt");

        try {
            OutputStream out = output.getOutputStream();
            if (!(out instanceof BufferedOutputStream)) {
                out = new BufferedOutputStream(out);
            }
            graphics.setupDocument(out, w, h);
            graphics.setViewportDimension(width, height);

            if (hints.containsKey(ImageTranscoder.KEY_BACKGROUND_COLOR)) {
                graphics.setBackgroundColor
                    ((Color)hints.get(ImageTranscoder.KEY_BACKGROUND_COLOR));
        }
            graphics.setGraphicContext
                (new org.apache.xmlgraphics.java2d.GraphicContext());
            graphics.setTransform(curTxf);

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
        BridgeContext ctx = new PSBridgeContext(userAgent, (isTextStroked() ? null : fontInfo),
                getImageManager(), getImageSessionContext());
        return ctx;
    }

}
