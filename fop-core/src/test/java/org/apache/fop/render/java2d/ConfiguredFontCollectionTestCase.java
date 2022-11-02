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
package org.apache.fop.render.java2d;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontUris;

public class ConfiguredFontCollectionTestCase {
    @Test
    public void testConfiguredFontCollection() throws IOException {
        File pfb = getFontFileNoExension("test/resources/fonts/type1/c0419bt_.pfb");
        File afm = getFontFileNoExension("test/resources/fonts/type1/c0419bt_.afm");
        Assert.assertFalse(pfb.getName().endsWith(".pfb"));
        try {
            FontUris fontUris = new FontUris(pfb.toURI(), null, afm.toURI(), null);
            EmbedFontInfo e = new EmbedFontInfo(fontUris, true, true, new ArrayList<FontTriplet>(), null);
            List<EmbedFontInfo> x = Collections.singletonList(e);
            InternalResourceResolver rr =
                    ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI());
            ConfiguredFontCollection c = new ConfiguredFontCollection(rr, x, true);
            FontInfo fi = new FontInfo();
            int num = c.setup(0, fi);
            Assert.assertEquals(num, 1);
            Assert.assertEquals(fi.getFonts().values().iterator().next().getFontName(), "Courier10PitchBT-Roman");
        } finally {
            pfb.delete();
            afm.delete();
        }
    }

    private File getFontFileNoExension(String s) throws IOException {
        FileInputStream pfb = new FileInputStream(s);
        File tmp = File.createTempFile("fop", "font");
        FileOutputStream os = new FileOutputStream(tmp);
        IOUtils.copy(pfb, os);
        os.close();
        pfb.close();
        return tmp;
    }
}
