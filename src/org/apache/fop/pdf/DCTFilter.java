/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import org.apache.fop.util.StreamUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DCT Filter class. Right now it is just used as a dummy filter flag so
 * we can write JPG images to the PDF. The encode method just returns the
 * data passed to it. In the future an actual JPEG compression should be
 * added to the encode method so other images can be compressed.
 *
 * @author Eric Dalquist
 */
public class DCTFilter extends PDFFilter {

    /**
     * Get filter name.
     * @return the pdf name for the DCT filter
     */
    public String getName() {
        return "/DCTDecode";
    }

    /**
     * Get the decode params for this filter.
     * @return the DCT filter has no decode params
     */
    public String getDecodeParms() {
        return null;
    }

    /**
     * Encode a stream with this filter.
     * Currently no encoding is performed, it is assumed that the data
     * is already encoded.
     * @param in the input data stream
     * @param out the output stream
     * @param length the length of the data
     * @throws IOException if there is an io error
     */
    public void encode(InputStream in, OutputStream out, int length) throws IOException {
        StreamUtilities.streamCopy(in, out, length);
        out.close();
    }

}

