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

import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * The Invoke Medium Map structured field identifies the Medium Map that is to
 * become active for the document. An Invoke Medium Map structured field affects
 * the document's current environment. The Medium Map's effect on current environment
 * parameter values lasts until a new Medium Map is invoked.
 */
public class InvokeMediumMap extends AbstractNamedAFPObject {

    /**
     * Constructor for the Invoke Medium Map
     * @param mediumMapName Name of the medium map
     */
    public InvokeMediumMap(String mediumMapName) {

        super(mediumMapName);

    }

    /**
     * Accessor method to write the AFP datastream for the Invoke Medium Map
     * @param os The stream to write to
     * @throws java.io.IOException if an I/O exception of some sort has occurred
     */
    public void writeDataStream(OutputStream os)
        throws IOException {

        byte[] data = new byte[17];

        data[0] = 0x5A;

        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(16, 2); //Ignore first byte
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a IPO
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xAB;
        data[5] = (byte) 0xCC;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < nameBytes.length; i++) {

            data[9 + i] = nameBytes[i];

        }

        os.write(data);

    }

}