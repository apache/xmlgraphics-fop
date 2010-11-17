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

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.fop.render.RendererContext;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageInfo;

/**
 * The AFP image information
 */
public class AFPRendererImageInfo {

    /** the image uri */
    protected final String uri;

    /** the current pos */
    protected final Rectangle2D pos;

    /** the origin */
    protected final Point origin;

    /** the foreign attributes */
    protected final Map foreignAttributes;

    /** the image info */
    protected final ImageInfo info;

    /** the image */
    protected final Image img;

    /** the renderer context */
    protected RendererContext rendererContext;

    /**
     * Main constructor
     *
     * @param uri the image uri
     * @param pos the image content area
     * @param origin the current position
     * @param info the image info
     * @param img the image
     * @param rendererContext the renderer context
     * @param foreignAttributes the foreign attributes
     */
    public AFPRendererImageInfo(String uri, Rectangle2D pos, Point origin,
            ImageInfo info, Image img, RendererContext rendererContext, Map foreignAttributes) {
        this.uri = uri;
        this.pos = pos;
        this.origin = origin;
        this.info = info;
        this.img = img;
        this.rendererContext = rendererContext;
        this.foreignAttributes = foreignAttributes;
    }

    /**
     * Sets the renderer context
     *
     * @param rendererContext the renderer context
     */
    public void setRendererContext(RendererContext rendererContext) {
        this.rendererContext = rendererContext;
    }

    /**
     * Returns the image info
     *
     * @return the image info
     */
    public ImageInfo getImageInfo() {
        return this.info;
    }

    /**
     * Returns the image
     *
     * @return the image
     */
    public Image getImage() {
        return this.img;
    }

    /**
     * Returns the renderer context
     *
     * @return the renderer context
     */
    public RendererContext getRendererContext() {
        return this.rendererContext;
    }

    /**
     * Return the foreign attributes
     * @return the foreign attributes
     */
    public Map getForeignAttributes() {
        return this.foreignAttributes;
    }

    /**
     * Return the uri
     *
     * @return the uri
     */
    public String getUri() {
        return this.uri;
    }

    /**
     * Return the origin
     *
     * @return the origin
     */
    public Point getOrigin() {
        return this.origin;
    }

    /**
     * Return the position
     *
     * @return the position
     */
    public Rectangle2D getPosition() {
        return this.pos;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "AFPRendererImageInfo{\n"
            + "\turi=" + uri + ",\n"
            + "\tinfo=" + info + ",\n"
            + "\tpos=" + pos + ",\n"
            + "\torigin=" + origin + ",\n"
            + "\timg=" + img + ",\n"
            + "\tforeignAttributes=" + foreignAttributes + ",\n"
            + "\trendererContext=" + rendererContext + "\n"
        + "}";

    }
}
