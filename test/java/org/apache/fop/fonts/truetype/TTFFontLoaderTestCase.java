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

package org.apache.fop.fonts.truetype;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.apps.io.URIResolverWrapper;
import org.apache.fop.fonts.EncodingMode;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test case for {@link TTFFontLoader}.
 */
public class TTFFontLoaderTestCase {

    @Test
    public void testUseKerning() throws IOException {
        boolean useComplexScriptFeatures = false;
        File file = new File("test/resources/fonts/ttf/DejaVuLGCSerif.ttf");
        URI absoluteFilePath = file.toURI();
        URIResolverWrapper resolver = ResourceResolverFactory.createDefaultWrapper();
        String fontName = "Deja Vu";
        boolean embedded = false;
        boolean useKerning = true;

        TTFFontLoader fontLoader = new TTFFontLoader(absoluteFilePath, fontName, embedded,
                EncodingMode.AUTO, useKerning, useComplexScriptFeatures, resolver);
        assertTrue(fontLoader.getFont().hasKerningInfo());
        useKerning = false;

        fontLoader = new TTFFontLoader(absoluteFilePath, fontName, embedded, EncodingMode.AUTO,
                useKerning, useComplexScriptFeatures, resolver);
        assertFalse(fontLoader.getFont().hasKerningInfo());
    }
}
