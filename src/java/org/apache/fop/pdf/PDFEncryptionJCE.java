/*
 * $Id: PDFEncryptionJCE.java,v 1.1.2.1 2003/03/05 18:58:15 pietsch Exp $
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

// Java
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.BadPaddingException;
import javax.crypto.NoSuchPaddingException;

import java.util.Random;

/**
 * class representing a /Filter /Standard object.
 *
 */
public class PDFEncryptionJCE extends PDFObject implements PDFEncryption {
    
    private class EncryptionFilter extends PDFFilter {
        private PDFEncryptionJCE encryption;
        private int number;
        private int generation;

        /** 
         * The constructor for the internal PDFEncryptionJCE filter
         * @param encryption The encryption object to use
         * @param number The number of the object to be encrypted
         * @param generation The generation of the object to be encrypted
         */        
        public EncryptionFilter(PDFEncryptionJCE encryption,
                                int number, int generation) {
            super();
            this.encryption = encryption;
            this.number  = number;
            this.generation = generation;
        }

        /** 
         * Return a PDF string representation of the filter. In this
         * case no filter name is passed.
         * @return The filter name, blank in this case
         */
        public String getName() {
            return "";
        }

        /** 
         * Return a parameter dictionary for this filter, or null
         * @return The parameter dictionary. In this case, null.
         */
        public String getDecodeParms() {
            return null;
        }

        /** 
         * Encode the given data with the filter
         * @param data The data to be encrypted
         * @return The encrypted data
         */
        public byte[] encode(byte[] data) {
            return encryption.encryptData(data, number, generation);
        }
        
        /**
         * @see org.apache.fop.pdf.PDFFilter#encode(InputStream, OutputStream, int)
         */
        public void encode(InputStream in, OutputStream out, int length) 
                                                        throws IOException {
            byte[] buffer = new byte[length];
            in.read(buffer);
            buffer = encode(buffer);
            out.write(buffer);
        }
        
    }

