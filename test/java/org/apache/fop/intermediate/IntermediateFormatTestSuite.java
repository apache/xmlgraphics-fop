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

package org.apache.fop.intermediate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.transform.TransformerFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A test suite for testing the Intermediate Format output.
 */
public final class IntermediateFormatTestSuite {

    private IntermediateFormatTestSuite() {
        // This is a utility class
    }

    /**
     * Creates a suite of Intermediate Format tests.
     *
     * @return the test suite
     * @throws IOException if an I/O error occurs while loading one of the tests
     */
    public static Test suite() throws IOException {

        File backupDir = new File("build/test-results/intermediate");
        backupDir.mkdirs();

        IFTester ifTester = new IFTester(TransformerFactory.newInstance(), backupDir);

        TestSuite suite = new TestSuite();
        File testDir = new File("test/intermediate");
        String[] tests = testDir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (String test : tests) {
            File testFile = new File(testDir, test);
            suite.addTest(new IFTestCase(testFile, ifTester));
        }
        return suite;
    }
}
