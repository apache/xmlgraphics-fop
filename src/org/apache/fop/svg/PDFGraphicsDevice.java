/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsConfigTemplate;

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
    protected GraphicsConfiguration gc;

    /**
     * Create a new PDF graphics device.
     *
     * @param The gc we should reference
     */
    PDFGraphicsDevice(PDFGraphicsConfiguration gc) {
        this.gc = gc;
    }

    /**
     * Ignore template and return the only config we have
     *
     * @param gct the template configuration
     * @return the best configuration which is the only one
     */
    public GraphicsConfiguration getBestConfiguration(
      GraphicsConfigTemplate gct) {
        return gc;
    }

    /**
     * Return an array of our one GraphicsConfig
     *
     * @return an array containing the one graphics configuration
     */
    public GraphicsConfiguration[] getConfigurations() {
        return new GraphicsConfiguration[]{ gc };
    }

    /**
     * Return out sole GraphicsConfig.
     *
     * @return the grpahics configuration that created this object
     */
    public GraphicsConfiguration getDefaultConfiguration() {
        return gc;
    }

    /**
     * Generate an IdString..
     *
     * @return the ID string for this device, uses toString
     */
    public String getIDstring() {
        return toString();
    }

    /**
     * Let the caller know that we are "a printer"
     *
     * @return the type which is always printer
     */
    public int getType() {
        return GraphicsDevice.TYPE_PRINTER;
    }

}

