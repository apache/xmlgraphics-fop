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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sets the value of the current line type attribute when stroking GOCA shapes (structured fields)
 */
public class GraphicsSetLineType extends AbstractGraphicsDrawingOrder {

    /** the default line type */
    public static final byte DEFAULT = 0x00; // normally SOLID

    /** the default line type */
    public static final byte DOTTED = 0x01;

    /** short dashed line type */
    public static final byte SHORT_DASHED = 0x02;

    /** dashed dotted line type */
    public static final byte DASH_DOT = 0x03;

    /** double dotted line type */
    public static final byte DOUBLE_DOTTED = 0x04;

    /** long dashed line type */
    public static final byte LONG_DASHED = 0x05;

    /** dash double dotted line type */
    public static final byte DASH_DOUBLE_DOTTED = 0x06;

    /** solid line type */
    public static final byte SOLID = 0x07;

    /** invisible line type */
    public static final byte INVISIBLE = 0x08;

    /** line type */
    private byte type = DEFAULT;

    /**
     * Main constructor
     *
     * @param type line type
     */
    public GraphicsSetLineType(byte type) {
       this.type = type;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return 2;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[] {
            getOrderCode(), // GSLW order code
            type // line type
        };
        os.write(data);
    }

    private static final String[] TYPES = {
        "default (solid)", "dotted", "short dashed", "dash dotted", "double dotted",
        "long dashed", "dash double dotted", "solid", "invisible"
    };

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsSetLineType{type=" + TYPES[type] + "}";
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return 0x18;
    }
}