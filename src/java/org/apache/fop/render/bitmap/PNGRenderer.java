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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.writer.ImageWriter;
import org.apache.xmlgraphics.image.writer.ImageWriterParams;
import org.apache.xmlgraphics.image.writer.ImageWriterRegistry;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.FOPException;
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

    /** The file syntax prefix, eg. "page" will output "page1.png" etc */
    private String filePrefix;

    /** The output directory where images are to be written */
    private File outputDir;

    /** The OutputStream for the first Image */
    private OutputStream firstOutputStream;

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream) */
    public void startRenderer(OutputStream outputStream) throws IOException {
        log.info("rendering areas to PNG");
        setOutputDirectory();
        this.firstOutputStream = outputStream;
    }

    /**
     * Sets the output directory, either from the outfile specified on the
     * command line, or from the directory specified in configuration file. Also
     * sets the file name syntax, eg. "page"
     */
    private void setOutputDirectory() {

        // the file provided on the command line
        File f = getUserAgent().getOutputFile();
        if (f == null) {
            //No filename information available. Only the first page will be rendered.
            outputDir = null;
            filePrefix = null;
        } else {
            outputDir = f.getParentFile();

            // extracting file name syntax
            String s = f.getName();
            int i = s.lastIndexOf(".");
            if (s.charAt(i - 1) == '1') {
                i--; // getting rid of the "1"
            }
            filePrefix = s.substring(0, i);
        }

    }

    /** @see org.apache.fop.render.Renderer#stopRenderer() */
    public void stopRenderer() throws IOException {

        super.stopRenderer();

        for (int i = 0; i < pageViewportList.size(); i++) {

            OutputStream os = getCurrentOutputStream(i);
            if (os == null) {
                log.warn("No filename information available."
                        + " Stopping early after the first page.");
                break;
            }
            try {
                // Do the rendering: get the image for this page
                RenderedImage image = (RenderedImage) getPageImage((PageViewport) pageViewportList
                        .get(i));
    
                // Encode this image
                log.debug("Encoding page " + (i + 1));
                ImageWriterParams params = new ImageWriterParams();
                params.setResolution(Math.round(userAgent.getTargetResolution()));
                
                // Encode PNG image
                ImageWriter writer = ImageWriterRegistry.getInstance().getWriterFor(getMimeType());
                log.debug("Writing image using " + writer.getClass().getName());
                writer.writeImage(image, os, params);
            } finally {
                //Only close self-created OutputStreams
                if (os != firstOutputStream) {
                    IOUtils.closeQuietly(os);
                }
            }
        }
    }

    /**
     * Builds the OutputStream corresponding to this page
     * @param 0-based pageNumber
     * @return the corresponding OutputStream
     */
    private OutputStream getCurrentOutputStream(int pageNumber) {

        if (pageNumber == 0) {
            return firstOutputStream;
        }

        if (filePrefix == null) {
            return null;
        } else {
            File f = new File(outputDir,
                    filePrefix + (pageNumber + 1) + ".png");
            try {
                OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
                return os;
            } catch (FileNotFoundException e) {
                new FOPException("Can't build the OutputStream\n" + e);
                return null;
            }
        }
    }
}
