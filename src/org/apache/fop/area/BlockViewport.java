/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

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

