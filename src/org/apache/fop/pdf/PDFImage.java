/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.IOException;

public interface PDFImage {

    // key to look up XObject
    public String getKey();

    public void setup(PDFDocument doc);

    // image size
    public int getWidth();
    public int getHeight();

    // DeviceGray, DeviceRGB, or DeviceCMYK
    public PDFColorSpace getColorSpace();

    // bits per pixel
    public int getBitsPerPixel();

    public boolean isPS();

    // For transparent images
    public boolean isTransparent();
    public PDFColor getTransparentColor();
    public String getMask();
    public String getSoftMask();

    // get the image bytes, and bytes properties

    public PDFStream getDataStream() throws IOException;

    public PDFICCStream getICCStream();

}

