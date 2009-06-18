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
import java.io.UnsupportedEncodingException;

/**
 * The End Named Page Group (ENG) structured field terminates a page group that was
 * initiated by a Begin Named Page Group structured field.
 *
 * Note :This object will be used to represent an ENG
 * structured field. It is necessary as you can't end
 * a PageGroup because you don't know where the group
 * will end (as this is controlled by the tags in the FO).
 * <p>
 *
 */
public class EndPageGroup extends AbstractNamedAFPObject {

    public EndPageGroup(String groupId) {

        super(groupId);

        log.debug("A ENG is being created for group: " + groupId);

    }

    /**
     * Accessor method to write the AFP datastream for the End Page Group.
     * @param os The stream to write to
     * @throws java.io.IOException
     */
    public void writeDataStream(OutputStream os)
        throws IOException {
        byte[] data = new byte[17];

        data[0] = 0x5A; // Structured field identifier
        data[1] = 0x00; // Length byte 1
        data[2] = 0x10; // Length byte 2
        data[3] = (byte) 0xD3; // Structured field id byte 1
        data[4] = (byte) 0xA9; // Structured field id byte 2
        data[5] = (byte) 0xAD; // Structured field id byte 3
        data[6] = 0x00; // Flags
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        for (int i = 0; i < _nameBytes.length; i++) {

            data[9 + i] = _nameBytes[i];

        }

        os.write(data);
    }

}
