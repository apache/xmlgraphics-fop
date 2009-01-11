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

package org.apache.fop.render.afp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.fop.afp.DataStream;
import org.apache.fop.afp.fonts.AFPPageFonts;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.render.intermediate.AbstractBinaryWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

/**
 * {@code IFDocumentHandler} implementation that produces AFP (MO:DCA).
 */
public class AFPDocumentHandler extends AbstractBinaryWritingIFDocumentHandler
            implements AFPCustomizable {

    /** logging instance */
    private static Log log = LogFactory.getLog(AFPDocumentHandler.class);

    /** the resource manager */
    private AFPResourceManager resourceManager;

    /** the painting state */
    private final AFPPaintingState paintingState;

    /** unit converter */
    private final AFPUnitConverter unitConv;

    /** the AFP datastream */
    private DataStream dataStream;

    /**
     * Default constructor.
     */
    public AFPDocumentHandler() {
        this.resourceManager = new AFPResourceManager();
        this.paintingState = new AFPPaintingState();
        this.unitConv = paintingState.getUnitConverter();
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    /** {@inheritDoc} */
    public void setContext(IFContext context) {
        super.setContext(context);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return new AFPRendererConfigurator(getUserAgent());
    }

    AFPPaintingState getPaintingState() {
        return this.paintingState;
    }

    DataStream getDataStream() {
        return this.dataStream;
    }

    AFPResourceManager getResourceManager() {
        return this.resourceManager;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            if (getUserAgent() == null) {
                throw new IllegalStateException(
                        "User agent must be set before starting PostScript generation");
            }
            if (this.outputStream == null) {
                throw new IllegalStateException("OutputStream hasn't been set through setResult()");
            }
            paintingState.setColor(Color.WHITE);

            this.dataStream = resourceManager.createDataStream(paintingState, outputStream);

            this.dataStream.startDocument();
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
            this.dataStream.endDocument();
            this.dataStream = null;
            this.resourceManager.writeToStream();
            this.resourceManager = null;
        } catch (IOException ioe) {
            throw new IFException("I/O error in endDocument()", ioe);
        }
        super.endDocument();
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        try {
            dataStream.startPageGroup();
        } catch (IOException ioe) {
            throw new IFException("I/O error in startPageSequence()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /**
     * Returns the base AFP transform
     *
     * @return the base AFP transform
     */
    private AffineTransform getBaseTransform() {
        AffineTransform baseTransform = new AffineTransform();
        double scale = unitConv.mpt2units(1);
        baseTransform.scale(scale, scale);
        return baseTransform;
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        paintingState.clear();

        AffineTransform baseTransform = getBaseTransform();
        paintingState.concatenate(baseTransform);

        int pageWidth = Math.round(unitConv.mpt2units(size.width));
        paintingState.setPageWidth(pageWidth);

        int pageHeight = Math.round(unitConv.mpt2units(size.height));
        paintingState.setPageHeight(pageHeight);

        int pageRotation = paintingState.getPageRotation();
        int resolution = paintingState.getResolution();

        dataStream.startPage(pageWidth, pageHeight, pageRotation,
                resolution, resolution);

        //TODO Handle page extensions
        //renderPageObjectExtensions(pageViewport);

    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        super.startPageHeader();

    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        super.endPageHeader();
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        return new AFPPainter(this);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            AFPPageFonts pageFonts = paintingState.getPageFonts();
            if (pageFonts != null && !pageFonts.isEmpty()) {
                dataStream.addFontsToCurrentPage(pageFonts);
            }

            dataStream.endPage();
        } catch (IOException ioe) {
            throw new IFException("I/O error in endPage()", ioe);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        /*
        try {
        } catch (IOException ioe) {
            throw new IFException("I/O error in handleExtensionObject()", ioe);
        }
        */
    }

    // ---=== AFPCustomizable ===---

    /** {@inheritDoc} */
    public void setBitsPerPixel(int bitsPerPixel) {
        paintingState.setBitsPerPixel(bitsPerPixel);
    }

    /** {@inheritDoc} */
    public void setColorImages(boolean colorImages) {
        paintingState.setColorImages(colorImages);
    }

    /** {@inheritDoc} */
    public void setNativeImagesSupported(boolean nativeImages) {
        paintingState.setNativeImagesSupported(nativeImages);
    }

    /** {@inheritDoc} */
    public void setResolution(int resolution) {
        paintingState.setResolution(resolution);
    }

    /** {@inheritDoc} */
    public int getResolution() {
        return paintingState.getResolution();
    }

    /** {@inheritDoc} */
    public void setDefaultResourceGroupFilePath(String filePath) {
        resourceManager.setDefaultResourceGroupFilePath(filePath);
    }

}
