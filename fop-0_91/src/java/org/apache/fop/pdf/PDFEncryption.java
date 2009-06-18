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

/**
 * This interface defines the contract for classes implementing PDF encryption.
 */
public interface PDFEncryption {

    /**
     * Returns the encryption parameters.
     * @return the encryption parameters
     */
    PDFEncryptionParams getParams();

    /**
     * Sets the encryption parameters.
     * @param params The parameterss to set
     */
    void setParams(PDFEncryptionParams params);

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
     * Returns the trailer entry for encryption.
     * @return the trailer entry
     */
    String getTrailerEntry();
}
