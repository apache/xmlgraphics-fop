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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link PDFEncryptionJCE} class.
 */
public class PDFEncryptionJCETestCase {

    private EncryptionTest test;

    private PDFEncryptionJCE encryptionObject;

    private static final class EncryptionTest {

        private int objectNumber = 1;

        private final PDFEncryptionParams encryptionParameters = new PDFEncryptionParams();

        private byte[] data;

        private byte[] encryptedData;

        private final EncryptionDictionaryTester encryptionDictionaryTester;

        EncryptionTest() {
            this(new EncryptionDictionaryTester());
        }

        EncryptionTest(EncryptionDictionaryTester encryptionDictionaryTester) {
            encryptionParameters.setUserPassword("TestUserPassword");
            encryptionParameters.setOwnerPassword("TestOwnerPassword");
            setData(0x00, 0xAA, 0xFF, 0x55, 0xCC, 0x33, 0xF0);
            this.encryptionDictionaryTester = encryptionDictionaryTester;
            this.encryptionDictionaryTester.setLength(
                    encryptionParameters.getEncryptionLengthInBits());
        }

        int getObjectNumber() {
            return objectNumber;
        }

        EncryptionTest setObjectNumber(int objectNumber) {
            this.objectNumber = objectNumber;
            return this;
        }

        byte[] getData() {
            return data;
        }

        EncryptionTest setData(int... data) {
            /*
             * Use an array of int to avoid having to cast some elements to byte in the
             * method call.
             */
            this.data = convertIntArrayToByteArray(data);
            return this;
        }

        byte[] getEncryptedData() {
            return encryptedData;
        }

        EncryptionTest setEncryptedData(int... encryptedData) {
            this.encryptedData = convertIntArrayToByteArray(encryptedData);
            return this;
        }

        private byte[] convertIntArrayToByteArray(int[] intArray) {
            byte[] byteArray = new byte[intArray.length];
            for (int i = 0; i < intArray.length; i++) {
                byteArray[i] = (byte) intArray[i];
            }
            return byteArray;
        }

        PDFEncryptionParams getEncryptionParameters() {
            return encryptionParameters;
        }

        EncryptionTest setUserPassword(String userPassword) {
            encryptionParameters.setUserPassword(userPassword);
            return this;
        }

        EncryptionTest setOwnerPassword(String ownerPassword) {
            encryptionParameters.setOwnerPassword(ownerPassword);
            return this;
        }

        EncryptionTest setEncryptionLength(int encryptionLength) {
            encryptionParameters.setEncryptionLengthInBits(encryptionLength);
            encryptionDictionaryTester.setLength(encryptionLength);
            return this;
        }

        EncryptionTest disablePrint() {
            encryptionParameters.setAllowPrint(false);
            return this;
        }

        EncryptionTest disableEditContent() {
            encryptionParameters.setAllowEditContent(false);
            return this;
        }

        EncryptionTest disableCopyContent() {
            encryptionParameters.setAllowCopyContent(false);
            return this;
        }

        EncryptionTest disableEditAnnotations() {
            encryptionParameters.setAllowEditAnnotations(false);
            return this;
        }

        EncryptionTest disableFillInForms() {
            encryptionParameters.setAllowFillInForms(false);
            return this;
        }

        EncryptionTest disableAccessContent() {
            encryptionParameters.setAllowAccessContent(false);
            return this;
        }

        EncryptionTest disableAssembleDocument() {
            encryptionParameters.setAllowAssembleDocument(false);
            return this;
        }

        EncryptionTest disablePrintHq() {
            encryptionParameters.setAllowPrintHq(false);
            return this;
        }

        void testEncryptionDictionary(PDFEncryptionJCE encryptionObject) {
            encryptionDictionaryTester.test(encryptionObject);
        }
    }

    private static final class EncryptionDictionaryTester {

        private int version = 2;

        private int revision = 3;

        private int length = 128;

        private int permissions = -4;

        private String ownerEntry = "D9A98017F0500EF9B69738641C9B4CBA1229EDC3F2151BC6C9C4FB07B1CB315E";

        private String userEntry = "D3EF424BFEA2E434000E1A74941CC87300000000000000000000000000000000";

        EncryptionDictionaryTester setVersion(int version) {
            this.version = version;
            return this;
        }

        EncryptionDictionaryTester setRevision(int revision) {
            this.revision = revision;
            return this;
        }

