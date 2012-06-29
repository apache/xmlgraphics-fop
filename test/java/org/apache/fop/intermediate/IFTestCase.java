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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Test case for the IF output.
 */
@RunWith(Parameterized.class)
public class IFTestCase extends AbstractIFTest {

    /**
     * Gets the files for this test.
     *
     * @return a collection of file arrays containing the files to test
     * @throws IOException if an error occurs when reading the test files
     */
    @Parameters
    public static Collection<File[]> getParameters() throws IOException {
        File testDir = new File("test/intermediate");
        String[] tests = testDir.list(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        Collection<File[]> parameters = new ArrayList<File[]>();
        for (String test : tests) {
            parameters.add(new File[] { new File(testDir, test) });
        }
        return parameters;
    }

    private static IFTester ifTester;

    @BeforeClass
    public static void setupTestEnvironment() {
        File backupDir = new File("build/test-results/intermediate");
        backupDir.mkdirs();
        ifTester = new IFTester(TransformerFactory.newInstance(), backupDir);
    }

    /**
     * Creates a new test case.
     *
     * @param test the file containing the test case
     * @param ifTester the helper instance that will perform checks
     * @throws IOException if an I/O error occurs while loading the test case
     */
    public IFTestCase(File test) throws IOException {
        super(test);
        this.testDir = test.getParentFile();
    }

    @Override
    @Test
    public void runTest() throws Exception {
        Element testRoot = testAssistant.getTestRoot(testFile);
        NodeList nodes = testRoot.getElementsByTagName("if-checks");
        if (nodes.getLength() == 0) {
            throw new RuntimeException("No IF check found");
        }
        Element ifChecks = (Element) nodes.item(0);

        Document doc = buildIntermediateDocument(testAssistant.getTestcase2FOStylesheet());
        ifTester.doIFChecks(testFile.getName(), ifChecks, doc);
    }

    @Override
    protected void parseAndRender(Source src, OutputStream out) throws Exception {
        throw new IllegalStateException("Not applicable to this test");
    }

    @Override
    protected Document parseAndRenderToIntermediateFormat(Source src) throws Exception {
        throw new IllegalStateException("Not applicable to this test");
    }

}
