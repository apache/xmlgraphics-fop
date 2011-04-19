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
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Test case for the IF output.
 */
public class IFTestCase extends AbstractIFTestCase {

    private final IFTester ifTester;

    /**
     * Creates a new test case.
     *
     * @param test the file containing the test case
     * @param ifTester the helper instance that will perform checks
     * @throws IOException if an I/O error occurs while loading the test case
     */
    public IFTestCase(File test, IFTester ifTester) throws IOException {
        super(test);
        this.ifTester = ifTester;
        this.testDir = test.getParentFile();
    }

    /** {@inheritDoc} */
    @Override
    protected void runTest() throws Exception {
        Element testRoot = testAssistant.getTestRoot(testFile);
        NodeList nodes = testRoot.getElementsByTagName("if-checks");
        if (nodes.getLength() == 0) {
            throw new RuntimeException("No IF check found");
        }
        Element ifChecks = (Element) nodes.item(0);

        Document doc = buildIntermediateDocument(testAssistant.getTestcase2FOStylesheet());
        ifTester.doIFChecks(getName(), ifChecks, doc);
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
