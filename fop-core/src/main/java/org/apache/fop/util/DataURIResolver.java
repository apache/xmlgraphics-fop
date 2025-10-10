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

package org.apache.fop.util;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

/**
 * @deprecated
 * @see org.apache.xmlgraphics.util.uri.DataURIResolver
 */
@Deprecated
public class DataURIResolver implements URIResolver {

    private final URIResolver newResolver = new org.apache.xmlgraphics.util.uri.DataURIResolver();

    /**
     * @param href an href
     * @param base a base
     * @return a source
     * @throws TransformerException if not caught
     * @deprecated
     * @see org.apache.xmlgraphics.util.uri.DataURIResolver#resolve(String,
     *      String)
     */
    @Deprecated
    public Source resolve(String href, String base) throws TransformerException {
        return newResolver.resolve(href, base);
    }

}
