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

package org.apache.fop.render.pcl.fonts.truetype;

import java.io.IOException;

import org.apache.fop.fonts.truetype.FontFileReader;

public class PCLTTFOS2FontTable extends PCLTTFTable {
    private int avgCharWidth;
    private int xHeight;
    private int widthClass;
    private int weightClass;
    private int capHeight;
    private int[] panose = new int[10];

    public PCLTTFOS2FontTable(FontFileReader in) throws IOException {
        super(in);
        int version = reader.readTTFUShort(); // Version
        avgCharWidth = reader.readTTFShort();
        weightClass = reader.readTTFShort();
        widthClass = reader.readTTFShort();
        skipShort(reader, 12);
        for (int i = 0; i < 10; i++) {
            panose[i] = reader.readTTFByte();
        }
        skipLong(reader, 4);
        skipByte(reader, 4);
        skipShort(reader, 8);
        if (version >= 2) {
            skipLong(reader, 2);
            xHeight = reader.readTTFShort();
            capHeight = reader.readTTFShort();
        }
    }

    public int getAvgCharWidth() {
        return avgCharWidth;
    }

    public int getXHeight() {
        return xHeight;
    }

    public int getWidthClass() {
        return widthClass;
    }

    public int getWeightClass() {
        return weightClass;
    }

    public int getCapHeight() {
        return capHeight;
    }

    public int[] getPanose() {
        return panose;
    }
}
