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

import org.apache.fop.fonts.FontType;

/**
 * Class representing a Type0 font.
 * <p>
 * Type0 fonts are specified on page 208 and onwards of the PDF 1.3 spec.
 */
public class PDFFontType0 extends PDFFont {

    /**
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontType0(String fontname, 
                        String basefont,
                        Object encoding) {
        super(fontname, FontType.TYPE0, basefont, encoding);
    }

    /**
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public PDFFontType0(String fontname, 
                        String basefont,
                        Object encoding, 
                        PDFCIDFont descendantFonts) {
        super(fontname, FontType.TYPE0, basefont, encoding);

        setDescendantFonts(descendantFonts);
    }

    /**
     * Set the descendant font
     * @param descendantFonts the CIDFont upon which this font is based
     */
    public void setDescendantFonts(PDFCIDFont descendantFonts) {
        put("DescendantFonts", new PDFArray(this, new PDFObject[] {descendantFonts}));
    }

    /**
     * Sets the character map
     * @param cmap the character map
     */
    public void setCMAP(PDFCMap cmap) {
        put("ToUnicode", cmap);
    }

}
