/*
 * Copyright 2005 The Apache Software Foundation.
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
import org.apache.fop.area.Block;


/**
 * Inline block parent area.
 * This is an inline area that can have one block area as a child
 */
public class InlineBlockParent extends InlineArea {

    /**
     * The list of inline areas added to this inline parent.
     */
    protected Block child = null;

    /**
     * Create a new inline block parent to add areas to.
     */
    public InlineBlockParent() {
    }

    /**
     * Override generic Area method.
     *
     * @param childArea the child area to add
     */
    public void addChildArea(Area childArea) {
        if (child != null) {
            throw new IllegalStateException("InlineBlockParent may have only one child area.");
        }
        if (childArea instanceof Block) {
            child = (Block) childArea;
            //Update extents from the child
            setIPD(childArea.getAllocIPD());
            setBPD(childArea.getAllocBPD());
        } else {
            throw new IllegalArgumentException("The child of an InlineBlockParent must be a"
                    + " Block area");
        }
    }

    /**
     * Get the child areas for this inline parent.
     *
     * @return the list of child areas
     */
    public Block getChildArea() {
        return child;
    }

}
