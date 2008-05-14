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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * An IOCA Image Data Object
 */
public class ImageObject extends AbstractDataObject {

    /**
     * The image segment
     */
    private ImageSegment imageSegment = null;

    /**
     * Constructor for the image object with the specified name,
     * the name must be a fixed length of eight characters.
     * @param name The name of the image.
     */
    public ImageObject(String name) {
        super(name);
    }

    /**
     * Set the dimensions of the image.
     * @param xresol the x resolution of the image
     * @param yresol the y resolution of the image
     * @param width the image width
     * @param height the image height
     */
    public void setImageParameters(int xresol, int yresol, int width, int height) {
        if (objectEnvironmentGroup == null) {
            objectEnvironmentGroup = new ObjectEnvironmentGroup();
        }
        objectEnvironmentGroup.setImageData(xresol, yresol, width, height);
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageSize(xresol, yresol, width, height);
    }

    /**
     * Sets the image encoding.
     * @param encoding The image encoding.
     */
    public void setImageEncoding(byte encoding) {
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageEncoding(encoding);
    }

    /**
     * Sets the image compression.
     * @param compression The image compression.
     */
    public void setImageCompression(byte compression) {
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageCompression(compression);
    }

    /**
     * Sets the image IDE size.
     * @param size The IDE size.
     */
    public void setImageIDESize(byte size) {
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageIDESize(size);
    }

    /**
     * Sets the image IDE color model.
     * @param colorModel    the IDE color model.
     */
    public void setImageIDEColorModel(byte colorModel) {
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageIDEColorModel(colorModel);
    }

    /**
     * Set the data of the image.
     * @param data The image data
     */
    public void setImageData(byte[] data) {
        if (imageSegment == null) {
            imageSegment = new ImageSegment();
        }
        imageSegment.setImageData(data);
    }

    /**
     * Helper method to return the start of the image object.
     * @param len the length of this ipd start
     * @return byte[] The data stream.
     */
    private byte[] getIPDStart(int len) {
        byte[] l = BinaryUtils.convert(len + 8, 2);
        byte[] data = new byte[] {
            0x5A, // Structured field identifier
            l[0], // Length byte 1
            l[1], // Length byte 2
            (byte) 0xD3, // Structured field id byte 1
            (byte) 0xEE, // Structured field id byte 2
            (byte) 0xFB, // Structured field id byte 3
            0x00, // Flags
            0x00, // Reserved
            0x00, // Reserved
        };
        return data;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        super.writeContent(os);
        if (imageSegment != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageSegment.write(baos);
            byte[] b = baos.toByteArray();
            int off = 0;
            while (off < b.length) {
                int len = Math.min(30000, b.length - off);
                os.write(getIPDStart(len));
                os.write(b, off, len);
                off += len;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xFB; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * Helper method to write the end of the Image Object.
     * @param os The stream to write to
     * @throws IOException in the event
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xFB; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }
}
