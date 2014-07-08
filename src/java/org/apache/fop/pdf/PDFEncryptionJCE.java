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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An implementation of the Standard Security Handler.
 */
public final class PDFEncryptionJCE extends PDFObject implements PDFEncryption {

    private final MessageDigest digest;

    private SecureRandom random;

    private byte[] encryptionKey;

    private String encryptionDictionary;

    private boolean useAlgorithm31a;

    private boolean encryptMetadata = true;

    private Version pdfVersion = Version.V1_4;

    private static byte[] ivZero = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private class EncryptionInitializer {

        private final PDFEncryptionParams encryptionParams;

        private int encryptionLength;

        private int version;

        private int revision;

        EncryptionInitializer(PDFEncryptionParams params) {
            this.encryptionParams = new PDFEncryptionParams(params);
        }

        void init() {
            encryptionLength = encryptionParams.getEncryptionLengthInBits();
            determineEncryptionAlgorithm();
            int permissions = Permission.computePermissions(encryptionParams);
            EncryptionSettings encryptionSettings = new EncryptionSettings(
                    encryptionLength, permissions,
                    encryptionParams.getUserPassword(), encryptionParams.getOwnerPassword(),
                    encryptionParams.encryptMetadata());
            InitializationEngine initializationEngine = createEngine(encryptionSettings);
            initializationEngine.run();
            encryptionDictionary = createEncryptionDictionary(permissions, initializationEngine);
            encryptMetadata = encryptionParams.encryptMetadata();
        }

        private InitializationEngine createEngine(EncryptionSettings encryptionSettings) {
            if (revision == 5) {
                return new Rev5Engine(encryptionSettings);
            } else if (revision == 2) {
                return new Rev2Engine(encryptionSettings);
            } else {
                return new Rev3Engine(encryptionSettings);
            }
        }

        private void determineEncryptionAlgorithm() {
            if (isVersion5Revision5Algorithm()) {
                version = 5;
                revision = 5;
                pdfVersion = Version.V1_7;
            } else if (isVersion1Revision2Algorithm()) {
                version = 1;
                revision = 2;
            } else {
                version = 2;
                revision = 3;
            }
        }

        private boolean isVersion1Revision2Algorithm() {
            return encryptionLength == 40
                    && encryptionParams.isAllowFillInForms()
                    && encryptionParams.isAllowAccessContent()
                    && encryptionParams.isAllowAssembleDocument()
                    && encryptionParams.isAllowPrintHq();
        }

        private boolean isVersion5Revision5Algorithm() {
            return encryptionLength == 256;
        }

        private String createEncryptionDictionary(final int permissions, InitializationEngine engine) {
            String encryptionDict = "<<\n"
                    + "/Filter /Standard\n"
                    + "/V " + version + "\n"
                    + "/R " + revision + "\n"
                    + "/Length " + encryptionLength + "\n"
                    + "/P " + permissions + "\n"
                    + engine.getEncryptionDictionaryPart()
                    + ">>";
            return encryptionDict;
        }

    }

    private static enum Permission {

        PRINT(3),
        EDIT_CONTENT(4),
        COPY_CONTENT(5),
        EDIT_ANNOTATIONS(6),
        FILL_IN_FORMS(9),
        ACCESS_CONTENT(10),
        ASSEMBLE_DOCUMENT(11),
        PRINT_HQ(12);

        private final int mask;

        /**
         * Creates a new permission.
         *
         * @param bit bit position for this permission, 1-based to match the PDF Reference
         */
        private Permission(int bit) {
            mask = 1 << (bit - 1);
        }

        private int removeFrom(int permissions) {
            return permissions - mask;
        }

        static int computePermissions(PDFEncryptionParams encryptionParams) {
            int permissions = -4;

            if (!encryptionParams.isAllowPrint()) {
                permissions = PRINT.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowCopyContent()) {
                permissions = COPY_CONTENT.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowEditContent()) {
                permissions = EDIT_CONTENT.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowEditAnnotations()) {
                permissions = EDIT_ANNOTATIONS.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowFillInForms()) {
                permissions = FILL_IN_FORMS.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowAccessContent()) {
                permissions = ACCESS_CONTENT.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowAssembleDocument()) {
                permissions = ASSEMBLE_DOCUMENT.removeFrom(permissions);
            }
            if (!encryptionParams.isAllowPrintHq()) {
                permissions = PRINT_HQ.removeFrom(permissions);
            }
            return permissions;
        }
    }

