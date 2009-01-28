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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Map Page Segment structured field identifies page segments that are required to present
 * a page on a physical medium.
 */
public class MapPageSegment extends AbstractAFPObject {

    private static final int MAX_SIZE = 127;

    /**
     * The collection of page segments (maximum of 127 stored as String)
     */
    private Set pageSegments = null;

    /**
     * Constructor for the Map Page Overlay
     */
    public MapPageSegment() {
    }

    private Set getPageSegments() {
        if (pageSegments == null) {
            this.pageSegments = new java.util.HashSet();
        }
        return this.pageSegments;
    }

    /**
     * Add a page segment to to the map page segment object.
     * @param name the name of the page segment.
     * @throws MaximumSizeExceededException if the maximum size is reached
     */
    public void addPageSegment(String name) throws MaximumSizeExceededException {
        if (getPageSegments().size() > MAX_SIZE) {
            throw new MaximumSizeExceededException();
        }
        if (name.length() > 8) {
            throw new IllegalArgumentException("The name of page segment " + name
                + " must not be longer than 8 characters");
        }
        if (log.isDebugEnabled()) {
            log.debug("addPageSegment():: adding page segment " + name);
        }
        getPageSegments().add(name);
    }

    /**
     * Indicates whether this object already contains the maximum number of
     * page segments.
     * @return true if the object is full
     */
    public boolean isFull() {
        return this.pageSegments.size() >= MAX_SIZE;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        int count = getPageSegments().size();
        byte groupLength = 0x0C;
        int groupsLength = count * groupLength;

        byte[] data = new byte[groupsLength + 12 + 1];

        data[0] = 0x5A;

        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(data.length - 1, 2); //Ignore the
        // first byte in
        // the length
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a MPS
        data[3] = (byte) 0xD3;
        data[4] = Type.MIGRATION;
        data[5] = Category.PAGE_SEGMENT;

        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        data[9] = groupLength;
        data[10] = 0x00; // Reserved
        data[11] = 0x00; // Reserved
        data[12] = 0x00; // Reserved

        int pos = 13;

        Iterator iter = this.pageSegments.iterator();
        while (iter.hasNext()) {
            pos += 4;

            String name = (String)iter.next();
            try {
                byte[] nameBytes = name.getBytes(AFPConstants.EBCIDIC_ENCODING);
                System.arraycopy(nameBytes, 0, data, pos, nameBytes.length);
            } catch (UnsupportedEncodingException usee) {
                log.error("UnsupportedEncodingException translating the name "
                    + name);
            }
            pos += 8;
        }
        os.write(data);
    }
}