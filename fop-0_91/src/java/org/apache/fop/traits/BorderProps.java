/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.traits;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.Constants;

import java.io.Serializable;

/**
 * Border properties.
 * Class to store border trait propties for the area tree.
 */
public class BorderProps implements Serializable {
    
    /** Separate border model */
    public static final int SEPARATE = 0;
    /** Collapsing border model, for borders inside a table */
    public static final int COLLAPSE_INNER = 1;
    /** Collapsing border model, for borders at the table's outer border */
    public static final int COLLAPSE_OUTER = 2;
    
    /** Border style (one of EN_*) */
    public int style; // Enum for border style
    /** Border color */
    public ColorType color;
    /** Border width */
    public int width;
    /** Border mode (one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER) */
    public int mode;

    /**
     * Constructs a new BorderProps instance.
     * @param style border style (one of EN_*)
     * @param width border width
     * @param color border color
     * @param mode border mode ((one of SEPARATE, COLLAPSE_INNER and COLLAPSE_OUTER)
     */
    public BorderProps(int style, int width, ColorType color, int mode) {
        this.style = style;
        this.width = width;
        this.color = color;
        this.mode = mode;
    }

    /**
     * @param bp the border properties or null
     * @return the effective width of the clipped part of the border
     */
    public static int getClippedWidth(BorderProps bp) {
        if ((bp != null) && (bp.mode != SEPARATE)) {
            return bp.width / 2;
        } else {
            return 0;
        }
    }
    
    private String getStyleString() {
        switch (style) {
        case Constants.EN_NONE: return "none";
        case Constants.EN_HIDDEN: return "hidden";
        case Constants.EN_DOTTED: return "dotted";
        case Constants.EN_DASHED: return "dashed";
        case Constants.EN_SOLID: return "solid";
        case Constants.EN_DOUBLE: return "double";
        case Constants.EN_GROOVE: return "groove";
        case Constants.EN_RIDGE: return "ridge";
        case Constants.EN_INSET: return "inset";
        case Constants.EN_OUTSET: return "outset";
        default: throw new IllegalStateException("Illegal border style: " + style);
        }
    }
    
    /** @see java.lang.Object#toString() */
    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append('(');
        sbuf.append(getStyleString());
        sbuf.append(',');
        sbuf.append(color);
        sbuf.append(',');
        sbuf.append(width);
        if (mode != SEPARATE) {
            sbuf.append(',');
            if (mode == COLLAPSE_INNER) {
                sbuf.append("collapse-inner");
            } else {
                sbuf.append("collapse-outer");
            }
        }
        sbuf.append(')');
        return sbuf.toString();
    }
}
