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

package org.apache.fop.afp.util;

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
 * <br>
 * MO:DCA structured fields consist of two parts: an introducer that identifies
 * the length and type of the structured field, and data that provides the
 * structured field's effect. The data is contained in a set of parameters,
 * which can consist of other data structures and data elements. The maximum
 * length of a structured field is 32767 bytes.
 */
public class StructuredFieldReader {

    /**
     * The input stream to read
     */
    private InputStream inputStream;

    /**
     * The constructor for the StructuredFieldReader
     * @param inputStream the input stream to process
     */
    public StructuredFieldReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Get the next structured field as identified by the identifier
     * parameter (this must be a valid MO:DCA structured field).
     * Note: The returned data does not include the field length and identifier!
     * @param identifier the three byte identifier
     * @throws IOException if an I/O exception occurred
     * @return the next structured field or null when there are no more
     */
    public byte[] getNext(byte[] identifier) throws IOException {

        byte[] bytes = AFPResourceUtil.getNext(identifier, this.inputStream);

        if (bytes != null) {
            //Users of this class expect the field data without length and identifier
            int srcPos = 2 + identifier.length;
            byte[] tmp = new byte[bytes.length - srcPos];
            System.arraycopy(bytes, srcPos, tmp, 0, tmp.length);
            bytes = tmp;
        }

        return bytes;

    }

}
