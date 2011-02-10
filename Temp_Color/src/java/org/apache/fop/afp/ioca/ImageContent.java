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

import org.apache.fop.afp.modca.AbstractStructuredObject;

/**
 * An IOCA Image Content
 */
public class ImageContent extends AbstractStructuredObject {

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

    /** the image size parameter */
    private ImageSizeParameter imageSizeParameter = null;

    /** the IDE Structure parameter */
    private IDEStructureParameter ideStructureParameter = null;

    /** the image encoding */
    private byte encoding = (byte)0x03;

    /** the image IDE (Image Data Element, Sample) size */
    private byte ideSize = 1;

    /** the image compression */
    private byte compression = (byte)0xC0;

    /** the image data */
    private byte[] data;

    /**
     * Main Constructor
     */
    public ImageContent() {
    }

    /**
     * Sets the image size parameter
     *
     * @param imageSizeParameter the image size parameter.
     */
    public void setImageSizeParameter(ImageSizeParameter imageSizeParameter) {
        this.imageSizeParameter = imageSizeParameter;
    }

    /**
     * Sets the IDE Structure parameter.
     * @param parameter the IDE Structure parameter
     */
    public void setIDEStructureParameter(IDEStructureParameter parameter) {
        this.ideStructureParameter = parameter;
    }

    /**
     * Returns the (optional) IDE Structure parameter
     * @return the IDE Structure parameter or null if none is set
     */
    public IDEStructureParameter getIDEStructureParameter() {
        return this.ideStructureParameter;
    }

    /**
     * Returns the (optional) IDE Structure parameter. If none is set an instance is prepared
     * with defaults for a bi-level image.
     * @return the IDE Structure parameter
     */
    public IDEStructureParameter needIDEStructureParameter() {
        if (this.ideStructureParameter == null) {
            setIDEStructureParameter(new IDEStructureParameter());
        }
        return getIDEStructureParameter();
    }

    /**
     * Sets the image encoding.
     *
     * @param enc The image encoding.
     */
    public void setImageEncoding(byte enc) {
        this.encoding = enc;
    }

    /**
     * Sets the image compression.
     *
     * @param comp The image compression.
     */
    public void setImageCompression(byte comp) {
        this.compression = comp;
    }

    /**
     * Sets the image IDE size.
     *
     * @param s The IDE size.
     */
    public void setImageIDESize(byte s) {
        this.ideSize = s;
    }

    /**
     * Sets the image IDE color model.
     *
     * @param color    the IDE color model.
     * @deprecated use {@link #setIDEStructureParameter(IDEStructureParameter)} instead
     */
    public void setImageIDEColorModel(byte color) {
        needIDEStructureParameter().setColorModel(color);
    }

    /**
     * Set either additive or subtractive mode (used for ASFLAG).
     * @param subtractive true for subtractive mode, false for additive mode
     * @deprecated use {@link #setIDEStructureParameter(IDEStructureParameter)} instead
     */
    public void setSubtractive(boolean subtractive) {
        needIDEStructureParameter().setSubtractive(subtractive);
    }

    /**
     * Set the image data (can be byte array or inputstream)
     *
     * @param imageData the image data
     */
    public void setImageData(byte[] imageData) {
        this.data = imageData;
    }

    private static final int MAX_DATA_LEN = 65535;

    /** {@inheritDoc} */
    protected void writeContent(OutputStream os) throws IOException {
        if (imageSizeParameter != null) {
            imageSizeParameter.writeToStream(os);
        }

        // TODO convert to triplet/parameter class
        os.write(getImageEncodingParameter());

        os.write(getImageIDESizeParameter());

        if (getIDEStructureParameter() != null) {
            getIDEStructureParameter().writeToStream(os);
        }

        boolean useFS10 = (this.ideSize == 1);
        if (!useFS10) {
            os.write(getExternalAlgorithmParameter());
        }

        final byte[] dataHeader = new byte[] {
                (byte)0xFE, // ID
                (byte)0x92, // ID
                0x00, // length
                0x00  // length
            };
        final int lengthOffset = 2;

        // Image Data
        if (data != null) {
            writeChunksToStream(data, dataHeader, lengthOffset, MAX_DATA_LEN, os);
        }
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        final byte[] startData = new byte[] {
            (byte)0x91, // ID
            0x01, // Length
            (byte)0xff, // Object Type = IOCA Image Object
        };
        os.write(startData);
    }

    /** {@inheritDoc} */
    protected void writeEnd(OutputStream os) throws IOException {
        final byte[] endData = new byte[] {
            (byte)0x93, // ID
            0x00, // Length
        };
        os.write(endData);
    }

    /**
     * Helper method to return the image encoding parameter.
     *
     * @return byte[] The data stream.
     */
    private byte[] getImageEncodingParameter() {
        final byte[] encodingData = new byte[] {
            (byte)0x95, // ID
            0x02, // Length
            encoding,
            0x01, // RECID
        };
        return encodingData;
    }

    /**
     * Helper method to return the external algorithm parameter.
     *
     * @return byte[] The data stream.
     */
    private byte[] getExternalAlgorithmParameter() {
        if (encoding == (byte)0x83 && compression != 0) {
            final byte[] extAlgData = new byte[] {
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
            extAlgData[1] = (byte)(extAlgData.length - 2);
            return extAlgData;
        }
        return new byte[0];
    }

    /**
     * Helper method to return the image encoding parameter.
     *
     * @return byte[] The data stream.
     */
    private byte[] getImageIDESizeParameter() {
        if (ideSize != 1) {
            final byte[] ideSizeData = new byte[] {
                    (byte)0x96, // ID
                    0x01, // Length
                    ideSize};
            return ideSizeData;
        } else {
            return new byte[0];
        }
    }

}
