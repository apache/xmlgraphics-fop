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
