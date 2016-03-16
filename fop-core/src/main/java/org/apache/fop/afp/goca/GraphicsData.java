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

import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.util.BinaryUtils;
import org.apache.fop.afp.util.StringUtils;

/**
 * A GOCA graphics data
 */
public final class GraphicsData extends AbstractGraphicsDrawingOrderContainer {

    /** the maximum graphics data length */
    public static final int MAX_DATA_LEN = GraphicsChainedSegment.MAX_DATA_LEN + 16;
    //+16 to avoid unnecessary, practically empty GraphicsData instances.

    /** the graphics segment */
    private GraphicsChainedSegment currentSegment;

    private boolean segmentedData;

    /**
     * Main constructor
     */
    public GraphicsData() {
    }

    /** {@inheritDoc} */
    @Override
    public int getDataLength() {
        return 8 + super.getDataLength();
    }

    /**
     * Sets the indicator that this instance is a part of a series of segmented data chunks.
     * This indirectly sets the SegFlag on the SFI header.
     * @param segmented true if this data object is not the last of the series
     */
    public void setSegmentedData(boolean segmented) {
        this.segmentedData = segmented;
    }

    /**
     * Returns a new segment name
     *
     * @return a new segment name
     */
    public String createSegmentName() {
        return StringUtils.lpad(String.valueOf(
                (super.objects != null ? super.objects.size() : 0) + 1),
            '0', 4);
    }

    /**
     * Creates a new graphics segment.
     *
     * @return a newly created graphics segment
     */
    public GraphicsChainedSegment newSegment() {
        return newSegment(false, false);
    }

    /**
     * Creates a new graphics segment.
     * @param appended true if this segment is appended to the previous one
     * @param prologPresent true if started with a prolog
     * @return a newly created graphics segment
     */
    public GraphicsChainedSegment newSegment(boolean appended, boolean prologPresent) {
        String segmentName = createSegmentName();
        if (currentSegment == null) {
            currentSegment = new GraphicsChainedSegment(segmentName);
        } else {
            currentSegment.setComplete(true);
            currentSegment = new GraphicsChainedSegment(segmentName,
                    currentSegment.getNameBytes(), appended, prologPresent);
        }
        super.addObject(currentSegment);
        return currentSegment;
    }

    /** {@inheritDoc} */
    @Override
    public void addObject(StructuredData object) {
        if (currentSegment == null
                || (currentSegment.getDataLength() + object.getDataLength())
                >= GraphicsChainedSegment.MAX_DATA_LEN) {
            newSegment(true, false);
        }
        currentSegment.addObject(object);
    }

    /**
     * Removes the current segment from this graphics data
     *
     * @return the current segment from this graphics data
     */
    public StructuredData removeCurrentSegment() {
        this.currentSegment = null;
        return super.removeLast();
    }

    /** {@inheritDoc} */
    @Override
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[9];
        copySF(data, SF_CLASS, Type.DATA, Category.GRAPHICS);
        int dataLength = getDataLength();
        byte[] len = BinaryUtils.convert(dataLength, 2);
        data[1] = len[0]; // Length byte 1
        data[2] = len[1]; // Length byte 2
        if (this.segmentedData) {
            data[6] |= 32; //Data is segmented
        }
        os.write(data);

        writeObjects(objects, os);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "GraphicsData(len: " + getDataLength() + ")";
    }

    /**
     * Adds the given segment to this graphics data
     *
     * @param segment a graphics chained segment
     */
    public void addSegment(GraphicsChainedSegment segment) {
        currentSegment = segment;
        super.addObject(currentSegment);
    }
}
