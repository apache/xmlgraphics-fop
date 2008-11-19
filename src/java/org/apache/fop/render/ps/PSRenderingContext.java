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

package org.apache.fop.render.ps;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for PostScript production.
 */
public class PSRenderingContext extends AbstractRenderingContext {

    private FontInfo fontInfo;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param fontInfo the font list
     */
    public PSRenderingContext(FOUserAgent userAgent,
            FontInfo fontInfo) {
        super(userAgent);
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_POSTSCRIPT;
    }

    /**
     * Returns the font list.
     * @return the font list
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

}
