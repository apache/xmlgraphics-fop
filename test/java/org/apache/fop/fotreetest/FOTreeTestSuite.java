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

package org.apache.fop.fotreetest;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.apache.fop.DebugHelper;
import org.apache.fop.fo.flow.table.CollapsedConditionalBorderTestCase;
import org.apache.fop.fo.flow.table.IllegalRowSpanTestCase;
import org.apache.fop.fo.flow.table.RowGroupBuilderTestCase;
import org.apache.fop.fo.flow.table.TableColumnColumnNumberTestCase;
import org.apache.fop.fo.flow.table.TooManyColumnsTestCase;
import org.apache.fop.layoutengine.LayoutEngineTestSuite;

/**
 * JUnit test suit for running layout engine test under JUnit control.
 */
public final class FOTreeTestSuite {

    static {
        DebugHelper.registerStandardElementListObservers();
    }
    
    private FOTreeTestSuite() {
        //don't instantiate!
    }
    
    /**
     * @return the test suite with all the tests (one for each XML file)
     * @throws IOException in case of an I/O problem
     */
    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite();
        addXMLTestCases(suite);
        addUnitTestCases(suite);
        return suite;
    }

    private static void addXMLTestCases(TestSuite suite) throws IOException {
        File mainDir = new File("test/fotree");

        final FOTreeTester tester = new FOTreeTester();
        
        IOFileFilter filter;
        String single = System.getProperty("fop.fotree.single");
        String startsWith = System.getProperty("fop.fotree.starts-with");
        if (single != null) {
            filter = new NameFileFilter(single);
        } else if (startsWith != null) {
            filter = new PrefixFileFilter(startsWith);
            filter = new AndFileFilter(filter, new SuffixFileFilter(".fo"));
        } else {
            filter = new SuffixFileFilter(".fo");
            filter = LayoutEngineTestSuite.decorateWithDisabledList(filter);
        }
        Collection files = FileUtils.listFiles(new File(mainDir, "testcases"), 
                filter, TrueFileFilter.INSTANCE);
        String privateTests = System.getProperty("fop.fotree.private");
        if ("true".equalsIgnoreCase(privateTests)) {
            Collection privateFiles = FileUtils.listFiles(
                    new File(mainDir, "private-testcases"), 
                    filter, TrueFileFilter.INSTANCE);
            files.addAll(privateFiles);
        }
        Iterator i = files.iterator();
        while (i.hasNext()) {
            File f = (File)i.next();
            addTestCase(suite, tester, f);
        }
    }
    
    private static void addTestCase(TestSuite suite, 
                final FOTreeTester tester, final File f) {
        suite.addTest(new FOTreeTestCase(f.getName()) {
            public void runTest() throws Exception {
                try {
                    prepare(tester, f);
                    testMain();
                } catch (Exception e) {
                    org.apache.commons.logging.LogFactory.getLog(this.getClass()).info(
                            "Error on " + f.getName());
                    throw e;
                }
            }
        });
    }

    private static void addUnitTestCases(TestSuite suite) {
        suite.addTestSuite(TooManyColumnsTestCase.class);
        suite.addTestSuite(IllegalRowSpanTestCase.class);
        suite.addTestSuite(RowGroupBuilderTestCase.class);
        suite.addTestSuite(TableColumnColumnNumberTestCase.class);
        suite.addTestSuite(CollapsedConditionalBorderTestCase.class);
    }

    private static class FOTreeTestCase extends TestCase {
        
        private FOTreeTester tester;
        private File testFile;
        
        public FOTreeTestCase(String name) {
            super(name);
        }
        
        public void prepare(FOTreeTester tester, File testFile) {
            //super(testFile.getName());
            this.tester = tester;
            this.testFile = testFile;
        }
        
        public void testMain() throws Exception {
            tester.runTest(testFile);
        }
    }    
}
