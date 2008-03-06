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
 * Class representing a Type3 font.
 * <p>
 * <b>CAUTION: this is not yet fully implemented!!!!!!!</b>
 * the /CharProcs is still missing its <code>toPDF()</code> method.
 * <p>
 * Type3 fonts are specified on page 206 and onwards of the PDF 1.3 spec.
 */
public class PDFFontType3 extends PDFFontNonBase14 {

    /**
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontType3(String fontname, 
                        String basefont,
                        Object encoding) {
        super(fontname, FontType.TYPE3, basefont, encoding);
    }

    /**
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param fontBBox the font's bounding box
     * @param fontMatrix the font's transformation matrix
     * @param charProcs the glyphs' definitions
     */
    public PDFFontType3(String fontname, 
                        String basefont,
                        Object encoding,
                        PDFRectangle fontBBox, PDFArray fontMatrix,
                        PDFCharProcs charProcs) {

        /* generic creation of PDF object */
        super(fontname, FontType.TYPE3, basefont, encoding /* , mapping */);

        setFontBBox(fontBBox);
        setFontMatrix(fontMatrix);
        setCharProcs(charProcs);
    }

    /**
     * Set the font's bounding box
     *
     * @param bbox bounding box for the font
     */
    public void setFontBBox(PDFRectangle bbox) {
        put("FontBBox", bbox);
    }

    /**
     * Set the font's transformation matrix
     *
     * @param matrix the transformation matrix for the font
     */
    public void setFontMatrix(PDFArray matrix) {
        put("FontMatrix", matrix);
    }

    /**
     * Set the glyphs' definitions.
     * <p>
     * The /CharProcs object needs to be registered in the document's resources.
     *
     * @param chars the glyphs' dictionary
     */
    public void setCharProcs(PDFCharProcs chars) {
        put("CharProcs", chars);
    }

}
