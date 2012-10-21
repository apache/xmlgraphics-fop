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

package org.apache.fop.render.svg;

import org.xml.sax.ContentHandler;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for SVG production.
 */
public class SVGRenderingContext extends AbstractRenderingContext {

    private ContentHandler handler;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param handler the target content handler
     */
    public SVGRenderingContext(FOUserAgent userAgent, ContentHandler handler) {
        super(userAgent);
        this.handler = handler;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_SVG;
    }

    /**
     * Returns the target content handler.
     * @return the content handler
     */
    public ContentHandler getContentHandler() {
        return this.handler;
    }

}
