/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
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
    public String getName() {
        return "/DCTDecode";
    }

    public String getDecodeParms() {
        return null;
    }

    public void encode(InputStream in, OutputStream out, int length) throws IOException {
        StreamUtilities.streamCopy(in, out, length);
        out.close();
    }
    
}

