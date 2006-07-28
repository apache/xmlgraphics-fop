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

import java.io.IOException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;

import org.apache.batik.transcoder.image.ImageTranscoder;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontSetup;
import org.apache.fop.svg.AbstractFOPTranscoder;
import org.apache.xmlgraphics.java2d.ps.AbstractPSDocumentGraphics2D;
import org.apache.xmlgraphics.java2d.ps.TextHandler;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

/**
 * This class enables to transcode an input to a PostScript document.
 *
 * <p>Two transcoding hints (<tt>KEY_WIDTH</tt> and
 * <tt>KEY_HEIGHT</tt>) can be used to respectively specify the image
 * width and the image height. If only one of these keys is specified,
 * the transcoder preserves the aspect ratio of the original image.
 *
 * <p>The <tt>KEY_BACKGROUND_COLOR</tt> defines the background color
 * to use for opaque image formats, or the background color that may
 * be used for image formats that support alpha channel.
 *
 * <p>The <tt>KEY_AOI</tt> represents the area of interest to paint
 * in device space.
 *
 * <p>Three additional transcoding hints that act on the SVG
 * processor can be specified:
 *
 * <p><tt>KEY_LANGUAGE</tt> to set the default language to use (may be
 * used by a &lt;switch> SVG element for example),
 * <tt>KEY_USER_STYLESHEET_URI</tt> to fix the URI of a user
 * stylesheet, and <tt>KEY_PIXEL_TO_MM</tt> to specify the pixel to
 * millimeter conversion factor.
 *
 * @author <a href="mailto:keiron@aftexsw.com">Keiron Liddle</a>
 * @version $Id$
 */
public abstract class AbstractPSTranscoder extends AbstractFOPTranscoder {

    private   Configuration                cfg      = null;
    protected AbstractPSDocumentGraphics2D graphics = null;

    /**
     * Constructs a new <tt>AbstractPSTranscoder</tt>.
     */
    public AbstractPSTranscoder() {
        super();
    }

    protected abstract AbstractPSDocumentGraphics2D createDocumentGraphics2D();

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
            FontInfo fontInfo = new FontInfo();   
            //TODO Do custom font configuration here somewhere/somehow 
            FontSetup.setup(fontInfo, null, null);   
            graphics.setCustomTextHandler(new NativeTextHandler(graphics, fontInfo));
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
            graphics.setupDocument(output.getOutputStream(), w, h);
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
    
    /** @return true if text should be stroked rather than painted using text operators */
    protected boolean isTextStroked() {
        boolean stroke = false;
        if (hints.containsKey(KEY_STROKE_TEXT)) {
            stroke = ((Boolean)hints.get(KEY_STROKE_TEXT)).booleanValue();
        }
        return stroke;
    }

    /** @see org.apache.batik.transcoder.SVGAbstractTranscoder#createBridgeContext() */
    protected BridgeContext createBridgeContext() {

        BridgeContext ctx = new BridgeContext(userAgent);
        if (!isTextStroked()) {
            TextHandler handler = graphics.getCustomTextHandler();
            if (handler instanceof NativeTextHandler) {
                NativeTextHandler nativeTextHandler = (NativeTextHandler)handler; 
                PSTextPainter textPainter = new PSTextPainter(nativeTextHandler);
                ctx.setTextPainter(textPainter);
                ctx.putBridge(new PSTextElementBridge(textPainter));
            }
        }

        //ctx.putBridge(new PSImageElementBridge());
        return ctx;
    }


}
