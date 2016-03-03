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

import org.apache.xmlgraphics.image.loader.ImageFlavor;

/**
 * Defines the set of supported ImageFlavors for the PostScript renderer.
 */
public interface PSSupportedFlavors {

    /** The flavors supported inline with PostScript level 2. */
    ImageFlavor[] LEVEL_2_FLAVORS_INLINE = new ImageFlavor[]
                                                 {ImageFlavor.RAW_EPS,
                                                  ImageFlavor.RAW_CCITTFAX,
                                                  ImageFlavor.GRAPHICS2D,
                                                  ImageFlavor.BUFFERED_IMAGE,
                                                  ImageFlavor.RENDERED_IMAGE,
                                                  ImageFlavor.XML_DOM};

    /** The flavors supported inline with PostScript level 3 and higher. */
    ImageFlavor[] LEVEL_3_FLAVORS_INLINE = new ImageFlavor[]
                                                 {ImageFlavor.RAW_EPS,
                                                  ImageFlavor.RAW_JPEG,
                                                  ImageFlavor.RAW_CCITTFAX,
                                                  ImageFlavor.GRAPHICS2D,
                                                  ImageFlavor.BUFFERED_IMAGE,
                                                  ImageFlavor.RENDERED_IMAGE,
                                                  ImageFlavor.XML_DOM};

    /** The flavors supported as forms with PostScript level 2. */
    ImageFlavor[] LEVEL_2_FLAVORS_FORM = new ImageFlavor[]
                                                 {//ImageFlavor.RAW_EPS,
                                                  ImageFlavor.RAW_CCITTFAX,
                                                  ImageFlavor.GRAPHICS2D,
                                                  ImageFlavor.BUFFERED_IMAGE,
                                                  ImageFlavor.RENDERED_IMAGE/*,
                                                  ImageFlavor.XML_DOM*/};

    /** The flavors supported as forms with PostScript level 3 or higher. */
    ImageFlavor[] LEVEL_3_FLAVORS_FORM = new ImageFlavor[]
                                                 {//ImageFlavor.RAW_EPS,
                                                  ImageFlavor.RAW_JPEG,
                                                  ImageFlavor.RAW_CCITTFAX,
                                                  ImageFlavor.GRAPHICS2D,
                                                  ImageFlavor.BUFFERED_IMAGE,
                                                  ImageFlavor.RENDERED_IMAGE/*,
                                                  ImageFlavor.XML_DOM*/};

}
