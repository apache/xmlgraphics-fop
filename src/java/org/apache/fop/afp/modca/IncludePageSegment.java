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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.afp.util.BinaryUtils;

/**
 * The Include Page Segment structured field references a page segment resource
 * object that is to be presented on the page or overlay presentation space. The IPS
 * specifies a reference point on the including page or overlay coordinate system that
 * may be used to position objects contained in the page segment. A page segment
 * can be referenced at any time during page or overlay state, but not during an
 * object state. The page segment inherits the active environment group definition of
 * the including page or overlay.
 *
 * Note : No use for Triplets.
 *
 * A 'real' example for where this will be used is for
 * the dynamic placing of overlay objects, such as signatures
 * that may have to be placed at different positions on a document.
 *
 */
public class IncludePageSegment extends AbstractNamedAFPObject {

    /**
     * The x position where we need to put this object on the page
     */
    private int x;
    
    /**
     * The y position where we need to put this object on the page
     */
    private int y;
    
    /**
     * Constructor for the Include Page Segment
     * 
     * @param name Name of the page segment
     * @param x The x position
     * @param y The y position
     */
    public IncludePageSegment(String name, int x, int y) {
        super(name);
        
        this.x = x;
        this.y = y;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        byte[] data = new byte[23]; //(9 +14)
        copySF(data, Type.INCLUDE, Category.PAGE_SEGMENT);

        // Set the total record length
        byte[] len = BinaryUtils.convert(22, 2); //Ignore first byte
        data[1] = len[0];
        data[2] = len[1];

        byte[] xPos = BinaryUtils.convert(x, 3);
        data[17] = xPos[0]; // x coordinate
        data[18] = xPos[1];
        data[19] = xPos[2];

        byte[] yPos = BinaryUtils.convert(y, 3);
        data[20] = yPos[0]; // y coordinate
        data[21] = yPos[1];
        data[22] = yPos[2];

        os.write(data);
    }
}
