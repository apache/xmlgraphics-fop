/*
 * $Id: PDFICCStream.java,v 1.7 2003/03/07 08:25:47 jeremias Exp $
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

import java.awt.color.ICC_Profile;

/**
 * Special PDFStream for ICC profiles (color profiles).
 */
public class PDFICCStream extends PDFStream {
    
    private int origLength;
    private int len1, len3;

    private ICC_Profile cp;
    private PDFColorSpace pdfColorSpace;

    /**
     * @see org.apache.fop.pdf.PDFObject#PDFObject(int)
     */
    public PDFICCStream(int num) {
        super(num);
        cp = null;
    }

    /**
     * Sets the color space to encode in PDF.
     * @param cp the ICC profile
     * @param alt the PDF color space
     */
    public void setColorSpace(ICC_Profile cp, PDFColorSpace alt) {
        this.cp = cp;
        pdfColorSpace = alt;
    }

    /**
     * overload the base object method so we don't have to copy
     * byte arrays around so much
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(java.io.OutputStream stream)
        throws java.io.IOException {

        setData(cp.getData());

        int length = 0;
        String filterEntry = applyFilters();
        StringBuffer pb = new StringBuffer();
        pb.append(this.number).append(" ").append(this.generation).append(" obj\n<< ");
        pb.append("/N ").append(cp.getNumComponents()).append(" ");

        if (pdfColorSpace != null) {
            pb.append("/Alternate /").append(pdfColorSpace.getColorSpacePDFString()).append(" ");
        }

        pb.append("/Length ").append((data.getSize() + 1)).append(" ").append(filterEntry);
        pb.append(" >>\n");
        byte[] p = pb.toString().getBytes();
        stream.write(p);
        length += p.length;
        length += outputStreamData(stream);
        p = "endobj\n".getBytes();
        stream.write(p);
        length += p.length;
        return length;
    }
}
