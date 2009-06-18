/*
 * $Id$
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

import java.util.Hashtable;

/**
 * class representing a /Filter /Standard object.
 *
 */
public class PDFEncryption extends PDFObject {
    /**
     * create a /Filter /Standard object.
     *
     * @param number the object's number
     */
    public PDFEncryption(int number) {
        throw new IllegalStateException("PDF encryption not available.");
    }

    /** This method allows the setting of the user password
     * @param value The string to use as the user password. It may be blank but not null.
     */    
    public void setUserPassword(String value) {
    }

    /** Sets the owner password for the PDF
     * @param value The owner password
     */    
    public void setOwnerPassword(String value) {
    }
    
    /** Set whether the document will allow printing.
     * @param value The new permision value
     */    
    public void setAllowPrint(boolean value) {
    }

    /** Set whether the document will allow the content to be extracted
     * @param value The new permission value
     */    
    public void setAllowCopyContent(boolean value) {
    }

    /** Set whether the document will allow content editting
     * @param value The new permission value
     */    
    public void setAllowEditContent(boolean value) {
    }

    /** Set whether the document will allow annotation modificcations
     * @param value The new permission value
     */    
    public void setAllowEditAnnotation(boolean value) {
    }
    
    /** Returns the document file ID
     * @return The file ID
     */    
//    public byte [] getFileID() {
//        return null;
//    }
    
    /** This method returns the indexed file ID
     * @param index The index to access the file ID
     * @return The file ID
     */    
    public String getFileID(int index) {
        return null;
    }
        
    /** This method initializes the encryption algorithms and values
     */    
    public void init() {
    }

    /** This method encrypts the passed data using the generated keys.
     * @param data The data to be encrypted
     * @param number The block number
     * @param generation The block generation
     * @return The encrypted data
     */    
//    public byte [] encryptData(byte [] data, int number, int generation) {
//        return null;
//    }

    /** Creates PDFFilter for the encryption object
     * @param number The object number
     * @param generation The objects generation
     * @return The resulting filter
     */    
    public PDFFilter makeFilter(int number, int generation) {
        return null;
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF
     */
    public byte[] toPDF() throws IllegalStateException {
        throw new IllegalStateException("PDF Encryption not available.");
    }

    static public boolean encryptionAvailable() {
        return false;
    }
}
