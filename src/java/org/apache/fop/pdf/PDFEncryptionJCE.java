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

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
            log.debug("new encryption filter for number "
                + number + " and generation " + generation);
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
        public PDFObject getDecodeParms() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public OutputStream applyFilter(OutputStream out) throws IOException {
            return new CipherOutputStream(out,
                    encryption.initCipher(number, generation));
        }

    }

    private static final char [] PAD
                             = {0x28, 0xBF, 0x4E, 0x5E, 0x4E, 0x75, 0x8A, 0x41,
                                0x64, 0x00, 0x4E, 0x56, 0xFF, 0xFA, 0x01, 0x08,
                                0x2E, 0x2E, 0x00, 0xB6, 0xD0, 0x68, 0x3E, 0x80,
                                0x2F, 0x0C, 0xA9, 0xFE, 0x64, 0x53, 0x69, 0x7A};

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
    //private Cipher cipher = null;
    private Random random = new Random();
    // Control attributes
    private PDFEncryptionParams params;
    // Output attributes
    private byte[] fileID = null;
    private byte[] encryptionKey = null;
    private String dictionary = null;

    /**
     * Create a /Filter /Standard object.
     *
     * @param objnum the object's number
     */
    public PDFEncryptionJCE(int objnum) {
        /* generic creation of object */
        super();
        setObjectNumber(objnum);
        try {
            digest = MessageDigest.getInstance("MD5");
            //cipher = Cipher.getInstance("RC4");
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        /*} catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e.getMessage());*/
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
            return PDFText.toHex(getFileID());
        }

        byte[] id = new byte[16];
        random.nextBytes(id);
        return PDFText.toHex(id);
    }

    private byte[] encryptWithKey(byte[] data, byte[] key) {
        try {
            final Cipher c = initCipher(key);
            return c.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private Cipher initCipher(byte[] key) {
        try {
            Cipher c = Cipher.getInstance("RC4");
            SecretKeySpec keyspec = new SecretKeySpec(key, "RC4");
            c.init(Cipher.ENCRYPT_MODE, keyspec);
            return c;
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    private Cipher initCipher(int number, int generation) {
        byte[] hash = calcHash(number, generation);
        int size = hash.length;
        hash = digest.digest(hash);
        byte[] key = calcKey(hash, size);
        return initCipher(key);
    }

    private byte[] encryptWithHash(byte[] data, byte[] hash, int size) {
        hash = digest.digest(hash);

        byte[] key = calcKey(hash, size);

        return encryptWithKey(data, key);
    }

    private byte[] calcKey(byte[] hash, int size) {
        byte[] key = new byte[size];

        for (int i = 0; i < size; i++) {
            key[i] = hash[i];
        }
        return key;
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
        this.dictionary = getObjectID()
                        + "<< /Filter /Standard\n"
                        + "/V 1\n"
                        + "/R 2\n"
                        + "/Length 40\n"
                        + "/P "  + permissions + "\n"
                        + "/O " + PDFText.toHex(oValue) + "\n"
                        + "/U " + PDFText.toHex(uValue) + "\n"
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
        byte[] hash = calcHash(number, generation);
        return encryptWithHash(data, hash, hash.length);
    }

    /** {@inheritDoc} */
    public byte[] encrypt(byte[] data, PDFObject refObj) {
        PDFObject o = refObj;
        while (o != null && !o.hasObjectNumber()) {
            o = o.getParent();
        }
        if (o == null) {
            throw new IllegalStateException("No object number could be obtained for a PDF object");
        }
        return encryptData(data, o.getObjectNumber(), o.getGeneration());
    }

    private byte[] calcHash(int number, int generation) {
        byte[] hash = new byte[this.encryptionKey.length + 5];

        int i = 0;
        while (i < this.encryptionKey.length) {
            hash[i] = this.encryptionKey[i]; i++;
        }

        hash[i++] = (byte) (number >>> 0);
        hash[i++] = (byte) (number >>> 8);
        hash[i++] = (byte) (number >>> 16);
        hash[i++] = (byte) (generation >>> 0);
        hash[i++] = (byte) (generation >>> 8);
        return hash;
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
    public void applyFilter(AbstractPDFStream stream) {
        stream.getFilterList().addFilter(
                this.makeFilter(stream.getObjectNumber(), stream.getGeneration()));
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

        return encode(this.dictionary);
    }

    /**
     * {@inheritDoc}
     */
    public String getTrailerEntry() {
        return "/Encrypt " + getObjectNumber() + " "
                    + getGeneration() + " R\n"
                    + "/ID[" + getFileID(1) + getFileID(2) + "]\n";
    }
}