    private static final char [] PAD = 
                               { 0x28, 0xBF, 0x4E, 0x5E, 0x4E, 0x75, 0x8A, 0x41,
                                 0x64, 0x00, 0x4E, 0x56, 0xFF, 0xFA, 0x01, 0x08, 
                                 0x2E, 0x2E, 0x00, 0xB6, 0xD0, 0x68, 0x3E, 0x80, 
                                 0x2F, 0x0C, 0xA9, 0xFE, 0x64, 0x53, 0x69, 0x7A };
    private static final char[] DIGITS = 
                                 {'0', '1', '2', '3', '4', '5', '6', '7',
                                  '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
                                  
    /** Value of PRINT permission */                                  
    public static final int PERMISSION_PRINT            =  4;
    /** Value of content editting permission */    
    public static final int PERMISSION_EDIT_CONTENT     =  8;
    /** Value of content extraction permission */    
    public static final int PERMISSION_COPY_CONTENT     = 16;
    /** Value of annotation editting permission */    
    public static final int PERMISSION_EDIT_ANNOTATIONS = 32;
    
    // Encryption tools
    private MessageDigest digest = null;
    private Cipher cipher = null;
    private Random random = new Random();
    // Control attributes
    private PDFEncryptionParams params;
    // Output attributes
    private byte[] fileID = null;
    private byte[] encryptionKey = null;
    private String dictionary = null;

    /**
     * create a /Filter /Standard object.
     *
     * @param number the object's number
     */
    public PDFEncryptionJCE(int number) {
        /* generic creation of object */
        super(number);
        try {
            digest = MessageDigest.getInstance("MD5");
            cipher = Cipher.getInstance("RC4");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    /**
     * Local factory method.
     * @param objnum PDF object number for the encryption object
     * @param params PDF encryption parameters
     * @return PDFEncryption the newly created PDFEncryption object
     */
    public static PDFEncryption make(int objnum, PDFEncryptionParams params) {
        PDFEncryptionJCE impl = new PDFEncryptionJCE(objnum);
        impl.setParams(params);
        impl.init();
        return impl;
    }


    /**
     * Returns the encryption parameters.
     * @return the encryption parameters
     */
    public PDFEncryptionParams getParams() {
        return this.params;
    }

    /**
     * Sets the encryption parameters.
     * @param params The parameterss to set
     */
    public void setParams(PDFEncryptionParams params) {
        this.params = params;
    }

    // Internal procedures
    
    private byte[] prepPassword(String password) {
        byte[] obuffer = new byte[32];
        byte[] pbuffer = password.getBytes();

        int i = 0;
        int j = 0;
        
        while (i < obuffer.length && i < pbuffer.length) {
            obuffer[i] = pbuffer[i];
            i++;
        }
        while (i < obuffer.length) {
            obuffer[i++] = (byte) PAD[j++];
        }

        return obuffer;
    }

    private String toHex(byte[] value) {
        StringBuffer buffer = new StringBuffer();
        
        for (int i = 0; i < value.length; i++) {
            buffer.append(DIGITS[(value[i] >>> 4) & 0x0F]);
            buffer.append(DIGITS[value[i] & 0x0F]);
        }
        
        return buffer.toString();
    }
    
    /** 
     * Returns the document file ID
     * @return The file ID
     */    
    public byte[] getFileID() {
        if (fileID == null) {
            fileID = new byte[16];
            random.nextBytes(fileID);
        }
        
        return fileID;
    }
    
    /** 
     * This method returns the indexed file ID
     * @param index The index to access the file ID
     * @return The file ID
     */    
    public String getFileID(int index) {
        if (index == 1) {
            return toHex(getFileID());
        }
        
        byte[] id = new byte[16];
        random.nextBytes(id);
        return toHex(id);
    }
        
    private byte[] encryptWithKey(byte[] data, byte[] key) {
        try {
            SecretKeySpec keyspec = new SecretKeySpec(key, "RC4");
            cipher.init(Cipher.ENCRYPT_MODE, keyspec);
            return cipher.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    private byte[] encryptWithHash(byte[] data, byte[] hash, int size) {
        hash = digest.digest(hash);
        
        byte[] key = new byte[size];

        for (int i = 0; i < size; i++) {
            key[i] = hash[i];
        }
        
        return encryptWithKey(data, key);
    }

    /** 
     * This method initializes the encryption algorithms and values
     */    
    public void init() {
        // Generate the owner value
        byte[] oValue;
        if (params.getOwnerPassword().length() > 0) {
            oValue = encryptWithHash(
                    prepPassword(params.getUserPassword()), 
                    prepPassword(params.getOwnerPassword()), 5);
        } else {
            oValue = encryptWithHash(
                    prepPassword(params.getUserPassword()), 
                    prepPassword(params.getUserPassword()), 5);
        }

        // Generate permissions value
        int permissions = -4;

        if (!params.isAllowPrint()) {
            permissions -= PERMISSION_PRINT;
        }
        if (!params.isAllowCopyContent()) {
            permissions -= PERMISSION_COPY_CONTENT;
        }
        if (!params.isAllowEditContent()) {
            permissions -= PERMISSION_EDIT_CONTENT;
        }
        if (!params.isAllowEditAnnotations()) {
            permissions -= PERMISSION_EDIT_ANNOTATIONS;
        }

        // Create the encrption key
        digest.update(prepPassword(params.getUserPassword()));
        digest.update(oValue);
        digest.update((byte) (permissions >>> 0));
        digest.update((byte) (permissions >>> 8));
        digest.update((byte) (permissions >>> 16));
        digest.update((byte) (permissions >>> 24));
        digest.update(getFileID());

        byte [] hash = digest.digest();
        this.encryptionKey = new byte[5];

        for (int i = 0; i < 5; i++) {
            this.encryptionKey[i] = hash[i];
        }
        
        // Create the user value
        byte[] uValue = encryptWithKey(prepPassword(""), this.encryptionKey);
        
        // Create the dictionary
        this.dictionary = this.number + " " + this.generation
                        + " obj\n<< /Filter /Standard\n"
                        + "/V 1\n"
                        + "/R 2\n"
                        + "/Length 40\n"
                        + "/P "  + permissions + "\n"
                        + "/O <" + toHex(oValue) + ">\n"
                        + "/U <" + toHex(uValue) + ">\n"
                        + ">>\n"
                        + "endobj\n";
    }

    /** 
     * This method encrypts the passed data using the generated keys.
     * @param data The data to be encrypted
     * @param number The block number
     * @param generation The block generation
     * @return The encrypted data
     */    
    public byte[] encryptData(byte[] data, int number, int generation) {
        if (this.encryptionKey == null) {
            throw new IllegalStateException("PDF Encryption has not been initialized");
        }
        
        byte[] hash = new byte[this.encryptionKey.length + 5];
            
        int i = 0;
            
        while (i < this.encryptionKey.length) {
            hash[i] = this.encryptionKey[i]; i++;
        }
            
        hash[i++] = (byte) (number >>> 0);
        hash[i++] = (byte) (number >>> 8);
        hash[i++] = (byte) (number >>> 16);
        hash[i++] = (byte) (generation >>> 0);
        hash[i++] = (byte) (generation >>> 8);;
        
        return encryptWithHash(data, hash, hash.length);
    }

    /** 
     * Creates PDFFilter for the encryption object
     * @param number The object number
     * @param generation The objects generation
     * @return The resulting filter
     */    
    public PDFFilter makeFilter(int number, int generation) {
        return new EncryptionFilter(this, number, generation);
    }

    /**
     * Adds a PDFFilter to the PDFStream object
     * @param stream the stream to add an encryption filter to
     */    
    public void applyFilter(PDFStream stream) {
        stream.addFilter(this.makeFilter(stream.number, stream.generation));
    }
    
    /**
     * Represent the object in PDF
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        if (this.dictionary == null) {
            throw new IllegalStateException("PDF Encryption has not been initialized");
        }
        
        try {
            return this.dictionary.getBytes(PDFDocument.ENCODING);
        } catch (UnsupportedEncodingException ue) {
            return this.dictionary.getBytes();
        }       
    }

    /**
     * @see org.apache.fop.pdf.PDFEncryption#getTrailerEntry()
     */
    public String getTrailerEntry() {
        return "/Encrypt " + number + " " 
                    + generation + " R\n"
                    + "/ID[<" + getFileID(1) + "><"
                    + getFileID(2) + ">]\n";
    }
}
