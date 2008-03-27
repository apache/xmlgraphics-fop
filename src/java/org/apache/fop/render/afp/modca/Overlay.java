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

package org.apache.fop.render.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An overlay is a MO:DCA-P resource object.
 * 
 * It may be stored in an external resource library or it may be
 * carried in a resource group. An overlay is similar to a page in
 * that it defines its own environment and carries the same data objects.
 */
public class Overlay extends AbstractPageObject {

    /**
     * Construct a new overlay object for the specified name argument, the overlay
     * name should be an 8 character identifier.
     *
     * @param name
     *            the name of the page.
     * @param width
     *            the width of the page.
     * @param height
     *            the height of the page.
     * @param rotation
     *            the rotation of the page.
     * @param widthResolution
     *            the width resolution of the page.
     * @param heightResolution
     *            the height resolution of the page.
     */
    public Overlay(String name, int width, int height, int rotation,
            int widthResolution, int heightResolution) {
        super(name, width, height, rotation, widthResolution, heightResolution);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeStart(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA8; // Structured field id byte 2
        data[5] = (byte) 0xDF; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }

    /**
     * {@inheritDoc}
     */
    protected void writeEnd(OutputStream os) throws IOException {
        byte[] data = new byte[17];
        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xDF; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved
        for (int i = 0; i < nameBytes.length; i++) {
            data[9 + i] = nameBytes[i];
        }
        os.write(data);
    }
}
