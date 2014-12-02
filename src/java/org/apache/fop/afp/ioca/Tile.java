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
import org.apache.fop.afp.util.BinaryUtils;

public class Tile extends AbstractStructuredObject {

    private static final int MAX_DATA_LEN = 8191;
    private TilePosition tilePosition = null;
    private TileSize tileSize = null;
    private BandImage bandImage;
    private byte[] data;

    private IDEStructureParameter ideStructureParameter;
    private byte encoding = (byte) 0x03;
    private byte ideSize = 1;
//    private byte compression = (byte) 0xC0; // Baseline DCT in case of JPEG compression

    @Override
    public void writeContent(OutputStream os) throws IOException {
        tilePosition.writeToStream(os);
        tileSize.writeToStream(os);
        os.write(getImageEncodingParameter());
        os.write(getImageIDESizeParameter());
        if (bandImage != null) {
            bandImage.writeToStream(os);
        }
        if (ideStructureParameter != null) {
            ideStructureParameter.writeToStream(os);
        }
        if (data != null) {
            byte[] c = new byte[data.length / 4];
            byte[] m = new byte[data.length / 4];
            byte[] y = new byte[data.length / 4];
            byte[] k = new byte[data.length / 4];
            for (int j = 0; j < data.length / 4; j++) {
                c[j] = data[4 * j];
                m[j] = data[4 * j + 1];
                y[j] = data[4 * j + 2];
                k[j] = data[4 * j + 3];
            }
            final byte[] dataHeader = new byte[] {(byte) 0xFE, // ID
                    (byte) 0x9C, // ID
                    0x00, // length
                    0x00, // length
                    0x00, // bandnum
                    0x00, // reserved
                    0x00 // reserved
            };
            final int lengthOffset = 2;
            dataHeader[4] = (byte) 0x01;
            writeChunksToStream(c, dataHeader, lengthOffset, MAX_DATA_LEN, os);
            dataHeader[4] = (byte) 0x02;
            writeChunksToStream(m, dataHeader, lengthOffset, MAX_DATA_LEN, os);
            dataHeader[4] = (byte) 0x03;
            writeChunksToStream(y, dataHeader, lengthOffset, MAX_DATA_LEN, os);
            dataHeader[4] = (byte) 0x04;
            writeChunksToStream(k, dataHeader, lengthOffset, MAX_DATA_LEN, os);
        }
    }

    @Override
    protected void writeStart(OutputStream os) throws IOException {
        final byte[] startData = new byte[] {(byte) 0x8C, // ID
                0x00 // Length
        };
        os.write(startData);
    }

    @Override
    protected void writeEnd(OutputStream os) throws IOException {
        final byte[] endData = new byte[] {(byte) 0x8D, // ID
                0x00, // Length
        };
        os.write(endData);
    }

    public void setPosition(TilePosition tilePosition) {
        this.tilePosition = tilePosition;
    }

    public void setSize(TileSize tileSize) {
        this.tileSize = tileSize;
    }

    public void setImageData(byte[] imageData) {
        this.data = imageData.clone();
    }

    protected static void writeChunksToStream(byte[] data, byte[] dataHeader, int lengthOffset,
            int maxChunkLength, OutputStream os) throws IOException {
        int dataLength = data.length;
        maxChunkLength -= 3;
        int numFullChunks = dataLength / maxChunkLength;
        int lastChunkLength = dataLength % maxChunkLength;

        byte[] len = {(byte) 0x1f, (byte) 0xff};
        int off = 0;
        if (numFullChunks > 0) {
            // write out full data chunks
            dataHeader[lengthOffset] = len[0]; // Length byte 1
            dataHeader[lengthOffset + 1] = len[1]; // Length byte 2
            for (int i = 0; i < numFullChunks; i++, off += maxChunkLength) {
                os.write(dataHeader);
                os.write(data, off, maxChunkLength);
            }
        }

        if (lastChunkLength > 0) {
            // write last data chunk
            len = BinaryUtils.convert(3 + lastChunkLength, 2);
            dataHeader[lengthOffset] = len[0]; // Length byte 1
            dataHeader[lengthOffset + 1] = len[1]; // Length byte 2
            os.write(dataHeader);
            os.write(data, off, lastChunkLength);
        }
    }

    public void setImageEncodingParameter(byte encoding) {
        this.encoding = encoding;
    }

    public void setImageIDESizeParameter(byte ideSize) {
        this.ideSize = ideSize;
    }

    public void setIDEStructureParameter(IDEStructureParameter ideStructureParameter) {
        this.ideStructureParameter = ideStructureParameter;
    }

    private byte[] getImageEncodingParameter() {
        final byte[] encodingData = new byte[] {(byte) 0x95, // ID
                0x02, // Length
                encoding, (byte) (encoding == ImageContent.COMPID_JPEG ? 0xFE : 0x01), // RECID
        };
        return encodingData;
    }

    private byte[] getImageIDESizeParameter() {
        if (ideSize != 1) {
            final byte[] ideSizeData = new byte[] {(byte) 0x96, // ID
                    0x01, // Length
                    ideSize};
            return ideSizeData;
        } else {
            return new byte[0];
        }
    }

    public void setBandImage(BandImage bandImage) {
        this.bandImage = bandImage;
    }

}
