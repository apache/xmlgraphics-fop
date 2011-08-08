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

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests the {@link FileIDGenerator} class.
 */
public abstract class FileIDGeneratorTestCase extends TestCase {

    /**
     * Returns a suite containing all the {@link FileIDGenerator} test cases.
     *
     * @return the test suite
     */
    public static final Test suite() {
        TestSuite suite = new TestSuite(new Class[] {
                RandomFileIDGeneratorTestCase.class,
                DigestFileIDGeneratorTestCase.class },
                FileIDGeneratorTestCase.class.getName());
        return suite;
    }

    /** The generator under test. */
    protected FileIDGenerator fileIDGenerator;


    /** Tests that the getOriginalFileID method generates valid output. */
    public void testOriginal() {
        byte[] fileID = fileIDGenerator.getOriginalFileID();
        fileIDMustBeValid(fileID);
    }

    /** Tests that the getUpdatedFileID method generates valid output. */
    public void testUpdated() {
        byte[] fileID = fileIDGenerator.getUpdatedFileID();
        fileIDMustBeValid(fileID);
    }

    private void fileIDMustBeValid(byte[] fileID) {
        assertNotNull(fileID);
        assertEquals(16, fileID.length);
    }

    /** Tests that multiple calls to getOriginalFileID method always return the same value. */
    public void testOriginalMultipleCalls() {
        byte[] fileID1 = fileIDGenerator.getUpdatedFileID();
        byte[] fileID2 = fileIDGenerator.getUpdatedFileID();
        assertTrue(Arrays.equals(fileID1, fileID2));
    }

    /** Tests that getUpdatedFileID returns the same value as getOriginalFileID. */
    public void testUpdateEqualsOriginal() {
        byte[] originalFileID = fileIDGenerator.getOriginalFileID();
        byte[] updatedFileID = fileIDGenerator.getUpdatedFileID();
        assertTrue(Arrays.equals(originalFileID, updatedFileID));
    }

    /**
     * Tests the random file ID generator.
     */
    public static class RandomFileIDGeneratorTestCase extends FileIDGeneratorTestCase {

        @Override
        protected void setUp() throws Exception {
            fileIDGenerator = FileIDGenerator.getRandomFileIDGenerator();
        }

    }

    /**
     * Tests the file ID generator based on an MD5 digest.
     */
    public static class DigestFileIDGeneratorTestCase extends FileIDGeneratorTestCase {

        @Override
        protected void setUp() throws Exception {
            fileIDGenerator = FileIDGenerator.getDigestFileIDGenerator(
                    new PDFDocument("Apache FOP"));
        }

    }

}
