/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * CCF Filter class. Right now it is just used as a dummy filter flag so
 * we can write TIFF images to the PDF. The encode method just returns the
 * data passed to it. In the future an actual CCITT Group 4 compression should be
 * added to the encode method so other images can be compressed.
 *
 * @author Manuel Mall
 */
public class CCFFilter extends PDFFilter {

    private String m_decodeParms;

    public String getName() {
        return "/CCITTFaxDecode";
    }

    public String getDecodeParms() {
        return this.m_decodeParms;
    }

    public void setDecodeParms(String decodeParms) {
        this.m_decodeParms = decodeParms;
    }

    public byte[] encode(byte[] data) {
        return data;
    }
}

