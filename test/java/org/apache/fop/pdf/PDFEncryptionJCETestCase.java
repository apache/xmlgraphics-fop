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
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Tests the {@link PDFEncryptionJCE} class.
 */
public class PDFEncryptionJCETestCase extends TestCase {

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

        private int version = 1;

        private int revision = 2;

        private int length = 40;

        private int permissions = -4;

        private String ownerEntry
                = "3EE8C4000CA44B2645EED029C9EA7D4FC63C6D9B89349E8FA5A40C7691AB96B5";

        private String userEntry
                = "D1810D9E6E488BA5D2DDCBB3F974F7472D0D5389F554DB55574A787DC5C59884";

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

            dictionary.mustContain("1" + whitespace + "0" + whitespace + "obj");

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

    public final void testMake() {
        PDFEncryption testEncryptionObj = createEncryptionObject(new PDFEncryptionParams());
        assertTrue(testEncryptionObj instanceof PDFEncryptionJCE);
        assertEquals(1, ((PDFEncryptionJCE) testEncryptionObj).getObjectNumber());
    }

    public void testBasic() throws IOException {
        test = new EncryptionTest();
        test.setData(0x00).setEncryptedData(0x56);
        runEncryptionTests();

        test.setData(0xAA).setEncryptedData(0xFC);
        runEncryptionTests();

        test.setData(0xFF).setEncryptedData(0xA9);
        runEncryptionTests();

        test = new EncryptionTest().setEncryptedData(0x56, 0x0C, 0xFC, 0xA5, 0xAB, 0x61, 0x73);
        runEncryptionTests();
    }

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

    public void testDisableRev2Permissions() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setPermissions(-64)
                .setUserEntry("3E65D0090746C4C37C5EF23C1BDB6323E00C24C4B2D744DD3BFB654CD58591A1");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(3)
                .disablePrint()
                .disableEditContent()
                .disableCopyContent()
                .disableEditAnnotations()
                .setEncryptedData(0x66, 0xEE, 0xA7, 0x93, 0xC4, 0xB1, 0xB4);
        runEncryptionTests();
    }

    public void testDisableRev3Permissions() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setVersion(2)
                .setRevision(3)
                .setPermissions(-3844)
                .setOwnerEntry("8D4BCA4F4AB2BAB4E38F161D61F937EC50BE5EB30C2DC05EA409D252CD695E55")
                .setUserEntry("0F01171E22C7FB27B079C132BA4277DE00000000000000000000000000000000");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(4)
                .disableFillInForms()
                .disableAccessContent()
                .disableAssembleDocument()
                .disablePrintHq()
                .setEncryptedData(0x8E, 0x3C, 0xD2, 0x05, 0x50, 0x48, 0x82);
        runEncryptionTests();
    }

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

    public void testDifferentPasswords() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setOwnerEntry("D11C233C65E9DC872E858ABBD8B62198771167ADCE7AB8DC7AE0A1A7E21A1E25")
                .setUserEntry("6F449167DB8DDF0D2DF4602DDBBA97ABF9A9101F632CC16AB0BE74EB9500B469");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(6)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("ADifferentOwnerPassword")
                .setEncryptedData(0x27, 0xAC, 0xB1, 0x6C, 0x42, 0xE0, 0xA8);
        runEncryptionTests();
    }

    public void testNoOwnerPassword() throws IOException {
        EncryptionDictionaryTester encryptionDictionaryTester = new EncryptionDictionaryTester()
                .setOwnerEntry("5163AAF3EE74C76D7C223593A84C8702FEA8AA4493E4933FF5B5A5BBB20AE4BB")
                .setUserEntry("42DDF1C1BF3AB04786D5038E7B0A723AE614D944E1DE91A922FC54F5F2345E00");
        test = new EncryptionTest(encryptionDictionaryTester)
                .setObjectNumber(7)
                .setUserPassword("ADifferentUserPassword")
                .setOwnerPassword("")
                .setEncryptedData(0xEC, 0x2E, 0x5D, 0xC2, 0x7F, 0xAD, 0x58);
        runEncryptionTests();
    }

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
        return (PDFEncryptionJCE) PDFEncryptionJCE.make(1, params, doc);
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
