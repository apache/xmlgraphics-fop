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
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.fop.layoutengine.LayoutEngineTestSuite;

/**
 * JUnit test suite for the intermediate format
 */
public final class LayoutIFTestSuite {

    private LayoutIFTestSuite() {
        // This is a utility class
    }

    /**
     * @return the test suite with all the tests (one for each XML file)
     * @throws IOException in case of an I/O problem
     */
    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite();

        Collection files = LayoutEngineTestSuite.getTestFiles();

        Iterator i = files.iterator();
        while (i.hasNext()) {
            File f = (File)i.next();
            addIFTestCase(suite, f);
        }

        return suite;
    }

    private static void addIFTestCase(TestSuite suite,
            final File f) throws IOException {
        suite.addTest(new IFParserTestCase(f));
    }

}
