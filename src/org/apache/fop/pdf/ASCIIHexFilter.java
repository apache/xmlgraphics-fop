/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ASCIIHexFilter extends PDFFilter {
    private static final String ASCIIHEX_EOD = ">";


    public String getName() {
        return "/ASCIIHexDecode";
    }

    public String getDecodeParms() {
        return null;
    }

    public byte[] encode(byte[] data) {

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int val = (int)(data[i] & 0xFF);
            if (val < 16)
                buffer.append("0");
            buffer.append(Integer.toHexString(val));
        }
        buffer.append(ASCIIHEX_EOD);

        return buffer.toString().getBytes();

    }

}
