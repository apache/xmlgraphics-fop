/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

import org.apache.fop.fonts.FontType;

/**
 * Class representing a font descriptor (/FontDescriptor object).
 * <p>
 * Font descriptors are specified on page 222 and onwards of the PDF 1.3 spec.
 */
public class PDFFontDescriptor extends PDFObject {

    // Required fields
    private int ascent;
    private int capHeight;
    private int descent;
    private int flags;
    private PDFRectangle fontBBox;
    private String basefont;    // PDF-spec: FontName
    private int italicAngle;
    private int stemV;
    // Optional fields
    private int stemH = 0;
    private int xHeight = 0;
    private int leading = 0;
    private int avgWidth = 0;
    private int maxWidth = 0;
    private int missingWidth = 0;
    private PDFStream fontfile;
    // private String charSet = null;

    private FontType subtype;

    /**
     * Create the /FontDescriptor object
     *
     * @param number the object's number
     * @param ascent the maximum height above the baseline
     * @param descent the maximum depth below the baseline
     * @param capHeight height of the capital letters
     * @param flags various characteristics of the font
     * @param fontBBox the bounding box for the described font
     * @param basefont the base font name
     * @param italicAngle the angle of the vertical dominant strokes
     * @param stemV the width of the dominant vertical stems of glyphs
     */
    public PDFFontDescriptor(int number, String basefont, int ascent,
                             int descent, int capHeight, int flags,
                             PDFRectangle fontBBox, int italicAngle,
                             int stemV) {

        /* generic creation of PDF object */
        super(number);

        /* set fields using paramaters */
        this.basefont = basefont;
        this.ascent = ascent;
        this.descent = descent;
        this.capHeight = capHeight;
        this.flags = flags;
        this.fontBBox = fontBBox;
        this.italicAngle = italicAngle;
        this.stemV = stemV;
    }

    /**
     * Set the optional metrics.
     * @param avgWidth The average width of characters in this font. 
     * The default value is 0.
     * @param maxWidth The maximum width of characters in this font. 
     * The default value is 0.
     * @param missingWidth missing width
     * @param leading the desired spacing between lines of text. 
     * The default value is 0.
     * @param stemH The vertical width of the dominant horizontal stems of 
     * glyphs in the font. The default value is 0.
     * @param xHeight The y-coordinate of the top of flat non-ascending 
     * lowercase letters, measured from the baseline. The default value is 0.
     */
    public void setMetrics(int avgWidth, int maxWidth, int missingWidth,
                           int leading, int stemH, int xHeight) {
        this.avgWidth = avgWidth;
        this.maxWidth = maxWidth;
        this.missingWidth = missingWidth;
        this.leading = leading;
        this.stemH = stemH;
        this.xHeight = xHeight;
    }

    /**
     * Set the optional font file stream
     *
     * @param subtype the font type defined in the font stream
     * @param fontfile the stream containing an embedded font
     */
    public void setFontFile(FontType subtype, PDFStream fontfile) {
        this.subtype = subtype;
        this.fontfile = fontfile;
    }

    // public void setCharSet(){}//for subset fonts

    /**
     * Produce the PDF representation for the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n<< /Type /FontDescriptor"
                                          + "\n/FontName /" + this.basefont);

        p.append("\n/FontBBox ");
        p.append(fontBBox.toPDFString());
        p.append("\n/Flags ");
        p.append(flags);
        p.append("\n/CapHeight ");
        p.append(capHeight);
        p.append("\n/Ascent ");
        p.append(ascent);
        p.append("\n/Descent ");
        p.append(descent);
        p.append("\n/ItalicAngle ");
        p.append(italicAngle);
        p.append("\n/StemV ");
        p.append(stemV);
        // optional fields
        if (stemH != 0) {
            p.append("\n/StemH ");
            p.append(stemH);
        }
        if (xHeight != 0) {
            p.append("\n/XHeight ");
            p.append(xHeight);
        }
        if (avgWidth != 0) {
            p.append("\n/AvgWidth ");
            p.append(avgWidth);
        }
        if (maxWidth != 0) {
            p.append("\n/MaxWidth ");
            p.append(maxWidth);
        }
        if (missingWidth != 0) {
            p.append("\n/MissingWidth ");
            p.append(missingWidth);
        }
        if (leading != 0) {
            p.append("\n/Leading ");
            p.append(leading);
        }
        if (fontfile != null) {
            if (subtype == FontType.TYPE1) {
                p.append("\n/FontFile ");
            } else {
                p.append("\n/FontFile2 ");
            }
            p.append(fontfile.referencePDF());
        }
        // charSet for subset fonts // not yet implemented
        // CID optional field
        fillInPDF(p);
        p.append("\n >>\nendobj\n");
        return p.toString().getBytes();
    }

    /**
     * Fill in the specifics for the font's descriptor.
     * <p>
     * The given buffer already contains the fields common to all descriptors.
     *
     * @param begin the buffer to be completed with the specific fields
     */
    protected void fillInPDF(StringBuffer begin) {
        //nop
    }

}
