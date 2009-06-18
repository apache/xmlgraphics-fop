/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;
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
