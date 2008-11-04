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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPGraphics2D;
import org.apache.fop.afp.modca.GraphicsObject;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * Paints SVG as a GOCA Graphics Object using Batik
 */
public class GraphicsObjectPainterAFP implements Graphics2DImagePainter {
    /** Static logging instance */
    protected static Log log = LogFactory.getLog(GraphicsObjectPainterAFP.class);

    private final AFPGraphics2D graphics2D;

    /** the batik root node of the svg document */
    private GraphicsNode root;

    /**
     * Main constructor
     *
     * @param graphics an AFP graphics 2D implementation
     */
    public GraphicsObjectPainterAFP(AFPGraphics2D graphics) {
        final boolean textAsShapes = false;
        this.graphics2D = new AFPGraphics2D(textAsShapes);
    }

    /**
     * Sets the graphics node
     *
     * @param rootNode the graphics root node
     */
    public void setGraphicsNode(GraphicsNode rootNode) {
        this.root = rootNode;
    }

    /** {@inheritDoc} */
    public void paint(Graphics2D g2d, Rectangle2D area) {
        log.debug("Painting SVG using GOCA");

        // tell batik to paint the graphics object
        root.paint(g2d);

        // dispose of the graphics 2d implementation
        g2d.dispose();
    }

    /** {@inheritDoc} */
    public Dimension getImageSize() {
        return null;
    }

    /**
     * Sets the GOCA Graphics Object
     *
     * @param graphicsObject the GOCA Graphics Object
     */
    public void setGraphicsObject(GraphicsObject graphicsObject) {
        this.graphics2D.setGraphicsObject(graphicsObject);
    }

}
