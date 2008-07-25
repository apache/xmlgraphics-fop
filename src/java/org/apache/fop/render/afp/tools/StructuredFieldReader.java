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

package org.apache.fop.render.afp.tools;

import java.io.IOException;
import java.io.InputStream;

/**
 * A helper class to read structured fields from a MO:DCA document. Each
 * component of a mixed object document is explicitly defined and delimited
 * in the data. This is accomplished through the use of MO:DCA data structures,
 * called structured fields. Structured fields are used to envelop document
 * components and to provide commands and information to applications using
 * the data. Structured fields may contain one or more parameters. Each
 * parameter provides one value from a set of values defined by the architecture.
 * <p/>
 * MO:DCA structured fields consist of two parts: an introducer that identifies
 * the length and type of the structured field, and data that provides the
 * structured field's effect. The data is contained in a set of parameters,
 * which can consist of other data structures and data elements. The maximum
 * length of a structured field is 32767 bytes.
 * <p/>
 */
public class StructuredFieldReader {

    /**
     * The input stream to read
     */
    private InputStream inputStream = null;

    /**
     * The constructor for the StructuredFieldReader
     * @param inputStream the input stream to process
     */
    public StructuredFieldReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Get the next structured field as identified by the identifer
     * parameter (this must be a valid MO:DCA structured field.
     * @param identifier the three byte identifier
     * @throws IOException if an I/O exception occurred
     * @return the next structured field
     */
    public byte[] getNext(byte[] identifier) throws IOException {

        int bufferPointer = 0;
        byte[] bufferData = new byte[identifier.length + 2];
        for (int x = 0; x < identifier.length; x++) {
            bufferData[x] = (byte) 0;
        }

        int c;
        while ((c = inputStream.read()) > -1) {

            bufferData[bufferPointer] = (byte) c;

            // Check the last characters in the buffer
            int index = 0;
            boolean found = true;

            for (int i = identifier.length - 1; i > -1; i--) {

                int p = bufferPointer - index;
                if (p < 0) {
                    p = bufferData.length + p;
                }

                index++;

                if (identifier[i] != bufferData[p]) {
                    found = false;
                    break;
                }

            }

            if (found) {

                byte[] length = new byte[2];

                int a = bufferPointer - identifier.length;
                if (a < 0) {
                    a = bufferData.length + a;
                }

                int b = bufferPointer - identifier.length - 1;
                if (b < 0) {
                    b = bufferData.length + b;
                }

                length[0] = bufferData[b];
                length[1] = bufferData[a];

                int reclength = ((length[0] & 0xFF) << 8)
                                + (length[1] & 0xFF) - identifier.length - 2;

                byte[] retval = new byte[reclength];

                inputStream.read(retval, 0, reclength);

                return retval;

            }

            bufferPointer++;
            if (bufferPointer >= bufferData.length) {
                bufferPointer = 0;
            }

        }

        return new byte[] {
        };
    }
}
