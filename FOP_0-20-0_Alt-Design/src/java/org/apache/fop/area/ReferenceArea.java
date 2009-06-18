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

import java.awt.geom.AffineTransform;

/**
 * Interface for <code>reference-area</code>s; i.e. areas which provide a
 * context for possible changes in <code>writing-mode</code> or
 * <code>reference-orientation</code>.
 *  
 * @author pbw
 * @version $Revision$ $Name$
 */
public interface ReferenceArea {

    /**
     * Java's text handling includes facilities for managing writing
     * mode.  <code>java.awt.ComponentOrientation</code> handles the
     * standard FO writing methods - LT (lr-tb), RT (rl-tb) and TR (tb-rl),
     * as well as TL (tb-lr - e.g. Mongolian).
     * Because these are dealt with within the context of a page-based
     * co-ordinate system (left,top = 0,0, right,bottom = x,y), there is
     * no need to apply any Affine transform to discriminate these cases.
     * <p>When a <code>reference-orientation</code> is applied, however,
     * and an area is rotated with reference to its containing area,
     * such a transform must be applied.
     * <p>Transforms will also be required to map Java page co-ordinates to
     * Adobe 1st quadrant co-ordinates for PDF and Postscript rendering.
     *
     * @param matrix the transform to map the contents of this reference-area
     * into standard Java page co-ordinates. <i><b>N.B.</b></i> The
     * <code>AffineTransform</code> should be null if no tranformation is
     * required.
     */
    public void setCoordTransformer(AffineTransform matrix);

    /**
     * Get the transform mapping this reference area into standard page
     * co-ordinates.  May return null.
     *
     * @return the current transform of this reference area.
     */
    public AffineTransform getCoordTransformer();

    // TODO - methods to apply transformation to content-rectangle of the
    // reference area wrt the allocation-rectangle
}
