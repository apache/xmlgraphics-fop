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

import java.awt.image.RenderedImage;
import java.io.IOException;

/**
 * This interface represents an optional feature that can be provided by
 * a renderer. It is exposed by calling the getImageAdapter() method
 * on the renderer. Renderers that support this feature allow painting
 * of images (RendererImage instances).
 */
public interface ImageAdapter {

    /**
     * Paints an image at the given position.
     * @param image the image which will be painted
     * @param context the renderer context for the current renderer
     * @param x X position of the image
     * @param y Y position of the image
     * @param width width of the image
     * @param height height of the image
     * @throws IOException In case of an I/O error while writing the output format
     */
    void paintImage(RenderedImage image,
            RendererContext context,
            int x, int y, int width, int height) throws IOException;

}
