/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

import org.apache.fop.datatypes.ColorType;

import java.io.Serializable;

// properties should be serialized by the holder
public class Trait implements Serializable {
    public static final int ID_LINK = 0;
    public static final int INTERNAL_LINK = 1; //resolved
    public static final int EXTERNAL_LINK = 2;
    public static final int FONT_FAMILY = 3;
    public static final int FONT_SIZE = 4;
    public static final int FONT_WEIGHT = 5;
    public static final int FONT_STYLE = 6;
    public static final int COLOR = 7;
    public static final int ID_AREA = 8;
    public static final int BACKGROUND = 9;
    public static final int UNDERLINE = 10;
    public static final int OVERLINE = 11;
    public static final int LINETHROUGH = 12;
    public static final int OFFSET = 13;
    public static final int SHADOW = 14;

    public static final int FONT_STATE = 100;

    public int propType;
    public Object data;

    public static class Background {
        ColorType color;
        String url;
        int repeat;
        int horiz;
        int vertical;
    }
}

