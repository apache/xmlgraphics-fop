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
 
package org.apache.fop.fonts;

/**
 * FontTriplet contains information on name, weight, style of one font
 */
public class FontTriplet {
    
    private String name, weight, style;
    
    /**
     * Creates a new font triplet.
     * @param name font name
     * @param weight font weight (normal, bold etc.)
     * @param style font style (normal, italic etc.)
     */
    public FontTriplet(String name, String weight, String style) {
        this.name = name;
        this.weight = weight;
        this.style = style;
    }

    /**
     * Returns the font name.
     * @return the font name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the font weight.
     * @return the font weight
     */
    public String getWeight() {
        return weight;
    }

    /**
     * Returns the font style.
     * @return the font style
     */
    public String getStyle() {
        return style;
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getName() + "," + getStyle() + "," + getWeight();
    }
}

