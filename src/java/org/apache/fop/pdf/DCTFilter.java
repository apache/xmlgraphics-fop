/*
 * $Id: DCTFilter.java,v 1.6 2003/03/07 08:25:47 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
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

    /**
     * @see org.apache.fop.pdf.PDFFilter#applyFilter(OutputStream)
     */
    public OutputStream applyFilter(OutputStream out) throws IOException {
        return out;
        //No active filtering, OutputStream is already expected to be DCT encoded
    }

}

