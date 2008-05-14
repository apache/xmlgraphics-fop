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
 * The No Operation structured field may be used to carry comments
 * or any other type of unarchitected data. Although not recommended,
 * it may also be used to carry semantic data in private or exchange data
 * streams. However, because receivers of interchange data streams should
 * ignore the content of No Operation structured fields and because
 * receiver-generator products are not required to propagate
 * No Operation structured fields, no semantics should be attached to
 * the data carried by the No Operation structured field in interchange
 */
public class NoOperation extends AbstractAFPObject {
    
    /** Up to 32759 bytes of data with no architectural definition */
    private static final int MAX_DATA_LEN = 32759;
    
    /**
     * Byte representation of the comment 
     */
    private String content;

    /**
     * Construct a tag logical element with the name and value specified.
     * 
     * @param content the content to record
     */
    public NoOperation(String content) {
        this.content = content;
    }
    
    /**
     * Accessor method to obtain the byte array AFP datastream for the
     * NoOperation.
     * 
     * @param os The outputsteam stream
     * @throws java.io.IOException if an I/O exception occurs during processing
     */
    public void write(OutputStream os) throws IOException {
        byte[] contentData = content.getBytes(AFPConstants.EBCIDIC_ENCODING);
        int contentLen = contentData.length;
        
        // packet maximum of 32759 bytes
        if (contentLen > MAX_DATA_LEN) {
            contentLen = MAX_DATA_LEN;
        }
        
        byte[] data = new byte[9 + contentLen];
        
        data[0] = 0x5A;
        
        // Set the total record length
        byte[] rl1 = BinaryUtils.convert(8 + contentLen, 2);
        
        //Ignore first byte
        data[1] = rl1[0];
        data[2] = rl1[1];

        // Structured field ID for a TLE
        data[3] = (byte) 0xD3;
        data[4] = (byte) 0xEE;
        data[5] = (byte) 0xEE;

        data[6] = 0x00; // Reserved
        data[7] = 0x00; // Reserved
        data[8] = 0x00; // Reserved

        int pos = 9;
        for (int i = 0; i < contentLen; i++) {
            data[pos++] = contentData[i];
        }
        os.write(data);
    }

}