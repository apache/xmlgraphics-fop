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

import org.apache.fop.datastructs.Node;
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
extends BlockViewportArea
implements Viewport {

    /**
     * Creates a page viewport with a null page ref area and rectangular area,
     * and the given parent node and sync object
     * @param pageSeq the page-sequence which generated this viewport. This is
     * also the generated-by node
     * @param parent
     * @param sync
     */
    public PageViewport(
            FoPageSequence pageSeq,
            Node parent,
            Object sync) {
        // The pageSeq is also the generating node
        super(pageSeq, pageSeq, parent, sync);
        refArea = null;
    }

    /**
     * Creates and returns a <code>PageViewport</code> with no rectangular
     * area. The area created references a null <code>PageRefArea</code>.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param parent the <code>Page</code> object
     * @param sync
     * @return the created viewport area
     */
    public static PageViewport nullPageVport(
            FoPageSequence pageSeq, Node parent, Object sync) {
        PageViewport vport =
            new PageViewport(pageSeq, parent, sync);
        vport.setReferenceArea(PageRefArea.nullPageRef(
                pageSeq, vport, sync));
        return vport;
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
