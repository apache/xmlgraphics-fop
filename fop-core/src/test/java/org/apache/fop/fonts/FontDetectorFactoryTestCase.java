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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;

public class FontDetectorFactoryTestCase {

    @Test
    public void testDetect() throws FOPException, URISyntaxException {
        FontDetector factory = FontDetectorFactory.createDefault();

        InternalResourceResolver mockInternalResourceResolver = mock(InternalResourceResolver.class);
        when(mockInternalResourceResolver.getBaseURI()).thenReturn(new URI("exist://localhost"));

        FontManager mockFontManager = mock(FontManager.class);
        when(mockFontManager.getResourceResolver()).thenReturn(mockInternalResourceResolver);
        FontAdder mockFontAdder = mock(FontAdder.class);
        FontEventListener mockEventListener = mock(FontEventListener.class);

        boolean exceptionThrown = false;
        try {
            factory.detect(mockFontManager, mockFontAdder, false, mockEventListener, new ArrayList<>());
        } catch (Exception e) {
            if (!(e instanceof MalformedURLException)) {
                throw e; // Re-throw unexpected exceptions
            }
            exceptionThrown = true;
        }

        assertFalse("If the URL is malformed, we should just log a warning", exceptionThrown);
    }
}
