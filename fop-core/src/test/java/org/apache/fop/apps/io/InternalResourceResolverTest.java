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

package org.apache.fop.apps.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.xmlgraphics.io.ResourceResolver;
import org.junit.Test;

public class InternalResourceResolverTest {

    /**
     * Represents the {@link InternalResourceResolver} without the JAR scheme patch.
     */
    private class UnpatchedInternalResourceResolver extends InternalResourceResolver {

        public UnpatchedInternalResourceResolver(final URI baseUri, final ResourceResolver resourceResolver) {
            super(baseUri, resourceResolver);
        }

        @Override
        public URI resolveFromBase(final URI uri) {
            final URI baseUri = super.getBaseURI();
            return baseUri.resolve(uri);
        }
    }

    @Test
    public void unpatchedResolveBaseURIWithSimpleScheme() throws URISyntaxException {
        final UnpatchedInternalResourceResolver upirr =
                new UnpatchedInternalResourceResolver(new URI("file:/path/"), null);
        assertEquals(upirr.resolveFromBase(new URI("test/myFile.png")), new URI("file:/path/test/myFile.png"));
    }

    @Test
    public void unpatchedResolveBaseURIWithExtendedJarScheme() throws URISyntaxException {
        final UnpatchedInternalResourceResolver upirr =
                new UnpatchedInternalResourceResolver(new URI("jar:file:/file.jar!/path/"), null);
        assertNotEquals(upirr.resolveFromBase(new URI("test/myFile.png")),
                new URI("jar:file:/file.jar!/path/test/myFile.png"));
    }

    @Test
    public void patchedResolveBaseURIWithSimpleScheme() throws URISyntaxException {
        final InternalResourceResolver pirr = new InternalResourceResolver(new URI("file:/path/"), null);
        assertEquals(pirr.resolveFromBase(new URI("test/myFile.png")), new URI("file:/path/test/myFile.png"));
    }

    @Test
    public void patchedResolveBaseURIWithExtendedJarScheme() throws URISyntaxException {
        final InternalResourceResolver pirr = new InternalResourceResolver(new URI("jar:file:/file.jar!/path/"), null);
        assertEquals(pirr.resolveFromBase(new URI("test/myFile.png")),
                new URI("jar:file:/file.jar!/path/test/myFile.png"));
    }

    @Test
    public void patchedResolveBaseURIWithoutScheme() throws URISyntaxException {
        final InternalResourceResolver pirr = new InternalResourceResolver(new URI("path/"), null);
        assertEquals(pirr.resolveFromBase(new URI("test/myFile.png")), new URI("path/test/myFile.png"));
    }

}
