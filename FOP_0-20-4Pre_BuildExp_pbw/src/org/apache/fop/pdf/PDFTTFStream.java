/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

public class PDFTTFStream extends PDFStream {
    private int origLength;

    public PDFTTFStream(int num, int len) {
        super(num);
        origLength = len;
    }

    // overload the base object method so we don't have to copy
    // byte arrays around so much
    protected int output(java.io.OutputStream stream)
            throws java.io.IOException {
        int length = 0;
        String filterEntry = applyFilters();
        String preData = new String(this.number + " " + this.generation
                                    + " obj\n<< /Length "
                                    + (_data.size() + 1) + " " + filterEntry
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

    public void setData(byte[] data, int size) throws java.io.IOException {
        _data.reset();
        System.out.println("Writing " + size + " bytes of font data");
        _data.write(data, 0, size);
    }

}
