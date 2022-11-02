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
package org.apache.fop.render.pcl.fonts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.CMapSegment;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.render.java2d.CustomFontMetricsMapper;

public class PCLSoftFontManagerTestCase {
    @Test
    public void testSplitCompositeGlyphs() throws Exception {
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        PCLSoftFontManager pclSoftFontManager = new PCLSoftFontManager(new HashMap());
        MultiByteFont mbf = new MultiByteFont(ua.getResourceResolver(), EmbeddingMode.SUBSET);
        mbf.setEmbedURI(new URI("test/resources/fonts/ttf/DejaVuLGCSerif.ttf"));
        mbf.setFontType(FontType.TRUETYPE);
        CMapSegment cMapSegment1 = new CMapSegment('a', 'a', 1);
        CMapSegment cMapSegment2 = new CMapSegment('\u00E0', '\u00E0', 2);
        mbf.setCMap(new CMapSegment[] {cMapSegment1, cMapSegment2});
        mbf.mapChar('a');
        mbf.mapChar('\u00E0');
        CustomFontMetricsMapper font = new CustomFontMetricsMapper(mbf);
        ByteArrayOutputStream bos = pclSoftFontManager.makeSoftFont(font, "");
        String[] fontStrings = bos.toString().split("DejaVu changes are in public domain");
        Assert.assertEquals(fontStrings.length, 3);
    }
}
