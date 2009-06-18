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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a /Font object.
 * <p>
 * A more complete object expressing the base font name and encoding of a
 * font along with an internal name for the font used within
 * streams of content.
 * <p>
 * Fonts are specified on page 198 and onwards of the PDF 1.3 spec.
 */
public class PDFFont extends PDFDictionary {

    /** Internal F-number for each font (it is not written to the font dict) */
    private String fontname;

    /**
     * create the /Font object
     *
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFont(String fontname, FontType subtype,
                   String basefont,
                   Object encoding) {

        /* generic creation of PDF object */
        super();

        this.fontname = fontname;
        put("Type", new PDFName("Font"));
        put("Subtype", getPDFNameForFontType(subtype));
        //put("Name", new PDFName(fontname));
        put("BaseFont", new PDFName(basefont));
        if (encoding instanceof PDFEncoding) {
            setEncoding((PDFEncoding)encoding);
        } else if (encoding instanceof String) {
            setEncoding((String)encoding);
        }
    }

    /**
     * Sets the Encoding value of the font.
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {
        if (encoding != null) {
            put("Encoding", new PDFName(encoding));
        }
    }

    /**
     * Sets the Encoding value of the font.
     * @param encoding the encoding
     */
    public void setEncoding(PDFEncoding encoding) {
        if (encoding != null) {
            put("Encoding", encoding);
        }
    }

    /**
     * factory method with the basic parameters
     *
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @return the generated PDFFont object
     */
    public static PDFFont createFont(String fontname,
                                     FontType subtype, String basefont,
                                     Object encoding) {
        if (subtype == FontType.TYPE0) {
            return new PDFFontType0(fontname, basefont,
                                    encoding);
        } else if ((subtype == FontType.TYPE1)
                || (subtype == FontType.MMTYPE1)) {
            return new PDFFontType1(fontname, basefont,
                                    encoding);
        } else if (subtype == FontType.TYPE3) {
            //return new PDFFontType3(number, fontname, basefont, encoding);
            return null; //NYI
        } else if (subtype == FontType.TRUETYPE) {
            return new PDFFontTrueType(fontname, basefont,
                                       encoding);
        } else {
            return null;    // should not happen
        }
    }

    /**
     * Get the internal name used for this font.
     * @return the internal name
     */
    public String getName() {
        return this.fontname;
    }

    /**
     * Returns the name of the BaseFont.
     * @return the BaseFont
     */
    public PDFName getBaseFont() {
        return (PDFName)get("BaseFont");
    }

    /**
     * Returns the PDF name for a certain font type.
     * @param fontType font type
     * @return String corresponding PDF name
     */
    protected PDFName getPDFNameForFontType(FontType fontType) {
        if (fontType == FontType.TYPE0) {
            return new PDFName(fontType.getName());
        } else if (fontType == FontType.TYPE1) {
            return new PDFName(fontType.getName());
        } else if (fontType == FontType.MMTYPE1) {
            return new PDFName(fontType.getName());
        } else if (fontType == FontType.TYPE3) {
            return new PDFName(fontType.getName());
        } else if (fontType == FontType.TRUETYPE) {
            return new PDFName(fontType.getName());
        } else {
            throw new IllegalArgumentException("Unsupported font type: " + fontType.getName());
        }
    }

    /**
     * Validates the PDF object prior to serialization.
     */
    protected void validate() {
        if (getDocumentSafely().getProfile().isFontEmbeddingRequired()) {
            if (this.getClass() == PDFFont.class) {
                throw new PDFConformanceException("For " + getDocumentSafely().getProfile()
                    + ", all fonts, even the base 14"
                    + " fonts, have to be embedded! Offending font: " + getBaseFont());
            }
        }
    }

    /** {@inheritDoc} */
    protected int output(OutputStream stream) throws IOException {
        validate();
        return super.output(stream);
    }

}
