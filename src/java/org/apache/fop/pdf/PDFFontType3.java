/*
 * $Id: PDFFontType3.java,v 1.4 2003/03/07 08:25:47 jeremias Exp $
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
 * Class representing a Type3 font.
 * <p>
 * <b>CAUTION: this is not yet fully implemented!!!!!!!</b>
 * the /CharProcs is still missing its <code>toPDF()</code> method.
 * <p>
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
     * Create the /Font object
     *
     * @param fontname the internal name for the font
     * @param basefont the base font name
     * @param encoding the character encoding schema used by the font
     */
    public PDFFontType3(String fontname, 
                        String basefont,
                        Object encoding) {

        /* generic creation of PDF object */
        super(fontname, FontType.TYPE3, basefont, encoding /* , mapping */);

        this.fontBBox = null;
        this.fontMatrix = null;
        this.charProcs = null;
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

        this.fontBBox = fontBBox;
        this.fontMatrix = fontMatrix;
        this.charProcs = charProcs;
    }

    /**
     * Set the font's bounding box
     *
     * @param bbox bounding box for the font
     */
    public void setFontBBox(PDFRectangle bbox) {
        this.fontBBox = bbox;
    }

    /**
     * Set the font's transformation matrix
     *
     * @param matrix the transformation matrix for the font
     */
    public void setFontMatrix(PDFArray matrix) {
        this.fontMatrix = matrix;
    }

    /**
     * Set the glyphs' definitions.
     * <p>
     * The /CharProcs object needs to be registered in the document's resources.
     *
     * @param chars the glyphs' dictionary
     */
    public void setCharProcs(PDFCharProcs chars) {
        this.charProcs = chars;
    }

    /**
     * @see org.apache.fop.pdf.PDFFont#fillInPDF(StringBuffer)
     */
    protected void fillInPDF(StringBuffer target) {
        if (fontBBox != null) {
            target.append("\n/FontBBox ");
            target.append(fontBBox.toPDF());
        }
        if (fontMatrix != null) {
            target.append("\n/FontMatrix ");
            target.append(fontMatrix.toPDF());
        }
        if (charProcs != null) {
            target.append("\n/CharProcs ");
            target.append(charProcs.referencePDF());
        }
    }

}
