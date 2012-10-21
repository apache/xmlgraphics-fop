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

import org.apache.xmlgraphics.java2d.ps.AbstractPSDocumentGraphics2D;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;

/**
 * <p>This class enables to transcode an input to a EPS document.</p>
 *
 * <p>Two transcoding hints (<code>KEY_WIDTH</code> and
 * <code>KEY_HEIGHT</code>) can be used to respectively specify the image
 * width and the image height. If only one of these keys is specified,
 * the transcoder preserves the aspect ratio of the original image.
 *
 * <p>The <code>KEY_BACKGROUND_COLOR</code> defines the background color
 * to use for opaque image formats, or the background color that may
 * be used for image formats that support alpha channel.
 *
 * <p>The <code>KEY_AOI</code> represents the area of interest to paint
 * in device space.
 *
 * <p>Three additional transcoding hints that act on the SVG
 * processor can be specified:
 *
 * <p><code>KEY_LANGUAGE</code> to set the default language to use (may be
 * used by a &lt;switch> SVG element for example),
 * <code>KEY_USER_STYLESHEET_URI</code> to fix the URI of a user
 * stylesheet, and <code>KEY_PIXEL_TO_MM</code> to specify the pixel to
 * millimeter conversion factor.
 *
 * <p>This work was authored by Keiron Liddle (keiron@aftexsw.com).</p>
 */
public class EPSTranscoder extends AbstractPSTranscoder {

    /**
     * Constructs a new {@link EPSTranscoder}.
     */
    public EPSTranscoder() {
        super();
    }

    /** {@inheritDoc} */
    protected AbstractPSDocumentGraphics2D createDocumentGraphics2D() {
        return new EPSDocumentGraphics2D(false);
    }

}
