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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ResourceResolverFactory {

    private ResourceResolverFactory() {
    }

    public static ResourceResolver createDefaultResourceResolver() {
        return DefaultResourceResolver.INSTANCE;
    }

    public static ResourceResolver createTempAwareResourceResolver(TempResourceResolver tempResourceResolver,
            ResourceResolver defaultResourceResolver) {
        return new TempAwareResourceResolver(tempResourceResolver, defaultResourceResolver);
    }

    public static URIResolverWrapper createDefaultWrapper() {
        // Not sure if this is the right place for this, but I don't have any better ideas as of yet
        URI thisUri = new File(".").getAbsoluteFile().toURI();
        return new URIResolverWrapper(thisUri, new DefaultResourceResolver());
    }

    public static SchemaAwareResourceResolverBuilder createSchemaAwareResourceResolverBuilder(
            ResourceResolver defaultResolver) {
        return new SchemaAwareResourceResolverBuilderImpl(defaultResolver);
    }



    private static final class DefaultResourceResolver implements ResourceResolver {

        private static final ResourceResolver INSTANCE = new DefaultResourceResolver();

        private final TempAwareResourceResolver delegate;

        private  DefaultResourceResolver() {
            delegate = new  TempAwareResourceResolver(new DefaultTempResourceResolver(),
                    new NormalResourceResolver());
        }

        public Resource getResource(URI uri) throws IOException {
            return delegate.getResource(uri);
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return delegate.getOutputStream(uri);
        }

    }

    private static final class TempAwareResourceResolver implements ResourceResolver {

        private final TempResourceResolver tempResourceResolver;

        private final ResourceResolver defaultResourceResolver;

        public TempAwareResourceResolver(TempResourceResolver tempResourceHandler,
                ResourceResolver defaultResourceResolver) {
            this.tempResourceResolver = tempResourceHandler;
            this.defaultResourceResolver = defaultResourceResolver;
        }

        private static boolean isTempUri(URI uri) {
            return TempResourceURIGenerator.isTempUri(uri);
        }

        public Resource getResource(URI uri) throws IOException {
            if (isTempUri(uri)) {
                return tempResourceResolver.getResource(uri.getPath());
            } else {
                return defaultResourceResolver.getResource(uri);
            }
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            if (isTempUri(uri)) {
                return tempResourceResolver.getOutputStream(uri.getPath());
            } else {
                return defaultResourceResolver.getOutputStream(uri);
            }
        }

    }

    private static class DefaultTempResourceResolver implements TempResourceResolver {
        private static File getTempFile(String path) throws IOException {
            File file = new File(System.getProperty("java.io.tmpdir"), path);
            file.deleteOnExit();
            return file;
        }

        public Resource getResource(String id) throws IOException {
            return new Resource(getTempFile(id).toURI().toURL().openStream());
        }

        public OutputStream getOutputStream(String id) throws IOException {
            File file = getTempFile(id);
            // TODO handle error
            file.createNewFile();
            return new FileOutputStream(file);
        }
    }

    private static class NormalResourceResolver implements ResourceResolver {
        public Resource getResource(URI uri) throws IOException {
            return new Resource(uri.toURL().openStream());
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return new FileOutputStream(new File(uri));
        }
    }

    private static final class SchemaAwareResourceResolver implements ResourceResolver {

        private final Map<String, ResourceResolver> schemaHandlingResourceResolvers;

        private final ResourceResolver defaultResolver;

        private SchemaAwareResourceResolver(Map<String, ResourceResolver> schemaHandlingResourceResolvers,
                ResourceResolver defaultResolver) {
            this.schemaHandlingResourceResolvers = schemaHandlingResourceResolvers;
            this.defaultResolver = defaultResolver;
        }

        private ResourceResolver getResourceResolverForSchema(URI uri) {
            String schema = uri.getScheme();
            if (schemaHandlingResourceResolvers.containsKey(schema)) {
                return schemaHandlingResourceResolvers.get(schema);
            } else {
                return defaultResolver;
            }
        }

        public Resource getResource(URI uri) throws IOException {
            return getResourceResolverForSchema(uri).getResource(uri);
        }

        public OutputStream getOutputStream(URI uri) throws IOException {
            return getResourceResolverForSchema(uri).getOutputStream(uri);
        }
    }

    public interface SchemaAwareResourceResolverBuilder {

        void registerResourceResolverForSchema(String schema, ResourceResolver resourceResolver);

        ResourceResolver build();
    }

    private static final class CompletedSchemaAwareResourceResolverBuilder
    implements SchemaAwareResourceResolverBuilder {

        private static final SchemaAwareResourceResolverBuilder INSTANCE
                = new CompletedSchemaAwareResourceResolverBuilder();

        public ResourceResolver build() {
            throw new IllegalStateException("Resource resolver already built");
        }

        public void registerResourceResolverForSchema(String schema,
                ResourceResolver resourceResolver) {
            throw new IllegalStateException("Resource resolver already built");
        }
    }

    private static final class ActiveSchemaAwareResourceResolverBuilder
    implements SchemaAwareResourceResolverBuilder {

        private final Map<String, ResourceResolver> schemaHandlingResourceResolvers
                = new HashMap<String, ResourceResolver>();

        private final ResourceResolver defaultResolver;

        private ActiveSchemaAwareResourceResolverBuilder(ResourceResolver defaultResolver) {
            this.defaultResolver = defaultResolver;
        }

        public void registerResourceResolverForSchema(String schema, ResourceResolver resourceResolver) {
            schemaHandlingResourceResolvers.put(schema, resourceResolver);
        }

        public ResourceResolver build() {
            return new SchemaAwareResourceResolver(
                    Collections.unmodifiableMap(schemaHandlingResourceResolvers), defaultResolver);
        }

    }

    private static final class SchemaAwareResourceResolverBuilderImpl
    implements SchemaAwareResourceResolverBuilder {

        private SchemaAwareResourceResolverBuilder delegate;

        private SchemaAwareResourceResolverBuilderImpl(ResourceResolver defaultResolver) {
            this.delegate = new ActiveSchemaAwareResourceResolverBuilder(defaultResolver);
        }

        public void registerResourceResolverForSchema(String schema, ResourceResolver resourceResolver) {
            delegate.registerResourceResolverForSchema(schema, resourceResolver);
        }

        public ResourceResolver build() {
            ResourceResolver resourceResolver = delegate.build();
            delegate = CompletedSchemaAwareResourceResolverBuilder.INSTANCE;
            return resourceResolver;
        }
    }

}
