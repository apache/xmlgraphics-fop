/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: PageViewport.java,v 1.16 2003/03/05 15:19:31 jeremias Exp $
 */ 
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * Page viewport that specifies the viewport area.
 * This is the top level object for a page and remains valid for the life
 * of the document and the area tree.
 * This object may be used as a key to reference a page.
 * The immediate and only child of any viewport is a reference-area; in this
 * cast the page-reference-area.
 * The page reference area is then rendered inside the PageRefArea object
 */
public class PageViewport
extends AbstractViewport
implements Viewport {

    /**
     * Create a page viewport at the root of a tree, synchronized on itself,
     * with a given page reference area and viewport dimensions
     * @param pageId
     * @param p the page reference area for the contents of this page
     * @param bounds the dimensions of the viewport
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Rectangle2D bounds,
            PageRefArea p) {
        super(bounds, pageSeq, generatedBy);
        refArea = p;
    }

    /**
     * Create a page viewport.
     * @param parent node of this viewport
     * @param sync object on which the Area is synchronized
     * @param pageId the unique identifier of this page
     * @param p the page reference area for the contents of this page
     * @param bounds the dimensions of the viewport
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Rectangle2D bounds,
            PageRefArea p,
            Node parent,
            Object sync) {
        super(bounds, pageSeq, generatedBy, parent, sync);
        refArea = p;
    }

    /**
     * Create a page viewport with a given parent node, sync object and ID
     * @param parent
     * @param sync
     * @param pageId
     */
    public PageViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            long pageId,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        refArea = null;
    }
    
    /**
     * Get the page reference area with the contents.
     * @return the page reference area
     */
    public PageRefArea getPageRefArea() {
        synchronized (sync) {
            return (PageRefArea)getReferenceArea();
        }
    }

    /**
     * Get the id references for this page.
     * @return always null
     */
    public String[] getIDs() {
        return null;
    }

    /**
     * Clone this page.
     * Used by the page master to create a copy of an original page.
     * @return a copy of this page and associated viewports
     */
    public Object clone() {
        synchronized (sync) {
            PageViewport pv;
            try {
                pv = (PageViewport)(super.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
            pv.refArea = (PageRefArea)(refArea.clone());
            return pv;
        }
    }

    /**
     * Clear the pageRefArea contents to save memory.
     * This object is kept for the life of the area tree since
     * it holds id and marker information and is used as a key.
     */
    public void clear() {
        synchronized (sync) {
            refArea = null;
        }
    }
    
}
