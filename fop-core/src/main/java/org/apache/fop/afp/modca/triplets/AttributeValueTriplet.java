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

package org.apache.fop.afp.modca.triplets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.fop.afp.AFPConstants;

/**
 * The attribute value triplet is used to specify a value for a document
 * attribute.
 */
public class AttributeValueTriplet extends AbstractTriplet {
    private String attVal;

    private int userEncoding = -1; //no encoding by default

    /**
     * Main constructor
     *
     * @param attVal an attribute value
     */
    public AttributeValueTriplet(String attVal) {
        super(ATTRIBUTE_VALUE);
        this.attVal = truncate(attVal, MAX_LENGTH - 4);
    }

    public AttributeValueTriplet(String attVal, int userEncoding) {
        this(attVal);
        this.userEncoding = userEncoding;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = super.getData();
        data[2] = 0x00; // Reserved
        data[3] = 0x00; // Reserved

        // convert name and value to ebcdic
        byte[] tleByteValue = null;
        try {
            if (this.userEncoding != -1) {
                tleByteValue = attVal.getBytes("Cp" + userEncoding);
            } else {
                tleByteValue = attVal.getBytes(AFPConstants.EBCIDIC_ENCODING);
            }
        } catch (UnsupportedEncodingException usee) {
            throw new IllegalArgumentException(attVal + " encoding failed");
        }
        System.arraycopy(tleByteValue, 0, data, 4, tleByteValue.length);
        os.write(data);
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 4 + attVal.length();
    }

    /** {@inheritDoc} */
    public String toString() {
        return attVal;
    }
}
