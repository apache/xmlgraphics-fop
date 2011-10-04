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

import org.apache.fop.events.BasicEventTestCase;
import org.apache.fop.pdf.FileIDGeneratorTestCase;
import org.apache.fop.pdf.PDFEncryptionJCETestCase;
import org.apache.fop.pdf.PDFFactoryTestCase;
import org.apache.fop.pdf.PDFObjectTestCase;
import org.apache.fop.traits.BorderPropsTestCase;
import org.apache.fop.util.BitmapImageUtilTestCase;
import org.apache.fop.util.ColorUtilTestCase;
import org.apache.fop.util.ElementListUtilsTestCase;
import org.apache.fop.util.PDFNumberTestCase;
import org.apache.fop.util.XMLResourceBundleTestCase;

/**
 * Test suite for FOP's utility classes.
 */
@RunWith(Suite.class)
@SuiteClasses({
    PDFNumberTestCase.class,
    PDFObjectTestCase.class,
    ColorUtilTestCase.class,
    BorderPropsTestCase.class,
    ElementListUtilsTestCase.class,
    BasicEventTestCase.class,
    XMLResourceBundleTestCase.class,
    URIResolutionTestCase.class,
    FileIDGeneratorTestCase.class,
    PDFFactoryTestCase.class,
    PDFEncryptionJCETestCase.class,
    BitmapImageUtilTestCase.class
})
public class UtilityCodeTestSuite {
}
