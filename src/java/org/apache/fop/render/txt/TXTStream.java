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

package org.apache.fop.render.txt;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Helper class for text streams.
 */
public class TXTStream {

    private static final String DEFAULT_ENCODING = "UTF-8";

    private OutputStream out;
    private boolean doOutput = true;
    private String encoding = DEFAULT_ENCODING;

    /**
     * Main constructor.
     * @param os OutputStream to write to
     */
    public TXTStream(OutputStream os) {
        out = os;
    }

    /**
     * Adds a String to the OutputStream
     * @param str String to add
     */
    public void add(String str) {
        if (!doOutput) {
            return;
        }

        try {
            byte[] buff = str.getBytes(encoding);
            out.write(buff);
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Controls whether output is actually written.
     * @param doout true to enable output, false to suppress
     */
    public void setDoOutput(boolean doout) {
        doOutput = doout;
    }

    /**
     * Set the encoding for the text stream.
     * @param encoding the encoding, if null, "UTF-8" is chosen as default
     */
    public void setEncoding(String encoding) {
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.encoding = DEFAULT_ENCODING;
        }
    }
}

