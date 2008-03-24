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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;

import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.PageViewport;
import org.apache.fop.render.java2d.Java2DRenderer;

/**
 * PNG Renderer This class actually does not render itself, instead it extends
 * <code>org.apache.fop.render.java2D.Java2DRenderer</code> and just encode
 * rendering results into PNG format using Batik's image codec
 */
public class PNGRenderer extends Java2DRenderer {

    /** The MIME type for png-Rendering */
    public static final String MIME_TYPE = MimeConstants.MIME_PNG;

    /** The file extension expected for PNG files */
    private static final String PNG_FILE_EXTENSION = "png";

    /** The OutputStream for the first Image */
    private OutputStream firstOutputStream;
    
    /** Helper class for generating multiple files */
    private MultiFileRenderingUtil multiFileUtil;

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream) throws IOException {
        log.info("rendering areas to PNG");
        multiFileUtil = new MultiFileRenderingUtil(PNG_FILE_EXTENSION, 
                    getUserAgent().getOutputFile());
        this.firstOutputStream = outputStream;
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {

        super.stopRenderer();

        for (int i = 0; i < pageViewportList.size(); i++) {

            OutputStream os = getCurrentOutputStream(i);
            if (os == null) {
                BitmapRendererEventProducer eventProducer
                    = BitmapRendererEventProducer.Factory.create(
                            getUserAgent().getEventBroadcaster());
                eventProducer.stoppingAfterFirstPageNoFilename(this);
                break;
            }
            try {
                // Do the rendering: get the image for this page
                PageViewport pv = (PageViewport)pageViewportList.get(i);
                RenderedImage image = (RenderedImage)getPageImage(pv);
    
                // Encode this image
                if (log.isDebugEnabled()) {
                    log.debug("Encoding page " + (i + 1));
                }
                writeImage(os, image);
            } finally {
                //Only close self-created OutputStreams
                if (os != firstOutputStream) {
                    IOUtils.closeQuietly(os);
                }
            }
        }
    }

    private void writeImage(OutputStream os, RenderedImage image) throws IOException {
        ImageWriterParams params = new ImageWriterParams();
        params.setResolution(Math.round(userAgent.getTargetResolution()));
        
        // Encode PNG image
        ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(getMimeType());
        if (writer == null) {
            BitmapRendererEventProducer eventProducer
                = BitmapRendererEventProducer.Factory.create(
                        getUserAgent().getEventBroadcaster());
            eventProducer.noImageWriterFound(this, getMimeType());
        }
        if (log.isDebugEnabled()) {
            log.debug("Writing image using " + writer.getClass().getName());
        }
        writer.writeImage(image, os, params);
    }

    /**
     * Returns the OutputStream corresponding to this page
     * @param pageNumber 0-based page number
     * @return the corresponding OutputStream
     * @throws IOException In case of an I/O error
     */
    protected OutputStream getCurrentOutputStream(int pageNumber) throws IOException {

        if (pageNumber == 0) {
            return firstOutputStream;
        } else {
            return multiFileUtil.createOutputStream(pageNumber);
        }

    }
}
