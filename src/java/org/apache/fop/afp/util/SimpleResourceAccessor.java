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

package org.apache.fop.afp.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Simple implementation of the {@link ResourceAccessor} interface for access relative to a
 * base URI.
 */
public class SimpleResourceAccessor implements ResourceAccessor {

    private URI baseURI;

    /**
     * Creates a new simple resource accessor.
     * @param baseURI the base URI to resolve relative URIs against (may be null)
     */
    public SimpleResourceAccessor(URI baseURI) {
        this.baseURI = baseURI;
    }

    /**
     * Creates a new simple resource accessor.
     * @param baseDir the base directory to resolve relative filenames against (may be null)
     */
    public SimpleResourceAccessor(File baseDir) {
        this(baseDir != null ? baseDir.toURI() : null);
    }

    /**
     * Returns the base URI.
     * @return the base URI (or null if no base URI was set)
     */
    public URI getBaseURI() {
        return this.baseURI;
    }

    /**
     * Resolve the given URI against the baseURI.
     * @param uri the URI to resolve
     * @return the resolved URI
     */
    protected URI resolveAgainstBase(URI uri) {
        return (getBaseURI() != null ? getBaseURI().resolve(uri) : uri);
    }

    /** {@inheritDoc} */
    public InputStream createInputStream(URI uri) throws IOException {
        URI resolved = resolveAgainstBase(uri);
        URL url = resolved.toURL();
        return url.openStream();
    }

}
