/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Special PDFStream for ICC profiles (color profiles).
 */
public class PDFICCStream extends PDFStream {

    private ICC_Profile cp;
    private PDFDeviceColorSpace pdfColorSpace;

    /**
     * @see org.apache.fop.pdf.PDFObject#PDFObject()
     */
    public PDFICCStream() {
        super();
        cp = null;
    }

    /**
     * Sets the color space to encode in PDF.
     * @param icc the ICC profile
     * @param alt the PDF color space
     */
    public void setColorSpace(ICC_Profile icc, PDFDeviceColorSpace alt) {
        this.cp = icc;
        pdfColorSpace = alt;
    }

    /**
     * Returns the associated ICC profile. Note that this will return null once the
     * ICC stream has been written to the PDF file.
     * @return the ICC profile (or null if the stream has already been written)
     */
    public ICC_Profile getICCProfile() {
        return this.cp;
    }

    /**
     * overload the base object method so we don't have to copy
     * byte arrays around so much
     * {@inheritDoc}
     */
    @Override
    public int output(java.io.OutputStream stream)
                throws java.io.IOException {
        int length = super.output(stream);
        this.cp = null; //Free ICC stream when it's not used anymore
        return length;
    }

    /** {@inheritDoc} */
    @Override
    protected void outputRawStreamData(OutputStream out) throws IOException {
        cp.write(out);
    }

    /** {@inheritDoc} */
    @Override
    protected void populateStreamDict(Object lengthEntry) {
        put("N", cp.getNumComponents());
        if (pdfColorSpace != null) {
            put("Alternate", new PDFName(pdfColorSpace.getName()));
        }
        super.populateStreamDict(lengthEntry);
    }

}
