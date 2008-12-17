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

package org.apache.fop.render.ps;

import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;

import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.RenderingContext;

/**
 * Specialized image handler interface for PostScript output. Implementations can optionally
 * support creating PostScript forms. The implementation shall check the rendering context
 * to see if forms functionality is enabled in the
 * {@link #isCompatible(org.apache.fop.render.RenderingContext, org.apache.xmlgraphics.image.loader.Image)}
 * method.
 */
public interface PSImageHandler extends ImageHandler {

    /**
     * Generates a PostScript form for the given {@link Image} instance.
     * @param context the rendering context
     * @param image the image to be handled
     * @param form the associated form resource
     * @throws IOException if an I/O error occurs
     */
    void generateForm(RenderingContext context, Image image, PSImageFormResource form)
        throws IOException;

}
