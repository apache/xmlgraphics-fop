/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.IOException;

/**
 * Special PDFStream for embeddable TrueType fonts.
 */
public class PDFTTFStream extends PDFStream {
    
    private int origLength;

    /**
     * Main constructor
     * @param num PDF object number
     * @param len original length
     */
    public PDFTTFStream(int num, int len) {
        super(num);
        origLength = len;
    }

    /**
     * Overload the base object method so we don't have to copy
     * byte arrays around so much
     * @see org.apache.fop.pdf.PDFObject#output(OutputStream)
     */
    protected int output(java.io.OutputStream stream)
            throws java.io.IOException {
        int length = 0;
        String filterEntry = applyFilters();
        String preData = new String(this.number + " " + this.generation
                                    + " obj\n<< /Length "
                                    + (data.getSize() + 1) + " " + filterEntry
                                    + " " + "/Length1 " + origLength
                                    + " >>\n");

        byte[] p = preData.getBytes();
        stream.write(p);
        length += p.length;

        length += outputStreamData(stream);
        p = "endobj\n".getBytes();
        stream.write(p);
        length += p.length;
        return length;
    }

    /**
     * Sets the TrueType font data.
     * @param data the font payload
     * @param size size of the payload
     * @throws IOException in case of an I/O problem
     */
    public void setData(byte[] data, int size) throws IOException {
        this.data.reset();
        /**@todo Log using Logger */
        System.out.println("Writing " + size + " bytes of font data");
        this.data.getOutputStream().write(data, 0, size);
    }

}
