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

package org.apache.fop.render.pcl.fonts;

public class PCLFontSegment {
    private SegmentID identifier;
    private byte[] data;

    public PCLFontSegment(SegmentID identifier, byte[] data) {
        this.identifier = identifier;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public SegmentID getIdentifier() {
        return identifier;
    }

    public int getSize() {
        return (identifier == SegmentID.NULL) ? 0 : data.length;
    }

    public enum SegmentID {
        CC(17219), // Character Complement
        CP(17232), // Copyright
        GT(18260), // Global TrueType Data
        IF(18758), // Intellifont Face Data
        PA(20545), // PANOSE Description
        XW(22619), // XWindows Font Name
        NULL(65535); // Null Segment

        private int complementID;

        SegmentID(int complementID) {
            this.complementID = complementID;
        }

        public int getValue() {
            return complementID;
        }
    }
}
