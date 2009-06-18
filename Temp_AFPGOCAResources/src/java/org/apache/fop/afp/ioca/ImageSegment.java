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

package org.apache.fop.afp.ioca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.Factory;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;

/**
 * An Image Segment is represented by a set of self-defining fields, fields
 * that describe their own contents.  It starts with a Begin Segment, and
 * ends with an End Segment.
 *
 * Between the Begin Segment and End Segment is the image information to
 * be processed, called the Image Content.
 *
 * Only one Image Content can exist within a single IOCA Image Segment.
 */
public class ImageSegment extends AbstractNamedAFPObject {

    /**
     * The ImageContent for the image segment
     */
    private ImageContent imageContent = null;

    private final Factory factory;

    /**
     * Constructor for the image segment with the specified name,
     * the name must be a fixed length of eight characters.
     * @param factory the object factory
     *
     * @param name the name of the image.
     */
    public ImageSegment(Factory factory, String name) {
        super(name);
        this.factory = factory;
    }

    private ImageContent getImageContent() {
        if (imageContent == null) {
            this.imageContent = factory.createImageContent();
        }
        return imageContent;
    }

    /**
     * Sets the image size parameters resolution, hsize and vsize.
     *
     * @param hsize The horizontal size of the image.
     * @param vsize The vertical size of the image.
     * @param hresol The horizontal resolution of the image.
     * @param vresol The vertical resolution of the image.
     */
    public void setImageSize(int hsize, int vsize, int hresol, int vresol) {
        ImageSizeParameter imageSizeParameter
            = factory.createImageSizeParameter(hsize, vsize, hresol, vresol);
        getImageContent().setImageSizeParameter(imageSizeParameter);
    }

    /**
     * Sets the image encoding.
     *
     * @param encoding The image encoding.
     */
    public void setEncoding(byte encoding) {
        getImageContent().setImageEncoding(encoding);
    }

    /**
     * Sets the image compression.
     *
     * @param compression The image compression.
     */
    public void setCompression(byte compression) {
        getImageContent().setImageCompression(compression);
    }

    /**
     * Sets the image IDE size.
     *
     * @param size The IDE size.
     */
    public void setIDESize(byte size) {
        getImageContent().setImageIDESize(size);
    }

    /**
     * Sets the image IDE color model.
     *
     * @param colorModel the IDE color model.
     */
    public void setIDEColorModel(byte colorModel) {
        getImageContent().setImageIDEColorModel(colorModel);
    }

    /**
     * Set the data image data.
     *
     * @param data the image data
     */
    public void setData(byte[] imageData) {
        getImageContent().setImageData(imageData);
    }

    /** {@inheritDoc} */
    public void writeContent(OutputStream os) throws IOException {
        if (imageContent != null) {
            imageContent.writeToStream(os);
        }
    }

    private static final int NAME_LENGTH = 4;

    /** {@inheritDoc} */
    protected int getNameLength() {
        return NAME_LENGTH;
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] nameBytes = getNameBytes();
        byte[] data = new byte[] {
            0x70, // ID
            0x04, // Length
            nameBytes[0], // Name byte 1
            nameBytes[1], // Name byte 2
            nameBytes[2], // Name byte 3
            nameBytes[3], // Name byte 4
        };
        os.write(data);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            0x71, // ID
            0x00, // Length
        };
        os.write(data);
    }
}
