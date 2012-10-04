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

package org.apache.fop.fonts;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;

public class FontManagerTestCase {

    private FontManager sut;
    private FontCacheManager fontCacheManager;
    private FontDetector fontDetector;
    private InternalResourceResolver resolver;

    @Before
    public void setUp() {
        resolver = mock(InternalResourceResolver.class);
        fontCacheManager = mock(FontCacheManager.class);
        fontDetector = mock(FontDetector.class);

        sut = new FontManager(resolver, fontDetector, fontCacheManager);
    }

    @Test
    public void testSetCacheFile() {
        URI testURI = URI.create("test/uri");
        sut.setCacheFile(testURI);

        InOrder inOrder = inOrder(resolver, fontCacheManager);
        inOrder.verify(resolver).resolveFromBase(testURI);
        inOrder.verify(fontCacheManager).setCacheFile(any(URI.class));
    }

    @Test
    public void testGetFontCache() {
        sut.getFontCache();
        verify(fontCacheManager).load();
    }

    @Test
    public void testSaveCache() throws FOPException {
        sut.saveCache();
        verify(fontCacheManager).save();
    }

    @Test
    public void testDeleteCache() throws FOPException {
        sut.deleteCache();
        verify(fontCacheManager).delete();
    }
}