    private static final class EncryptionSettings {

        final int encryptionLength;

        final int permissions;

        final String userPassword;

        final String ownerPassword;

        final boolean encryptMetadata;

        EncryptionSettings(int encryptionLength, int permissions,
                String userPassword, String ownerPassword, boolean encryptMetadata) {
            this.encryptionLength = encryptionLength;
            this.permissions = permissions;
            this.userPassword = userPassword;
            this.ownerPassword = ownerPassword;
            this.encryptMetadata = encryptMetadata;
        }

    }

    private abstract class InitializationEngine {

        protected final int encryptionLengthInBytes;

        protected final int permissions;

        private final String userPassword;

        private final String ownerPassword;

        protected byte[] oValue;

        protected byte[] uValue;

        protected byte[] preparedUserPassword;

        protected byte[] preparedOwnerPassword;

        InitializationEngine(EncryptionSettings encryptionSettings) {
            this.encryptionLengthInBytes = encryptionSettings.encryptionLength / 8;
            this.permissions = encryptionSettings.permissions;
            this.userPassword = encryptionSettings.userPassword;
            this.ownerPassword = encryptionSettings.ownerPassword;
        }

        void run() {
            preparedUserPassword = preparePassword(userPassword);
            if (ownerPassword == null || ownerPassword.length() == 0) {
                preparedOwnerPassword = preparedUserPassword;
            } else {
                preparedOwnerPassword = preparePassword(ownerPassword);
            }
        }

        protected String getEncryptionDictionaryPart() {
            String encryptionDictionaryPart = "/O " + PDFText.toHex(oValue) + "\n"
                    + "/U " + PDFText.toHex(uValue) + "\n";
            return encryptionDictionaryPart;
        }

        protected abstract void computeOValue();

        protected abstract void computeUValue();

        protected abstract void createEncryptionKey();

        protected abstract byte[] preparePassword(String password);
    }

    private abstract class RevBefore5Engine extends InitializationEngine {

        /** Padding for passwords. */
        protected final byte[] padding = new byte[] {(byte) 0x28, (byte) 0xBF, (byte) 0x4E, (byte) 0x5E,
                (byte) 0x4E, (byte) 0x75, (byte) 0x8A, (byte) 0x41, (byte) 0x64, (byte) 0x00, (byte) 0x4E,
                (byte) 0x56, (byte) 0xFF, (byte) 0xFA, (byte) 0x01, (byte) 0x08, (byte) 0x2E, (byte) 0x2E,
                (byte) 0x00, (byte) 0xB6, (byte) 0xD0, (byte) 0x68, (byte) 0x3E, (byte) 0x80, (byte) 0x2F,
                (byte) 0x0C, (byte) 0xA9, (byte) 0xFE, (byte) 0x64, (byte) 0x53, (byte) 0x69, (byte) 0x7A};

        RevBefore5Engine(EncryptionSettings encryptionSettings) {
            super(encryptionSettings);
        }

        /**
         * Applies Algorithm 3.3 Page 79 of the PDF 1.4 Reference.
         *
         */
        protected void computeOValue() {
            // Step 1
            byte[] md5Input = preparedOwnerPassword;
            // Step 2
            digest.reset();
            byte[] hash = digest.digest(md5Input);
            // Step 3
            hash = computeOValueStep3(hash);
            // Step 4
            byte[] key = new byte[encryptionLengthInBytes];
            System.arraycopy(hash, 0, key, 0, encryptionLengthInBytes);
            // Steps 5, 6
            byte[] encryptionResult = encryptWithKey(key, preparedUserPassword);
            // Step 7
            oValue = computeOValueStep7(key, encryptionResult);
        }

