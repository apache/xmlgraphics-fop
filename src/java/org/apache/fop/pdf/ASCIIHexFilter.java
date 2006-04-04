/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

import java.io.OutputStream;
import java.io.IOException;

import org.apache.xmlgraphics.util.io.ASCIIHexOutputStream;

/**
 * ASCII Hex filter for PDF streams.
 * This filter converts a pdf stream to ASCII hex data.
 */
public class ASCIIHexFilter extends PDFFilter {
    
    /**
     * Get the name of this filter.
     *
     * @return the name of this filter for pdf
     */
    public String getName() {
        return "/ASCIIHexDecode";
    }

    /**
     * @see org.apache.fop.pdf.PDFFilter#isASCIIFilter()
     */
    public boolean isASCIIFilter() {
        return true;
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
     * @see org.apache.fop.pdf.PDFFilter#applyFilter(OutputStream)
     */
    public OutputStream applyFilter(OutputStream out) throws IOException {
        return new ASCIIHexOutputStream(out);
    }

}
