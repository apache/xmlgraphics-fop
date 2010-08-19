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
 * A utility class for glyphs and glyph sequences.
 * @author Glenn Adams
 */
public final class GlyphUtils {

    private GlyphUtils() {
    }

    /**
     * Map a glyph (or character) code sequence to  a string, used only
     * for debugging and logging purposes.
     * @param cs character (glyph) id sequence
     * @return a string representation of code sequence
     */
    public static String toString ( CharSequence cs ) {
        StringBuffer sb = new StringBuffer();
        sb.append ( '[' );
        for ( int i = 0, n = cs.length(); i < n; i++ ) {
            int c = cs.charAt ( i );
            if ( i > 0 ) {
                sb.append ( ',' );
            }
            sb.append ( Integer.toString ( c ) );
        }
        sb.append ( ']' );
        return sb.toString();
    }

}
