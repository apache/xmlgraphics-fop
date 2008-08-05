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

package org.apache.fop.render.intermediate;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.ImageHandlerRegistry;

/**
 * Abstract base class for IFPainter implementations.
 */
public abstract class AbstractIFPainter implements IFPainter {

    private FOUserAgent userAgent;

    /** Image handler registry */
    protected ImageHandlerRegistry imageHandlerRegistry = new ImageHandlerRegistry();

    /**
     * Default constructor.
     */
    public AbstractIFPainter() {
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent ua) {
        if (this.userAgent != null) {
            throw new IllegalStateException("The user agent was already set");
        }
        this.userAgent = ua;
    }

    /**
     * Returns the user agent.
     * @return the user agent
     */
    protected FOUserAgent getUserAgent() {
        return this.userAgent;
    }

    private AffineTransform combine(AffineTransform[] transforms) {
        AffineTransform at = new AffineTransform();
        for (int i = 0, c = transforms.length; i < c; i++) {
            at.concatenate(transforms[i]);
        }
        return at;
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform[] transforms, Dimension size, Rectangle clipRect)
            throws IFException {
        startViewport(combine(transforms), size, clipRect);
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform[] transforms) throws IFException {
        startGroup(combine(transforms));
    }

}
