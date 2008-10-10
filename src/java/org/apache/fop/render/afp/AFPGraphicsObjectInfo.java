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

import java.awt.geom.Rectangle2D;

import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;
import org.apache.xmlgraphics.util.MimeConstants;

/**
 *  A graphics object info which contains necessary painting objects
 */
public class AFPGraphicsObjectInfo extends AFPDataObjectInfo {

    /** the graphics object painter implementation */
    private Graphics2DImagePainter painter;

    /** the graphics object area */
    private Rectangle2D area;

    /** the AFP graphics 2d implementation */
    private AFPGraphics2D g2d;

    /**
     * Returns the graphics painter
     *
     * @return the graphics painter
     */
    public Graphics2DImagePainter getPainter() {
        return this.painter;
    }

    /**
     * Sets the graphics painter
     *
     * @param graphicsPainter the graphics painter
     */
    public void setPainter(Graphics2DImagePainter graphicsPainter) {
        this.painter = graphicsPainter;
    }

    /**
     * Returns the graphics area
     *
     * @return the graphics area
     */
    public Rectangle2D getArea() {
        return this.area;
    }

    /**
     * Sets the graphics area area
     *
     * @param area the graphics object area
     */
    public void setArea(Rectangle2D area) {
        this.area = area;
    }

    /**
     * Sets the AFP graphics 2D implementation
     *
     * @param g2d the AFP graphics 2D implementation
     */
    public void setGraphics2D(AFPGraphics2D g2d) {
        this.g2d = g2d;
    }

    /**
     * Returns the AFP graphics 2D implementation
     *
     * @return the AFP graphics 2D implementation
     */
    public AFPGraphics2D getGraphics2D() {
        return this.g2d;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "GraphicsObjectInfo{" + super.toString() + "}";
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MimeConstants.MIME_SVG;
    }

}
