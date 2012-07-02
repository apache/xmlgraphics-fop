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

package org.apache.fop.fonts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.apache.fop.fonts.truetype.FontFileReaderTestCase;
import org.apache.fop.fonts.truetype.TTFFileTestCase;
import org.apache.fop.fonts.truetype.TTFSubSetFileTestCase;
import org.apache.fop.fonts.truetype.TTFTableNameTestCase;

/**
 * A test suite designed for org.apache.fop.fonts.*
 */
@RunWith(Suite.class)
@SuiteClasses({
        EncodingModeTestCase.class,
        FontFileReaderTestCase.class,
        TTFFileTestCase.class,
        TTFSubSetFileTestCase.class,
        TTFTableNameTestCase.class })
public final class FOPFontsTestSuite {
}
