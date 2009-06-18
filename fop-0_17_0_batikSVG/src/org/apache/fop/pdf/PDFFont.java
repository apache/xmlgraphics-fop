/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.fop.pdf;

/**
 * class representing a /Font object.
 *
 * A more complete object expressing the base font name and encoding of a
 * font along with an internal name for the font used within
 * streams of content.
 *
 * Fonts are specified on page 198 and onwards of the PDF 1.3 spec.
 */
public class PDFFont extends PDFObject {

    /** font subtype to be used as parameter to createFont() */
    public static final byte TYPE0 = 0;
    /** font subtype to be used as parameter to createFont() */
    public static final byte TYPE1 = 1;
    /** font subtype to be used as parameter to createFont() */
    public static final byte MMTYPE1 = 2;
    /** font subtype to be used as parameter to createFont() */
    public static final byte TYPE3 = 3;
    /** font subtype to be used as parameter to createFont() */
    public static final byte TRUETYPE = 4;

    /** font subtype names as output in the PDF */
    protected static final String[] TYPE_NAMES = new String[] // take care of the order
            {"Type0", "Type1", "MMType1", "Type3", "TrueType"};

    /** the internal name for the font (eg "F1") */
    protected String fontname;

    /**
     * the font's subtype
     * (as defined by the constants TYPE0, TYPE1, MMTYPE1, TYPE3, TRUETYPE)
     */
    protected byte subtype;

    /** the base font name (eg "Helvetica") */
    protected String basefont;

    /**
     * the character encoding scheme used by the font.
     * It can be a String for standard encodings, or
     * a PDFEncoding for a more complex scheme, or
     * a PDFStream containing a CMap in a Type0 font.
     * If <code>null</code> then not written out in the PDF.
     */
    protected Object encoding;

    /** the Unicode mapping mechanism */
    //protected PDFToUnicode mapping;

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFont(int number, String fontname, byte subtype, String basefont,
            Object encoding/*, PDFToUnicode mapping*/) {

        /* generic creation of PDF object */
        super(number);

        /* set fields using paramaters */
        this.fontname = fontname;
        this.subtype = subtype;
        this.basefont = basefont;
        this.encoding = encoding;
        //this.mapping = mapping;
    }

    /**
     * factory method with the basic parameters
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public static PDFFont createFont(int number, String fontname,
            byte subtype, String basefont, Object encoding) {
        switch (subtype) {
            case TYPE0 :
                return new PDFFontType0(number, fontname, subtype,
                                        basefont, encoding);
            case TYPE1 :
            case MMTYPE1 :
                return new PDFFontType1(number, fontname, subtype, basefont, encoding);
            /*
            case TYPE3 :
                return new PDFFontType3(number, fontname, subtype, basefont, encoding);
            */
            case TRUETYPE :
                return new PDFFontTrueType(number, fontname, subtype, basefont, encoding);
        }
        return null;    // should not happend
    }

    /**
     * factory method with the extended parameters
     * for Type1, MMType1 and TrueType
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param firstChar the first character code in the font
     * @param lastChar the last character code in the font
     * @param widths an array of size (lastChar - firstChar +1)
     * @param descriptor the descriptor for other font's metrics
     */
    public static PDFFont createFont(int number, String fontname,
            byte subtype, String basefont, Object encoding,
            int firstChar, int lastChar, PDFArray widths,
            PDFFontDescriptor descriptor) {

        PDFFontNonBase14 font;
        switch (subtype) {
            case TYPE0 :
                font = new PDFFontType0(number, fontname, subtype,
                                        basefont, encoding);
                font.setDescriptor(descriptor);
                return font;
            case TYPE1 :
            case MMTYPE1 :
                font = new PDFFontType1(number, fontname, subtype, basefont, encoding);
                font.setWidthMetrics(firstChar, lastChar, widths);
                font.setDescriptor(descriptor);
                return font;
            case TYPE3 :
                return null;    // should not happend

            case TRUETYPE :
               font = new PDFFontTrueType(number, fontname, subtype, basefont, encoding);
               font.setWidthMetrics(firstChar, lastChar, widths);
               font.setDescriptor(descriptor);
               return font;

        }
        return null;    // should not happend
    }

    /**
     * get the internal name used for this font
     *
     * @return the internal name
     */
    public String getName() {
        return this.fontname;
    }

    /**
     * produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer();
        p.append(this.number + " " + this.generation
                + " obj\n<< /Type /Font\n/Subtype /" + TYPE_NAMES[this.subtype]
                + "\n/Name /" + this.fontname + "\n/BaseFont /" + this.basefont);
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
        return p.toString().getBytes();
    }

    /**
     * fill in the specifics for the font's subtype.
     *
     * the given buffer already contains the fields common to all font types.
     *
     * @param begin the buffer to be completed with the type specific fields
     */
    protected void fillInPDF(StringBuffer begin) {
    }
}
