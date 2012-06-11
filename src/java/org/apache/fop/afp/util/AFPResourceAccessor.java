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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.fop.apps.io.URIResolverWrapper;

/**
 * Defines an interface through which external resource objects can be accessed.
 */
public final class AFPResourceAccessor {

    private final URIResolverWrapper resolver;
    private final String baseURI;

    /**
     * Constructor for resource to be accessed via the {@link FOUserAgent}. This contructor
     * can take two base URIs: the category base URI is the one to use when differentiating between
     * normal resources (ex. images) and font resources. So, if fonts need to be accessed, you can
     * set the {@link org.apache.fop.fonts.FontManager}'s base URI instead of the one on the
     * {@link org.apache.fop.apps.FopFactory}.
     * @param userAgent the FO user agent
     * @param categoryBaseURI the category base URI (may be null)
     * @param baseURI the custom base URI to resolve relative URIs against (may be null)
     */
    public AFPResourceAccessor(URIResolverWrapper resolver, String baseURI) {
        this.resolver = resolver;
        this.baseURI = baseURI;
    }

    public AFPResourceAccessor(URIResolverWrapper resolver) {
        this.resolver = resolver;
        this.baseURI = null;
    }

    private URI getResourceURI(URI uri) {
        if (baseURI == null) {
            return uri;
        }
        try {
            URI baseURI = URIResolverWrapper.getBaseURI(this.baseURI);
            return baseURI.resolve(uri);
        } catch (URISyntaxException use) {
            return uri;
        }
    }

    /** {@inheritDoc} */
    public InputStream createInputStream(URI uri) throws IOException {
        return resolver.resolveIn(getResourceURI(uri));
    }

}
