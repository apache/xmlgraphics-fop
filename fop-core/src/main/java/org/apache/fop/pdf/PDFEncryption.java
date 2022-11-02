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

/**
 * This interface defines the contract for classes implementing PDF encryption.
 */
public interface PDFEncryption {

    /**
     * Adds a PDFFilter to the PDFStream object
     * @param stream the stream to add an encryption filter to
     */
    void applyFilter(AbstractPDFStream stream);

    /**
     * Encrypt an array of bytes using a reference PDFObject for calculating
     * the encryption key.
     * @param data data to encrypt
     * @param refObj reference PDFObject
     * @return byte[] the encrypted data
     */
    byte[] encrypt(byte[] data, PDFObject refObj);

    /**
     * Returns the /Encrypt entry in the file trailer dictionary.
     *
     * @return the string "/Encrypt n g R\n" where n and g are the number and generation
     * of the document's encryption dictionary
     */
    String getTrailerEntry();

    /**
     * Returns the PDF version required by the current encryption algorithm.
     * @return the PDF Version
     */
    Version getPDFVersion();
}
