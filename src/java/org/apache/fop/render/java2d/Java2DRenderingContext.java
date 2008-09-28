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

package org.apache.fop.render.java2d;

import java.awt.Graphics2D;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for PDF production.
 */
public class Java2DRenderingContext extends AbstractRenderingContext {

    private FontInfo fontInfo;
    private Graphics2D g2d;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param g2d the target Graphics2D instance
     * @param fontInfo the font list
     */
    public Java2DRenderingContext(FOUserAgent userAgent, Graphics2D g2d, FontInfo fontInfo) {
        super(userAgent);
        this.g2d = g2d;
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return null; //not applicable
    }

    /**
     * Returns the target Graphics2D object.
     * @return the Graphics2D object
     */
    public Graphics2D getGraphics2D() {
        return this.g2d;
    }

}
