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
public class BlockContentRectangle extends ContentRectangle {

    /**
     * @param area
     */
    public BlockContentRectangle(Area area) {
        super(area);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param area
     * @param ipOrigin
     * @param bpOrigin
     * @param ipDim
     * @param bpDim
     */
    public BlockContentRectangle(Area area, double ipOrigin, double bpOrigin,
            double ipDim, double bpDim) {
        super(area, ipOrigin, bpOrigin, ipDim, bpDim);
        // TODO Auto-generated constructor stub
    }

    private BlockAllocationRectangle allocation;

    public void setBlockAllocationRectangle(
            BlockAllocationRectangle allocation) {
        this.allocation = allocation;
        // Set the content dimension of the allocation rectangle
        
    }
}
