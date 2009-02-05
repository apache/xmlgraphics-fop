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
 * Simple implementation of the {@link ResourceAccessor} interface for access via files.
 */
public class SimpleResourceAccessor implements ResourceAccessor {

    private URI baseURI;

    /**
     * Creates a new simple resource accessor.
     * @param basePath the base path to resolve relative URIs to
     */
    public SimpleResourceAccessor(File basePath) {
        this.baseURI = basePath.toURI();
    }

    /**
     * Creates a new simple resource accessor.
     * @param basePath the base path to resolve relative URIs to
     */
    public SimpleResourceAccessor(String basePath) {
        this(new File(basePath));
    }

    /** {@inheritDoc} */
    public InputStream createInputStream(URI uri) throws IOException {
        URI resolved = this.baseURI.resolve(uri);
        URL url = resolved.toURL();
        return url.openStream();
    }

}
