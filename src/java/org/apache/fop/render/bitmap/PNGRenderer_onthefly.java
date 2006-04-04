/*
 * Copyright 2005-2006 The Apache Software Foundation.
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

package org.apache.fop.render.bitmap;

import java.awt.image.RenderedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.xmlgraphics.image.codec.png.PNGEncodeParam;
import org.apache.xmlgraphics.image.codec.png.PNGImageEncoder;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.PageViewport;
import org.apache.fop.render.java2d.Java2DRenderer;

/**
 * PNG Renderer This class actually does not render itself, instead it extends
 * <code>org.apache.fop.render.java2D.Java2DRenderer</code> and just encode
 * rendering results into PNG format using Batik's image codec
 */
public class PNGRenderer_onthefly extends Java2DRenderer {

    /** The MIME type for png-Rendering */
    public static final String MIME_TYPE = "image/png";

    /** The file syntax prefix, eg. "page" will output "page1.png" etc */
    private String fileSyntax;

    /** The output directory where images are to be written */
    private File outputDir;

    /** The PNGEncodeParam for the image */
    private PNGEncodeParam renderParams;

    /** The OutputStream for the first Image */
    private OutputStream firstOutputStream;

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** @see org.apache.fop.render.Renderer#supportsOutOfOrder() */
    public boolean supportsOutOfOrder() {
        return true;
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

        outputDir = f.getParentFile();

        // extracting file name syntax
        String s = f.getName();
        int i = s.lastIndexOf(".");
        if (s.charAt(i - 1) == '1') {
            i--; // getting rid of the "1"
        }
        fileSyntax = s.substring(0, i);
    }

    /** @see org.apache.fop.render.Renderer#renderPage(org.apache.fop.area.PageViewport) */
    public void renderPage(PageViewport pageViewport) throws IOException,
            FOPException {

        // Do the rendering: get the image for this page
        RenderedImage image = (RenderedImage) getPageImage(pageViewport);

        // Encode this image
        log.debug("Encoding page" + (getCurrentPageNumber() + 1));
        renderParams = PNGEncodeParam.getDefaultEncodeParam(image);
        OutputStream os = getCurrentOutputStream(getCurrentPageNumber());
        if (os != null) {
            try {
                PNGImageEncoder encoder = new PNGImageEncoder(os, renderParams);
                encoder.encode(image);
            } finally {
                //Only close self-created OutputStreams
                if (os != firstOutputStream) {
                    IOUtils.closeQuietly(os);
                }
            }
        }

        setCurrentPageNumber(getCurrentPageNumber() + 1);
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

        File f = new File(outputDir + File.separator + fileSyntax
                + (pageNumber + 1) + ".png");
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(f));
            return os;
        } catch (FileNotFoundException e) {
            new FOPException("Can't build the OutputStream\n" + e);
            return null;
        }
    }
}
