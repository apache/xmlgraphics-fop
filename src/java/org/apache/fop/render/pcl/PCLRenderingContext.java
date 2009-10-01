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

package org.apache.fop.render.pcl;

import java.awt.geom.Point2D;

import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.AbstractRenderingContext;

/**
 * Rendering context for PCL production. The class is abstract and must be subclassed to
 * provide the missing functionality.
 */
public abstract class PCLRenderingContext extends AbstractRenderingContext {

    private PCLGenerator generator;
    private PCLRenderingUtil pclUtil;
    private boolean sourceTransparency = false;

    /**
     * Main constructor.
     * @param userAgent the user agent
     * @param generator the PCL generator
     */
    public PCLRenderingContext(FOUserAgent userAgent,
            PCLGenerator generator, PCLRenderingUtil pclUtil) {
        super(userAgent);
        this.generator = generator;
        this.pclUtil = pclUtil;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_PCL; //not applicable
    }

    /**
     * Returns the PCL generator.
     * @return the PCL generator
     */
    public PCLGenerator getPCLGenerator() {
        return this.generator;
    }

    /**
     * Returns the PCL rendering utility.
     * @return the PCL rendering utility.
     */
    public PCLRenderingUtil getPCLUtil() {
        return this.pclUtil;
    }

    /**
     * Indicates whether source transparency should be enabled when painting bitmaps.
     * @return true when source transparency is enabled
     */
    public boolean isSourceTransparencyEnabled() {
        return this.sourceTransparency;
    }

    /**
     * Enables or disables source transparency when painting bitmaps.
     * @param value true to enable source transparency, false to disable
     */
    public void setSourceTransparencyEnabled(boolean value) {
        this.sourceTransparency = value;
    }

    /**
     * Transforms a point into the PCL coordinate system.
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the transformed point in PCL coordinates
     */
    public abstract Point2D transformedPoint(int x, int y);

    /**
     * Returns the current {@link GraphicContext} instance.
     * @return the graphic context
     */
    public abstract GraphicContext getGraphicContext();

}
