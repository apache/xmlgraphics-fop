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
 * $Id$
 */
package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * This is an abstract reference area for the page regions - currently
 * region-body, region-before, region-after, region-start and region-end.
 * It is cloneable through the ReferenceArea interface implementation.
 */
public abstract class RegionRefArea
extends BlockReferenceArea
implements ReferenceArea {
    
    // the list of block areas from the static flow
    private ArrayList blocks = new ArrayList();

    /**
     * Creates a new region reference area, without a defined rectangular area
     * @param pageSeq the generating page sequence
     * @param generatedBy the generating node; in this case, the page sequence
     * @param parent the associated viewport area
     * @param sync
     */
    public RegionRefArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Get the block in this region.
     *
     * @return the list of blocks in this region
     */
    public List getBlocks() {
        return blocks;
    }

}
