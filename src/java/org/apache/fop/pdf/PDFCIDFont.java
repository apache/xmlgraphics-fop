/*
 * $Id: PDFCIDFont.java,v 1.6 2003/03/07 08:25:47 jeremias Exp $
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

import org.apache.fop.fonts.CIDFontType;

// based on work by Takayuki Takeuchi

/**
 * Class representing a "character identifier" font (p 210 and onwards).
 */
public class PDFCIDFont extends PDFObject {

    private String basefont;
    private CIDFontType cidtype;
    private Integer dw;
    private PDFWArray w;
    private int[] dw2;
    private PDFWArray w2;
    private PDFCIDSystemInfo systemInfo;
    private PDFCIDFontDescriptor descriptor;
    private PDFCMap cmap;

    /**
     * /CIDToGIDMap (only for CIDFontType2, see p 212)
     * can be either "Identity" (default) or a PDFStream
     */
    private PDFStream cidMap;


    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param registry name of the issuer
     * @param ordering Unique name of the font
     * @param supplement Supplement number
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
                      int[] w, String registry, String ordering,
                      int supplement, PDFCIDFontDescriptor descriptor) {

        this(basefont, cidtype, dw, 
                new PDFWArray(w), 
                new PDFCIDSystemInfo(registry, ordering, supplement),
                descriptor);
    }

    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param systemInfo CID system info
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
                      int[] w, PDFCIDSystemInfo systemInfo,
                      PDFCIDFontDescriptor descriptor) {

        this(basefont, cidtype, dw, 
                new PDFWArray(w), 
                systemInfo,
                descriptor);
    }

    /**
     * Create the /Font object
     * @param basefont Name of the basefont
     * @param cidtype CID type
     * @param dw default width
     * @param w array of character widths
     * @param systemInfo CID system info
     * @param descriptor CID font descriptor
     */
    public PDFCIDFont(String basefont, CIDFontType cidtype, int dw,
                      PDFWArray w, PDFCIDSystemInfo systemInfo,
                      PDFCIDFontDescriptor descriptor) {

        super();

        this.basefont = basefont;
        this.cidtype = cidtype;
        this.dw = new Integer(dw);
        this.w = w;
        this.dw2 = null;
        this.w2 = null;
        this.systemInfo = systemInfo;
        this.descriptor = descriptor;
        this.cidMap = null;
        this.cmap = null;
    }

    /**
     * Set the /DW attribute
     * @param dw the default width
     */
    public void setDW(int dw) {
        this.dw = new Integer(dw);
    }

    /**
     * Set the /W array
     * @param w the width array
     */
    public void setW(PDFWArray w) {
        this.w = w;
    }

    /**
     * Set the (two elements) /DW2 array
     * @param dw2 the default metrics for vertical writing
     */
    public void setDW2(int[] dw2) {
        this.dw2 = dw2;
    }

    /**
     * Set the two elements of the /DW2 array
     * @param posY position vector
     * @param displacementY displacement vector
     */
    public void setDW2(int posY, int displacementY) {
        this.dw2 = new int[] {
            posY, displacementY
        };
    }

    /**
     * Set the CMap used as /ToUnicode cmap
     * @param cmap character map
     */
    public void setCMAP(PDFCMap cmap) {
        this.cmap = cmap;
    }

    /**
     * Set the /W2 array
     * @param w2 array of metrics for vertical writing
     */
    public void setW2(PDFWArray w2) {
        this.w2 = w2;
    }

    /**
     * Set the /CIDToGIDMap (to be used only for CIDFontType2)
     * @param map mapping information
     */
    public void setCIDMap(PDFStream map) {
        this.cidMap = map;
    }

    /**
     * Set the /CIDToGIDMap (to be used only for CIDFontType2) to "Identity"
     */
    public void setCIDMapIdentity() {
        this.cidMap = null;    // not an error here, simply use the default
    }

    /**
     * Returns the PDF name for a certain CID font type.
     * @param cidFontType CID font type
     * @return corresponding PDF name
     */
    protected String getPDFNameForCIDFontType(CIDFontType cidFontType) {
        if (cidFontType == CIDFontType.CIDTYPE0) {
            return cidFontType.getName();
        } else if (cidFontType == CIDFontType.CIDTYPE2) {
            return cidFontType.getName();
        } else {
            throw new IllegalArgumentException("Unsupported CID font type: " 
                        + cidFontType.getName());
        }
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID());
        p.append("<< /Type /Font");
        p.append("\n/BaseFont /");
        p.append(this.basefont);
        if (cidMap != null) {
            p.append(" \n/CIDToGIDMap ");
            p.append(cidMap.referencePDF());
        }
        p.append(" \n/Subtype /");
        p.append(getPDFNameForCIDFontType(this.cidtype));
        p.append("\n");
        p.append(systemInfo.toPDFString());
        p.append("\n/FontDescriptor ");
        p.append(this.descriptor.referencePDF());

        if (cmap != null) {
            p.append("\n/ToUnicode ");
            p.append(cmap.referencePDF());
        }
        if (dw != null) {
            p.append("\n/DW ");
            p.append(this.dw);
        }
        if (w != null) {
            p.append("\n/W ");
            p.append(w.toPDFString());
        }
        if (dw2 != null) {
            p.append("\n/DW2 [");    // always two values, see p 211
            p.append(this.dw2[0]);
            p.append(this.dw2[1]);
            p.append("] \n>>\nendobj\n");
        }
        if (w2 != null) {
            p.append("\n/W2 ");
            p.append(w2.toPDFString());
            p.append(" \n>>\nendobj\n");
        }
        p.append(" \n>>\nendobj\n");
        return p.toString();
    }

}

