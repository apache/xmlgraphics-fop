/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

public class PDFT1Stream extends PDFStream {
    private int origLength;
    private int len1, len3;
    private byte[] originalData = null;

    public PDFT1Stream(int num, int len) {
        super(num);
        origLength = len;
    }

    private final static boolean byteCmp(byte[] src, int offset, byte[] cmp) {
        boolean ret = true;
        for (int i = 0; ret == true && i < cmp.length; i++) {
            // System.out.println("Compare: ");
            // System.out.println("         "+src[offset+i]+" "+cmp[i]);
            if (src[offset + i] != cmp[i])
                ret = false;
        }
        return ret;
    }

    /**
     * calculates the Length1 and Length3 PDFStream attributes for type1
     * font embedding
     */
    private void calcLengths(byte[] originalData) {
        // Calculate length 1 and 3
        // System.out.println ("Checking font, size = "+originalData.length);

        // Length1 is the size of the initial ascii portion
        // search for "currentfile eexec"
        // Get the first binary number and search backwards for "eexec"
        len1 = 30;

        byte[] eexec = (new String("currentfile eexec")).getBytes();
        // System.out.println("Length1="+len1);
        while (!byteCmp(originalData, len1 - eexec.length, eexec))
            len1++;
        // Skip newline
        len1++;

        // Length3 is length of the last portion of the file
        len3 = 0;
        byte[] cltom = (new String("cleartomark")).getBytes();
        len3 -= cltom.length;
        while (!byteCmp(originalData, origLength + len3, cltom)) {
            len3--;
            // System.out.println("Len3="+len3);
        }
        len3 = -len3;
        len3++;
        // Eat 512 zeroes
        int numZeroes = 0;
        byte[] ws1 = "\n".getBytes();
        byte[] ws2 = "\r".getBytes();
        byte[] ws3 = "0".getBytes();
        while ((originalData[origLength - len3] == ws1[0] || originalData[origLength - len3] == ws2[0] || originalData[origLength - len3] == ws3[0])
               && numZeroes < 512) {
            len3++;
            if (originalData[origLength - len3] == ws3[0])
                numZeroes++;
        }
        // System.out.println("Length3="+len3);
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
                                    + " " + "/Length1 " + len1 + " /Length2 "
                                    + (origLength - len3 - len1)
                                    + " /Length3 " + len3 + " >>\n");

        byte[] p = preData.getBytes();
        stream.write(p);
        length += p.length;

        length += outputStreamData(stream);
        p = "endobj\n".getBytes();
        stream.write(p);
        length += p.length;
        System.out.println("Embedded Type1 font");
        return length;
    }

    public void setData(byte[] data, int size) throws java.io.IOException {
        calcLengths(data);
        _data.reset();
        // System.out.println("Writing " + size + " bytes of font data");
        _data.write(data, 0, size);
    }

}
