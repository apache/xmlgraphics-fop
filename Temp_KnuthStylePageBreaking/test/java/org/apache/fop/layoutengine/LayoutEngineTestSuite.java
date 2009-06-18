/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layoutengine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test suit for running layout engine test under JUnit control.
 */
public class LayoutEngineTestSuite {

    private static String[] readLinesFromFile(File f) throws IOException {
        List lines = new java.util.ArrayList();
        Reader reader = new FileReader(f);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        return (String[])lines.toArray(new String[lines.size()]);
    }
    
    /**
     * @return the test suite with all the tests (one for each XML file)
     * @throws IOException in case of an I/O problem
     */
    public static Test suite() throws IOException {
        TestSuite suite = new TestSuite();

        File mainDir = new File("test/layoutengine");
        File backupDir = new File("build/test-results/layoutengine");
        backupDir.mkdirs();

        final LayoutEngineTester tester = new LayoutEngineTester(backupDir);
        
        IOFileFilter filter;
        String single = System.getProperty("fop.layoutengine.single");
        String startsWith = System.getProperty("fop.layoutengine.starts-with");
        if (single != null) {
            filter = new NameFileFilter(single);
        } else if (startsWith != null) {
            filter = new PrefixFileFilter(startsWith);
            filter = new AndFileFilter(filter, new SuffixFileFilter(".xml"));
        } else {
            filter = new SuffixFileFilter(".xml");
            String disabled = System.getProperty("fop.layoutengine.disabled");
            if (disabled != null && disabled.length() > 0) {
                filter = new AndFileFilter(new NotFileFilter(
                        new NameFileFilter(readLinesFromFile(new File(disabled)))),
                        filter);
            }
            
        }
        Collection files = FileUtils.listFiles(new File(mainDir, "testcases"), 
                filter, TrueFileFilter.INSTANCE);
        Iterator i = files.iterator();
        while (i.hasNext()) {
            File f = (File)i.next();
            addTestCase(suite, tester, f);
        }
        
        return suite;
    }
    
    private static void addTestCase(TestSuite suite, 
                final LayoutEngineTester tester, final File f) {
        suite.addTest(new LayoutEngineTestCase(f.getName()) {
            public void runTest() throws Exception {
                prepare(tester, f);
                testMain();
            }
        });
    }
    
    private static class LayoutEngineTestCase extends TestCase {
        
        private LayoutEngineTester tester;
        private File testFile;
        
        public LayoutEngineTestCase(String name) {
            super(name);
        }
        
        public void prepare(LayoutEngineTester tester, File testFile) {
            //super(testFile.getName());
            this.tester = tester;
            this.testFile = testFile;
        }
        
        public void testMain() throws Exception {
            tester.runTest(testFile);
        }
    }    
}
