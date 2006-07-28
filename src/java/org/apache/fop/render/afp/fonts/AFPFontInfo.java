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

package org.apache.fop.render.afp.fonts;

import java.util.List;

/**
 * FontInfo contains meta information on fonts
 */
public class AFPFontInfo {

    private AFPFont font;
    private List fontTriplets;

    /**
     * Main constructor
     * @param afpFont The AFP Font
     * @param fontTriplets List of font triplets to associate with this font
     */
    public AFPFontInfo(AFPFont afpFont, List fontTriplets) {
        this.font = afpFont;
        this.fontTriplets = fontTriplets;
    }

    /**
     * Returns the afp font
     * @return the afp font
     */
    public AFPFont getAFPFont() {
        return font;
    }

    /**
     * Returns the list of font triplets associated with this font.
     * @return List of font triplets
     */
    public List getFontTriplets() {
        return fontTriplets;
    }

}

