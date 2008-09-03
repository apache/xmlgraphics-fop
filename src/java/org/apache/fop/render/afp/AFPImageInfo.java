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

/* $Id: $ */

package org.apache.fop.render.afp;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageInfo;

/**
 * The AFP image information
 */
public class AFPImageInfo {

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

    /**
     * Main constructor
     *
     * @param uri the image uri
     * @param pos the image content area
     * @param origin the current position
     * @param info the image info
     * @param img the image
     * @param foreignAttributes the foreign attributes
     */
    public AFPImageInfo(String uri, Rectangle2D pos, Point origin,
            ImageInfo info, Image img, Map foreignAttributes) {
        this.uri = uri;
        this.pos = pos;
        this.origin = origin;
        this.info = info;
        this.img = img;
        this.foreignAttributes = foreignAttributes;
    }

}
