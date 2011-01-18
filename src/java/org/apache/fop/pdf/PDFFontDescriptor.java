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
 * Class representing a font descriptor (/FontDescriptor object).
 * <p>
 * Font descriptors are specified on page 222 and onwards of the PDF 1.3 spec.
 */
public class PDFFontDescriptor extends PDFDictionary {

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
    public PDFFontDescriptor(                                    // CSOK: ParameterNumber
            String basefont, int ascent,
            int descent, int capHeight, int flags,
            PDFRectangle fontBBox, int italicAngle,
            int stemV) {
        super();

        put("Type", new PDFName("FontDescriptor"));
        put("FontName", new PDFName(basefont));
        put("FontBBox", fontBBox);
        put("Flags", flags);
        put("CapHeight", capHeight);
        put("Ascent", ascent);
        put("Descent", descent);
        put("ItalicAngle", italicAngle);
        put("StemV", stemV);
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
        if (avgWidth != 0) {
            put("AvgWidth", avgWidth);
        }
        if (maxWidth != 0) {
            put("MaxWidth", maxWidth);
        }
        if (missingWidth != 0) {
            put("MissingWidth", missingWidth);
        }
        if (leading != 0) {
            put("Leading", leading);
        }
        if (stemH != 0) {
            put("StemH", stemH);
        }
        if (xHeight != 0) {
            put("XHeight", xHeight);
        }
    }

    /**
     * Set the optional font file stream
     *
     * @param subtype the font type defined in the font stream
     * @param fontfile the stream containing an embedded font
     */
    public void setFontFile(FontType subtype, AbstractPDFStream fontfile) {
        if (subtype == FontType.TYPE1) {
            put("FontFile", fontfile);
        } else {
            put("FontFile2", fontfile);
        }
    }

    /** @return the FontFile or null if the font is not embedded */
    public AbstractPDFStream getFontFile() {
        AbstractPDFStream stream;
        stream = (AbstractPDFStream)get("FontFile");
        if (stream == null) {
            stream = (AbstractPDFStream)get("FontFile2");
        }
        if (stream == null) {
            stream = (AbstractPDFStream)get("FontFile3");
        }
        return stream;
    }

    /**
     * Sets the CIDSet stream for this font descriptor. (Optional)
     * @param cidSet the CIDSet stream
     */
    public void setCIDSet(AbstractPDFStream cidSet) {
        put("CIDSet", cidSet);
    }

    /** @return the CIDSet stream or null if not applicable */
    public AbstractPDFStream getCIDSet() {
        return (AbstractPDFStream)get("CIDSet");
    }

    /**
     * {@inheritDoc}
     */
    /*
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
        if (getCIDSet() != null) {
            p.append("\n/CIDSet ");
            p.append(getCIDSet().referencePDF());
        }
        // charSet for subset fonts // not yet implemented
        // CID optional field
        fillInPDF(p);
        p.append(" >>\nendobj\n");
        return p.toString();
    }*/

    /**
     * Fill in the specifics for the font's descriptor.
     * <p>
     * The given buffer already contains the fields common to all descriptors.
     *
     * @param begin the buffer to be completed with the specific fields
     *//*
    protected void fillInPDF(StringBuffer begin) {
        //nop
    }*/

}
