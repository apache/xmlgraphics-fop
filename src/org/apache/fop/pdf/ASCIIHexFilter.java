/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package org.apache.fop.pdf;

import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

/**
 * ASCII Hex filter for PDF streams.
 * This filter converts a pdf stream to ASCII hex data.
 */
public class ASCIIHexFilter extends PDFFilter {
    private static final String ASCIIHEX_EOD = ">";

    /**
     * Get the name of this filter.
     *
     * @return the name of this filter for pdf
     */
    public String getName() {
        return "/ASCIIHexDecode";
    }

    /**
     * Get the decode params.
     *
     * @return always null
     */
    public String getDecodeParms() {
        return null;
    }

    /**
     * Encode the pdf stream using this filter.
     *
     * @param in the input stream to read the data from
     * @param out the output stream to write data to
     * @param length the length of data to read from the stream
     * @throws IOException if an error occurs reading or writing to
     *                     the streams
     */
    public void encode(InputStream in, OutputStream out, int length) throws IOException {
        Writer writer = new OutputStreamWriter(out);
        for (int i = 0; i < length; i++) {
            int val = (int)(in.read() & 0xFF);
            if (val < 16) {
                writer.write("0");
            }
            writer.write(Integer.toHexString(val));
        }
        writer.write(ASCIIHEX_EOD);
        writer.close();
    }

}
