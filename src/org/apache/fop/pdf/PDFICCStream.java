/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.awt.color.ICC_Profile;

public class PDFICCStream extends PDFStream {
    private int origLength;
    private int len1, len3;

    private ICC_Profile cp;
    private PDFColorSpace pdfColorSpace;

    public void setColorSpace(ICC_Profile cp, PDFColorSpace alt) {
        this.cp = cp;
        pdfColorSpace = alt;
    }

    public PDFICCStream(int num) {
        super(num);
        cp = null;
    }

        // overload the base object method so we don't have to copy
        // byte arrays around so much
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

        pb.append("/Length ").append((_data.getSize() + 1)).append(" ").append(filterEntry);
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
