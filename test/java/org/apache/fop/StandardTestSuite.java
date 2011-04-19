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

package org.apache.fop;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.fop.area.ViewportTestSuite;
import org.apache.fop.fonts.DejaVuLGCSerifTest;
import org.apache.fop.image.loader.batik.ImageLoaderTestCase;
import org.apache.fop.image.loader.batik.ImagePreloaderTestCase;
import org.apache.fop.intermediate.IFMimickingTestCase;
import org.apache.fop.render.afp.AFPTestSuite;
import org.apache.fop.render.extensions.prepress.PageBoundariesTest;
import org.apache.fop.render.extensions.prepress.PageScaleTest;
import org.apache.fop.render.pdf.PDFAConformanceTestCase;
import org.apache.fop.render.pdf.PDFCMapTestCase;
import org.apache.fop.render.pdf.PDFEncodingTestCase;
import org.apache.fop.render.pdf.PDFsRGBSettingsTestCase;
import org.apache.fop.render.ps.PSTestSuite;
import org.apache.fop.render.rtf.RichTextFormatTestSuite;
import org.apache.fop.traits.MinOptMaxTest;

/**
 * Test suite for basic functionality of FOP.
 */
public class StandardTestSuite {

    /**
     * Builds the test suite
     * @return the test suite
     */
    public static Test suite() {
        TestSuite suite = new TestSuite("Basic functionality test suite for FOP");
        //$JUnit-BEGIN$
        suite.addTest(BasicDriverTestSuite.suite());
        suite.addTest(UtilityCodeTestSuite.suite());
        suite.addTest(new TestSuite(PDFAConformanceTestCase.class));
        suite.addTest(new TestSuite(PDFEncodingTestCase.class));
        suite.addTest(new TestSuite(PDFCMapTestCase.class));
        suite.addTest(new TestSuite(PDFsRGBSettingsTestCase.class));
        suite.addTest(new TestSuite(DejaVuLGCSerifTest.class));
        suite.addTest(AFPTestSuite.suite());
        suite.addTest(PSTestSuite.suite());
        suite.addTest(RichTextFormatTestSuite.suite());
        suite.addTest(new TestSuite(ImageLoaderTestCase.class));
        suite.addTest(new TestSuite(ImagePreloaderTestCase.class));
        suite.addTest(new TestSuite(IFMimickingTestCase.class));
        suite.addTest(new TestSuite(PageBoundariesTest.class));
        suite.addTest(new TestSuite(PageScaleTest.class));
        suite.addTest(new TestSuite(MinOptMaxTest.class));
        suite.addTest(ViewportTestSuite.suite());
        //$JUnit-END$
        return suite;
    }
}
