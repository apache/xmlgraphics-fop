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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.cff.CFFDataInput;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fontbox.cff.charset.CFFCharset;

public class OTFFile extends OpenFont {

    protected CFFFont fileFont;

    public OTFFile() throws IOException {
        checkForFontbox();
    }

    private void checkForFontbox() throws IOException {
        try {
            Class.forName("org.apache.fontbox.cff.CFFFont");
        } catch (ClassNotFoundException ex) {
            throw new IOException("The Fontbox jar was not found in the classpath. This is "
                                   + "required for OTF CFF ssupport.");
        }
    }

    @Override
    protected void updateBBoxAndOffset() throws IOException {
        List<Mapping> gidMappings = getGIDMappings(fileFont);
        Map<Integer, String> sidNames = constructNameMap(gidMappings);
        UnicodeMapping[] mappings = unicodeMappings.toArray(new UnicodeMapping[unicodeMappings.size()]);
        for (int i = 0; i < mappings.length; i++) {
            int glyphIdx = mappings[i].getGlyphIndex();
            Mapping m = gidMappings.get(glyphIdx);
            String name = sidNames.get(m.getSID());
            mtxTab[glyphIdx].setName(name);
        }
    }

    private List<Mapping> getGIDMappings(CFFFont font) {
        List<Mapping> gidMappings = new ArrayList<Mapping>();
        Mapping notdef = new Mapping();
        gidMappings.add(notdef);
        for (CFFCharset.Entry entry : font.getCharset().getEntries()) {
            String name = entry.getName();
            byte[] bytes = font.getCharStringsDict().get(name);
            if (bytes == null) {
                continue;
            }
            Mapping mapping = new Mapping();
            mapping.setSID(entry.getSID());
            mapping.setName(name);
            mapping.setBytes(bytes);
            gidMappings.add(mapping);
        }
        return gidMappings;
    }

    private Map<Integer, String> constructNameMap(Collection<Mapping> mappings) {
        Map<Integer, String> sidNames = new HashMap<Integer, String>();
        Iterator<Mapping> it = mappings.iterator();
        while (it.hasNext()) {
            Mapping mapping = it.next();
            sidNames.put(mapping.getSID(), mapping.getName());
        }
        return sidNames;
    }

    private static class Mapping {
        private int sid;
        private String name;
        private byte[] bytes;

        public void setSID(int sid) {
            this.sid = sid;
        }

        public int getSID() {
            return sid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public byte[] getBytes() {
            return bytes;
        }
    }


    @Override
    protected void initializeFont(FontFileReader in) throws IOException {
        fontFile = in;
        fontFile.seekSet(0);
        CFFParser parser = new CFFParser();
        fileFont = parser.parse(in.getAllBytes()).get(0);
    }

    protected void readName() throws IOException {
        Object familyName = fileFont.getProperty("FamilyName");
        if (familyName != null && !familyName.equals("")) {
            familyNames.add(familyName.toString());
            fullName = familyName.toString();
        } else {
            fullName = fileFont.getName();
            familyNames.add(fullName);
        }
    }

    /**
     * Reads the CFFData from a given font file
     * @param fontFile The font file being read
     * @return The byte data found in the CFF table
     */
    public static byte[] getCFFData(FontFileReader fontFile) throws IOException {
        byte[] cff = fontFile.getAllBytes();
        CFFDataInput input = new CFFDataInput(fontFile.getAllBytes());
        input.readBytes(4); //OTTO
        short numTables = input.readShort();
        input.readShort(); //searchRange
        input.readShort(); //entrySelector
        input.readShort(); //rangeShift

        for (int q = 0; q < numTables; q++) {
            String tagName = new String(input.readBytes(4));
            readLong(input); //Checksum
            long offset = readLong(input);
            long length = readLong(input);
            if (tagName.equals("CFF ")) {
                cff = new byte[(int)length];
                System.arraycopy(fontFile.getAllBytes(), (int)offset, cff, 0, cff.length);
                break;
            }
        }
        return cff;
    }

    private static long readLong(CFFDataInput input) throws IOException {
        return (input.readCard16() << 16) | input.readCard16();
    }
}
