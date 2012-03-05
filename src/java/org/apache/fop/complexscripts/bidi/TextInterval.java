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

package org.apache.fop.complexscripts.bidi;

import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.flow.AbstractGraphics;
import org.apache.fop.fo.flow.AbstractPageNumberCitation;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.Leader;

// CSOFF: LineLengthCheck
// CSOFF: SimplifyBooleanReturnCheck

/**
 * <p>The <code>TextInterval</code> class is a utility class, the instances of which are used
 * to record backpointers to associated nodes over sub-intervals of a delimited text range.</p>
 *
 * <p>This work was originally authored by Glenn Adams (gadams@apache.org).</p>
 */
class TextInterval {
    private FONode fn;              // associated node
    private int textStart;          // starting index within delimited text range of associated node's text
    private int start;              // starting index within delimited text range
    private int end;                // ending index within delimited text range
    private int level;              // resolved level or default (-1)
    TextInterval ( FONode fn, int start, int end ) {
        this ( fn, start, start, end, -1 );
    }
    TextInterval ( FONode fn, int textStart, int start, int end, int level ) {
        this.fn = fn;
        this.textStart = textStart;
        this.start = start;
        this.end = end;
        this.level = level;
    }
    FONode getNode() {
        return fn;
    }
    int getTextStart() {
        return textStart;
    }
    int getStart() {
        return start;
    }
    int getEnd() {
        return end;
    }
    int getLevel() {
        return level;
    }
    void setLevel ( int level ) {
        this.level = level;
    }
    public int length() {
        return end - start;
    }
    public String getText() {
        if ( fn instanceof FOText ) {
            return ( (FOText) fn ) .getCharSequence() .toString();
        } else if ( fn instanceof Character ) {
            return new String ( new char[] {( (Character) fn ) .getCharacter()} );
        } else {
            return null;
        }
    }
    public void assignTextLevels() {
        if ( fn instanceof FOText ) {
            ( (FOText) fn ) .setBidiLevel ( level, start - textStart, end - textStart );
        } else if ( fn instanceof Character ) {
            ( (Character) fn ) .setBidiLevel ( level );
        } else if ( fn instanceof AbstractPageNumberCitation ) {
            ( (AbstractPageNumberCitation) fn ) .setBidiLevel ( level );
        } else if ( fn instanceof AbstractGraphics ) {
            ( (AbstractGraphics) fn ) .setBidiLevel ( level );
        } else if ( fn instanceof Leader ) {
            ( (Leader) fn ) .setBidiLevel ( level );
        }
    }
    public boolean equals ( Object o ) {
        if ( o instanceof TextInterval ) {
            TextInterval ti = (TextInterval) o;
            if ( ti.getNode() != fn ) {
                return false;
            } else if ( ti.getStart() != start ) {
                return false;
            } else if ( ti.getEnd() != end ) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
    public int hashCode() {
        int l = ( fn != null ) ? fn.hashCode() : 0;
        l = ( l ^ start ) + ( l << 19 );
        l = ( l ^ end )   + ( l << 11 );
        return l;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        char c;
        if ( fn instanceof FOText ) {
            c = 'T';
        } else if ( fn instanceof Character ) {
            c = 'C';
        } else if ( fn instanceof BidiOverride ) {
            c = 'B';
        } else if ( fn instanceof AbstractPageNumberCitation ) {
            c = '#';
        } else if ( fn instanceof AbstractGraphics ) {
            c = 'G';
        } else if ( fn instanceof Leader ) {
            c = 'L';
        } else {
            c = '?';
        }
        sb.append ( c );
        sb.append ( "[" + start + "," + end + "][" + textStart + "](" + level + ")" );
        return sb.toString();
    }
}

