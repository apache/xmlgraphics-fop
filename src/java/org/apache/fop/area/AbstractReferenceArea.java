/*
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
 * Created on 21/02/2004
 * $Id$
 */
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class AbstractReferenceArea
    extends Area
    implements ReferenceArea {

    // Set up as identity matrix
    protected CoordTransformer transformer = new CoordTransformer();

    /**
     * @param parent
     * @param index
     * @param areaSync
     * @throws IndexOutOfBoundsException
     */
    public AbstractReferenceArea(Node parent, int index, Object areaSync)
        throws IndexOutOfBoundsException {
        super(parent, index, areaSync);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param parent
     * @param areaSync
     * @throws IndexOutOfBoundsException
     */
    public AbstractReferenceArea(Node parent, Object areaSync)
        throws IndexOutOfBoundsException {
        super(parent, areaSync);
        // TODO Auto-generated constructor stub
    }

    /**
     * Set the Coordinate Transformation Matrix which transforms content
     * coordinates in this reference area which are specified in
     * terms of "start" and "before" into coordinates in a system which
     * is positioned in "absolute" directions (with origin at lower left of
     * the reference area.
     *
     * @param transformer to position this reference area
     */
    public void setCoordTransformer(CoordTransformer transformer) {
        this.transformer = transformer;
    }

    /**
     * Get the current transformer of this reference area.
     *
     * @return the current transformer to position this reference area
     */
    public CoordTransformer getCoordTransformer() {
        return this.transformer;
    }

    /**
     * Clone this reference area.
     *
     * @return a copy of this reference area
     */
    public Object clone() {
        AbstractReferenceArea absRefArea;
        try {
            absRefArea = (RegionRefArea)(super.clone());
        } catch (CloneNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new InternalError();
        }
        absRefArea.transformer = transformer;
        return absRefArea;
    }
    
}
