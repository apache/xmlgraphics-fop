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
 * $Id: RegionViewport.java,v 1.9 2003/03/05 15:19:31 jeremias Exp $
 */ 
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * Region Viewport reference area.
 * This area is the viewport for a region and contains a region area.
 */
public class RegionViewport
extends BlockViewportArea
implements Viewport {

    /**
     * Creates a new region viewport with a null rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the generating node; in this case, the page sequence
     * @param parent the <code>main-reference-area</code>
     * @param sync
     */
    public RegionViewport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Set the region-reference-area for this region viewport.
     *
     * @param regRef the child region inside this viewport
     */
    public void setRegionRefArea(RegionRefArea regRef) {
        setReferenceArea(regRef);
    }

    /**
     * Get the region for this region viewport.
     *
     * @return the child region inside this viewport
     */
    public RegionRefArea getRegionRefArea() {
        return (RegionRefArea)(getReferenceArea());
    }

}

