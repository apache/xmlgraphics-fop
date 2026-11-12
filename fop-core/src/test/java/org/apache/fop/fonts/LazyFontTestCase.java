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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;

public class LazyFontTestCase {
    @Test
    public void testFontError() throws URISyntaxException {
        FontUris fontUris = new FontUris(new URI("test"), null);
        InternalResourceResolver rr = ResourceResolverFactory.createDefaultInternalResourceResolver(new URI("."));
        LazyFont lazyFont = new LazyFont(new EmbedFontInfo(fontUris, true, true, null, null), rr, true);
        String ex = null;
        try {
            lazyFont.getAscender();
        } catch (RuntimeException e) {
            ex = e.getMessage();
        }
        Assert.assertEquals(ex, "Failed to read font file test URI is not absolute");
    }

    @Test
    public void testMissingFontFileGetRealFont() throws URISyntaxException {
        URI missing = new File("does-not-exist.ttf").getAbsoluteFile().toURI();
        FontUris fontUris = new FontUris(missing, null);
        InternalResourceResolver rr = ResourceResolverFactory.createDefaultInternalResourceResolver(new URI("."));
        LazyFont lazyFont = new LazyFont(new EmbedFontInfo(fontUris, true, true, null, null), rr, true);
        RuntimeException e = Assert.assertThrows(RuntimeException.class, lazyFont::getRealFont);
        Assert.assertEquals(RuntimeException.class, e.getClass());
        Assert.assertTrue(e.getMessage(), e.getMessage().startsWith("Failed to read font file " + missing + " "));
        Assert.assertTrue(e.getCause() instanceof FileNotFoundException);
    }
}
