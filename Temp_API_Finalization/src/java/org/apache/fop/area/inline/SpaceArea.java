/*
 * Copyright 2004-2005 The Apache Software Foundation.
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
package org.apache.fop.area.inline;

/**
 * A space
 */
public class SpaceArea extends InlineArea {
    
    /**
     * The space for this space area
     */
    protected String space;

    /**
     * The correction offset for the next area
     */
    protected int offset = 0;
    
    /**
     * Is this space adjustable?
     */
    protected boolean isAdjustable;

    /**
     * Create a space area
     * @param s the space character
     * @param o the offset for the next area
     * @param a is this space adjustable?
     */
    public SpaceArea(char s, int o, boolean a) {
        space = new String() + s;
        offset = o;
        isAdjustable = a;
    }
    
    /**
     * @return Returns the space.
     */
    public String getSpace() {
        return new String(space);
    }

    /**
     * @return Returns the offset.
     */
    public int getOffset() {
        return offset;
    }
    /**
     * @param o The offset to set.
     */
    public void setOffset(int o) {
        offset = o;
    }
}
