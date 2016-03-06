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

public class PCLTTFPCLTFontTable extends PCLTTFTable {
    private long version;
    private long fontNumber;
    private int pitch;
    private int xHeight;
    private int style;
    private int typeFamily;
    private int capHeight;
    private int symbolSet;
    private String typeface;
    private String characterComplement;
    private String filename;
    private int strokeWeight;
    private int widthType;
    private int serifStyle;

    public PCLTTFPCLTFontTable(FontFileReader in) throws IOException {
        super(in);
        version = reader.readTTFULong();
        fontNumber = reader.readTTFULong();
        pitch = reader.readTTFUShort();
        xHeight = reader.readTTFUShort();
        style = reader.readTTFUShort();
        typeFamily = reader.readTTFUShort();
        capHeight = reader.readTTFUShort();
        symbolSet = reader.readTTFUShort();
        typeface = reader.readTTFString(16);
        characterComplement = reader.readTTFString(8);
        filename = reader.readTTFString(6);
        strokeWeight = reader.readTTFUShort();
        widthType = reader.readTTFUShort();
        serifStyle = reader.readTTFUByte();
    }

    public long getVersion() {
        return version;
    }

    public long getFontNumber() {
        return fontNumber;
    }

    public int getPitch() {
        return pitch;
    }

    public int getXHeight() {
        return xHeight;
    }

    public int getStyle() {
        return style;
    }

    public int getTypeFamily() {
        return typeFamily;
    }

    public int getCapHeight() {
        return capHeight;
    }

    public int getSymbolSet() {
        return symbolSet;
    }

    public String getTypeface() {
        return typeface;
    }

    public String getCharacterComplement() {
        return characterComplement;
    }

    public String getFilename() {
        return filename;
    }

    public int getStrokeWeight() {
        return strokeWeight;
    }

    public int getWidthType() {
        return widthType;
    }

    public int getSerifStyle() {
        return serifStyle;
    }
}