        EncryptionDictionaryTester setLength(int length) {
            this.length = length;
            return this;
        }

        EncryptionDictionaryTester setPermissions(int permissions) {
            this.permissions = permissions;
            return this;
        }

        EncryptionDictionaryTester setOwnerEntry(String ownerEntry) {
            this.ownerEntry = ownerEntry;
            return this;
        }

        EncryptionDictionaryTester setUserEntry(String userEntry) {
            this.userEntry = userEntry;
            return this;
        }

        void test(PDFEncryptionJCE encryptionObject) {
            byte[] encryptionDictionary = encryptionObject.toPDF();
            RegexTestedCharSequence dictionary = new RegexTestedCharSequence(encryptionDictionary);

            final String whitespace = "\\s+";
            final String digits = "\\d+";
            final String hexDigits = "\\p{XDigit}+";

            dictionary.mustContain("/Filter" + whitespace + "/Standard\\b");

            dictionary.mustContain("/V" + whitespace + "(" + digits + ")")
                    .withGroup1EqualTo(Integer.toString(version));

            dictionary.mustContain("/R" + whitespace + "(" + digits + ")")
                    .withGroup1EqualTo(Integer.toString(revision));

            dictionary.mustContain("/Length" + whitespace + "(" + digits + ")")
                    .withGroup1EqualTo(Integer.toString(length));

            dictionary.mustContain("/P" + whitespace + "(-?" + digits + ")")
                    .withGroup1EqualTo(Integer.toString(permissions));

            dictionary.mustContain("/O" + whitespace + "<(" + hexDigits + ")>")
                    .withGroup1EqualTo(ownerEntry);

            dictionary.mustContain("/U" + whitespace + "<(" + hexDigits + ")>")
                    .withGroup1EqualTo(userEntry);
        }
    }

    private static final class RegexTestedCharSequence {

        private final String string;

        private Matcher matcher;

        RegexTestedCharSequence(byte[] bytes) {
            try {
                string = new String(bytes, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        RegexTestedCharSequence mustContain(String regex) {
            Pattern pattern = Pattern.compile(regex);
            matcher = pattern.matcher(string);
            assertTrue(matcher.find());
            return this;
        }

        RegexTestedCharSequence withGroup1EqualTo(String expected) {
            assertEquals(expected, matcher.group(1));
            return this;
        }
    }

    @Test
    public final void testMake() {
        PDFEncryption testEncryptionObj = createEncryptionObject(new PDFEncryptionParams());
        assertTrue(testEncryptionObj instanceof PDFEncryptionJCE);
        assertEquals(1, ((PDFEncryptionJCE) testEncryptionObj).getObjectNumber().getNumber());
    }

    @Test
    public void testBasic() throws IOException {
        test = new EncryptionTest();
        test.setData(0x00).setEncryptedData(0x24);
        runEncryptionTests();

        test.setData(0xAA).setEncryptedData(0x8E);
        runEncryptionTests();

        test.setData(0xFF).setEncryptedData(0xDB);
        runEncryptionTests();

        test = new EncryptionTest().setEncryptedData(0x24, 0x07, 0x85, 0xF7, 0x87, 0x31, 0x90);
        runEncryptionTests();
    }

    @Test
    public void test128bit() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setPermissions(-4)
                .setOwnerEntry("D9A98017F0500EF9B69738641C9B4CBA1229EDC3F2151BC6C9C4FB07B1CB315E")
                .setUserEntry("D3EF424BFEA2E434000E1A74941CC87300000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(2)
                .setEncryptionLength(128)
                .setEncryptedData(0xE3, 0xCB, 0xB2, 0x55, 0xD9, 0x26, 0x55);
        runEncryptionTests();
    }

    @Test
    public void testDisableRev2Permissions() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(1)
                .setRevision(2)
                .setLength(40)
                .setPermissions(-64)
                .setOwnerEntry("3EE8C4000CA44B2645EED029C9EA7D4FC63C6D9B89349E8FA5A40C7691AB96B5")
                .setUserEntry("3E65D0090746C4C37C5EF23C1BDB6323E00C24C4B2D744DD3BFB654CD58591A1");
        test = new EncryptionTest(encryptionDictionaryTester).setObjectNumber(3).setEncryptionLength(40)
                .disablePrint().disableEditContent().disableCopyContent().disableEditAnnotations()
                .setEncryptedData(0x66, 0xEE, 0xA7, 0x93, 0xC4, 0xB1, 0xB4);
        runEncryptionTests();
    }

    @Test
    public void testDisableRev3Permissions() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setLength(40)
                .setPermissions(-3844)
                .setOwnerEntry("8D4BCA4F4AB2BAB4E38F161D61F937EC50BE5EB30C2DC05EA409D252CD695E55")
                .setUserEntry("0F01171E22C7FB27B079C132BA4277DE00000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(4)
                .setEncryptionLength(40)
                .disableFillInForms()
                .disableAccessContent()
                .disableAssembleDocument()
                .disablePrintHq()
                .setEncryptedData(0x8E, 0x3C, 0xD2, 0x05, 0x50, 0x48, 0x82);
        runEncryptionTests();
    }

    @Test
    public void test128bitDisableSomePermissions() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setPermissions(-1304)
                .setOwnerEntry("D9A98017F0500EF9B69738641C9B4CBA1229EDC3F2151BC6C9C4FB07B1CB315E")
                .setUserEntry("62F0E4D8641D482E0F8E71A89270045A00000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(5)
                .setEncryptionLength(128)
                .disablePrint()
                .disableCopyContent()
                .disableFillInForms()
                .disableAssembleDocument()
                .setEncryptedData(0xF7, 0x85, 0x4F, 0xB0, 0x50, 0x5C, 0xDF);
        runEncryptionTests();
    }

    @Test
    public void testDifferentPasswords() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setRevision(2)
                .setVersion(1)
                .setLength(40)
                .setOwnerEntry("D11C233C65E9DC872E858ABBD8B62198771167ADCE7AB8DC7AE0A1A7E21A1E25")
                .setUserEntry("6F449167DB8DDF0D2DF4602DDBBA97ABF9A9101F632CC16AB0BE74EB9500B469");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(6)
                .setEncryptionLength(40)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("ADifferentOwnerPassword")
                .setEncryptedData(0x27, 0xAC, 0xB1, 0x6C, 0x42, 0xE0, 0xA8);
        runEncryptionTests();
    }

