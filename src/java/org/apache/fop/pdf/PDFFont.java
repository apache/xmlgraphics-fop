/*
 * Copyright 1999-2004,2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * Class representing a /Font object.
 * <p>
 * A more complete object expressing the base font name and encoding of a
 * font along with an internal name for the font used within
 * streams of content.
 * <p>
 * Fonts are specified on page 198 and onwards of the PDF 1.3 spec.
 */
public class PDFFont extends PDFObject {

    /**
     * the internal name for the font (eg "F1")
     */
    protected String fontname;

    /**
     * the font's subtype
     * (as defined by the constants FontType: TYPE0, TYPE1, MMTYPE1, TYPE3, TRUETYPE)
     */
    protected FontType subtype;

    /**
     * the base font name (eg "Helvetica")
     */
    protected String basefont;

    /**
     * the character encoding scheme used by the font.
     * It can be a String for standard encodings, or
     * a PDFEncoding for a more complex scheme, or
     * a PDFStream containing a CMap in a Type0 font.
     * If <code>null</code> then not written out in the PDF.
     */
    protected Object encoding;

    /**
     * the Unicode mapping mechanism
     */
    // protected PDFToUnicode mapping;

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
                   Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super();

        /* set fields using paramaters */
        this.fontname = fontname;
        this.subtype = subtype;
        this.basefont = basefont;
        this.encoding = encoding;
        // this.mapping = mapping;
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
            return null;    // should not happend
        }
    }

    /**
     * factory method with the extended parameters
     * for Type1, MMType1 and TrueType
     *
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param firstChar the first character code in the font
     * @param lastChar the last character code in the font
     * @param widths an array of size (lastChar - firstChar +1)
     * @param descriptor the descriptor for other font's metrics
     * @return the generated PDFFont object
     */
    public static PDFFont createFont(String fontname,
                                     FontType subtype, String basefont,
                                     Object encoding, int firstChar,
                                     int lastChar, PDFArray widths,
                                     PDFFontDescriptor descriptor) {

        PDFFontNonBase14 font;
        if (subtype == FontType.TYPE0) {
            font = new PDFFontType0(fontname, basefont,
                                    encoding);
            font.setDescriptor(descriptor);
            return font;
        } else if ((subtype == FontType.TYPE1) 
                || (subtype == FontType.MMTYPE1)) {
            font = new PDFFontType1(fontname, basefont,
                                    encoding);
            font.setWidthMetrics(firstChar, lastChar, widths);
            font.setDescriptor(descriptor);
            return font;
        } else if (subtype == FontType.TYPE3) {
            return null; //NYI, should not happend
        } else if (subtype == FontType.TRUETYPE) {
            font = new PDFFontTrueType(fontname, basefont,
                                       encoding);
            font.setWidthMetrics(firstChar, lastChar, widths);
            font.setDescriptor(descriptor);
            return font;
        } else {
            return null;    // should not happend
        }
    }

    /**
     * get the internal name used for this font
     * @return the internal name
     */
    public String getName() {
        return this.fontname;
    }

    /**
     * Returns the PDF name for a certain font type.
     * @param fontType font type
     * @return String corresponding PDF name
     */
    protected String getPDFNameForFontType(FontType fontType) {
        if (fontType == FontType.TYPE0) {
            return fontType.getName();
        } else if (fontType == FontType.TYPE1) {
            return fontType.getName();
        } else if (fontType == FontType.MMTYPE1) {
            return fontType.getName();
        } else if (fontType == FontType.TYPE3) {
            return fontType.getName();
        } else if (fontType == FontType.TRUETYPE) {
            return fontType.getName();
        } else {
            throw new IllegalArgumentException("Unsupported font type: " + fontType.getName());
        }
    }

    /**
     * Validates the PDF object prior to serialization.
     */
    protected void validate() {
        if (getDocumentSafely().getPDFAMode().isPDFA1LevelB()) {
            if (this.getClass() == PDFFont.class) {
                throw new PDFConformanceException("For PDF/A-1, all fonts, even the base 14"
                    + " fonts, have to be embedded! Offending font: " + this.basefont);
            }
        }
    }
    
    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        validate();
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID());
        p.append("<< /Type /Font\n/Subtype /"
                 + getPDFNameForFontType(this.subtype)
                 + "\n/Name /" + this.fontname
                 + "\n/BaseFont /" + this.basefont);
        if (encoding != null) {
            p.append("\n/Encoding ");
            if (encoding instanceof PDFEncoding) {
                p.append(((PDFEncoding)this.encoding).referencePDF());
            } else if (encoding instanceof PDFStream) {
                p.append(((PDFStream)this.encoding).referencePDF());
            } else {
                p.append("/").append((String)encoding);
            }
        }
        fillInPDF(p);
        p.append(" >>\nendobj\n");
        return p.toString();
    }

    /**
     * This method is called to receive the specifics for the font's subtype.
     * <p>
     * The given buffer already contains the fields common to all font types.
     *
     * @param target the buffer to be completed with the type specific fields
     */
    protected void fillInPDF(StringBuffer target) {
        //nop
    }

}
