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
 
package org.apache.fop.pdf;

// based on work by Takayuki Takeuchi

/**
 * Class representing a font descriptor for CID fonts.
 *
 * Font descriptors for CID fonts are specified on page 227 and onwards of the PDF 1.3 spec.
 */
public class PDFCIDFontDescriptor extends PDFFontDescriptor {

    /**
     * Create a /FontDescriptor object.
     *
     * @param basefont the base font name
     * @param fontBBox the bounding box for the described font
     * @param flags various characteristics of the font
     * @param capHeight height of the capital letters
     * @param stemV the width of the dominant vertical stems of glyphs
     * @param italicAngle the angle of the vertical dominant strokes
     * @param lang the language
     */
    public PDFCIDFontDescriptor(String basefont, int[] fontBBox,
                                int capHeight, int flags, int italicAngle,
                                int stemV, String lang) {

        super(basefont, fontBBox[3], fontBBox[1], capHeight, flags,
              new PDFRectangle(fontBBox), italicAngle, stemV);

        put("MissingWidth", new Integer(500));
        if (lang != null) {
            put("Lang", lang);
        }
    }

    /**
     * Set the CID set stream.
     * @param cidSet the PDF stream containing the CID set
     */
    public void setCIDSet(PDFStream cidSet) {
        if (cidSet != null) {
            put("CIDSet", cidSet);
        }
    }

}
