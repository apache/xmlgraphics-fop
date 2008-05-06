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

/* $Id: $ */

package org.apache.fop.config;

import java.io.File;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.render.PrintRenderer;

public class FontsSubstitutionTestCase extends BaseConstructiveUserConfigTestCase {

    public FontsSubstitutionTestCase(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    protected byte[] convertFO(File foFile, FOUserAgent ua, boolean dumpPdfFile) throws Exception {
        PrintRenderer renderer = (PrintRenderer)ua.getRendererFactory().createRenderer(ua, MimeConstants.MIME_PDF);
        FontInfo fontInfo = new FontInfo();
        renderer.setupFontInfo(fontInfo);
        FontManager fontManager = ua.getFactory().getFontManager();
        fontManager.setupRenderer(renderer);
        FontTriplet triplet = new FontTriplet("Times", "italic", Font.WEIGHT_NORMAL);
        String internalFontKey = fontInfo.getInternalFontKey(triplet);
        // Times italic should now be mapped to the 15th font (custom font)
        // not the original base 14 (F6)
        if (!"F15".equals(internalFontKey)) {
            throw new Exception("font substitution failed :" + triplet);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getUserConfigFilename() {
        return "test_fonts_substitution.xconf";
    }
}
