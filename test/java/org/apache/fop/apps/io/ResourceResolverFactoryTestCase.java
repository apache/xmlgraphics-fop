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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ResourceResolverFactoryTestCase {

    private static final byte[] DATA = new byte[]{(byte) 0, (byte) 1, (byte) 2};

    private void writeDataTo(File f) throws IOException {
        writeDataTo(new FileOutputStream(f));
    }

    private void writeDataTo(OutputStream os) throws IOException {
        os.write(DATA);
        os.close();
    }

    private void checkStream(InputStream inputStream) throws IOException {
        byte[] actual = new byte[DATA.length];
        for (int i = 0; i < DATA.length; i++) {
            actual[i] = (byte) inputStream.read();
        }
        assertEquals(-1, inputStream.read());
        assertArrayEquals(DATA, actual);
    }

    @Test
    public void testDefaultResourceResolverGetResource() throws Exception {
        ResourceResolver sut = ResourceResolverFactory.createDefaultResourceResolver();
        File inputFile = File.createTempFile("prefix", "suffix");
        InputStream is = null;
        try {
            writeDataTo(inputFile);
            is = sut.getResource(inputFile.toURI());
            checkStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
            inputFile.delete();
        }
    }

    @Test
    public void testDefaultResourceResolverGetOutput() throws Exception {
        ResourceResolver sut = ResourceResolverFactory.createDefaultResourceResolver();
        File outputFile = File.createTempFile("prefix", "suffix");
        writeDataTo(sut.getOutputStream(outputFile.toURI()));
        InputStream is = new FileInputStream(outputFile);
        try {
            checkStream(is);
        } finally {
            is.close();
        }
    }

    private static class TestCreateTempAwareResourceResolverHelper implements ResourceResolver {

        final TempResourceResolver tempResourceResolver = mock(TempResourceResolver.class);

        final ResourceResolver defaultResourceResolver = mock(ResourceResolver.class);

        final ResourceResolver sut = ResourceResolverFactory.createTempAwareResourceResolver(
                tempResourceResolver, defaultResourceResolver);

        public Resource getResource(URI uri) throws IOException {
            return sut.getResource(uri);
        }
        public OutputStream getOutputStream(URI uri) throws IOException {
            return sut.getOutputStream(uri);
        }
    }

    @Test
    public void testCreateTempAwareResourceResolverForTmpResource() throws Exception {
        URI uri = URI.create("tmp:///id");
        TestCreateTempAwareResourceResolverHelper helper = new TestCreateTempAwareResourceResolverHelper();
        helper.getResource(uri);
        verify(helper.tempResourceResolver, times(1)).getResource(uri.getPath());
        verify(helper.defaultResourceResolver, never()).getResource(uri);
    }

    @Test
    public void testCreateTempAwareResourceResolverForRegularResource() throws Exception {
        URI uri = URI.create("file:///path/to/file");
        TestCreateTempAwareResourceResolverHelper helper = new TestCreateTempAwareResourceResolverHelper();
        helper.getResource(uri);
        verify(helper.tempResourceResolver, never()).getResource(uri.getPath());
        verify(helper.defaultResourceResolver, times(1)).getResource(uri);
    }

    @Test
    public void testCreateTempAwareResourceResolverForTmpOuput() throws Exception {
        URI uri = URI.create("tmp:///id");
        TestCreateTempAwareResourceResolverHelper helper = new TestCreateTempAwareResourceResolverHelper();
        helper.getOutputStream(uri);
        verify(helper.tempResourceResolver, times(1)).getOutputStream(uri.getPath());
        verify(helper.defaultResourceResolver, never()).getOutputStream(uri);
    }

    @Test
    public void testCreateTempAwareResourceResolverForRegularOutput() throws Exception {
        URI uri = URI.create("file:///path/to/file");
        TestCreateTempAwareResourceResolverHelper helper = new TestCreateTempAwareResourceResolverHelper();
        helper.getOutputStream(uri);
        verify(helper.tempResourceResolver, never()).getOutputStream(uri.getPath());
        verify(helper.defaultResourceResolver, times(1)).getOutputStream(uri);
    }

    @Test
    public void testCreateSchemaAwareResourceResolverForDefaultResource() throws Exception {
        URI uri = URI.create("file:///path/to/file");
        TestCreateSchemaAwareResourceResolverBuilderHelper helper
        = new TestCreateSchemaAwareResourceResolverBuilderHelper();
        helper.getResource(uri);
        verify(helper.registedResourceResolver, never()).getResource(uri);
        verify(helper.defaultResourceResolver, times(1)).getResource(uri);
    }

    @Test
    public void testCreateSchemaAwareResourceResolverForRegisteredResource() throws Exception {
        URI uri = URI.create(TestCreateSchemaAwareResourceResolverBuilderHelper.SCHEMA + ":///path");
        TestCreateSchemaAwareResourceResolverBuilderHelper helper
        = new TestCreateSchemaAwareResourceResolverBuilderHelper();
        helper.getResource(uri);
        verify(helper.registedResourceResolver, times(1)).getResource(uri);
        verify(helper.defaultResourceResolver, never()).getResource(uri);
    }

    @Test
    public void testCreateSchemaAwareResourceResolverForDefaultOutput() throws Exception {
        URI uri = URI.create("file:///path/to/file");
        TestCreateSchemaAwareResourceResolverBuilderHelper helper
        = new TestCreateSchemaAwareResourceResolverBuilderHelper();
        helper.getOutputStream(uri);
        verify(helper.registedResourceResolver, never()).getOutputStream(uri);
        verify(helper.defaultResourceResolver, times(1)).getOutputStream(uri);
    }

    @Test
    public void testCreateSchemaAwareResourceResolverForRegisteredOutput() throws Exception {
        URI uri = URI.create(TestCreateSchemaAwareResourceResolverBuilderHelper.SCHEMA + ":///path");
        TestCreateSchemaAwareResourceResolverBuilderHelper helper
        = new TestCreateSchemaAwareResourceResolverBuilderHelper();
        helper.getOutputStream(uri);
        verify(helper.registedResourceResolver, times(1)).getOutputStream(uri);
        verify(helper.defaultResourceResolver, never()).getOutputStream(uri);
    }

    private static class TestCreateSchemaAwareResourceResolverBuilderHelper implements ResourceResolver {

        private static final String SCHEMA = "protocol";

        final ResourceResolver registedResourceResolver = mock(ResourceResolver.class);

        final ResourceResolver defaultResourceResolver = mock(ResourceResolver.class);

        final ResourceResolver sut;

        TestCreateSchemaAwareResourceResolverBuilderHelper() {
            ResourceResolverFactory.SchemaAwareResourceResolverBuilder builder
                    = ResourceResolverFactory.createSchemaAwareResourceResolverBuilder(
                            defaultResourceResolver);
            builder.registerResourceResolverForSchema(SCHEMA, registedResourceResolver);
            sut = builder.build();

        }

        public Resource getResource(URI uri) throws IOException {
            return sut.getResource(uri);
        }
        public OutputStream getOutputStream(URI uri) throws IOException {
            return sut.getOutputStream(uri);
        }
    }

}

