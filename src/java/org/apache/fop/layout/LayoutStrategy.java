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


package org.apache.fop.layout;

import org.apache.fop.apps.Document;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.AreaTree;
import org.apache.fop.fo.pagination.PageSequence;

/**
 * Abstract class defining the highest-level information for a layout strategy.
 * Subclasses implement a layout strategy that converts an FO Tree into an
 * Area Tree.
 */
public abstract class LayoutStrategy {

    private String name = "undefined";
    public Document document;

    public LayoutStrategy(Document document) {
        this.document = document;
    }

    /**
     * Returns the name of this LayoutStrategy.
     * @return the String name of this LayoutStrategy
     */
    public String getName() {
        return name;
    }

    /**
     * Format a PageSequence into an AreaTree
     * @param pageSeq the PageSequence to be formatted
     * @param areaTree the AreaTree in which to place the formatted PageSequence
     * @throws FOPException for errors during layout
     */
    public abstract void format (PageSequence pageSeq, AreaTree areaTree)
            throws FOPException;

    /**
     * Indicates whether an FO Tree should be built for this layout strategy.
     * Override this in subclasses if an FO Tree is not needed.
     * @return true if an FO Tree is needed, false otherwise
     */
    public boolean foTreeNeeded() {
        return true;
    }

}
