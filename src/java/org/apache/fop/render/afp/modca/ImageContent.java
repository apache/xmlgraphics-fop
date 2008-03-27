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
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * Image content IOCA object
 */
public class ImageContent extends AbstractStructuredAFPObject {

    /**
     * The CCITT T.4 Group 3 Coding Standard (G3 MH-Modified Huffman) is a
     * compression method standardized by the International Telegraph and
     * Telephone Consultative Committee (CCITT) for facsimile.  It enables
     * one-dimensional compression.
     */
    public static final byte COMPID_G3_MH = (byte)0x80;
    
    /**
     * The CCITT T.4 Group 3 Coding Option (G3 MR-Modified READ) is a
     * compression method standardized by the International Telegraph and
     * Telephone Consultative Committee (CCITT) for facsimile. It enables
     * two-dimensional compression.
     */
    public static final byte COMPID_G3_MR = (byte)0x81;

    /**
     * The CCITT T.6 Group 4 Coding Standard (G4 MMR-Modified Modified READ) is a
     * compression method standardized by the International Telegraph and
     * Telephone Consultative Committee (CCITT) for facsimile.  It enables
     * two-dimensional compression. 
     */
    public static final byte COMPID_G3_MMR = (byte)0x82;

    /**
     * The image size parameter
     */
    private ImageSizeParameter imageSizeParam = null;

    /**
     * The image encoding
     */
    private byte encoding = 0x03;

    /**
     * The image ide size
     */
    private byte size = 1;

    /**
     * The image compression
     */
    private byte compression = (byte)0xC0;

    /**
     * The image color model
     */
    private byte colorModel = 0x01;

    /**
     * The image data
     */
    private byte[] imageData = null;

    /**
     * Constructor for the image content
     */
    public ImageContent() {
    }

    /**
     * Sets the image size parameters
     * resolution, hsize and vsize.
     * @param hresol The horizontal resolution of the image.
     * @param vresol The vertical resolution of the image.
     * @param hsize The horizontal size of the image.
     * @param vsize The vertival size of the image.
     */
    public void setImageSize(int hresol, int vresol, int hsize, int vsize) {
        this.imageSizeParam = new ImageSizeParameter(hresol, vresol, hsize, vsize);
    }

    /**
     * Sets the image encoding.
     * @param enc The image encoding.
     */
    public void setImageEncoding(byte enc) {
        this.encoding = enc;
    }

    /**
     * Sets the image compression.
     * @param comp The image compression.
     */
    public void setImageCompression(byte comp) {
        this.compression = comp;
    }

    /**
     * Sets the image IDE size.
     * @param siz The IDE size.
     */
    public void setImageIDESize(byte siz) {
        this.size = siz;
    }

    /**
     * Sets the image IDE color model.
     * @param model    the IDE color model.
     */
    public void setImageIDEColorModel(byte model) {
        this.colorModel = model;
    }

    /**
     * Set the data of the image.
     * @param data the image data
     */
    public void setImageData(byte[] data) {
        this.imageData = data;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeContent(OutputStream os) throws IOException {
        if (imageSizeParam != null) {
            imageSizeParam.writeDataStream(os);
        }
        os.write(getImageEncodingParameter());
        os.write(getImageIDESizeParameter());
        os.write(getIDEStructureParameter());
        os.write(getExternalAlgorithmParameter());
        if (imageData != null) {
            int off = 0;
            while (off < imageData.length) {
                int len = Math.min(30000, imageData.length - off);
                os.write(getImageDataStart(len));
                os.write(imageData, off, len);
                off += len;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            (byte)0x91, // ID
                  0x01, // Length
            (byte)0xff, // Object Type = IOCA Image Object
        };
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            (byte)0x93, // ID
                  0x00, // Length
        };
        os.write(data);
    }

    /**
     * Helper method to return the start of the image segment.
     * @return byte[] The data stream.
     */
    private byte[] getImageDataStart(int len) {
        byte[] imageDataStartData = new byte[] {
            (byte)0xFE, // ID
            (byte)0x92, // ID
                  0x00, // Length
                  0x00, // Length
        };
        byte[] l = BinaryUtils.convert(len, 2);
        imageDataStartData[2] = l[0];
        imageDataStartData[3] = l[1];
        return imageDataStartData;
    }

    /**
     * Helper method to return the image encoding parameter.
     * @return byte[] The data stream.
     */
    private byte[] getImageEncodingParameter() {
        byte[] imageEncParamData = new byte[] {
            (byte)0x95, // ID
                  0x02, // Length
                  encoding,
                  0x01, // RECID
        };
        return imageEncParamData;
    }

    /**
     * Helper method to return the external algorithm parameter.
     * @return byte[] The data stream.
     */
    private byte[] getExternalAlgorithmParameter() {
        if (encoding == (byte)0x83 && compression != 0) {
            byte[] extAlgParamData = new byte[] {
                (byte)0x95, // ID
                      0x00, // Length
                      0x10, // ALGTYPE = Compression Algorithm
                      0x00, // Reserved
                (byte)0x83, // COMPRID = JPEG
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
              compression, // MARKER
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
            };
            extAlgParamData[1] = (byte)(extAlgParamData.length - 2);
            return extAlgParamData;
        }
        return new byte[0];
    }

    /**
     * Helper method to return the image encoding parameter.
     * @return byte[] The data stream.
     */
    private byte[] getImageIDESizeParameter() {
        byte[] imageIDESizeParamData = new byte[] {
            (byte)0x96, // ID
                  0x01, // Length
                  size,
        };
        return imageIDESizeParamData;
    }

    /**
     * Helper method to return the external algorithm parameter.
     * @return byte[] The data stream.
     */
    private byte[] getIDEStructureParameter() {
        if (colorModel != 0 && size == 24) {
            byte bits = (byte)(size / 3);
            byte[] ideStructParamData = new byte[] {
                (byte)0x9B, // ID
                      0x00, // Length
                      0x00, // FLAGS
                      0x00, // Reserved
               colorModel, // COLOR MODEL
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
                      bits,
                      bits,
                      bits,
            };
            ideStructParamData[1] = (byte)(ideStructParamData.length - 2);
            return ideStructParamData;
        }
        return new byte[0];
    }
}
