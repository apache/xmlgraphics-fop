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
 */
public class ImageContent extends AbstractAFPObject {

    /**
     * The image size parameter
     */
    private ImageSizeParameter _imageSizeParameter = null;

    /**
     * The image encoding
     */
    private byte _encoding = 0x03;

    /**
     * The image ide size
     */
    private byte _size = 1;

    /**
     * The image compression
     */
    private byte _compression = (byte)0xC0;

    /**
     * The image color model
     */
    private byte _colorModel = 0x01;

    /**
     * The image data
     */
    private byte _data[] = null;

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
        _imageSizeParameter = new ImageSizeParameter(hresol, vresol, hsize, vsize);
    }

    /**
     * Sets the image encoding.
     * @param encoding The image encoding.
     */
    public void setImageEncoding(byte encoding) {
        _encoding = encoding;
    }

    /**
     * Sets the image compression.
     * @param compression The image compression.
     */
    public void setImageCompression(byte compression) {
        _compression = compression;
    }

    /**
     * Sets the image IDE size.
     * @param size The IDE size.
     */
    public void setImageIDESize(byte size) {
        _size = size;
    }

    /**
     * Sets the image IDE color model.
     * @param size The IDE color model.
     */
    public void setImageIDEColorModel(byte colorModel) {
        _colorModel = colorModel;
    }

    /**
     * Set the data of the image.
     */
    public void setImageData(byte data[]) {
        _data = data;
    }

    /**
     * Accessor method to write the AFP datastream for the Image Content
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        writeStart(os);

        if (_imageSizeParameter != null) {
            _imageSizeParameter.writeDataStream(os);
        }

        os.write(getImageEncodingParameter());

        os.write(getImageIDESizeParameter());

        os.write(getIDEStructureParameter());

        os.write(getExternalAlgorithmParameter());

        if (_data != null) {
            int off = 0;
            while (off < _data.length) {
                int len = Math.min(30000, _data.length - off);
                os.write(getImageDataStart(len));
                os.write(_data, off, len);
                off += len;
            }
        }

        writeEnd(os);

    }

    /**
     * Helper method to write the start of the Image Content.
     * @param os The stream to write to
     */
    private void writeStart(OutputStream os)
        throws IOException {

        byte[] data = new byte[] {
            (byte)0x91, // ID
                  0x01, // Length
            (byte)0xff, // Object Type = IOCA Image Object
        };

        os.write(data);

    }

    /**
     * Helper method to write the end of the Image Content.
     * @param os The stream to write to
     */
    private void writeEnd(OutputStream os)
        throws IOException {

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

        byte[] data = new byte[] {
            (byte)0xFE, // ID
            (byte)0x92, // ID
                  0x00, // Length
                  0x00, // Length
        };

        byte[] l = BinaryUtils.convert(len, 2);
        data[2] = l[0];
        data[3] = l[1];


        return data;

    }

    /**
     * Helper method to return the image encoding parameter.
     * @return byte[] The data stream.
     */
    private byte[] getImageEncodingParameter() {

        byte[] data = new byte[] {
            (byte)0x95, // ID
                  0x02, // Length
                  _encoding,
                  0x01, // RECID
        };

        return data;

    }

    /**
     * Helper method to return the external algorithm parameter.
     * @return byte[] The data stream.
     */
    private byte[] getExternalAlgorithmParameter() {

        if (_encoding == (byte)0x83 && _compression != 0) {
            byte[] data = new byte[] {
                (byte)0x95, // ID
                      0x00, // Length
                      0x10, // ALGTYPE = Compression Algorithm
                      0x00, // Reserved
                (byte)0x83, // COMPRID = JPEG
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
              _compression, // MARKER
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
            };
            data[1] = (byte)(data.length - 2);
            return data;
        }
        return new byte[0];
    }

    /**
     * Helper method to return the image encoding parameter.
     * @return byte[] The data stream.
     */
    private byte[] getImageIDESizeParameter() {

        byte[] data = new byte[] {
            (byte)0x96, // ID
                  0x01, // Length
                  _size,
        };

        return data;

    }

    /**
     * Helper method to return the external algorithm parameter.
     * @return byte[] The data stream.
     */
    private byte[] getIDEStructureParameter() {

        if (_colorModel != 0 && _size == 24) {
            byte bits = (byte)(_size / 3);
            byte[] data = new byte[] {
                (byte)0x9B, // ID
                      0x00, // Length
                      0x00, // FLAGS
                      0x00, // Reserved
               _colorModel, // COLOR MODEL
                      0x00, // Reserved
                      0x00, // Reserved
                      0x00, // Reserved
                      bits,
                      bits,
                      bits,
            };
            data[1] = (byte)(data.length - 2);
            return data;
        }
        return new byte[0];
    }

}
