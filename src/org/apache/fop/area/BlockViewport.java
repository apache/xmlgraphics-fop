/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

/**
 * A BlockViewport.
 * This is used for block level Viewport/reference pairs.
 * The block-container creates this area.
 */
public class BlockViewport extends Block {
    // clipping for this viewport
    boolean clip = false;
    CTM viewportCTM;

    public BlockViewport() {

    }

    public void setCTM(CTM ctm) {
        viewportCTM = ctm;
    }

    public CTM getCTM() {
        return viewportCTM;
    }

    public void setClip(boolean cl) {
        clip = cl;
    }

    public boolean getClip() {
        return clip;
    }
}
