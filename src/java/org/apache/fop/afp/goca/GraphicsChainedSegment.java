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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * A GOCA graphics segment
 */
public final class GraphicsChainedSegment extends AbstractGraphicsDrawingOrderContainer {

    /** The maximum segment data length */
    protected static final int MAX_DATA_LEN = 8192;

    private byte[] predecessorNameBytes;
    private boolean appended;

    /**
     * Main constructor
     *
     * @param name
     *            the name of this graphics segment
     */
    public GraphicsChainedSegment(String name) {
        this(name, null, false);
    }

    /**
     * Constructor
     *
     * @param name
     *            the name of this graphics segment
     * @param predecessorNameBytes
     *            the name of the predecessor in this chain
     * @param appended true if this segment is appended to the previous one
     */
    public GraphicsChainedSegment(String name, byte[] predecessorNameBytes, boolean appended) {
        super(name);
        if (predecessorNameBytes != null) {
            this.predecessorNameBytes = new byte[predecessorNameBytes.length];
            System.arraycopy(predecessorNameBytes, 0,
                    this.predecessorNameBytes, 0, predecessorNameBytes.length);
        }
        this.appended = appended;
    }

    /** {@inheritDoc} */
    @Override
    public int getDataLength() {
        return 14 + super.getDataLength();
    }

    private static final byte APPEND_NEW_SEGMENT = 0;
//    private static final byte PROLOG = 4;
    private static final byte APPEND_TO_EXISING = 6;

    private static final int NAME_LENGTH = 4;

    /** {@inheritDoc} */
    @Override
    protected int getNameLength() {
        return NAME_LENGTH;
    }

    byte getOrderCode() {
        return 0x70;
    }

    /** {@inheritDoc} */
    @Override
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[14];
        data[0] = getOrderCode(); // BEGIN_SEGMENT
        data[1] = 0x0C; // Length of following parameters

        // segment name
        byte[] nameBytes = getNameBytes();
        System.arraycopy(nameBytes, 0, data, 2, NAME_LENGTH);

        data[6] = 0x00; // FLAG1 (ignored)
        data[7] = this.appended ? APPEND_TO_EXISING : APPEND_NEW_SEGMENT; //FLAG2

        int dataLength = super.getDataLength();
        byte[] len = BinaryUtils.convert(dataLength, 2);
        data[8] = len[0]; // SEGL
        data[9] = len[1];

        // P/S NAME (predecessor name)
        if (predecessorNameBytes != null) {
            System.arraycopy(predecessorNameBytes, 0, data, 10, NAME_LENGTH);
        }
        os.write(data);

        writeObjects(objects, os);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GraphicsChainedSegment(name=" + super.getName() + ", len: " + getDataLength() + ")";
    }
}