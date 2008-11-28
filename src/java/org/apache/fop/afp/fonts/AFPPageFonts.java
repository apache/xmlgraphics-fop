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

package org.apache.fop.afp.fonts;

/**
 * Holds the current page fonts
 */
public class AFPPageFonts extends java.util.HashMap {
    private static final long serialVersionUID = -4991896259427109041L;

    /**
     * Default constructor
     */
    public AFPPageFonts() {
        super();
    }

    /**
     * Parameterized constructor
     *
     * @param fonts an existing set of afp page fonts
     */
    public AFPPageFonts(AFPPageFonts fonts) {
        super(fonts);
    }

    /**
     * Registers a font on the current page and returns font attributes
     *
     * @param fontName the internal font name
     * @param font the AFPFont
     * @param fontSize the font point size
     * @return newly registered AFPFontAttributes
     */
    public AFPFontAttributes registerFont(String fontName, AFPFont font, int fontSize) {
        String pageFontKey = fontName + "_" + fontSize;
        AFPFontAttributes afpFontAttributes = (AFPFontAttributes)super.get(pageFontKey);
        // Add to page font mapping if not already present
        if (afpFontAttributes == null) {
            afpFontAttributes = new AFPFontAttributes(fontName, font, fontSize);
            super.put(pageFontKey, afpFontAttributes);
            int fontRef = super.size();
            afpFontAttributes.setFontReference(fontRef);
        }
        return afpFontAttributes;
    }
}