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

// FOP


/**
 * Base class for PDF font classes
 */
public abstract class Typeface implements FontMetrics {

    /**
     * Get the encoding of the font.
     * @return the encoding
     */
    public abstract String getEncoding();

    /**
     * Map a Unicode character to a code point in the font.
     * @param c character to map
     * @return the mapped character
     */
    public abstract char mapChar(char c);

    /**
     * Determines whether the font is a multibyte font.
     * @return True if it is multibyte
     */
    public boolean isMultiByte() {
        return false;
    }

}