    @Test
    public void testNoOwnerPassword() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setRevision(2)
                .setVersion(1)
                .setLength(40)
                .setOwnerEntry("5163AAF3EE74C76D7C223593A84C8702FEA8AA4493E4933FF5B5A5BBB20AE4BB")
                .setUserEntry("42DDF1C1BF3AB04786D5038E7B0A723AE614D944E1DE91A922FC54F5F2345E00");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(7)
                .setEncryptionLength(40)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("")
                .setEncryptedData(0xEC, 0x2E, 0x5D, 0xC2, 0x7F, 0xAD, 0x58);
        runEncryptionTests();
    }

    @Test
    public void test128bitDisableSomePermissionsDifferentPasswords() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setPermissions(-2604)
                .setOwnerEntry("F83CA049FAA2F774F8541F25E746A92EE2A7F060C46C91C693E673BF18FF7B36")
                .setUserEntry("88A4C58F5385B5F08FACA0636D790EDF00000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(8)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("ADifferentOwnerPassword")
                .setEncryptionLength(128)
                .disableEditContent()
                .disableEditAnnotations()
                .disableAccessContent()
                .disablePrintHq()
                .setEncryptedData(0x77, 0x54, 0x67, 0xA5, 0xCC, 0x73, 0xDE);
        runEncryptionTests();
    }

    @Test
    public void test128bitNoPermissionNoOwnerPassword() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setPermissions(-3904)
                .setOwnerEntry("3EEB3FA5594CBD935BFB2F83FB184DD41FBCD7C36A04F1FFD0899B0DFFCFF96B")
                .setUserEntry("D972B72DD2633F613B0DDB7511C719C500000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(9)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("")
                .setEncryptionLength(128)
                .disablePrint()
                .disableEditContent()
                .disableCopyContent()
                .disableEditAnnotations()
                .disableFillInForms()
                .disableAccessContent()
                .disableAssembleDocument()
                .disablePrintHq()
                .setEncryptedData(0x0C, 0xAD, 0x49, 0xC7, 0xE5, 0x05, 0xB8);
        runEncryptionTests();
    }

