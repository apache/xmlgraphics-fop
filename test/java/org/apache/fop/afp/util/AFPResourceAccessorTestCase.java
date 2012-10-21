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

package org.apache.fop.afp.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.fop.apps.io.InternalResourceResolver;

public class AFPResourceAccessorTestCase {

    private InternalResourceResolver nullBaseResourceResolver;
    private InternalResourceResolver absoluteBaseResourceResolver;
    private InternalResourceResolver relativeBaseResourceResolver;
    private final URI absoluteBaseURI = URI.create("this:///purely.for.testing");
    private final URI relativeBaseURI = URI.create("./this.is.purely.for.testing");
    private AFPResourceAccessor nullBaseURISut;
    private AFPResourceAccessor absoluteBaseURISut;
    private AFPResourceAccessor relativeBaseURISut;

    @Before
    public void setUp() {
        nullBaseResourceResolver = mock(InternalResourceResolver.class);
        absoluteBaseResourceResolver = mock(InternalResourceResolver.class);
        relativeBaseResourceResolver = mock(InternalResourceResolver.class);
        nullBaseURISut = new AFPResourceAccessor(nullBaseResourceResolver);
        absoluteBaseURISut = new AFPResourceAccessor(absoluteBaseResourceResolver,
                absoluteBaseURI.toASCIIString());
        relativeBaseURISut = new AFPResourceAccessor(relativeBaseResourceResolver,
                relativeBaseURI.toASCIIString());
    }

    @Test
    public void testCreateInputStream() throws IOException, URISyntaxException {
        URI testURI = URI.create("test");
        nullBaseURISut.createInputStream(testURI);
        verify(nullBaseResourceResolver).getResource(testURI);

        absoluteBaseURISut.createInputStream(testURI);
        verify(absoluteBaseResourceResolver).getResource(getActualURI(absoluteBaseURI, testURI));

        relativeBaseURISut.createInputStream(testURI);
        verify(relativeBaseResourceResolver).getResource(getActualURI(relativeBaseURI, testURI));
    }

    private URI getActualURI(URI baseURI, URI testURI) throws URISyntaxException {
        return InternalResourceResolver.getBaseURI(baseURI.toASCIIString()).resolve(testURI);
    }

    @Test
    public void testResolveURI() throws URISyntaxException {
        String testURI = "anotherTestURI";
        assertEquals(URI.create("./" + testURI), nullBaseURISut.resolveURI(testURI));

        assertEquals(getActualURI(absoluteBaseURI, URI.create(testURI)),
                absoluteBaseURISut.resolveURI(testURI));

        assertEquals(getActualURI(relativeBaseURI, URI.create(testURI)),
                relativeBaseURISut.resolveURI(testURI));
    }
}