        /**
         * Applies Algorithm 3.2 Page 78 of the PDF 1.4 Reference.
         */
        protected void createEncryptionKey() {
            // Steps 1, 2
            digest.reset();
            digest.update(preparedUserPassword);
            // Step 3
            digest.update(oValue);
            // Step 4
            digest.update((byte) (permissions >>> 0));
            digest.update((byte) (permissions >>> 8));
            digest.update((byte) (permissions >>> 16));
            digest.update((byte) (permissions >>> 24));
            // Step 5
            digest.update(getDocumentSafely().getFileIDGenerator().getOriginalFileID());
            byte[] hash = digest.digest();
            // Step 6
            hash = createEncryptionKeyStep6(hash);
            // Step 7
            encryptionKey = new byte[encryptionLengthInBytes];
            System.arraycopy(hash, 0, encryptionKey, 0, encryptionLengthInBytes);
        }

        /**
         * Adds padding to the password as directed in page 78 of the PDF 1.4 Reference.
         *
         * @param password the password
         * @return the password with additional padding if necessary
         */
        protected byte[] preparePassword(String password) {
            int finalLength = 32;
            byte[] preparedPassword = new byte[finalLength];
            try {
                byte[] passwordBytes = password.getBytes("UTF-8");
                if (passwordBytes.length >= finalLength) {
                    System.arraycopy(passwordBytes, 0, preparedPassword, 0, finalLength);
                } else {
                    System.arraycopy(passwordBytes, 0, preparedPassword, 0, passwordBytes.length);
                    System.arraycopy(padding, 0, preparedPassword, passwordBytes.length, finalLength
                            - passwordBytes.length);
                }
                return preparedPassword;
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e);
            }
        }

        void run() {
            super.run();
            computeOValue();
            createEncryptionKey();
            computeUValue();
        }

        protected abstract byte[] computeOValueStep3(byte[] hash);

        protected abstract byte[] computeOValueStep7(byte[] key, byte[] encryptionResult);

