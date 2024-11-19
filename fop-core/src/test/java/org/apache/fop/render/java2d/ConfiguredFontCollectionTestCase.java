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
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
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

    @Test
    public void testLazyLoading() {
        List<EmbedFontInfo> list = Arrays.asList(
                addFont("test/resources/fonts/type1/c0419bt_.pfb", "test/resources/fonts/type1/c0419bt_.afm", true),
                addFont("x1", "x2", true));
        List<String> fileReads = new ArrayList<>();
        ConfiguredFontCollection collection = new ConfiguredFontCollection(getResourceResolver(fileReads), list, true);
        FontInfo fontInfo = new FontInfo();
        int numOfFonts = collection.setup(0, fontInfo);
        Assert.assertEquals(numOfFonts, 2);
        Assert.assertTrue(fileReads.isEmpty());
        Assert.assertEquals(fontInfo.getFonts().get("F0").getFontName(), "Courier10PitchBT-Roman");
        Assert.assertEquals(fileReads.size(), 3);
        Assert.assertTrue(fileReads.get(0).contains("c0419bt_.afm"));
        Assert.assertTrue(fileReads.get(2).contains("c0419bt_.pfb"));
        String ex = "";
        try {
            fontInfo.getFonts().get("F1").getFontName();
        } catch (RuntimeException e) {
            ex = e.getMessage();
        }
        Assert.assertTrue(ex.startsWith("Failed to read font file"));
        Assert.assertEquals(fileReads.size(), 4);
    }

    @Test
    public void testNoLazyLoading() {
        List<EmbedFontInfo> list = Arrays.asList(
                addFont("test/resources/fonts/type1/c0419bt_.pfb", "test/resources/fonts/type1/c0419bt_.afm", false),
                addFont("x1", "x2", false));
        List<String> fileReads = new ArrayList<>();
        ConfiguredFontCollection collection = new ConfiguredFontCollection(getResourceResolver(fileReads), list, true);
        FontInfo fontInfo = new FontInfo();
        int numOfFonts = collection.setup(0, fontInfo);
        Assert.assertEquals(numOfFonts, 2);
        Assert.assertEquals(fileReads.size(), 4);
        Assert.assertEquals(fontInfo.getFonts().get("F0").getFontName(), "Courier10PitchBT-Roman");
        Assert.assertTrue(fileReads.get(0).contains("c0419bt_.afm"));
        Assert.assertTrue(fileReads.get(2).contains("c0419bt_.pfb"));
        Assert.assertTrue(fileReads.get(3).contains("x2"));
    }

    private InternalResourceResolver getResourceResolver(final List<String> fileReads) {
        return ResourceResolverFactory.createInternalResourceResolver(new File(".").toURI(),
                new ResourceResolver() {
                    public Resource getResource(URI uri) throws IOException {
                        fileReads.add(uri.toASCIIString());
                        return new Resource(uri.toURL().openStream());
                    }
                    public OutputStream getOutputStream(URI uri) {
                        return null;
                    }
                });
    }

    private EmbedFontInfo addFont(String pfb, String afm, boolean lazyLoad) {
        File pfbFile = new File(pfb);
        File afmFile = new File(afm);
        FontUris fontUris = new FontUris(pfbFile.toURI(), null, afmFile.toURI(), null);
        return new EmbedFontInfo(fontUris, true, true, new ArrayList<FontTriplet>(), null,
                EncodingMode.AUTO, EmbeddingMode.AUTO, false, false, true, lazyLoad);
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
