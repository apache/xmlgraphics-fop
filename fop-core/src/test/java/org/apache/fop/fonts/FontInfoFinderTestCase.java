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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.autodetect.FontInfoFinder;

public class FontInfoFinderTestCase {
    @Test
    public void testInvalidTTC() throws Exception {
        InternalResourceResolver rr = ResourceResolverFactory.createDefaultInternalResourceResolver(
                new File(".").toURI());
        File ttc = File.createTempFile("fop", ".ttc");
        File ttf = new File("test/resources/fonts/ttf/glb12.ttf");
        byte[] ttfBytes = IOUtils.toByteArray(new FileInputStream(ttf));
        new FileOutputStream(ttc).write(ttfBytes);
        EmbedFontInfo[] embedFontInfos = new FontInfoFinder().find(ttc.toURI(), rr, null);
        ttc.delete();
        Assert.assertNull(embedFontInfos);
    }

    @Test
    public void testOOMError() {
        InternalResourceResolver rr = ResourceResolverFactory.createInternalResourceResolver(new File(".").toURI(),
            new ResourceResolver() {
                public Resource getResource(URI uri) {
                    throw new Error();
                }
                public OutputStream getOutputStream(URI uri) {
                    return null;
                }
            });
        EmbedFontInfo[] embedFontInfos = new FontInfoFinder().find(new File(".").toURI(), rr, null);
        Assert.assertNull(embedFontInfos);
    }
}
