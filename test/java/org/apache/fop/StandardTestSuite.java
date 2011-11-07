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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.apache.fop.afp.fonts.CharactersetEncoderTestCase;
import org.apache.fop.afp.parser.MODCAParserTestCase;
import org.apache.fop.area.ViewportTestSuite;
import org.apache.fop.fonts.DejaVuLGCSerifTestCase;
import org.apache.fop.fonts.truetype.GlyfTableTestCase;
import org.apache.fop.image.loader.batik.ImageLoaderTestCase;
import org.apache.fop.image.loader.batik.ImagePreloaderTestCase;
import org.apache.fop.intermediate.IFMimickingTestCase;
import org.apache.fop.render.extensions.prepress.PageBoundariesTestCase;
import org.apache.fop.render.extensions.prepress.PageScaleTestCase;
import org.apache.fop.render.pdf.PDFAConformanceTestCase;
import org.apache.fop.render.pdf.PDFCMapTestCase;
import org.apache.fop.render.pdf.PDFEncodingTestCase;
import org.apache.fop.render.pdf.PDFsRGBSettingsTestCase;
import org.apache.fop.render.pdf.RenderPDFTestSuite;
import org.apache.fop.render.ps.PSTestSuite;
import org.apache.fop.render.rtf.RichTextFormatTestSuite;
import org.apache.fop.traits.MinOptMaxTestCase;

/**
 * Test suite for basic functionality of FOP.
 */
@RunWith(Suite.class)
@SuiteClasses({
    BasicDriverTestSuite.class,
    UtilityCodeTestSuite.class,
    PDFAConformanceTestCase.class,
    PDFEncodingTestCase.class,
    PDFCMapTestCase.class,
    PDFsRGBSettingsTestCase.class,
    DejaVuLGCSerifTestCase.class,
    RichTextFormatTestSuite.class,
    ImageLoaderTestCase.class,
    ImagePreloaderTestCase.class,
    IFMimickingTestCase.class,
    PageBoundariesTestCase.class,
    PageScaleTestCase.class,
    org.apache.fop.afp.AFPTestSuite.class,
    GlyfTableTestCase.class,
    ViewportTestSuite.class,
    RenderPDFTestSuite.class,
    MODCAParserTestCase.class,
    CharactersetEncoderTestCase.class,
    org.apache.fop.render.afp.AFPTestSuite.class,
    PSTestSuite.class,
    MinOptMaxTestCase.class
})
public class StandardTestSuite {
}
