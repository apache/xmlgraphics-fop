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

import java.io.UnsupportedEncodingException;

import org.apache.fop.afp.AFPConstants;

public abstract class AbstractGraphicsString extends AbstractGraphicsCoord {

    /** Up to 255 bytes of character data */
    protected static final int MAX_STR_LEN = 255;

    /** the string to draw */
    protected final String str;

    /**
     * Constructor (relative)
     *
     * @param str the text string
     */
    public AbstractGraphicsString(String str) {
        super(null);
        if (str.length() > MAX_STR_LEN) {
            str = str.substring(0, MAX_STR_LEN);
            log.warn("truncated character string, longer than " + MAX_STR_LEN + " chars");
        }
        this.str = str;
    }

    /**
     * Constructor (absolute)
     *
     * @param str the text string
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public AbstractGraphicsString(String str, int x, int y) {
        super(x, y);
        this.str = str;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2 + str.length();
    }

    /**
     * Returns the text string as an encoded byte array
     *
     * @return the text string as an encoded byte array
     */
    protected byte[] getStringAsBytes() throws UnsupportedEncodingException {
        return str.getBytes(AFPConstants.EBCIDIC_ENCODING);
    }

}
