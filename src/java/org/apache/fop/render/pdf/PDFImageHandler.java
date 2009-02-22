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

package org.apache.fop.render.pdf;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;

import org.apache.xmlgraphics.image.loader.Image;

import org.apache.fop.pdf.PDFXObject;
import org.apache.fop.render.ImageHandlerBase;
import org.apache.fop.render.RendererContext;

/**
 * This interface is used for handling all sorts of image type for PDF output.
 */
public interface PDFImageHandler extends ImageHandlerBase {

    /**
     * Generates the PDF objects for the given {@link Image} instance. If the handler generates
     * an XObject, it shall return it or otherwise return null. A generated XObject shall be
     * placed in the current viewport according to the two parameters "origin" and "pos".
     * @param context the PDF renderer context
     * @param image the image to be handled
     * @param origin the current position in the current viewport (in millipoints)
     * @param pos the position and scaling of the image relative to the origin point
     *                  (in millipoints)
     * @return the generated XObject or null if no XObject was generated
     * @throws IOException if an I/O error occurs
     */
    PDFXObject generateImage(RendererContext context, Image image,
            Point origin, Rectangle pos) throws IOException;

}
