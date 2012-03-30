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

package org.apache.fop.fonts.truetype;


/**
 * This class holds the True Type Format table names as in the Directory Table of a TTF font file.
 * This class must also support custom tables found in fonts (thus an enum wasn't used).
 */
public final class TTFTableName {
    /** The first table in a True Type font file containing metadata about other tables. */
    public static final TTFTableName DIRECTORY_TABLE = new TTFTableName("dirTable");

    /** Embedded bitmap data */
    public static final TTFTableName EBDT = new TTFTableName("EBDT");

    /** Embedded bitmap location data */
    public static final TTFTableName EBLC = new TTFTableName("EBLC");

    /** Embedded bitmap scaling data */
    public static final TTFTableName EBSC = new TTFTableName("EBSC");

    /** A font forge specific table */
    public static final TTFTableName FFTM = new TTFTableName("FFTM");

    /** Divides glyphs into various classes that make using the GPOS/GSUB tables easier. */
    public static final TTFTableName GDEF = new TTFTableName("GDEF");

    /** Provides kerning information, mark-to-base, etc. for opentype fonts */
    public static final TTFTableName GPOS = new TTFTableName("GPOS");

    /** Provides ligature information, swash, etc. for opentype fonts */
    public static final TTFTableName GSUB = new TTFTableName("GSUB");

    /** Linear threshold table */
    public static final TTFTableName LTSH = new TTFTableName("LTSH");

    /** OS/2 and Windows specific metrics */
    public static final TTFTableName OS2 = new TTFTableName("OS/2");

    /** PCL 5 data*/
    public static final TTFTableName PCLT = new TTFTableName("PCLT");

    /** Vertical Device Metrics table */
    public static final TTFTableName VDMX = new TTFTableName("VDMX");

    /** character to glyph mapping */
    public static final TTFTableName CMAP = new TTFTableName("cmap");

    /** Control Value Table */
    public static final TTFTableName CVT = new TTFTableName("cvt ");

    /** font program */
    public static final TTFTableName FPGM = new TTFTableName("fpgm");

    /** grid-fitting and scan conversion procedure (grayscale) */
    public static final TTFTableName GASP = new TTFTableName("gasp");

    /** glyph data */
    public static final TTFTableName GLYF = new TTFTableName("glyf");

    /** horizontal device metrics */
    public static final TTFTableName HDMX = new TTFTableName("hdmx");

    /** font header */
    public static final TTFTableName HEAD = new TTFTableName("head");

    /** horizontal header */
    public static final TTFTableName HHEA = new TTFTableName("hhea");

    /** horizontal metrics */
    public static final TTFTableName HMTX = new TTFTableName("hmtx");

    /** kerning */
    public static final TTFTableName KERN = new TTFTableName("kern");

    /** index to location */
    public static final TTFTableName LOCA = new TTFTableName("loca");

    /** maximum profile */
    public static final TTFTableName MAXP = new TTFTableName("maxp");

    /** naming table */
    public static final TTFTableName NAME = new TTFTableName("name");

    /** PostScript information */
    public static final TTFTableName POST = new TTFTableName("post");

    /** CVT Program */
    public static final TTFTableName PREP = new TTFTableName("prep");

    /** Vertical Metrics header */
    public static final TTFTableName VHEA = new TTFTableName("vhea");

    /** Vertical Metrics */
    public static final TTFTableName VMTX = new TTFTableName("vmtx");

    private final String name;

    private TTFTableName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the table as it should be in the Table Directory.
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the appropriate TTFTableName object when given the string representation.
     * @param tableName table name as in the Directory Table.
     * @return TTFTableName
     */
    public static TTFTableName getValue(String tableName) {
        if (tableName != null) {
            return new TTFTableName(tableName);
        }
        throw new IllegalArgumentException("A TrueType font table name must not be null");
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TTFTableName)) {
            return false;
        }
        TTFTableName to = (TTFTableName) o;
        return this.name.equals(to.getName());
    }

}
