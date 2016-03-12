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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.apache.fop.render.ps.fonts.PSTTFGeneratorTestCase;
import org.apache.fop.render.ps.fonts.PSTTFGlyphOutputStreamTestCase;
import org.apache.fop.render.ps.fonts.PSTTFOutputStreamTestCase;
import org.apache.fop.render.ps.fonts.PSTTFTableOutputStreamTestCase;


/**
 * A test Suite for org.apache.fop.render.ps.*
 */
@RunWith(Suite.class)
@SuiteClasses({
        PSTTFGeneratorTestCase.class,
        PSTTFOutputStreamTestCase.class,
        PSTTFGlyphOutputStreamTestCase.class,
        PSTTFTableOutputStreamTestCase.class
})
public final class RenderPSTestSuite {
}
