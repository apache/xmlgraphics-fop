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

package org.apache.fop.render;

import java.awt.Rectangle;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;

/**
 * This interface is a service provider interface for image handlers.
 */
public interface ImageHandler extends ImageHandlerBase {

    /**
     * Indicates whether the image handler is compatible with the indicated target represented
     * by the rendering context object and with the image to be processed. The image is also
     * passed as a parameter because a handler might not support every subtype of image that is
     * presented. For example: in the case of {@link ImageXMLDOM}, the image might carry an SVG
     * or some other XML format. One handler might only handle SVG but no other XML format.
     * @param targetContext the target rendering context
     * @param image the image to be processed (or null if only to check based on the rendering
     *              context)
     * @return true if this handler is compatible with the target rendering context
     */
    boolean isCompatible(RenderingContext targetContext, Image image);

    /**
     * Handles the given {@link Image} instance painting it at the indicated position in the
     * output format being generated.
     * @param context the rendering context
     * @param image the image to be handled
     * @param pos the position and scaling of the image relative to the origin point of the
     *          current viewport (in millipoints)
     * @throws IOException if an I/O error occurs
     */
    void handleImage(RenderingContext context, Image image,
            Rectangle pos) throws IOException;

}
