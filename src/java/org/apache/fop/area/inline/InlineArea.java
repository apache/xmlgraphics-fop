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

package org.apache.fop.area.inline;

import org.apache.fop.area.Area;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.BorderProps;

/**
 * Inline Area
 * This area is for all inline areas that can be placed
 * in a line area.
 */
public class InlineArea extends Area {
    // int width;
    private int height;
    /**
     * The content ipd of this inline area
     */
    protected int contentIPD = 0;

    /**
     * offset position from top of parent area
     */
    protected int verticalPosition = 0;

    /**
     * Set the width of this inline area.
     * Currently sets the ipd.
     *
     * @param w the width
     */
    public void setWidth(int w) {
        contentIPD = w;
    }

    /**
     * Get the width of this inline area.
     * Currently gets the ipd.
     *
     * @return the width
     */
    public int getWidth() {
        return contentIPD;
    }

    /**
     * Set the inline progression dimension of this inline area.
     *
     * @param ipd the inline progression dimension
     */
    public void setIPD(int ipd) {
        this.contentIPD = ipd;
    }

    /**
     * Get the inline progression dimension
     *
     * @return the inline progression dimension of this area
     */
    public int getIPD() {
        return this.contentIPD;
    }

    /**
     * Increase the inline progression dimensions of this area.
     * This is used for inline parent areas that contain mulitple child areas.
     *
     * @param ipd the inline progression to increase by
     */
    public void increaseIPD(int ipd) {
        this.contentIPD += ipd;
    }

    /**
     * Set the height of this inline area.
     *
     * @param h the height value to set
     */
    public void setHeight(int h) {
        height = h;
    }

    /**
     * Get the height of this inline area.
     *
     * @return the height of the inline area
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the allocation inline progression dimension of this area.
     * This adds the content, borders and the padding to find the
     * total allocated IPD.
     *
     * @return the total IPD allocation for this area
     */
    public int getAllocIPD() {
        // If start or end border or padding is non-zero, add to content IPD
        int iBP = contentIPD;
        Object t;
        if ((t = getTrait(Trait.PADDING_START)) != null) {
            iBP += ((Integer) t).intValue();
        }
        if ((t = getTrait(Trait.PADDING_END)) != null) {
            iBP += ((Integer) t).intValue();
        }
        if ((t = getTrait(Trait.BORDER_START)) != null) {
            iBP += ((BorderProps) t).width;
        }
        if ((t = getTrait(Trait.BORDER_END)) != null) {
            iBP += ((BorderProps) t).width;
        }
        return iBP;
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
}

