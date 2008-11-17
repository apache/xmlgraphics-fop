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

package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A GOCA graphics string
 */
public class GraphicsString extends AbstractGraphicsString {

    /**
     * Constructor
     *
     * @param str the character string
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public GraphicsString(String str, int x, int y) {
        super(str, x, y);
    }

    /** {@inheritDoc} */
    byte getOrderCode() {
        return (byte)0xC3;
    }

    /** {@inheritDoc} */
    public int getDataLength() {
        return super.getDataLength() + (coords.length * 2);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = getData();
        byte[] strData = getStringAsBytes();
        System.arraycopy(strData, 0, data, 6, strData.length);

        os.write(data);
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsString{x=" + coords[0] + ", y=" + coords[1] + "str='" + str + "'" + "}";
    }
}