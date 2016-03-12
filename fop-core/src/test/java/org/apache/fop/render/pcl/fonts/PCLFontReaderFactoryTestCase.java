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

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontType;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFFontReader;

public class PCLFontReaderFactoryTestCase {
    private static final String TEST_FONT_TTF = "./test/resources/fonts/ttf/DejaVuLGCSerif.ttf";

    @Test
    public void verifyTypeIdentification() throws Exception {
        CustomFont sbFont = mock(CustomFont.class);
        when(sbFont.getInputStream()).thenReturn(new FileInputStream(new File(TEST_FONT_TTF)));
        when(sbFont.getEmbedFileURI()).thenReturn(new URI(TEST_FONT_TTF));
        CustomFontMetricsMapper customFont = new CustomFontMetricsMapper(sbFont);
        when(customFont.getFontType()).thenReturn(FontType.TRUETYPE);
        // Have to mock the input stream twice otherwise get a Stream is closed exception
        when(((CustomFont) customFont.getRealFont()).getInputStream()).thenReturn(
                new FileInputStream(new File(TEST_FONT_TTF)));
        PCLFontReaderFactory fontReaderFactory = PCLFontReaderFactory.getInstance(null);
        assertTrue(fontReaderFactory.createInstance(customFont) instanceof PCLTTFFontReader);
    }
}
