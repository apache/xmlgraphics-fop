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

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.image.loader.impl.ImageRawPNG;
import org.apache.xmlgraphics.image.loader.impl.ImageRawStream;
import org.apache.xmlgraphics.ps.ImageEncoder;

/**
 * ImageEncoder implementation for PNG images.
 */
public class ImageEncoderPNG implements ImageEncoder {
    private final ImageRawPNG image;
    private int numberOfInterleavedComponents;

    /**
     * Main constructor
     * @param image the PNG image
     */
    public ImageEncoderPNG(ImageRawPNG image) {
        this.image = image;
        ColorModel cm = this.image.getColorModel();
        if (cm instanceof IndexColorModel) {
            numberOfInterleavedComponents = 1;
        } else {
            // this can be 1 (gray), 2 (gray + alpha), 3 (rgb) or 4 (rgb + alpha)
            // numberOfInterleavedComponents = (cm.hasAlpha() ? 1 : 0) + cm.getNumColorComponents();
            numberOfInterleavedComponents = cm.getNumComponents();
        }
    }

    /** {@inheritDoc} */
    public void writeTo(OutputStream out) throws IOException {
        // TODO: refactor this code with equivalent PDF code
        InputStream in = ((ImageRawStream) image).createInputStream();
        InflaterInputStream infStream = null;
        DataInputStream dataStream = null;
        ByteArrayOutputStream baos = null;
        DeflaterOutputStream dos = null;
        try {
            if (numberOfInterleavedComponents == 1 || numberOfInterleavedComponents == 3) {
                // means we have Gray, RGB, or Palette
                IOUtils.copy(in, out);
            } else {
                // means we have Gray + alpha or RGB + alpha
                int numBytes = numberOfInterleavedComponents - 1; // 1 for Gray, 3 for RGB
                int numColumns = image.getSize().getWidthPx();
                infStream = new InflaterInputStream(in, new Inflater());
                dataStream = new DataInputStream(infStream);
                int offset = 0;
                int bytesPerRow = numberOfInterleavedComponents * numColumns;
                int filter;
                // here we need to inflate the PNG pixel data, which includes alpha, separate the alpha
                // channel and then deflate the RGB channels back again
                // TODO: not using the baos below and using the original out instead (as happens in PDF)
                // would be preferable but that does not work with the rest of the postscript code; this
                // needs to be revisited
                baos = new ByteArrayOutputStream();
                dos = new DeflaterOutputStream(/* out */baos, new Deflater());
                while ((filter = dataStream.read()) != -1) {
                    byte[] bytes = new byte[bytesPerRow];
                    dataStream.readFully(bytes, 0, bytesPerRow);
                    dos.write((byte) filter);
                    for (int j = 0; j < numColumns; j++) {
                        dos.write(bytes, offset, numBytes);
                        offset += numberOfInterleavedComponents;
                    }
                    offset = 0;
                }
                dos.close();
                IOUtils.copy(new ByteArrayInputStream(baos.toByteArray()), out);
            }
        } finally {
            IOUtils.closeQuietly(dos);
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(dataStream);
            IOUtils.closeQuietly(infStream);
            IOUtils.closeQuietly(in);
        }
    }

    /** {@inheritDoc} */
    public String getImplicitFilter() {
        String filter = "<< /Predictor 15 /Columns " + image.getSize().getWidthPx();
        filter += " /Colors " + (numberOfInterleavedComponents > 2 ? 3 : 1);
        filter += " /BitsPerComponent " + image.getBitDepth() + " >> /FlateDecode";
        return filter;
    }
}
