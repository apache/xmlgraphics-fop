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

package org.apache.fop.render.bitmap;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;
import org.apache.xmlgraphics.image.writer.MultiImageWriter;

import org.apache.fop.apps.FopFactoryConfigurator;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.java2d.Java2DPainter;
import org.apache.fop.render.java2d.Java2DUtil;

/**
 * {@code IFDocumentHandler} implementation that produces PCL 5.
 */
public class TIFFDocumentHandler extends AbstractBinaryWritingIFDocumentHandler
            implements TIFFConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(TIFFDocumentHandler.class);

    private ImageWriter imageWriter;
    private MultiImageWriter multiImageWriter;

    private int pageCount;
    private Dimension currentPageDimensions;
    private BufferedImage currentImage;

    private BitmapRenderingSettings bitmapSettings = new BitmapRenderingSettings();

    private double scaleFactor = 1.0;

    /**
     * Default constructor.
     */
    public TIFFDocumentHandler() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_TIFF;
    }

    /** {@inheritDoc} */
    public void setContext(IFContext context) {
        super.setContext(context);

        //Set target resolution
        int dpi = Math.round(context.getUserAgent().getTargetResolution());
        getSettings().getWriterParams().setResolution(dpi);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new TIFFRendererConfigurator(getUserAgent());
    }

    /**
     * Returns the settings for bitmap rendering.
     * @return the settings object
     */
    public BitmapRenderingSettings getSettings() {
        return this.bitmapSettings;
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        FontInfo fi = Java2DUtil.buildDefaultJava2DBasedFontInfo(fontInfo, getUserAgent());
        setFontInfo(fi);
    }

    //----------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PDF generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }

            // Creates writer
            this.imageWriter = ImageWriterRegistry.getInstance().getWriterFor(getMimeType());
            if (this.imageWriter == null) {
                BitmapRendererEventProducer eventProducer
                    = BitmapRendererEventProducer.Provider.get(
                            getUserAgent().getEventBroadcaster());
                eventProducer.noImageWriterFound(this, getMimeType());
            }
            if (this.imageWriter.supportsMultiImageWriter()) {
                this.multiImageWriter = this.imageWriter.createMultiImageWriter(outputStream);
            }
            this.pageCount = 0;
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            if (this.multiImageWriter != null) {
                this.multiImageWriter.close();
            }
            this.multiImageWriter = null;
            this.imageWriter = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        this.pageCount++;
        this.currentPageDimensions = new Dimension(size);
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        double scale = scaleFactor
            * (25.4f / FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION)
                / getUserAgent().getTargetPixelUnitToMillimeter();
        int bitmapWidth = (int) ((this.currentPageDimensions.width * scale / 1000f) + 0.5f);
        int bitmapHeight = (int) ((this.currentPageDimensions.height * scale / 1000f) + 0.5f);
        this.currentImage = createBufferedImage(bitmapWidth, bitmapHeight);
        Graphics2D graphics2D = this.currentImage.createGraphics();
        // draw page background
        if (!getSettings().hasTransparentPageBackground()) {
            graphics2D.setBackground(getSettings().getPageBackgroundColor());
            graphics2D.setPaint(getSettings().getPageBackgroundColor());
            graphics2D.fillRect(0, 0, bitmapWidth, bitmapHeight);
        }

        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        if (getSettings().isAntiAliasingEnabled()
                && this.currentImage.getColorModel().getPixelSize() > 1) {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF);
            graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
        if (getSettings().isQualityRenderingEnabled()) {
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
        } else {
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
        }
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        graphics2D.scale(scale / 1000f, scale / 1000f);

        return new Java2DPainter(graphics2D, getContext(), getFontInfo());
    }

    /**
     * Creates a new BufferedImage.
     * @param bitmapWidth the desired width in pixels
     * @param bitmapHeight the desired height in pixels
     * @return the new BufferedImage instance
     */
    protected BufferedImage createBufferedImage(int bitmapWidth, int bitmapHeight) {
        return new BufferedImage(bitmapWidth, bitmapHeight, getSettings().getBufferedImageType());
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            if (this.multiImageWriter == null) {
                switch (this.pageCount) {
                case 1:
                    this.imageWriter.writeImage(
                            this.currentImage, this.outputStream,
                            getSettings().getWriterParams());
                    break;
                case 2:
                    BitmapRendererEventProducer eventProducer
                        = BitmapRendererEventProducer.Provider.get(
                                getUserAgent().getEventBroadcaster());
                    eventProducer.stoppingAfterFirstPageNoFilename(this);
                    break;
                default:
                    //ignore
                }
            } else {
                this.multiImageWriter.writeImage(this.currentImage,
                        getSettings().getWriterParams());
            }
            this.currentImage = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error while encoding BufferedImage", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        this.currentPageDimensions = null;
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (false) {
            //TODO Handle extensions
        } else {
            log.debug("Don't know how to handle extension object. Ignoring: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }

}
