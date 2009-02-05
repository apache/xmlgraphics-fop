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

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOUserAgent;

/**
 * Default implementation of the {@link ResourceAccessor} interface for use inside FOP.
 */
public class DefaultFOPResourceAccessor implements ResourceAccessor {

    private FOUserAgent userAgent;

    /**
     * Main constructor.
     * @param userAgent the FO user agent
     */
    public DefaultFOPResourceAccessor(FOUserAgent userAgent) {
        this.userAgent = userAgent;
    }

    /** {@inheritDoc} */
    public InputStream createInputStream(URI uri) throws IOException {
        Source src = userAgent.resolveURI(uri.toASCIIString());
        if (src == null) {
            return null;
        } else if (src instanceof StreamSource) {
            StreamSource ss = (StreamSource)src;
            InputStream in = ss.getInputStream();
            return in;
        } else {
            return null;
        }
    }

}
