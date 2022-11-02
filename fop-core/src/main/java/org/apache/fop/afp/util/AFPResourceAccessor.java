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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.io.InternalResourceResolver;

/**
 * Defines an interface through which external resource objects can be accessed.
 */
public final class AFPResourceAccessor {

    private static final Log log = LogFactory.getLog(AFPResourceAccessor.class);

    private final InternalResourceResolver resourceResolver;
    private final URI baseURI;
    private final URIResolver uriResolver;

    /**
     * Constructor for resource to be accessed via the {@link org.apache.fop.apps.FOUserAgent}. This
     * contructor takes a base URI for resolving font resource URIs. So, if fonts need to be
     * accessed, you can set the {@link org.apache.fop.fonts.FontManager}'s base URI instead of the
     * one on the {@link org.apache.fop.apps.FopFactory}.
     *
     * @param resourceResolver the resolver of resources
     * @param baseURI the custom base URI to resolve relative URIs against (may be null)
     */
    public AFPResourceAccessor(InternalResourceResolver resourceResolver, String baseURI) {
        this.resourceResolver = resourceResolver;
        URI actualBaseURI = null;
        URIResolver uriResolver;
        if (baseURI == null) {
            actualBaseURI = null;
            uriResolver = new NullBaseURIResolver();
        } else {
            try {
                actualBaseURI = InternalResourceResolver.getBaseURI(baseURI);
                uriResolver = new BaseURIResolver();
            } catch (URISyntaxException use) {
                log.error("The URI given \"" + baseURI + "\" is invalid: " + use.getMessage());
                actualBaseURI = null;
                uriResolver = new NullBaseURIResolver();
            }
        }
        this.baseURI = actualBaseURI;
        this.uriResolver = uriResolver;
    }

    /**
     * Constructor for resource to be accessed via the {@link org.apache.fop.apps.FOUserAgent}.
     *
     * @param resourceResolver the resolver of resources
     */
    public AFPResourceAccessor(InternalResourceResolver resourceResolver) {
        this(resourceResolver, null);
    }

    /**
     * Creates an {@link InputStream} given a URI.
     *
     * @param uri the URI of the InputStream
     * @return an InputStream
     * @throws IOException if an I/O error occurs while creating the InputStream.
     */
    public InputStream createInputStream(URI uri) throws IOException {
        return resourceResolver.getResource(uriResolver.resolveURI(uri));
    }

    /**
     * Returns the resolved URI, given the URI of a resource.
     *
     * @param uri the resource URI
     * @return the resolved URI
     */
    public URI resolveURI(String uri) {
        return uriResolver.resolveURI(uri);
    }

    private interface URIResolver {
        URI resolveURI(URI uri);

        URI resolveURI(String uri);
    }

    private static final class NullBaseURIResolver implements URIResolver {

        public URI resolveURI(URI uri) {
            return uri;
        }

        public URI resolveURI(String uri) {
            return URI.create("./" + uri.trim());
        }
    }

    private final class BaseURIResolver implements URIResolver {

        public URI resolveURI(URI uri) {
            return baseURI.resolve(uri);
        }

        public URI resolveURI(String uri) {
            return baseURI.resolve(uri.trim());
        }
    }
}
