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

package org.apache.fop.render.pcl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.UnitConv;

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
import org.apache.fop.render.pcl.extensions.PCLElementMapping;

/**
 * {@link org.apache.fop.render.intermediate.IFDocumentHandler} implementation
 * that produces PCL 5.
 */
public class PCLDocumentHandler extends AbstractBinaryWritingIFDocumentHandler
            implements PCLConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(PCLDocumentHandler.class);

    /** Utility class for handling all sorts of peripheral tasks around PCL generation. */
    protected PCLRenderingUtil pclUtil;

    /** The PCL generator */
    private PCLGenerator gen;

    private PCLPageDefinition currentPageDefinition;

    /** contains the pageWith of the last printed page */
    private long pageWidth = 0;
    /** contains the pageHeight of the last printed page */
    private long pageHeight = 0;

    /** the current page image (only set when all-bitmap painting is activated) */
    private BufferedImage currentImage;


    /**
     * Default constructor.
     */
    public PCLDocumentHandler() {
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PCL;
    }

    /** {@inheritDoc} */
    @Override
    public void setContext(IFContext context) {
        super.setContext(context);
        this.pclUtil = new PCLRenderingUtil(context.getUserAgent());
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new PCLRendererConfigurator(getUserAgent());
    }

    /** {@inheritDoc} */
    @Override
    public void setDefaultFontInfo(FontInfo fontInfo) {
        FontInfo fi = Java2DUtil.buildDefaultJava2DBasedFontInfo(fontInfo, getUserAgent());
        setFontInfo(fi);
    }

    PCLRenderingUtil getPCLUtil() {
        return this.pclUtil;
    }

    PCLGenerator getPCLGenerator() {
        return this.gen;
    }

    /** @return the target resolution */
    protected int getResolution() {
        int resolution = Math.round(getUserAgent().getTargetResolution());
        if (resolution <= 300) {
            return 300;
        } else {
            return 600;
        }
    }

    //----------------------------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public void startDocument() throws IFException {
        super.startDocument();
        try {
            this.gen = new PCLGenerator(this.outputStream, getResolution());
            this.gen.setDitheringQuality(pclUtil.getDitheringQuality());

            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
                gen.writeText("@PJL COMMENT Produced by " + getUserAgent().getProducer() + "\n");
                if (getUserAgent().getTitle() != null) {
                    gen.writeText("@PJL JOB NAME = \"" + getUserAgent().getTitle() + "\"\n");
                }
                gen.writeText("@PJL SET RESOLUTION = " + getResolution() + "\n");
                gen.writeText("@PJL ENTER LANGUAGE = PCL\n");
            }
            gen.resetPrinter();
            gen.setUnitOfMeasure(getResolution());
            gen.setRasterGraphicsResolution(getResolution());
        } catch (IOException e) {
            throw new IFException("I/O error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endDocumentHeader() throws IFException {
    }

    /** {@inheritDoc} */
    @Override
    public void endDocument() throws IFException {
        try {
            gen.separateJobs();
            gen.resetPrinter();
            if (!pclUtil.isPJLDisabled()) {
                gen.universalEndOfLanguage();
            }
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

        try {
            //Paper source
            Object paperSource = getContext().getForeignAttribute(
                    PCLElementMapping.PCL_PAPER_SOURCE);
            if (paperSource != null) {
                gen.selectPaperSource(Integer.parseInt(paperSource.toString()));
            }

            //Output bin
            Object outputBin = getContext().getForeignAttribute(
                    PCLElementMapping.PCL_OUTPUT_BIN);
            if (outputBin != null) {
                gen.selectOutputBin(Integer.parseInt(outputBin.toString()));
            }

            // Is Page duplex?
            Object pageDuplex = getContext().getForeignAttribute(
                    PCLElementMapping.PCL_DUPLEX_MODE);
            if (pageDuplex != null) {
                gen.selectDuplexMode(Integer.parseInt(pageDuplex.toString()));
            }

            //Page size
            final long pagewidth = size.width;
            final long pageheight = size.height;
            selectPageFormat(pagewidth, pageheight);
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        if (pclUtil.getRenderingMode() == PCLRenderingMode.BITMAP) {
            return createAllBitmapPainter();
        } else {
            return new PCLPainter(this, this.currentPageDefinition);
        }
    }

    private IFPainter createAllBitmapPainter() {
        double scale = gen.getMaximumBitmapResolution()
                / FopFactoryConfigurator.DEFAULT_TARGET_RESOLUTION;
        Rectangle printArea = this.currentPageDefinition.getLogicalPageRect();
        int bitmapWidth = (int)Math.ceil(
                UnitConv.mpt2px(printArea.width, gen.getMaximumBitmapResolution()));
        int bitmapHeight = (int)Math.ceil(
                UnitConv.mpt2px(printArea.height, gen.getMaximumBitmapResolution()));
        this.currentImage = createBufferedImage(bitmapWidth, bitmapHeight);
        Graphics2D graphics2D = this.currentImage.createGraphics();

        if (!PCLGenerator.isJAIAvailable()) {
            RenderingHints hints = new RenderingHints(null);
            //These hints don't seem to make a difference :-( Not seeing any dithering on Sun Java.
            hints.put(RenderingHints.KEY_DITHERING,
                    RenderingHints.VALUE_DITHER_ENABLE);
            graphics2D.addRenderingHints(hints);
        }

        //Ensure white page background
        graphics2D.setBackground(Color.WHITE);
        graphics2D.clearRect(0, 0, bitmapWidth, bitmapHeight);

        graphics2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        graphics2D.scale(scale / 1000f, scale / 1000f);
        graphics2D.translate(-printArea.x, -printArea.y);

        return new Java2DPainter(graphics2D, getContext(), getFontInfo());
    }

    private BufferedImage createBufferedImage(int bitmapWidth, int bitmapHeight) {
        int bitmapType;
        if (PCLGenerator.isJAIAvailable()) {
            //TYPE_BYTE_GRAY was used to work around the lack of dithering when using
            //TYPE_BYTE_BINARY. Adding RenderingHints didn't help.
            bitmapType = BufferedImage.TYPE_BYTE_GRAY;
            //bitmapType = BufferedImage.TYPE_INT_RGB; //Use to enable Batik gradients
        } else {
            bitmapType = BufferedImage.TYPE_BYTE_BINARY;
        }
        return new BufferedImage(
                bitmapWidth, bitmapHeight, bitmapType);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        if (this.currentImage != null) {
            try {
                //ImageWriterUtil.saveAsPNG(this.currentImage, new java.io.File("D:/page.png"));
                Rectangle printArea = this.currentPageDefinition.getLogicalPageRect();
                gen.setCursorPos(0, 0);
                gen.paintBitmap(this.currentImage, printArea.getSize(), true);
            } catch (IOException ioe) {
                throw new IFException("I/O error while encoding page image", ioe);
            } finally {
                this.currentImage = null;
            }
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            //Eject page
            gen.formFeed();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
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

    private void selectPageFormat(long pagewidth, long pageheight) throws IOException {
        //Only set the page format if it changes (otherwise duplex printing won't work)
        if ((pagewidth != this.pageWidth) || (pageheight != this.pageHeight))  {
            this.pageWidth = pagewidth;
            this.pageHeight = pageheight;

            this.currentPageDefinition = PCLPageDefinition.getPageDefinition(
                    pagewidth, pageheight, 1000);

            if (this.currentPageDefinition == null) {
                this.currentPageDefinition = PCLPageDefinition.getDefaultPageDefinition();
                log.warn("Paper type could not be determined. Falling back to: "
                        + this.currentPageDefinition.getName());
            }
            if (log.isDebugEnabled()) {
                log.debug("page size: " + currentPageDefinition.getPhysicalPageSize());
                log.debug("logical page: " + currentPageDefinition.getLogicalPageRect());
            }

            if (this.currentPageDefinition.isLandscapeFormat()) {
                gen.writeCommand("&l1O"); //Landscape Orientation
            } else {
                gen.writeCommand("&l0O"); //Portrait Orientation
            }
            gen.selectPageSize(this.currentPageDefinition.getSelector());

            gen.clearHorizontalMargins();
            gen.setTopMargin(0);
        }
    }

}
