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
package org.apache.fop.fonts.type1;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.io.ResourceResolverFactory;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontUris;

public class Type1FontLoaderTestCase {
    @Test
    public void testBoundingBox() throws IOException {
        File pfb = new File("test/resources/fonts/type1/c0419bt_.pfb");
        File pfbNoAFM = File.createTempFile("fop", "pfb");
        File pfm = File.createTempFile("fop", "pfm");
        try {
            IOUtils.copy(new FileInputStream(pfb), new FileOutputStream(pfbNoAFM));

            FileOutputStream fos = new FileOutputStream(pfm);
            fos.write(new byte[512]);
            fos.close();

            FontUris fontUris = new FontUris(pfbNoAFM.toURI(), null, null, pfm.toURI());
            CustomFont x = FontLoader.loadFont(fontUris, null, true, EmbeddingMode.AUTO, EncodingMode.AUTO, true, true,
                    ResourceResolverFactory.createDefaultInternalResourceResolver(new File(".").toURI()), false, false);
            Assert.assertEquals(x.getBoundingBox(0, 12).getBounds(), new Rectangle(-240, -60, 0, 60));
        } finally {
            pfbNoAFM.delete();
            pfm.delete();
        }
    }
}
