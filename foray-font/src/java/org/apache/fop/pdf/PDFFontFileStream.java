/*
 * Copyright 2004 The FOray Project.
 *      http://www.foray.org
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
 * 
 * This work is in part derived from the following work(s), used with the
 * permission of the licensor:
 *      Apache FOP, licensed by the Apache Software Foundation
 *
 */

/* $Id$ */
 
package org.apache.fop.pdf;

import org.axsl.fontR.FontConsumer;
import org.axsl.fontR.FontUse;
import org.axsl.fontR.output.FontPDF;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class PDFFontFileStream extends PDFStream {
    private FontUse font;
    private FontPDF fontOutput;

    public PDFFontFileStream(FontUse font, FontConsumer fontConsumer) {
        super();
        if (!font.getFont().isEmbeddable()) {
            // TODO vh: better error handling
            log.error("Can't create PDFFontFileStream for a Font "
                    + "that is not embeddable.");
            return;
        }
        this.font = font;
        this.fontOutput = (FontPDF)font.getFontOutput("application/pdf");
        byte[] fontFileStream = fontOutput.getContents();
        // TODO vh
//        addFilter("flate");
//        addFilter("ascii-85");
        try {
            setData(fontFileStream);
        } catch (IOException ioe) {
            log.error("Failed to embed font "
                    + font.getPostscriptName() + ": " + ioe.getMessage());
        }
    }

    protected int output(OutputStream stream) throws IOException {
        int length = 0;
        // TODO vh
//        String filterEntry = applyFilters();
        String preData
                = getObjectID() + "\n<< /Length "
                + getDataLength()
                + " "
//                + filterEntry
                + fontOutput.getPDFFontFileStreamAdditional()
                + " >>\n";

        byte[] p;
        try {
            p = preData.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = preData.getBytes();
        }

        stream.write(p);
        length += p.length;

        length += outputStreamData(data, stream);
        try {
            p = ("\nendobj\n").getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            p = ("\nendobj\n").getBytes();
        }
        stream.write(p);
        length += p.length;
        return length;
    }

}
