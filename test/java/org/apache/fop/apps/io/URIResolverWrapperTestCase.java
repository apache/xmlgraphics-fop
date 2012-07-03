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
package org.apache.fop.apps.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class URIResolverWrapperTestCase {

    private static final List<String> BASE_URIS = Collections.unmodifiableList(Arrays.asList(
                 new String[] {
                         ".",
                         "../",
                         "some/path",
                         "file:///absolute/file/path"}
                 ));

    private URI base;

    @Before
    public void setup() throws URISyntaxException {
        setBase(".");
    }

    private void setBase(String baseStr) throws URISyntaxException {
        base = new URI(baseStr);
    }

    @Test
    public void testResolveIn() throws Exception {
        String[] uris = new String[] {".", "resource", "path/to/resource"};
        for (String base : BASE_URIS) {
            setBase(base);
            for (String uriStr : uris) {
                URI uri = new URI(uriStr);
                URI expected = resolveFromBase(uri);
                test(uriStr, uri, expected);
            }
        }
    }

    @Test
    public void testResolveInBadUri() throws Exception {
        String[] uris = new String[] {"path\\to\\resource", "bad resource name"};
        for (String base : BASE_URIS) {
            setBase(base);
            for (String uriStr : uris) {
                assertBadSyntax(uriStr);
                URI uri = cleanURI(uriStr);
                URI expected = resolveFromBase(uri);
                test(uriStr, uri, expected);
            }
        }
    }

    @Test
    public void getBaseURI() throws URISyntaxException {
        assertEquals(InternalResourceResolver.getBaseURI("x/y/z/"), new URI("x/y/z/"));
        assertEquals(InternalResourceResolver.getBaseURI("x/y/z"), new URI("x/y/z/"));
    }

    @Test
    public void cleanURI() throws URISyntaxException {
        String[] uris = new String[] {".", "path/to/resource", "path\\to\\resource",
                "bad resource name"};
        for (String uri : uris) {
            assertEquals(InternalResourceResolver.cleanURI(uri), cleanURI(uri));
        }
        assertNull(InternalResourceResolver.cleanURI(null));
    }

    private void test(String uriStr, URI uri, URI expected) throws IOException, URISyntaxException {
        ResourceResolver resolver = mock(ResourceResolver.class);
        InternalResourceResolver sut = new InternalResourceResolver(base, resolver);
        sut.getResource(uriStr);
        verify(resolver).getResource(eq(expected));
        resolver = mock(ResourceResolver.class);
        sut = new InternalResourceResolver(base, resolver);
        sut.getResource(uri);
        verify(resolver).getResource(eq(expected));
    }

    private URI resolveFromBase(URI uri) {
        return base.resolve(uri);
    }

    private URI cleanURI(String raw) throws URISyntaxException {
        String fixedUri = raw.replace('\\', '/');
        fixedUri = fixedUri.replace(" ", "%20");
        return new URI(fixedUri);
    }

    private void assertBadSyntax(String badUri) {
        try {
            new URI(badUri);
            fail(badUri + " is correctly formed.");
        } catch (URISyntaxException e) {
            // PASS
        }
    }
}
