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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;


public class URIResolverWrapper {
    private final URI baseUri;
    private final ResourceResolver uriResolver;

    public URIResolverWrapper(URI baseUri, ResourceResolver uriResolver) {
        this.baseUri = baseUri;
        this.uriResolver = uriResolver;
    }

    public URI getBaseURI() {
        return baseUri;
    }

    public InputStream resolveIn(String stringUri) throws IOException, URISyntaxException {
        return resolveIn(cleanURI(stringUri));
    }

    public InputStream resolveIn(URI uri) throws IOException {
        return uriResolver.getResource(resolveFromBase(uri));
    }

    public OutputStream resolveOut(URI uri) throws IOException {
        return uriResolver.getOutputStream(resolveFromBase(uri));
    }

    private URI resolveFromBase(URI uri) {
        return baseUri.resolve(uri);
    }

    public static URI cleanURI(String base) throws URISyntaxException {
        // replace back slash with forward slash to ensure windows file:/// URLS are supported
        if (base == null) {
            return null;
        }
        String fixedUri = base.replace('\\', '/');
        fixedUri = fixedUri.replace(" ", "%20");
        URI baseURI = new URI(fixedUri);
        return baseURI;
    }

    public static URI getBaseURI(String base) throws URISyntaxException {
        String path = base + (base.endsWith("/") ? "" : "/");
        return cleanURI(path);
    }

}
