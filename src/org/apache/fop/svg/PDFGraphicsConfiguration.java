/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.svg;

import java.awt.*;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.awt.color.ColorSpace;
import java.awt.image.renderable.*;

/**
 * Our implementation of the class that returns information about
 * roughly what we can handle and want to see (alpha for example).
 */
class PDFGraphicsConfiguration extends GraphicsConfiguration {
    // We use this to get a good colormodel..
    static BufferedImage BIWithAlpha =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    // We use this to get a good colormodel..
    static BufferedImage BIWithOutAlpha =
        new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

    /**
     * Construct a buffered image with an alpha channel, unless
     * transparencty is OPAQUE (no alpha at all).
     */
    public BufferedImage createCompatibleImage(int width, int height,
            int transparency) {
        if (transparency == Transparency.OPAQUE)
            return new BufferedImage(width, height,
                                     BufferedImage.TYPE_INT_RGB);
        else
            return new BufferedImage(width, height,
                                     BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Construct a buffered image with an alpha channel.
     */
    public BufferedImage createCompatibleImage(int width, int height) {
        return new BufferedImage(width, height,
                                 BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * FIXX ME: This should return the page bounds in Pts,
     * I couldn't figure out how to get this for the current
     * page from the PDFDocument (this still works for now,
     * but it should be fixed...).
     */
    public Rectangle getBounds() {
        System.out.println("getting getBounds");
        return null;
    }

    /**
     * Return a good default color model for this 'device'.
     */
    public ColorModel getColorModel() {
        return BIWithAlpha.getColorModel();
    }

    /**
     * Return a good color model given <tt>transparency</tt>
     */
    public ColorModel getColorModel(int transparency) {
        if (transparency == Transparency.OPAQUE)
            return BIWithOutAlpha.getColorModel();
        else
            return BIWithAlpha.getColorModel();
    }

    /**
     * The default transform (1:1).
     */
    public AffineTransform getDefaultTransform() {
        System.out.println("getting getDefaultTransform");
        return new AffineTransform();
    }

    /**
     * The normalizing transform (1:1) (since we currently
     * render images at 72dpi, which we might want to change
     * in the future).
     */
    public AffineTransform getNormalizingTransform() {
        System.out.println("getting getNormalizingTransform");
        return new AffineTransform(2, 0, 0, 2, 0, 0);
    }

    /**
     * Return our dummy instance of GraphicsDevice
     */
    public GraphicsDevice getDevice() {
        return new PDFGraphicsDevice(this);
    }

    /*
     // for jdk1.4
     public java.awt.image.VolatileImage createCompatibleVolatileImage(int width, int height) {
     return null;
     }
     */
}

