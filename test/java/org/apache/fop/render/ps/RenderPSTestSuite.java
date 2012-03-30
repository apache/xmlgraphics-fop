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

package org.apache.fop.render.ps;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.fop.render.ps.fonts.PSTTFGeneratorTest;
import org.apache.fop.render.ps.fonts.PSTTFGlyphOutputStreamTest;
import org.apache.fop.render.ps.fonts.PSTTFOutputStreamTest;
import org.apache.fop.render.ps.fonts.PSTTFTableOutputStreamTest;


/**
 * A test Suite for org.apache.fop.render.ps.*
 */
public final class RenderPSTestSuite {
    /**
     * Constructor.
     */
    private RenderPSTestSuite() {
    }

    /**
     * Testing org.apache.fop.render.ps.*
     * @return test
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        //$JUnit-BEGIN$
        suite.addTest(new TestSuite(PSTTFGeneratorTest.class));
        suite.addTest(new TestSuite(PSTTFOutputStreamTest.class));
        suite.addTest(new TestSuite(PSTTFGlyphOutputStreamTest.class));
        suite.addTest(new TestSuite(PSTTFTableOutputStreamTest.class));
        //$JUnit-END$
        return suite;
    }
}
