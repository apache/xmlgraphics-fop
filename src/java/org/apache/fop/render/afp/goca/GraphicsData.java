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

package org.apache.fop.render.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.render.afp.modca.AbstractPreparedObjectContainer;
import org.apache.fop.render.afp.modca.PreparedAFPObject;
import org.apache.fop.render.afp.tools.BinaryUtils;
import org.apache.fop.render.afp.tools.StringUtils;

/**
 * A GOCA graphics data
 */
public final class GraphicsData extends AbstractPreparedObjectContainer {

    /** The maximum graphics data length */
    public static final int MAX_DATA_LEN = 32767;

    /** The graphics segment */
    private GraphicsChainedSegment currentSegment = null;

    /** {@inheritDoc} */
    public int getDataLength() {
        return 8 + super.getDataLength();
    }

    /** {@inheritDoc} */
    protected void writeStart(OutputStream os) throws IOException {
        super.writeStart(os);
        int l = getDataLength();
        byte[] len = BinaryUtils.convert(l, 2);
        byte[] data = new byte[] {
            0x5A, // Structured field identifier
            len[0], // Length byte 1
            len[1], // Length byte 2
            (byte) 0xD3, // Structured field id byte 1
            (byte) 0xEE, // Structured field id byte 2
            (byte) 0xBB, // Structured field id byte 3
            0x00, // Flags
            0x00, // Reserved
            0x00  // Reserved
        };
        os.write(data);
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
        if (currentSegment == null) {
            newSegment();
        }
        return this.currentSegment;
    }

    /**
     * Creates a new graphics segment
     *
     * @return a newly created graphics segment
     */
    public GraphicsChainedSegment newSegment() {
        String name = createSegmentName();
        if (currentSegment == null) {
            this.currentSegment = new GraphicsChainedSegment(name);
        } else {
            this.currentSegment = new GraphicsChainedSegment(name, currentSegment);
        }
        super.addObject(currentSegment);
        return currentSegment;
    }

    /** {@inheritDoc} */
    public PreparedAFPObject addObject(PreparedAFPObject drawingOrder) {
        if (currentSegment == null
            || (currentSegment.getDataLength() + drawingOrder.getDataLength())
            >= GraphicsChainedSegment.MAX_DATA_LEN) {
            newSegment();
        }
        currentSegment.addObject(drawingOrder);
        return drawingOrder;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsData";
    }
}