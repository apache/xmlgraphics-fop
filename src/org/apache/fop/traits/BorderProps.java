/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.traits;

import org.apache.fop.datatypes.ColorType;

import java.io.Serializable;

/**
 * Border properties.
 * Class to store border trait propties for the area tree.
 */
public class BorderProps implements Serializable {
    public int style; // Enum for border style
    public ColorType color; // Border color
    public int width; // Border width

    public BorderProps(int style, int width, ColorType color) {
        this.style = style;
        this.width = width;
        this.color = color;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        sbuf.append(style); // Should get a String value for this enum constant
        sbuf.append(',');
        sbuf.append(color);
        sbuf.append(',');
        sbuf.append(width);
        sbuf.append(')');
        return sbuf.toString();
    }
}
