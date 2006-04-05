/*
 * Copyright 2006 The Apache Software Foundation.
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

/* $Id: BasicDriverTestSuite.java 231325 2005-08-10 21:05:39Z jeremias $ */
 
package org.apache.fop;

import org.apache.fop.render.pdf.PDFAConformanceTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for basic functionality of FOP.
 */
public class StandardTestSuite {

    /**
     * Builds the test suite
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(
            "Basic functionality test suite for FOP");
        //$JUnit-BEGIN$
        suite.addTest(BasicDriverTestSuite.suite());
        suite.addTest(UtilityCodeTestSuite.suite());
        suite.addTest(new TestSuite(PDFAConformanceTestCase.class));
        //$JUnit-END$
        return suite;
    }
}
