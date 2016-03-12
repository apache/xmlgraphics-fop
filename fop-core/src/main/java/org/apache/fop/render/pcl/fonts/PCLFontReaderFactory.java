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

import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;
import org.apache.fop.render.pcl.fonts.truetype.PCLTTFFontReader;

public final class PCLFontReaderFactory {

    private PCLByteWriterUtil pclByteWriter;

    private PCLFontReaderFactory(PCLByteWriterUtil pclByteWriter) {
        this.pclByteWriter = pclByteWriter;
    }

    public static PCLFontReaderFactory getInstance(PCLByteWriterUtil pclByteWriter) {
        return new PCLFontReaderFactory(pclByteWriter);
    }

    public PCLFontReader createInstance(Typeface font) throws IOException {
        if (font.getFontType() == FontType.TRUETYPE || isCIDType2(font)) {
            return new PCLTTFFontReader(font, pclByteWriter);
        }
        // else if (font instanceof MultiByteFont && ((MultiByteFont) font).isOTFFile()) {
            // Placeholder for future Type 1 / OTF Soft font implementations e.g.
            // return new PCLOTFFontReader(font, pclByteWriter);
        // }
        return null;
    }

    private boolean isCIDType2(Typeface font) {
        CustomFontMetricsMapper fontMetrics = (CustomFontMetricsMapper) font;
        CustomFont customFont = (CustomFont) fontMetrics.getRealFont();

        if (customFont instanceof MultiByteFont) {
            return ((MultiByteFont) customFont).getCIDType() == CIDFontType.CIDTYPE2;
        }
        return false;
    }

}
