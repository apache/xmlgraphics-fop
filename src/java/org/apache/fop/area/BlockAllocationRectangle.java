/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 17/06/2004
 * $Id$
 */
package org.apache.fop.area;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class BlockAllocationRectangle extends AreaFrame implements
        AllocationRectangle {

    /**
     * @param area
     * @param contents
     */
    public BlockAllocationRectangle(Area area) {
        // For block-areas, the allocation-area is bounded in the
        // block-progression-direction by the border-rectangle, and in the
        // inline-progression-direction by the spaces-rectangle.
        // See 4.2.3 Geometric Definitions
        // The contents of the BlockAllocationRectangle is the ContentRectangle.
        // Initally, set up the AreaFrame representing the allocation
        // rectangle to co-incide with the content-rectangle.
        super(area, area.getContent());
        // Now extend the AreaFrame to co-incide with the
        // edges of the border rectangle in the BPDir, and with the edges of
        // the spaces rectangle in the IPDir.
        PaddingRectangle padding = area.getPadding();
        BorderRectangle borders = area.getBorders();
        SpacesRectangle spaces = area.getSpaces();
        setStart(spaces.getStart() + borders.getStart() + padding.getStart());
        setEnd(spaces.getEnd() + borders.getEnd() + padding.getEnd());
        setBefore(borders.getBefore() + padding.getBefore());
        setAfter(borders.getAfter() + padding.getAfter());
    }

}
