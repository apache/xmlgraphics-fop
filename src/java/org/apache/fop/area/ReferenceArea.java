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
 * Created on 30/01/2004
 * $Id$
 */
package org.apache.fop.area;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public interface ReferenceArea extends Cloneable {

    /**
     * Set the Coordinate Transformation Matrix which transforms content
     * coordinates in this reference area which are specified in
     * terms of "start" and "before" into coordinates in a system which
     * is positioned in "absolute" directions (with origin at lower left of
     * the reference area.
     *
     * @param matrix the current transform to position this region
     */
    public void setCoordTransformer(CoordTransformer matrix);

    /**
     * Get the current transformer of this reference area.
     *
     * @return the current transformer to position this reference area.
     */
    public CoordTransformer getCoordTransformer();
        
}
