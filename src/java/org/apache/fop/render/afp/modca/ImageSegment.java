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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

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
     * Default name for the object environment group
     */
    private static final String DEFAULT_NAME = "IS000001";

    /**
     * The ImageContent for the image segment
     */
    private ImageContent imageContent = null;

    /**
     * Default constructor for the ImageSegment.
     */
    public ImageSegment() {
        this(DEFAULT_NAME);
    }

    /**
     * Constructor for the image segment with the specified name,
     * the name must be a fixed length of eight characters.
     * 
     * @param name The name of the image.
     */
    public ImageSegment(String name) {
        super(name);
    }

    /** {@inheritDoc} */
    protected int getNameLength() {
        return 4;
    }

    /**
     * Sets the image size parameters resolution, hsize and vsize.
     * 
     * @param hresol The horizontal resolution of the image.
     * @param vresol The vertical resolution of the image.
     * @param hsize The horizontal size of the image.
     * @param vsize The vertival size of the image.
     */
    public void setImageSize(int hresol, int vresol, int hsize, int vsize) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageSize(hresol, vresol, hsize, vsize);
    }

    /**
     * Sets the image encoding.
     * 
     * @param encoding The image encoding.
     */
    public void setImageEncoding(byte encoding) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageEncoding(encoding);
    }

    /**
     * Sets the image compression.
     * 
     * @param compression The image compression.
     */
    public void setImageCompression(byte compression) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageCompression(compression);
    }

    /**
     * Sets the image IDE size.
     * 
     * @param size The IDE size.
     */
    public void setImageIDESize(byte size) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageIDESize(size);
    }

    /**
     * Sets the image IDE color model.
     * 
     * @param colorModel    the IDE color model.
     */
    public void setImageIDEColorModel(byte colorModel) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageIDEColorModel(colorModel);
    }

    /**
     * Set the data of the image.
     * 
     * @param data the image data
     */
    public void setImageData(byte[] data) {
        if (imageContent == null) {
            imageContent = new ImageContent();
        }
        imageContent.setImageData(data);
    }

    /** {@inheritDoc} */
    public void writeContent(OutputStream os) throws IOException {
        if (imageContent != null) {
            imageContent.write(os);
        }
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
