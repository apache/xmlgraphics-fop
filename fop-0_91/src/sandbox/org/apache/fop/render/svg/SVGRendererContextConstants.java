/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.render.svg;

import org.apache.fop.render.RendererContextConstants;

/**
 * Defines a number of standard constants (keys) for use by the RendererContext class.
 */
public interface SVGRendererContextConstants extends RendererContextConstants {

    /** The SVG document that this image is being drawn into. */
    String SVG_DOCUMENT = "svgDoc";

    /** The current SVG page g element. */
    String SVG_PAGE_G = "svgPageG";

}
