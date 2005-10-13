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

package org.apache.fop.layoutengine;

import org.w3c.dom.Document;

/**
 * This class holds references to all the results from the FOP processing run.
 */
public class LayoutResult {

    private Document areaTree;
    private ElementListCollector elCollector;
    
    /**
     * Creates a new LayoutResult instance.
     * @param areaTree the area tree DOM
     * @param elCollector the element list collector
     */
    public LayoutResult(Document areaTree, ElementListCollector elCollector) {
        this.areaTree = areaTree;
        this.elCollector = elCollector;
    }
 
    /** @return the generated area tree as DOM tree */
    public Document getAreaTree() {
        return this.areaTree;
    }
    
    /** @return the element list collector */
    public ElementListCollector getElementListCollector() {
        return this.elCollector;
    }
    
}
