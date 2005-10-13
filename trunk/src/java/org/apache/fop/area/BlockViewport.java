/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package org.apache.fop.area;

/**
 * A BlockViewport.
 * This is used for block level Viewport/reference pairs.
 * The block-container creates this area.
 */
public class BlockViewport extends Block {
    // clipping for this viewport
    private boolean clip = false;
    // transform if rotated or absolute
    private CTM viewportCTM;

    /**
     * Create a new block viewport area.
     */
    public BlockViewport() {
    }

    /**
     * Set the transform of this viewport.
     * If the viewport is rotated or has an absolute positioning
     * this transform will do the work.
     *
     * @param ctm the transformation
     */
    public void setCTM(CTM ctm) {
        viewportCTM = ctm;
    }

    /**
     * Get the transform of this block viewport.
     *
     * @return the transformation of this viewport
     *         or null if normally stacked without rotation
     */
    public CTM getCTM() {
        return viewportCTM;
    }

    /**
     * Set the clipping for this viewport.
     *
     * @param cl the clipping for the viewport
     */
    public void setClip(boolean cl) {
        clip = cl;
    }

    /**
     * Get the clipping for this viewport.
     *
     * @return the clipping for the viewport
     *         true if the contents should be clipped for this viewport
     */
    public boolean getClip() {
        return clip;
    }
}

