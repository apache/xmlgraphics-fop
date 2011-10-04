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

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class DejaVuLGCSerifTest {

    private FontResolver fontResolver = FontManager.createMinimalFontResolver();
    private CustomFont font;

    /**
     * sets up the testcase by loading the DejaVu Font.
     * 
     * @throws Exception
     *             if the test fails.
     */
    @Before
    public void setUp() throws Exception {
        File file = new File("test/resources/fonts/DejaVuLGCSerif.ttf");
        font = FontLoader.loadFont(file, "", true, EncodingMode.AUTO,
                fontResolver);
    }

    /**
     * Simple test to see if font name was detected correctly.
     */
    @Test
    public void testFontName() {
        assertEquals("DejaVuLGCSerif", font.getFontName());
    }
}
