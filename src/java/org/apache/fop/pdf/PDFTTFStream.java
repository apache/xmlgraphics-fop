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

package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Special PDFStream for embeddable TrueType fonts.
 */
public class PDFTTFStream extends AbstractPDFFontStream {

    private int origLength;
    private byte[] ttfData;

    /**
     * Main constructor
     * @param len original length
     */
    public PDFTTFStream(int len) {
        super();
        origLength = len;
    }

    /** {@inheritDoc} */
    protected int getSizeHint() throws IOException {
        if (this.ttfData != null) {
            return ttfData.length;
        } else {
            return 0; //no hint available
        }
    }

    /**
     * Overload the base object method so we don't have to copy
     * byte arrays around so much
     * {@inheritDoc}
     */
    public int output(java.io.OutputStream stream)
            throws java.io.IOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing " + origLength + " bytes of TTF font data");
        }

        int length = super.output(stream);
        log.debug("Embedded TrueType/OpenType font");
        return length;
    }

    /** {@inheritDoc} */
    protected void outputRawStreamData(OutputStream out) throws IOException {
        out.write(this.ttfData);
    }

    /** {@inheritDoc} */
    protected void populateStreamDict(Object lengthEntry) {
        put("Length1", origLength);
        super.populateStreamDict(lengthEntry);
    }

    /**
     * Sets the TrueType font data.
     * @param data the font payload
     * @param size size of the payload
     * @throws IOException in case of an I/O problem
     */
    public void setData(byte[] data, int size) throws IOException {
        this.ttfData = new byte[size];
        System.arraycopy(data, 0, this.ttfData, 0, size);
    }

}
