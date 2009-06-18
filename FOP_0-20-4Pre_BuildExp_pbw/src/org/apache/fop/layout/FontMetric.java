/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

/**
 * interface for font metric classes
 */
public interface FontMetric {

    public int getAscender(int size);
    public int getCapHeight(int size);
    public int getDescender(int size);
    public int getXHeight(int size);

    public int getFirstChar();
    public int getLastChar();

    /**
     * return width (in 1/1000ths of point size) of character at
     * code point i
     */
    public int width(int i, int size);
    public int[] getWidths(int size);
}
