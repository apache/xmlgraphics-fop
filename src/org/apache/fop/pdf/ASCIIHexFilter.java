/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.*;

public class ASCIIHexFilter extends PDFFilter {
    private static final String ASCIIHEX_EOD = ">";


    public String getName() {
        return "/ASCIIHexDecode";
    }

    public String getDecodeParms() {
        return null;
    }

    public void encode(InputStream in, OutputStream out, int length) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        for (int i = 0; i < length; i++) {
            int val = (int)(in.read() & 0xFF);
            if (val < 16)
                writer.write("0");
            writer.write(Integer.toHexString(val));
        }
        writer.write(ASCIIHEX_EOD);
        writer.close();
    }

}
