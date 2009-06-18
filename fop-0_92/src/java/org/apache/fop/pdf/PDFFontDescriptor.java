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
    private AbstractPDFStream fontfile;
    // private String charSet = null;

    private FontType subtype;

    /**
     * Create the /FontDescriptor object
     *
     * @param ascent the maximum height above the baseline
     * @param descent the maximum depth below the baseline
     * @param capHeight height of the capital letters
     * @param flags various characteristics of the font
     * @param fontBBox the bounding box for the described font
     * @param basefont the base font name
     * @param italicAngle the angle of the vertical dominant strokes
     * @param stemV the width of the dominant vertical stems of glyphs
     */
    public PDFFontDescriptor(String basefont, int ascent,
                             int descent, int capHeight, int flags,
                             PDFRectangle fontBBox, int italicAngle,
                             int stemV) {

        /* generic creation of PDF object */
        super();

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
    public void setFontFile(FontType subtype, AbstractPDFStream fontfile) {
        this.subtype = subtype;
        this.fontfile = fontfile;
    }

    /** @return the FontFile or null if the font is not embedded */
    public AbstractPDFStream getFontFile() {
        return this.fontfile;
    }
    
    // public void setCharSet(){}//for subset fonts

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID() 
                + "<< /Type /FontDescriptor"
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
        p.append(" >>\nendobj\n");
        return p.toString();
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
