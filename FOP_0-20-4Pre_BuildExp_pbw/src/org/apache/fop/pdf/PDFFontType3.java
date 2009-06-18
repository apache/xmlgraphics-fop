/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a Type3 font.
 *
 * <p><b>CAUTION: this is not yet fully implemented!!!!!!!</b>
 * the /CharProcs is still missing its <code>toPDF()</code> method.
 * </p>
 *
 * Type3 fonts are specified on page 206 and onwards of the PDF 1.3 spec.
 */
public class PDFFontType3 extends PDFFontNonBase14 {

    /**
     * font's required /FontBBox bounding box
     */
    protected PDFRectangle fontBBox;

    /**
     * font's required /FontMatrix array
     */
    protected PDFArray fontMatrix;

    /**
     * font's required /CharProcs dictionary
     */
    protected PDFCharProcs charProcs;

    /**
     * font's optional /Resources object
     */
    protected PDFResources resources;

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TYPE3)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     */
    public PDFFontType3(int number, String fontname, byte subtype,
                        String basefont,
                        Object encoding /* , PDFToUnicode mapping */) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding /* , mapping */);

        this.fontBBox = null;
        this.fontMatrix = null;
        this.charProcs = null;
    }

    /**
     * create the /Font object
     *
     * @param number the object's number
     * @param fontname the internal name for the font
     * @param subtype the font's subtype (PDFFont.TYPE3)
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     * @param mapping the Unicode mapping mechanism
     * @param fontBBox the font's bounding box
     * @param fontMatrix the font's transformation matrix
     * @param charProcs the glyphs' definitions
     */
    public PDFFontType3(int number, String fontname, byte subtype,
                        String basefont,
                        Object encoding /* , PDFToUnicode mapping */,
                        PDFRectangle fontBBox, PDFArray fontMatrix,
                        PDFCharProcs charProcs) {

        /* generic creation of PDF object */
        super(number, fontname, subtype, basefont, encoding /* , mapping */);

        this.fontBBox = fontBBox;
        this.fontMatrix = fontMatrix;
        this.charProcs = charProcs;
    }

    /**
     * set the font's bounding box
     *
     * @param bbox bounding box for the font
     */
    public void setFontBBox(PDFRectangle bbox) {
        this.fontBBox = bbox;
    }

    /**
     * set the font's transformation matrix
     *
     * @param matrix the transformation matrix for the font
     */
    public void setFontMatrix(PDFArray matrix) {
        this.fontMatrix = matrix;
    }

    /**
     * set the glyphs' definitions.
     * The /CharProcs object needs to be registered in the document's resources.
     *
     * @param chars the glyphs' dictionary
     */
    public void setCharProcs(PDFCharProcs chars) {
        this.charProcs = chars;
    }

    /**
     * fill in the specifics for the font's subtype.
     *
     * the given buffer already contains the fields common to all font types.
     *
     * @param p the buffer to be completed with the type specific fields
     */
    protected void fillInPDF(StringBuffer p) {
        if (fontBBox != null) {
            p.append("\n/FontBBox ");
            p.append(fontBBox.toPDF());
        }
        if (fontMatrix != null) {
            p.append("\n/FontMatrix ");
            p.append(fontMatrix.toPDF());
        }
        if (charProcs != null) {
            p.append("\n/CharProcs ");
            p.append(charProcs.referencePDF());
        }
    }

}
