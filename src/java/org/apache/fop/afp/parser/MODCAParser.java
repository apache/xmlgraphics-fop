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

package org.apache.fop.afp.parser;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * An simple MO:DCA/AFP parser.
 */
public class MODCAParser {

    private DataInputStream din;

    /**
     * Main constructor
     * @param in the {@link InputStream} to read the AFP file from.
     */
    public MODCAParser(InputStream in) {
        if (!in.markSupported()) {
            in = new java.io.BufferedInputStream(in);
        }
        this.din = new DataInputStream(in);
    }

    /**
     * Returns the {@link DataInputStream} used for parsing structured fields.
     * @return the data input stream
     */
    public DataInputStream getDataInputStream() {
        return this.din;
    }

    /**
     * Reads the next structured field from the input stream.
     * <p>
     * No structure validation of the MO:DCA file is performed.
     * @return a new unparsed structured field (or null when parsing is finished).
     * @throws IOException if an I/O error occurs
     */
    public UnparsedStructuredField readNextStructuredField() throws IOException {
        try {
            while (true) {
                byte b = din.readByte(); //Skip 0x5A character if necessary (ex. AFP)
                if (b == 0x0D || b == 0x0A) { //CR and LF may be used as field delimiters
                    continue;
                } else if (b == 0x5A) { //Carriage Control Character
                    break;
                }
            }
        } catch (EOFException eof) {
            return null;
        }
        return UnparsedStructuredField.readStructuredField(getDataInputStream());
    }

}
