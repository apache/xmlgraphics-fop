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

import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEncryption;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFRoot;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFWritable;

/**
 * A data class representing entries of the file trailer dictionary.
 */
public class TrailerDictionary {

    private final PDFDictionary dictionary;

    public TrailerDictionary(PDFDocument pdfDocument) {
        this.dictionary = new PDFDictionary();
        this.dictionary.setDocument(pdfDocument);
    }

    /** Sets the value of the Root entry. */
    public TrailerDictionary setRoot(PDFRoot root) {
        dictionary.put("/Root", root);
        return this;
    }

    /** Sets the value of the Info entry. */
    public TrailerDictionary setInfo(PDFInfo info) {
        dictionary.put("/Info", info);
        return this;
    }

    /** Sets the value of the Encrypt entry. */
    public TrailerDictionary setEncryption(PDFEncryption encryption) {
        dictionary.put("/Encrypt", encryption);
        return this;
    }

    /** Sets the value of the ID entry. */
    public TrailerDictionary setFileID(byte[] originalFileID, byte[] updatedFileID) {
        // TODO this is ugly! Used to circumvent the fact that the file ID will be
        // encrypted if directly stored as a byte array
        class FileID implements PDFWritable {

            private final byte[] fileID;

            FileID(byte[] id) {
                fileID = id;
            }

            public void outputInline(OutputStream out, StringBuilder textBuffer)
                    throws IOException {
                PDFDocument.flushTextBuffer(textBuffer, out);
                String hex = PDFText.toHex(fileID, true);
                byte[] encoded = hex.getBytes("US-ASCII");
                out.write(encoded);
            }

        }
        PDFArray fileID = new PDFArray(new FileID(originalFileID), new FileID(updatedFileID));
        dictionary.put("/ID", fileID);
        return this;
    }

    PDFDictionary getDictionary() {
        return dictionary;
    }

}
