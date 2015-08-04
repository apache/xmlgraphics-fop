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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PCLCharacterDefinition {
    private int charCode;
    private int charDefinitionSize;
    private byte[] glyfData;
    private boolean hasContinuation;
    private PCLCharacterFormat charFormat;
    private PCLCharacterClass charClass;
    private PCLByteWriterUtil pclByteWriter;
    private List<PCLCharacterDefinition> composites;
    private boolean isComposite;

    public PCLCharacterDefinition(int charCode, PCLCharacterFormat charFormat,
            PCLCharacterClass charClass, byte[] glyfData, PCLByteWriterUtil pclByteWriter,
            boolean isComposite) {
        this.charCode = charCode;
        this.charFormat = charFormat;
        this.charClass = charClass;
        this.glyfData = glyfData;
        this.pclByteWriter = pclByteWriter;
        this.isComposite = isComposite;
        // Glyph Data + (Descriptor Size) + (Character Data Size) + (Glyph ID) must
        // be less than 32767 otherwise it will result in a continuation structure.
        charDefinitionSize = glyfData.length + 4 + 2 + 2;
        hasContinuation = charDefinitionSize > 32767;
        composites = new ArrayList<PCLCharacterDefinition>();
    }

    public byte[] getCharacterCommand() throws IOException {
        return pclByteWriter.writeCommand(String.format("*c%dE", (isComposite) ? 65535 : charCode));
    }

    public byte[] getCharacterDefinitionCommand() throws IOException {
        return pclByteWriter.writeCommand(String.format("(s%dW", 10 + glyfData.length));
    }

    public byte[] getData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Write Character Descriptor
        if (!hasContinuation) {
            writeCharacterDescriptorHeader(0, baos);
            baos.write(glyfData);
        } else {
            int continuations = glyfData.length / 32767;
            for (int i = 0; i < continuations; i++) {
                writeCharacterDescriptorHeader(i == 0 ? 0 : 1, baos);
                int continuationStart = i * 32767;
                int continuationLength = continuationStart - glyfData.length < 32767
                        ? continuationStart - glyfData.length : 32767;
                baos.write(glyfData, continuationStart, continuationLength);
            }
        }
        baos.write(0); // Reserved
        byte[] charBytes = baos.toByteArray();
        long sum = 0;
        for (int i = 4; i < charBytes.length; i++) {
            sum += charBytes[i];
        }
        int remainder = (int) (sum % 256);
        baos.write(256 - remainder); // Checksum

        return baos.toByteArray();
    }

    private void writeCharacterDescriptorHeader(int continuation, ByteArrayOutputStream baos) throws IOException {
        baos.write(pclByteWriter.unsignedByte(charFormat.getValue()));
        baos.write(continuation);
        baos.write(pclByteWriter.unsignedByte(2)); // Descriptor size (from this byte to character data)
        baos.write(pclByteWriter.unsignedByte(charClass.getValue()));
        baos.write(pclByteWriter.unsignedInt(glyfData.length + 4));
        baos.write(pclByteWriter.unsignedInt(charCode));
    }

    public void addCompositeGlyph(PCLCharacterDefinition composite) {
        composites.add(composite);
    }

    public List<PCLCharacterDefinition> getCompositeGlyphs() {
        return composites;
    }

    /**
     * Character Format used in PCL Character Descriptor See Table 11-50 from PCL 5 Specification
     */
    public enum PCLCharacterFormat {
        LaserJet_Raster(4),
        Intellifont(10),
        TrueType(15);

        private int value;

        PCLCharacterFormat(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Character Class used in PCL Character Descriptor See Table 11-51 from PCL 5 Specification
     */
    public enum PCLCharacterClass {
        Bitmap(1),
        CompressedBitmap(2),
        Contour_Intellifont(3),
        Compound_Contour_Intellifont(4),
        TrueType(15);

        private int value;

        PCLCharacterClass(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
