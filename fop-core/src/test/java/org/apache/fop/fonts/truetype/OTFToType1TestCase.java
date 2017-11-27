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
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fontbox.type1.Type1Font;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.CFFToType1Font;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.FontUris;

public class OTFToType1TestCase {

    @Test
    public void testFont() throws IOException {
        Type1Font t1 = getFont("test/resources/fonts/otf/SourceSansProBold.otf");
        Assert.assertEquals(t1.getFontName(), "SourceSansPro-Bold.0");
        Assert.assertEquals(t1.getCharStringsDict().keySet().toString(), "[.notdef, d]");
        t1 = getFont("test/resources/fonts/otf/AlexBrushRegular.otf");
        Assert.assertEquals(t1.getFontName(), "AlexBrush-Regular.0");
    }

    @Test
    public void testFontType() throws IOException {
        CustomFont t1 = getRealFont("test/resources/fonts/otf/SourceSansProBold.otf");
        Assert.assertEquals(t1.getFontType(), FontType.TYPE1);
    }

    private Type1Font getFont(String s) throws IOException {
        InputStream is = ((CFFToType1Font)getRealFont(s)).getInputStreams().get(0);
        return Type1Font.createWithPFB(is);
    }

    private CustomFont getRealFont(String s) throws IOException {
        InternalResourceResolver rr = ResourceResolverFactory.createDefaultInternalResourceResolver(
                new File(".").toURI());
        CustomFont realFont = FontLoader.loadFont(new FontUris(new File(s).toURI(), null), null, true,
                EmbeddingMode.SUBSET, EncodingMode.AUTO, true, true, rr, false, true);
        realFont.mapChar('d');
        return realFont;
    }
}
