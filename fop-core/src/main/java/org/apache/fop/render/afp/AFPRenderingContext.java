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

package org.apache.fop.render.afp;

import java.util.Map;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPResourceManager;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for AFP (MO:DCA) production.
 */
public class AFPRenderingContext extends AbstractRenderingContext {

    private AFPResourceManager resourceManager;
    private AFPPaintingState paintingState;
    private FontInfo fontInfo;
    private Map foreignAttributes;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param resourceManager the resource manager
     * @param paintingState the painting state
     * @param fontInfo the font list
     * @param foreignAttributes a map of foreign attributes
     */
    public AFPRenderingContext(FOUserAgent userAgent,
            AFPResourceManager resourceManager,
            AFPPaintingState paintingState,
            FontInfo fontInfo, Map foreignAttributes) {
        super(userAgent);
        this.resourceManager = resourceManager;
        this.paintingState = paintingState;
        this.fontInfo = fontInfo;
        this.foreignAttributes = foreignAttributes;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_AFP;
    }

    /**
     * Returns the resource manager.
     * @return the resource manager
     */
    public AFPResourceManager getResourceManager() {
        return this.resourceManager;
    }

    /** @return painting state */
    public AFPPaintingState getPaintingState() {
        return this.paintingState;
    }

    /**
     * Returns the font list.
     * @return the font list
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Returns a Map of foreign attributes.
     * @return the foreign attributes (Map&lt;QName, Object&gt;)
     */
    public Map getForeignAttributes() {
        return this.foreignAttributes;
    }

}
