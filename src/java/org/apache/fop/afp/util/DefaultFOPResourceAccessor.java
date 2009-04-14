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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.fonts.FontManager;

/**
 * Default implementation of the {@link ResourceAccessor} interface for use inside FOP.
 */
public class DefaultFOPResourceAccessor extends SimpleResourceAccessor {

    private FOUserAgent userAgent;
    private String categoryBaseURI;

    /**
     * Constructor for resource to be accessed via the {@link FOUserAgent}. This contructor
     * can take two base URIs: the category base URI is the one to use when differentiating between
     * normal resources (ex. images) and font resources. So, if fonts need to be accessed, you can
     * set the {@link FontManager}'s base URI instead of the one on the {@link FopFactory}.
     * @param userAgent the FO user agent
     * @param categoryBaseURI the category base URI (may be null)
     * @param baseURI the custom base URI to resolve relative URIs against (may be null)
     */
    public DefaultFOPResourceAccessor(FOUserAgent userAgent, String categoryBaseURI, URI baseURI) {
        super(baseURI);
        this.userAgent = userAgent;
        this.categoryBaseURI = categoryBaseURI;
    }

    /** {@inheritDoc} */
    public InputStream createInputStream(URI uri) throws IOException {
        //Step 1: resolve against local base URI --> URI
        URI resolved = resolveAgainstBase(uri);

        //Step 2: resolve against the user agent --> stream
        Source src;
        src = userAgent.resolveURI(resolved.toASCIIString(), this.categoryBaseURI);

        if (src == null) {
            throw new FileNotFoundException("Resource not found: " + uri.toASCIIString());
        } else if (src instanceof StreamSource) {
            StreamSource ss = (StreamSource)src;
            InputStream in = ss.getInputStream();
            if (in != null) {
                return in;
            }
            if (ss.getReader() != null) {
                //Don't support reader, retry using system ID below
                IOUtils.closeQuietly(ss.getReader());
            }
        }
        URL url = new URL(src.getSystemId());
        return url.openStream();
    }

}
