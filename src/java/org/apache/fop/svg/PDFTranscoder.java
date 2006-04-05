/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import java.awt.Color;
import java.io.IOException;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UnitProcessor;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.keys.FloatKey;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGLength;

/**
 * This class enables to transcode an input to a pdf document.
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
public class PDFTranscoder extends AbstractFOPTranscoder
        implements Configurable {

    /**
     * The key is used to specify the resolution for on-the-fly images generated
     * due to complex effects like gradients and filters. 
     */
    public static final TranscodingHints.Key KEY_DEVICE_RESOLUTION = new FloatKey();

    private Configuration cfg = null;
    
    /** Graphics2D instance that is used to paint to */
    protected PDFDocumentGraphics2D graphics = null;

    /**
     * Constructs a new <tt>ImageTranscoder</tt>.
     */
    public PDFTranscoder() {
        super();
        this.handler = new FOPErrorHandler();
    }

    /**
     * @see org.apache.fop.svg.AbstractFOPTranscoder#createUserAgent()
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
     * @see org.apache.avalon.framework.configuration.Configurable#configure(Configuration)
     */
    public void configure(Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
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

        graphics = new PDFDocumentGraphics2D();
        
        try {
            if (this.cfg != null) {
                ContainerUtil.configure(graphics, this.cfg);
            }
            ContainerUtil.initialize(graphics);
        } catch (Exception e) {
            throw new TranscoderException(
                "Error while setting up PDFDocumentGraphics2D", e);
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

        // prepare the image to be painted
        //int w = (int)(width + 0.5);
        //int h = (int)(height + 0.5);

        try {
            if (hints.containsKey(KEY_DEVICE_RESOLUTION)) {
                graphics.setDeviceDPI(((Float)hints.get(KEY_DEVICE_RESOLUTION)).floatValue());
            }
            graphics.setupDocument(output.getOutputStream(), w, h);
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

    /** @see org.apache.batik.transcoder.SVGAbstractTranscoder#createBridgeContext() */
    protected BridgeContext createBridgeContext() {
        BridgeContext ctx = new PDFBridgeContext(userAgent, graphics.getFontInfo());
        return ctx;
    }

}
