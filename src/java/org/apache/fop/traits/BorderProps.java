/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