    @Test
    public void testAES256() throws UnsupportedEncodingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,
            IllegalBlockSizeException, BadPaddingException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        String dataText = "Test data to encrypt.";
        byte[] data = dataText.getBytes("UTF-8");
        PDFEncryptionParams params = new PDFEncryptionParams();
        params.setEncryptionLengthInBits(256);
        params.setUserPassword("userpassword");
        params.setOwnerPassword("ownerpassword");
        PDFEncryptionJCE encryption = createEncryptionObject(params);
        PDFText text = new PDFText();
        text.setObjectNumber(1); // obj number not used with AES 256, can be anything
        String dictionary = new String(encryption.toPDF());
        byte[] encrypted = encryption.encrypt(data, text);
        byte[] u = parseHexStringEntries(dictionary, "U");
        byte[] o = parseHexStringEntries(dictionary, "O");
        byte[] ue = parseHexStringEntries(dictionary, "UE");
        byte[] oe = parseHexStringEntries(dictionary, "OE");
        byte[] perms = parseHexStringEntries(dictionary, "Perms");
        // check byte arrays lengths
        assertEquals(48, u.length);
        assertEquals(48, o.length);
        assertEquals(32, ue.length);
        assertEquals(32, oe.length);
        assertEquals(16, perms.length);
        // check user password is valid
        byte[] userValSalt = new byte[8];
        byte[] userKeySalt = new byte[8];
        System.arraycopy(u, 32, userValSalt, 0, 8);
        System.arraycopy(u, 40, userKeySalt, 0, 8);
        byte[] uPassBytes = params.getUserPassword().getBytes("UTF-8");
        byte[] testUPass = new byte[uPassBytes.length + 8];
        System.arraycopy(uPassBytes, 0, testUPass, 0, uPassBytes.length);
        System.arraycopy(userValSalt, 0, testUPass, uPassBytes.length, 8);
        sha256.reset();
        sha256.update(testUPass);
        byte[] actualUPass = sha256.digest();
        byte[] expectedUPass = new byte[32];
        System.arraycopy(u, 0, expectedUPass, 0, 32);
        assertArrayEquals(expectedUPass, actualUPass);
        // check owner password is valid
        byte[] ownerValSalt = new byte[8];
        byte[] ownerKeySalt = new byte[8];
        System.arraycopy(o, 32, ownerValSalt, 0, 8);
        System.arraycopy(o, 40, ownerKeySalt, 0, 8);
        byte[] oPassBytes = params.getOwnerPassword().getBytes("UTF-8");
        byte[] testOPass = new byte[oPassBytes.length + 8 + 48];
        System.arraycopy(oPassBytes, 0, testOPass, 0, oPassBytes.length);
        System.arraycopy(ownerValSalt, 0, testOPass, oPassBytes.length, 8);
        System.arraycopy(u, 0, testOPass, oPassBytes.length + 8, 48);
        sha256.reset();
        sha256.update(testOPass);
        byte[] actualOPass = sha256.digest();
        byte[] expectedOPass = new byte[32];
        System.arraycopy(o, 0, expectedOPass, 0, 32);
        assertArrayEquals(expectedOPass, actualOPass);
        // compute encryption key from ue
        byte[] ivZero = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        IvParameterSpec ivspecZero = new IvParameterSpec(ivZero);
        Cipher cipherNoPadding = Cipher.getInstance("AES/CBC/NoPadding");
        byte[] tmpUKey = new byte[uPassBytes.length + 8];
        System.arraycopy(uPassBytes, 0, tmpUKey, 0, uPassBytes.length);
        System.arraycopy(userKeySalt, 0, tmpUKey, uPassBytes.length, 8);
        sha256.reset();
        sha256.update(tmpUKey);
        byte[] intUKey = sha256.digest();
        SecretKeySpec uSKeySpec = new SecretKeySpec(intUKey, "AES");
        cipherNoPadding.init(Cipher.DECRYPT_MODE, uSKeySpec, ivspecZero);
        byte[] uFileEncryptionKey = cipherNoPadding.doFinal(ue);
        // compute encryption key from oe
        byte[] tmpOKey = new byte[oPassBytes.length + 8 + 48];
        System.arraycopy(oPassBytes, 0, tmpOKey, 0, oPassBytes.length);
        System.arraycopy(ownerKeySalt, 0, tmpOKey, oPassBytes.length, 8);
        System.arraycopy(u, 0, tmpOKey, oPassBytes.length + 8, 48);
        sha256.reset();
        sha256.update(tmpOKey);
        byte[] intOKey = sha256.digest();
        SecretKeySpec oSKeySpec = new SecretKeySpec(intOKey, "AES");
        cipherNoPadding.init(Cipher.DECRYPT_MODE, oSKeySpec, ivspecZero);
        byte[] oFileEncryptionKey = cipherNoPadding.doFinal(oe);
        // check both keys are the same
        assertArrayEquals(uFileEncryptionKey, oFileEncryptionKey);
        byte[] fileEncryptionKey = new byte[uFileEncryptionKey.length];
        System.arraycopy(uFileEncryptionKey, 0, fileEncryptionKey, 0, uFileEncryptionKey.length);
        // decrypt perms
        SecretKeySpec sKeySpec = new SecretKeySpec(fileEncryptionKey, "AES");
        cipherNoPadding.init(Cipher.DECRYPT_MODE, sKeySpec, ivspecZero);
        byte[] decryptedPerms = cipherNoPadding.doFinal(perms);
        assertEquals('T', decryptedPerms[8]); // metadata encrypted by default
        assertEquals('a', decryptedPerms[9]);
        assertEquals('d', decryptedPerms[10]);
        assertEquals('b', decryptedPerms[11]);
        int expectedPermissions = -4; // default if nothing set
        int actualPermissions = decryptedPerms[3] << 24 | (decryptedPerms[2] & 0xFF) << 16
                | (decryptedPerms[1] & 0xFF) << 8 | (decryptedPerms[0] & 0xFF);
        assertEquals(expectedPermissions, actualPermissions);
        // decrypt data
        byte[] iv = new byte[16];
        System.arraycopy(encrypted, 0, iv, 0, 16);
        byte[] encryptedData = new byte[encrypted.length - 16];
        System.arraycopy(encrypted, 16, encryptedData, 0, encrypted.length - 16);
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivspec);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        assertArrayEquals(data, decryptedData);
    }

    private byte[] parseHexStringEntries(String dictionary, String entry) throws UnsupportedEncodingException {
        String token = "/" + entry + " <";
        int start = dictionary.indexOf(token) + token.length();
        int end = dictionary.indexOf(">", start);
        String parsedEntry = dictionary.substring(start, end);
        int length = parsedEntry.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(parsedEntry.charAt(i), 16) << 4) + Character.digit(
                    parsedEntry.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Creates an encryption object using a fixed file ID generator for test reproducibility.
     *
     * @param params the encryption parameters
     * @return PDFEncryptionJCE the encryption object
     */
    private PDFEncryptionJCE createEncryptionObject(PDFEncryptionParams params) {
        PDFDocument doc = new PDFDocument("Apache FOP") {

            @Override
            FileIDGenerator getFileIDGenerator() {
                return new FileIDGenerator() {

                    private final byte[] fixedFileID = new byte[] {
                            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
                            0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};

                    @Override
                    byte[] getOriginalFileID() {
                        return fixedFileID;
                    }

                    @Override
                    byte[] getUpdatedFileID() {
                        return fixedFileID;
                    }

                };
            }
        };
        return (PDFEncryptionJCE) PDFEncryptionJCE.make(new PDFObjectNumber(1), params, doc);
    }

    private void runEncryptionTests() throws IOException {
        encryptionObject = createEncryptionObject(test.getEncryptionParameters());
        runEncryptTest();
        runFilterTest();
        runEncryptionDictionaryTest();
    }

    private void runEncryptTest() {
        PDFText text = new PDFText();
        text.setObjectNumber(test.getObjectNumber());
        byte[] byteResult = encryptionObject.encrypt(test.getData(), text);

        assertTrue(Arrays.equals(test.getEncryptedData(), byteResult));
    }

    private void runFilterTest() throws IOException {
        PDFStream stream = new PDFStream();
        stream.setDocument(encryptionObject.getDocumentSafely());
        stream.setObjectNumber(test.getObjectNumber());
        stream.setData(test.getData());
        encryptionObject.applyFilter(stream);

        StreamCache streamCache = stream.encodeStream();
        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        streamCache.outputContents(testOutputStream);

        assertTrue(Arrays.equals(test.getEncryptedData(), testOutputStream.toByteArray()));
    }

    private void runEncryptionDictionaryTest() {
        test.testEncryptionDictionary(encryptionObject);
    }

}
