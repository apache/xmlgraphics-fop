/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.*;
import java.awt.Image;
import java.awt.image.*;
import java.awt.image.renderable.*;

/**
 * This implements the GraphicsDevice interface as appropriate for
 * a PDFGraphics2D.  This is quite simple since we only have one
 * GraphicsConfiguration for now (this might change in the future
 * I suppose).
 */
class PDFGraphicsDevice extends GraphicsDevice {

    /**
     * The Graphics Config that created us...
     */
    GraphicsConfiguration gc;

    /**
     * @param The gc we should reference
     */
    PDFGraphicsDevice(PDFGraphicsConfiguration gc) {
        this.gc = gc;
    }

    /**
     * Ignore template and return the only config we have
     */
    public GraphicsConfiguration getBestConfiguration(
      GraphicsConfigTemplate gct) {
        return gc;
    }

    /**
     * Return an array of our one GraphicsConfig
     */
    public GraphicsConfiguration[] getConfigurations() {
        return new GraphicsConfiguration[]{ gc };
    }

    /**
     * Return out sole GraphicsConfig.
     */
    public GraphicsConfiguration getDefaultConfiguration() {
        return gc;
    }

    /**
     * Generate an IdString..
     */
    public String getIDstring() {
        return toString();
    }

    /**
     * Let the caller know that we are "a printer"
     */
    public int getType() {
        return GraphicsDevice.TYPE_PRINTER;
    }

}

