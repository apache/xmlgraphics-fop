/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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
                + " obj\n<< /Length " + pfb.getLength() + " " 
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
