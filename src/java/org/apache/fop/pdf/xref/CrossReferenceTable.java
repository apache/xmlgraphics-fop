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

package org.apache.fop.pdf.xref;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;

/**
 * A cross-reference table, as described in Section 3.4.3 of the PDF 1.5 Reference.
 */
public class CrossReferenceTable extends CrossReferenceObject {

    private final List<Long> objectReferences;

    private final StringBuilder pdf = new StringBuilder(256);

    public CrossReferenceTable(TrailerDictionary trailerDictionary, long startxref,
            List<Long> location) {
        super(trailerDictionary, startxref);
        this.objectReferences = location;
    }

    public void output(OutputStream stream) throws IOException {
        outputXref();
        writeTrailer(stream);
    }

    private void outputXref() throws IOException {
        pdf.append("xref\n0 ");
        pdf.append(objectReferences.size() + 1);
        pdf.append("\n0000000000 65535 f \n");
        for (Long objectReference : objectReferences) {
            final String padding = "0000000000";
            String s = String.valueOf(objectReference);
            if (s.length() > 10) {
                throw new IOException("PDF file too large."
                        + " PDF 1.4 cannot grow beyond approx. 9.3GB.");
            }
            String loc = padding.substring(s.length()) + s;
            pdf.append(loc).append(" 00000 n \n");
        }
    }

    private void writeTrailer(OutputStream stream) throws IOException {
        pdf.append("trailer\n");
        stream.write(PDFDocument.encode(pdf.toString()));
        PDFDictionary dictionary = trailerDictionary.getDictionary();
        dictionary.put("/Size", objectReferences.size() + 1);
        dictionary.output(stream);
    }

}
