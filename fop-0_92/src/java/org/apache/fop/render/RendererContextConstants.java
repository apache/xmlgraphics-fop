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

package org.apache.fop.render;

/**
 * Defines a number of standard constants (keys) for use by the RendererContext class.
 */
public interface RendererContextConstants {

    /** The output stream that the document is being sent to. */
    String OUTPUT_STREAM = "outputStream";
    
    /** The current PageViewport being rendered. */
    String PAGE_VIEWPORT = "pageViewport";
    
    /** The target width of the image being painted. */
    String WIDTH = "width";

    /** The target height of the image being painted. */
    String HEIGHT = "height";

    /** The x position that this image is being drawn at. */
    String XPOS = "xpos";

    /** The y position that this image is being drawn at. */
    String YPOS = "ypos";

    /** The configuration for the XMLHandler. */
    String HANDLER_CONFIGURATION = "cfg";
    
}
