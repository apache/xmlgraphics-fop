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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;

/**
 * Inline Area
 * This area is for all inline areas that can be placed
 * in a line area.
 */
public class InlineArea extends Area {
    /**
     * offset position from top of parent area
     */
    protected int verticalPosition = 0;


    /**
     * Increase the inline progression dimensions of this area.
     * This is used for inline parent areas that contain mulitple child areas.
     *
     * @param ipd the inline progression to increase by
     */
    public void increaseIPD(int ipd) {
        this.ipd += ipd;
    }

    /**
     * Set the offset of this inline area.
     * This is used to set the offset of the inline area
     * which is normally relative to the top of the line
     * or the baseline.
     *
     * @param v the offset
     */
    public void setOffset(int v) {
        verticalPosition = v;
    }

    /**
     * Get the offset of this inline area.
     * This returns the offset of the inline area
     * which is normally relative to the top of the line
     * or the baseline.
     *
     * @return the offset
     */
    public int getOffset() {
        return verticalPosition;
    }
    
    public boolean hasUnderline() {
        return getBooleanTrait(Trait.UNDERLINE);
    }

    public boolean hasOverline() {
        return getBooleanTrait(Trait.OVERLINE);
    }
    
    public boolean hasLineThrough() {
        return getBooleanTrait(Trait.LINETHROUGH);
    }
    
    public boolean isBlinking() {
        return getBooleanTrait(Trait.BLINK);
    }
    
}

