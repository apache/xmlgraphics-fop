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

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

public class LazyFontTestCase {
    @Test
    public void testFontError() throws URISyntaxException {
        FontUris fontUris = new FontUris(new URI("test"), null);
        LazyFont lazyFont = new LazyFont(new EmbedFontInfo(fontUris, true, true, null, null), null, true);
        String ex = null;
        try {
            lazyFont.getAscender();
        } catch (RuntimeException e) {
            ex = e.getMessage();
        }
        Assert.assertEquals(ex, "Failed to read font file test");
    }
}
