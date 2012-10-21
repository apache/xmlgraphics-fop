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

package org.apache.fop.svg;

/** native image handler */
public interface NativeImageHandler {

    /**
     * Add a natively handled image directly to the document.
     * This is used by the ImageElementBridge to draw a natively handled image
     * (like JPEG or CCITT images)
     * directly into the document rather than converting the image into
     * a bitmap and increasing the size.
     *
     * @param image the image to draw
     * @param x the x position
     * @param y the y position
     * @param width the width to draw the image
     * @param height the height to draw the image
     */
    void addNativeImage(org.apache.xmlgraphics.image.loader.Image image, float x, float y,
                             float width, float height);

}
