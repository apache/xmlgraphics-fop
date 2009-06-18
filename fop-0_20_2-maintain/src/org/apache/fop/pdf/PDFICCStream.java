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

// Java
import java.io.UnsupportedEncodingException;
// Fop
import org.apache.fop.datatypes.ColorSpace;

public class PDFICCStream extends PDFStream {
    private int origLength;
    private int len1, len3;
    private byte[] originalData = null;

    private ColorSpace cs;

    public void setColorSpace(ColorSpace cs) throws java.io.IOException {
        this.cs = cs;
        setData(cs.getICCProfile());
    }

    public PDFICCStream(int num) {
        super(num);
        cs = null;
    }

    public PDFICCStream(int num, ColorSpace cs) throws java.io.IOException {
        super(num);
        setColorSpace(cs);
    }

    // overload the base object method so we don't have to copy
    // byte arrays around so much
    protected int output(java.io.OutputStream stream)
            throws java.io.IOException {
        int length = 0;
        String filterEntry = applyFilters();
        StringBuffer pb = new StringBuffer();
        pb.append(this.number).append(" ").append(this.generation).append(" obj\n<< ");
        pb.append("/N ").append(cs.getNumComponents()).append(" ");

        if (cs.getColorSpace() > 0)
            pb.append("/Alternate /").append(cs.getColorSpacePDFString()).append(" ");

        pb.append("/Length ").append((_data.size() + 1)).append(" ").append(filterEntry);
        pb.append(" >>\n");
        byte[] p;
        try {
            p = pb.toString().getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = pb.toString().getBytes();
        }       
        stream.write(p);
        length += p.length;
        length += outputStreamData(stream);
        try {
            p = "endobj\n".getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = "endobj\n".getBytes();
        }       
        stream.write(p);
        length += p.length;
        return length;
    }


}
