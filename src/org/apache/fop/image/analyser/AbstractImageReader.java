/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.fop.fo.FOUserAgent;

/**
 * Base class implementing ImageReader.
 * @author Pankaj Narula
 * @version 1.0
 * @see ImageReader
 */
public abstract class AbstractImageReader implements ImageReader {

    /**
     * Image width.
     */
    protected int width = 0;

    /**
     * Image height.
     */
    protected int height = 0;

    /**
     * Image stream.
     */
    protected BufferedInputStream imageStream = null;

    public abstract boolean verifySignature(String uri,
                                            BufferedInputStream fis, FOUserAgent ua) throws IOException;

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public abstract String getMimeType();

    public BufferedInputStream getInputStream() {
        return this.imageStream;
    }

}

