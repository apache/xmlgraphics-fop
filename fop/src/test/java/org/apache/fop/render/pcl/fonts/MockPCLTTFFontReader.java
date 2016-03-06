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

import java.io.IOException;

import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFFile;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFFontReader;

public class MockPCLTTFFontReader extends PCLTTFFontReader {

    public MockPCLTTFFontReader(Typeface font, PCLByteWriterUtil pclByteWriter) throws IOException {
        super(font, pclByteWriter);
    }

    @Override
    protected void loadFont() throws IOException {
        if (typeface instanceof CustomFontMetricsMapper) {
            CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper) typeface;
            CustomFont customFont = (CustomFont) fontMetrics.getRealFont();
            fontStream = customFont.getInputStream();
            reader = new FontFileReader(fontStream);

            ttfFont = new TTFFile();
            ttfFont.readFont(reader, customFont.getFullName());
            readFontTables();
        } else {
            // TODO - Handle when typeface is not in the expected format for a PCL TrueType object
        }
    }
}
