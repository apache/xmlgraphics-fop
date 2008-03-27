/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * Base class for font classes
 */
public abstract class Typeface implements FontMetrics {

    /**
     * Used to identify whether a font has been used (a character map operation is used as
     * the trigger). This could just as well be a boolean but is a long out of statistical interest.
     */
    private long charMapOps = 0;
    
    /**
     * Get the encoding of the font.
     * @return the encoding
     */
    public abstract String getEncodingName();

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public abstract char mapChar(char c);

    /**
     * Used for keeping track of character mapping operations in order to determine if a font
     * was used at all or not.
     */
    protected void notifyMapOperation() {
        this.charMapOps++;
    }
    
    /**
     * Indicates whether this font had to do any character mapping operations. If that was 
     * not the case, it's an indication that the font has never actually been used.
     * @return true if the font had to do any character mapping operations
     */
    public boolean hadMappingOperations() {
        return (this.charMapOps > 0);
    }

    /**
     * Determines whether this font contains a particular character/glyph.
     * @param c character to check
     * @return True if the character is supported, Falso otherwise
     */
    public abstract boolean hasChar(char c);
    
    /**
     * Determines whether the font is a multibyte font.
     * @return True if it is multibyte
     */
    public boolean isMultiByte() {
        return false;
    }

    /** {@inheritDoc} */
    public int getMaxAscent(int size) {
        return getAscender(size);
    }
    
}

