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

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.PCLByteWriterUtil;
import org.apache.fop.render.pcl.fonts.PCLSoftFont;

public class PCLTTFCharacterWriterTestCase {

    private PCLTTFCharacterWriter characterWriter;
    private PCLSoftFont softFont;
    private CustomFontMetricsMapper customFont = mock(CustomFontMetricsMapper.class);
    private static final String TEST_FONT_A = "./test/resources/fonts/ttf/DejaVuLGCSerif.ttf";

    @Test
    public void verifyCharacterDefinition() throws Exception {
        CustomFont sbFont = mock(CustomFont.class);
        when(customFont.getRealFont()).thenReturn(sbFont);
        softFont = new PCLSoftFont(1, customFont);
        TTFFile openFont = new TTFFile();
        FontFileReader reader = new FontFileReader(new FileInputStream(new File(TEST_FONT_A)));
        String header = OFFontLoader.readHeader(reader);
        openFont.readFont(reader, header);
        softFont.setOpenFont(openFont);
        softFont.setReader(reader);

        characterWriter = new PCLTTFCharacterWriter(softFont);
        byte[] charDefinition = characterWriter.writeCharacterDefinitions("f");
        PCLByteWriterUtil pclByteWriter = new PCLByteWriterUtil();
        // Character command
        byte[] command = pclByteWriter.writeCommand(String.format("*c%dE", 32));
        assertArrayEquals(getBytes(charDefinition, 0, 6), command);
        // Character definition command
        byte[] charDefCommand = pclByteWriter.writeCommand(String.format("(s%dW", 210));
        assertArrayEquals(getBytes(charDefinition, 6, 7), charDefCommand);
    }

    private byte[] getBytes(byte[] byteArray, int offset, int length) {
        byte[] result = new byte[length];
        int count = 0;
        for (int i = offset; i < offset + length; i++) {
            result[count++] = byteArray[i];
        }
        return result;
    }
}
