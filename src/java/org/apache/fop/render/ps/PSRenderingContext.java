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

import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for PostScript production.
 */
public class PSRenderingContext extends AbstractRenderingContext {

    private PSGenerator gen;
    private FontInfo fontInfo;
    private boolean createForms;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param gen the PostScript generator
     * @param fontInfo the font list
     */
    public PSRenderingContext(FOUserAgent userAgent,
            PSGenerator gen, FontInfo fontInfo) {
        this(userAgent, gen, fontInfo, false);
    }

    /**
     * Special constructor.
     * @param userAgent the user agent
     * @param gen the PostScript generator
     * @param fontInfo the font list
     * @param createForms true if form generation mode should be enabled
     */
    public PSRenderingContext(FOUserAgent userAgent,
            PSGenerator gen, FontInfo fontInfo, boolean createForms) {
        super(userAgent);
        this.gen = gen;
        this.fontInfo = fontInfo;
        this.createForms = createForms;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_POSTSCRIPT;
    }

    /**
     * Returns the PostScript generator.
     * @return the PostScript generator
     */
    public PSGenerator getGenerator() {
        return this.gen;
    }

    /**
     * Returns the font list.
     * @return the font list
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Indicates whether PS forms should be created for the images instead of inline images.
     * Note that not all image handlers will support this!
     * @return true if PS forms shall be created
     */
    public boolean isCreateForms() {
        return this.createForms;
    }

    /**
     * Create a copy of this rendering context and activate form mode.
     * @return the form-enabled rendering context
     */
    public PSRenderingContext toFormContext() {
        return new PSRenderingContext(getUserAgent(), getGenerator(), getFontInfo(), true);
    }

}
