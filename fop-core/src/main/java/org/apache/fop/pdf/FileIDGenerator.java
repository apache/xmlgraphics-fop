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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * A class to generate the File Identifier of a PDF document (the ID entry of the file
 * trailer dictionary).
 */
abstract class FileIDGenerator {

    abstract byte[] getOriginalFileID();

    abstract byte[] getUpdatedFileID();

    private static final class RandomFileIDGenerator extends FileIDGenerator {

        private byte[] fileID;

        private RandomFileIDGenerator() {
            Random random = new Random();
            fileID = new byte[16];
            random.nextBytes(fileID);
        }

        @Override
        byte[] getOriginalFileID() {
            return fileID;
        }

        @Override
        byte[] getUpdatedFileID() {
            return fileID;
        }

    }

    private static final class DigestFileIDGenerator extends FileIDGenerator {

        private byte[] fileID;

        private final PDFDocument document;

        private final MessageDigest digest;

        DigestFileIDGenerator(PDFDocument document) throws NoSuchAlgorithmException {
            this.document = document;
            this.digest = MessageDigest.getInstance("MD5");
        }

        @Override
        byte[] getOriginalFileID() {
            if (fileID == null) {
                generateFileID();
            }
            return fileID;
        }

        @Override
        byte[] getUpdatedFileID() {
            return getOriginalFileID();
        }

        private void generateFileID() {
            DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS");
            digest.update(PDFDocument.encode(df.format(new Date())));
            // Ignoring the filename here for simplicity even though it's recommended
            // by the PDF spec
            digest.update(PDFDocument.encode(String.valueOf(document.getCurrentFileSize())));
            digest.update(document.getInfo().toPDF());
            fileID = digest.digest();
        }

    }

    /**
     * Use this method when the file ID is needed before the document is finalized. The
     * digest method recommended by the PDF Reference is based, among other things, on the
     * file size.
     *
     * @return an instance that generates a random sequence of bytes for the File
     * Identifier
     */
    static FileIDGenerator getRandomFileIDGenerator() {
        return new RandomFileIDGenerator();
    }

    /**
     * Returns an instance that generates a file ID using the digest method recommended by
     * the PDF Reference. To properly follow the Reference, the size of the document must
     * no longer change after this method is called.
     *
     * @param document the document whose File Identifier must be generated
     * @return the generator
     * @throws NoSuchAlgorithmException if the MD5 Digest algorithm is not available
     */
    static FileIDGenerator getDigestFileIDGenerator(PDFDocument document)
            throws NoSuchAlgorithmException {
        return new DigestFileIDGenerator(document);
    }
}
