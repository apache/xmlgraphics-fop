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

/* $Id: $ */

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.modca.StructuredDataObject;
import org.apache.fop.afp.util.BinaryUtils;
import org.apache.fop.afp.util.StringUtils;

/**
 * A GOCA graphics data
 */
public final class GraphicsData extends AbstractGraphicsObjectContainer {

    /** The maximum graphics data length */
    public static final int MAX_DATA_LEN = 32767;

    /** The graphics segment */
    private GraphicsChainedSegment segment = null;

    /** {@inheritDoc} */
    public int getDataLength() {
        return 8 + super.getDataLength();
    }

    /**
     * Begins a graphics area (start of fill)
     */
    public void beginArea() {
        getSegment().beginArea();
    }

    /**
     * Ends a graphics area (end of fill)
     */
    public void endArea() {
        getSegment().endArea();
    }

    /**
     * Returns a new segment name
     *
     * @return a new segment name
     */
    private String createSegmentName() {
        return StringUtils.lpad(String.valueOf(
                (super.objects != null ? super.objects.size() : 0) + 1),
            '0', 4);
    }

    /**
     * Returns the current graphics segment, creating one if one does not exist
     *
     * @return the current graphics chained segment
     */
    private GraphicsChainedSegment getSegment() {
        if (segment == null) {
            newSegment();
        }
        return this.segment;
    }

    /**
     * Creates a new graphics segment
     *
     * @return a newly created graphics segment
     */
    public GraphicsChainedSegment newSegment() {
        String name = createSegmentName();
        if (segment == null) {
            this.segment = new GraphicsChainedSegment(name);
        } else {
            this.segment = new GraphicsChainedSegment(name, segment);
        }
        super.addObject(segment);
        return segment;
    }

    /** {@inheritDoc} */
    public void addObject(StructuredDataObject drawingOrder) {
        if (segment == null
            || (segment.getDataLength() + drawingOrder.getDataLength())
            >= GraphicsChainedSegment.MAX_DATA_LEN) {
            newSegment();
        }
        segment.addObject(drawingOrder);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[9];
        copySF(data, SF_CLASS, Type.DATA, Category.GRAPHICS);
        int dataLength = getDataLength();
        byte[] len = BinaryUtils.convert(dataLength, 2);
        data[1] = len[0]; // Length byte 1
        data[2] = len[1]; // Length byte 2
        os.write(data);

        // get first segment in chain and write (including all its connected segments)
        GraphicsChainedSegment firstSegment = (GraphicsChainedSegment)objects.get(0);
        firstSegment.writeToStream(os);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsData";
    }
}