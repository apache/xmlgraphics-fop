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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test suit for running layout engine test under JUnit control.
 */
public class LayoutEngineTestSuite {

    /**
     * @return the test suite with all the tests (one for each XML file)
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();

        File mainDir = new File("test/layoutengine");
        File backupDir = new File("build/test-results/layoutengine");
        backupDir.mkdirs();

        final LayoutEngineTester tester = new LayoutEngineTester(backupDir);
        
        Collection files = FileUtils.listFiles(new File(mainDir, "testcases"), 
                new String[] {"xml"}, true);
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