        protected abstract byte[] createEncryptionKeyStep6(byte[] hash);

    }

    private class Rev2Engine extends RevBefore5Engine {

        Rev2Engine(EncryptionSettings encryptionSettings) {
            super(encryptionSettings);
        }

        @Override
        protected byte[] computeOValueStep3(byte[] hash) {
            return hash;
        }

        @Override
        protected byte[] computeOValueStep7(byte[] key, byte[] encryptionResult) {
            return encryptionResult;
        }

        @Override
        protected byte[] createEncryptionKeyStep6(byte[] hash) {
            return hash;
        }

        @Override
        protected void computeUValue() {
            uValue = encryptWithKey(encryptionKey, padding);
        }

    }

    private class Rev3Engine extends RevBefore5Engine {

        Rev3Engine(EncryptionSettings encryptionSettings) {
            super(encryptionSettings);
        }

        @Override
        protected byte[] computeOValueStep3(byte[] hash) {
            for (int i = 0; i < 50; i++) {
                hash = digest.digest(hash);
            }
            return hash;
        }

        @Override
        protected byte[] computeOValueStep7(byte[] key, byte[] encryptionResult) {
            return xorKeyAndEncrypt19Times(key, encryptionResult);
        }

        @Override
        protected byte[] createEncryptionKeyStep6(byte[] hash) {
            for (int i = 0; i < 50; i++) {
                digest.update(hash, 0, encryptionLengthInBytes);
                hash = digest.digest();
            }
            return hash;
        }

        @Override
        protected void computeUValue() {
            // Step 1 is encryptionKey
            // Step 2
            digest.reset();
            digest.update(padding);
            // Step 3
            digest.update(getDocumentSafely().getFileIDGenerator().getOriginalFileID());
            // Step 4
            byte[] encryptionResult = encryptWithKey(encryptionKey, digest.digest());
            // Step 5
            encryptionResult = xorKeyAndEncrypt19Times(encryptionKey, encryptionResult);
            // Step 6
            uValue = new byte[32];
            System.arraycopy(encryptionResult, 0, uValue, 0, 16);
            // Add the arbitrary padding
            Arrays.fill(uValue, 16, 32, (byte) 0);
        }

        private byte[] xorKeyAndEncrypt19Times(byte[] key, byte[] input) {
            byte[] result = input;
            byte[] encryptionKey = new byte[key.length];
            for (int i = 1; i <= 19; i++) {
                for (int j = 0; j < key.length; j++) {
                    encryptionKey[j] = (byte) (key[j] ^ i);
                }
                result = encryptWithKey(encryptionKey, result);
            }
            return result;
        }

    }

    private class Rev5Engine extends InitializationEngine {

        // private SecureRandom random = new SecureRandom();
        private byte[] userValidationSalt = new byte[8];
        private byte[] userKeySalt = new byte[8];
        private byte[] ownerValidationSalt = new byte[8];
        private byte[] ownerKeySalt = new byte[8];
        private byte[] ueValue;
        private byte[] oeValue;
        private final boolean encryptMetadata;

        Rev5Engine(EncryptionSettings encryptionSettings) {
            super(encryptionSettings);
            this.encryptMetadata = encryptionSettings.encryptMetadata;
        }

        void run() {
            super.run();
            random = new SecureRandom();
            createEncryptionKey();
            computeUValue();
            computeOValue();
            computeUEValue();
            computeOEValue();
        }

        protected String getEncryptionDictionaryPart() {
            String encryptionDictionaryPart = super.getEncryptionDictionaryPart();
            encryptionDictionaryPart += "/OE " + PDFText.toHex(oeValue) + "\n"
                    + "/UE " + PDFText.toHex(ueValue) + "\n"
                    + "/Perms " + PDFText.toHex(computePermsValue(permissions)) + "\n"
                    + "/EncryptMetadata " + encryptMetadata + "\n"
                    // note: I think Length below should be 256 but Acrobat 9 uses 32...
                    + "/CF <</StdCF <</AuthEvent /DocOpen /CFM /AESV3 /Length 32>>>>\n"
                    + "/StmF /StdCF /StrF /StdCF\n";
            return encryptionDictionaryPart;
        }

        /**
         * Algorithm 3.8-1 (page 20, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        @Override
        protected void computeUValue() {
            byte[] userBytes = new byte[16];
            random.nextBytes(userBytes);
            System.arraycopy(userBytes, 0, userValidationSalt, 0, 8);
            System.arraycopy(userBytes, 8, userKeySalt, 0, 8);
            digest.reset();
            byte[] prepared = preparedUserPassword;
            byte[] concatenated = new byte[prepared.length + 8];
            System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
            System.arraycopy(userValidationSalt, 0, concatenated, prepared.length, 8);
            digest.update(concatenated);
            byte[] sha256 = digest.digest();
            uValue = new byte[48];
            System.arraycopy(sha256, 0, uValue, 0, 32);
            System.arraycopy(userValidationSalt, 0, uValue, 32, 8);
            System.arraycopy(userKeySalt, 0, uValue, 40, 8);
        }

        /**
         * Algorithm 3.9-1 (page 20, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        @Override
        protected void computeOValue() {
            byte[] ownerBytes = new byte[16];
            random.nextBytes(ownerBytes);
            System.arraycopy(ownerBytes, 0, ownerValidationSalt, 0, 8);
            System.arraycopy(ownerBytes, 8, ownerKeySalt, 0, 8);
            digest.reset();
            byte[] prepared = preparedOwnerPassword;
            byte[] concatenated = new byte[prepared.length + 56];
            System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
            System.arraycopy(ownerValidationSalt, 0, concatenated, prepared.length, 8);
            System.arraycopy(uValue, 0, concatenated, prepared.length + 8, 48);
            digest.update(concatenated);
            byte[] sha256 = digest.digest();
            oValue = new byte[48];
            System.arraycopy(sha256, 0, oValue, 0, 32);
            System.arraycopy(ownerValidationSalt, 0, oValue, 32, 8);
            System.arraycopy(ownerKeySalt, 0, oValue, 40, 8);
        }

        /**
         * See Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3, page 20, paragraph 5.
         */
        protected void createEncryptionKey() {
            encryptionKey = new byte[encryptionLengthInBytes];
            random.nextBytes(encryptionKey);
        }

        /**
         * Algorithm 3.2a-1 (page 19, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        protected byte[] preparePassword(String password) {
            byte[] passwordBytes;
            byte[] preparedPassword;
            try {
                // the password needs to be normalized first but we are bypassing that step for now
                passwordBytes = password.getBytes("UTF-8");
                if (passwordBytes.length > 127) {
                    preparedPassword = new byte[127];
                    System.arraycopy(passwordBytes, 0, preparedPassword, 0, 127);
                } else {
                    preparedPassword = new byte[passwordBytes.length];
                    System.arraycopy(passwordBytes, 0, preparedPassword, 0, passwordBytes.length);
                }
                return preparedPassword;
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e.getMessage());
            }
        }

        /**
         * Algorithm 3.8-2 (page 20, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        private void computeUEValue() {
            digest.reset();
            byte[] prepared = preparedUserPassword;
            byte[] concatenated = new byte[prepared.length + 8];
            System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
            System.arraycopy(userKeySalt, 0, concatenated, prepared.length, 8);
            digest.update(concatenated);
            byte[] ueEncryptionKey = digest.digest();
            ueValue = encryptWithKey(ueEncryptionKey, encryptionKey, true, ivZero);
        }

        /**
         * Algorithm 3.9-2 (page 20, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        private void computeOEValue() {
            digest.reset();
            byte[] prepared = preparedOwnerPassword;
            byte[] concatenated = new byte[prepared.length + 56];
            System.arraycopy(prepared, 0, concatenated, 0, prepared.length);
            System.arraycopy(ownerKeySalt, 0, concatenated, prepared.length, 8);
            System.arraycopy(uValue, 0, concatenated, prepared.length + 8, 48);
            digest.update(concatenated);
            byte[] oeEncryptionKey = digest.digest();
            oeValue = encryptWithKey(oeEncryptionKey, encryptionKey, true, ivZero);
        }

        /**
         * Algorithm 3.10 (page 20, Adobe Supplement to the ISO 32000, BaseVersion: 1.7, ExtensionLevel: 3)
         */
        public byte[] computePermsValue(int permissions) {
            byte[] perms = new byte[16];
            long extendedPermissions = 0xffffffff00000000L | permissions;
            for (int k = 0; k < 8; k++) {
                perms[k] = (byte) (extendedPermissions & 0xff);
                extendedPermissions >>= 8;
            }
            if (encryptMetadata) {
                perms[8] = 'T';
            } else {
                perms[8] = 'F';
            }
            perms[9] = 'a';
            perms[10] = 'd';
            perms[11] = 'b';
            byte[] randomBytes = new byte[4];
            random.nextBytes(randomBytes);
            System.arraycopy(randomBytes, 0, perms, 12, 4);
            byte[] encryptedPerms = encryptWithKey(encryptionKey, perms, true, ivZero);
            return encryptedPerms;
        }
    }

    private class EncryptionFilter extends PDFFilter {

        private int streamNumber;

        private int streamGeneration;

        EncryptionFilter(int streamNumber, int streamGeneration) {
            this.streamNumber  = streamNumber;
            this.streamGeneration = streamGeneration;
        }

        /**
         * Returns a PDF string representation of this filter.
         *
         * @return the empty string
         */
        public String getName() {
            return "";
        }

        /**
         * Returns a parameter dictionary for this filter.
         *
         * @return null, this filter has no parameters
         */
        public PDFObject getDecodeParms() {
            return null;
        }

        /** {@inheritDoc} */
        public OutputStream applyFilter(OutputStream out) throws IOException {
            if (useAlgorithm31a) {
                byte[] iv = new byte[16];
                random.nextBytes(iv);
                Cipher cipher = initCipher(encryptionKey, false, iv);
                out.write(iv);
                out.flush();
                return new CipherOutputStream(out, cipher);
            } else {
                byte[] key = createEncryptionKey(streamNumber, streamGeneration);
                Cipher cipher = initCipher(key);
                return new CipherOutputStream(out, cipher);
            }
        }

    }

    private PDFEncryptionJCE(int objectNumber, PDFEncryptionParams params, PDFDocument pdf) {
        setObjectNumber(objectNumber);
        try {
            if (params.getEncryptionLengthInBits() == 256) {
                digest = MessageDigest.getInstance("SHA-256");
            } else {
                digest = MessageDigest.getInstance("MD5");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
        setDocument(pdf);
        EncryptionInitializer encryptionInitializer = new EncryptionInitializer(params);
        encryptionInitializer.init();
        useAlgorithm31a = encryptionInitializer.isVersion5Revision5Algorithm();
    }

    /**
     * Creates and returns an encryption object.
     *
     * @param objectNumber the object number for the encryption dictionary
     * @param params the encryption parameters
     * @param pdf the PDF document to be encrypted
     * @return the newly created encryption object
     */
    public static PDFEncryption make(
            int objectNumber, PDFEncryptionParams params, PDFDocument pdf) {
        return new PDFEncryptionJCE(objectNumber, params, pdf);
    }

    /** {@inheritDoc} */
    public byte[] encrypt(byte[] data, PDFObject refObj) {
        PDFObject o = refObj;
        while (o != null && !o.hasObjectNumber()) {
            o = o.getParent();
        }
        if (o == null && !useAlgorithm31a) {
            throw new IllegalStateException("No object number could be obtained for a PDF object");
        }
        if (useAlgorithm31a) {
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            byte[] encryptedData = encryptWithKey(encryptionKey, data, false, iv);
            byte[] storedData = new byte[encryptedData.length + 16];
            System.arraycopy(iv, 0, storedData, 0, 16);
            System.arraycopy(encryptedData, 0, storedData, 16, encryptedData.length);
            return storedData;
        } else {
            byte[] key = createEncryptionKey(o.getObjectNumber(), o.getGeneration());
            return encryptWithKey(key, data);
        }
    }

    /** {@inheritDoc} */
    public void applyFilter(AbstractPDFStream stream) {
        if (!encryptMetadata && stream instanceof PDFMetadata) {
            return;
        }
        stream.getFilterList().addFilter(
                new EncryptionFilter(stream.getObjectNumber(), stream.getGeneration()));
    }

    /**
     *  Prepares the encryption dictionary for output to a PDF file.
     *
     *  @return the encryption dictionary as a byte array
     */
    public byte[] toPDF() {
        assert encryptionDictionary != null;
        return encode(this.encryptionDictionary);
    }

    /** {@inheritDoc} */
    public String getTrailerEntry() {
        return "/Encrypt " + getObjectNumber() + " " + getGeneration() + " R\n";
    }

    private static byte[] encryptWithKey(byte[] key, byte[] data) {
        try {
            final Cipher c = initCipher(key);
            return c.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static byte[] encryptWithKey(byte[] key, byte[] data, boolean noPadding, byte[] iv) {
        try {
            final Cipher c = initCipher(key, noPadding, iv);
            return c.doFinal(data);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (BadPaddingException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private static Cipher initCipher(byte[] key) {
        try {
            SecretKeySpec keyspec = new SecretKeySpec(key, "RC4");
            Cipher cipher = Cipher.getInstance("RC4");
            cipher.init(Cipher.ENCRYPT_MODE, keyspec);
            return cipher;
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    private static Cipher initCipher(byte[] key, boolean noPadding, byte[] iv) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv);
            Cipher cipher = noPadding ? Cipher.getInstance("AES/CBC/NoPadding") : Cipher
                    .getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
            return cipher;
        } catch (InvalidKeyException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        } catch (NoSuchPaddingException e) {
            throw new UnsupportedOperationException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Applies Algorithm 3.1 from the PDF 1.4 Reference.
     *
     * @param objectNumber the object number
     * @param generationNumber the generation number
     * @return the key to use for encryption
     */
    private byte[] createEncryptionKey(int objectNumber, int generationNumber) {
        // Step 1 passed in
        // Step 2
        byte[] md5Input = prepareMD5Input(objectNumber, generationNumber);
        // Step 3
        digest.reset();
        byte[] hash = digest.digest(md5Input);
        // Step 4
        int keyLength = Math.min(16, md5Input.length);
        byte[] key = new byte[keyLength];
        System.arraycopy(hash, 0, key, 0, keyLength);
        return key;
    }

    private byte[] prepareMD5Input(int objectNumber, int generationNumber) {
        byte[] md5Input = new byte[encryptionKey.length + 5];
        System.arraycopy(encryptionKey, 0, md5Input, 0, encryptionKey.length);
        int i = encryptionKey.length;
        md5Input[i++] = (byte) (objectNumber >>> 0);
        md5Input[i++] = (byte) (objectNumber >>> 8);
        md5Input[i++] = (byte) (objectNumber >>> 16);
        md5Input[i++] = (byte) (generationNumber >>> 0);
        md5Input[i++] = (byte) (generationNumber >>> 8);
        return md5Input;
    }

    /** {@inheritDoc} */
    public Version getPDFVersion() {
        return pdfVersion;
    }

}
