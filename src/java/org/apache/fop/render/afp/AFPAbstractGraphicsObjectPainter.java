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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.GraphicsObject;
import org.apache.xmlgraphics.java2d.Graphics2DImagePainter;

/**
 * A simple AFP Graphics 2D painter
 */
public abstract class AFPAbstractGraphicsObjectPainter implements Graphics2DImagePainter {
    /** Static logging instance */
    protected static Log log = LogFactory.getLog(AFPAbstractGraphicsObjectPainter.class);

    private final AFPGraphics2D graphics2D;

    /**
     * Default constructor
     */
    public AFPAbstractGraphicsObjectPainter() {
        final boolean textAsShapes = false;
        this.graphics2D = new AFPGraphics2D(textAsShapes);
    }

    /**
     * Constructor
     *
     * @param graphics the afp graphics 2d implementation
     */
    public AFPAbstractGraphicsObjectPainter(AFPGraphics2D graphics) {
        this.graphics2D = graphics;
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