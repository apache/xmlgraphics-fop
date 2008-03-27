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

package org.apache.fop.render.afp.goca;

import org.apache.fop.render.afp.modca.AbstractPreparedAFPObject;
import org.apache.fop.render.afp.tools.BinaryUtils;

/**
 * A GOCA graphics image data
 */
public final class GraphicsImageData extends AbstractPreparedAFPObject {    
    
    /** the maximum image data length */
    public static final short MAX_DATA_LEN = 255;

    /**
     * Main constructor
     * 
     * @param imageData the image data
     * @param startIndex the start index to read the image data from 
     */
    public GraphicsImageData(byte[] imageData, int startIndex) {
        int dataLen = MAX_DATA_LEN;
        if (startIndex + MAX_DATA_LEN >= imageData.length) {
            dataLen = imageData.length - startIndex - 1;
        }
        super.data = new byte[dataLen + 2];
        data[0] = (byte) 0x92; // GIMD
        data[1] = BinaryUtils.convert(dataLen, 1)[0]; // LENGTH
        System.arraycopy(imageData, startIndex, data, 2, dataLen);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "GraphicsImageData("
            + (data != null ? "" + (data.length - 2) : "null")
            + ")";
    }
}