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

// FOP
import org.apache.fop.fonts.type1.PFBData;

public class PDFT1Stream extends PDFStream {
    
    private PFBData pfb;

    public PDFT1Stream(int num) {
        super(num);
    }


    // overload the base object method so we don't have to copy
    // byte arrays around so much
    protected int output(java.io.OutputStream stream)
            throws java.io.IOException {
        if (pfb == null) throw new NullPointerException("pfb must not be null at this point");
        int length = 0;
        String filterEntry = applyFilters();
        String preData = this.number + " " + this.generation
                + " obj\n<< /Length " + getDataLength() + " " 
                + filterEntry  
                + " /Length1 " + pfb.getLength1()
                + " /Length2 " + pfb.getLength2()
                + " /Length3 " + pfb.getLength3() + " >>\n";

        byte[] p;
        try {
            p = preData.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = preData.getBytes();
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
        //System.out.println("Embedded Type1 font");
        return length;
    }

    public void setData(PFBData pfb) throws java.io.IOException {
        _data.reset();
        // System.out.println("Writing " + size + " bytes of font data");
        this.pfb = pfb;
        pfb.outputAllParts(_data);
    }

}
