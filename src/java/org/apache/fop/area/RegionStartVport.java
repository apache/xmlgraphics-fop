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
 * Created on 21/02/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Rectangle2D;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class RegionStartVport extends RegionViewport {

    /**
     * Creates a new region-start area with no defined rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the node which generated this reference area; in this
     * case, the <code>page-sequence</code>
     * @param parent the page-reference-area
     * @param sync
     */
    public RegionStartVport(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

    /**
     * Creates a new region-start area with the defined rectangular area
     * @param area the rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param generatedBy the node which generated this reference area; in this
     * case, the <code>page-sequence</code>
     * @param parent the page-reference-area
     * @param sync
     */
    public RegionStartVport(
            Rectangle2D area,
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(area, pageSeq, generatedBy, parent, sync);
    }

    /**
     * Creates and returns a <code>RegionStartVport</code> with no
     * rectangular area.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param generatedBy the node which generated this reference area; in this
     * case, the <code>page-sequence</code>
     * @param parent the <code>region-body-viewport-area</code>
     * @param sync
     * @return the created reference area
     */
    public static RegionStartVport nullRegionStartVport(
            FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        RegionStartVport vport =
            new RegionStartVport(pageSeq, generatedBy, parent, sync);
        vport.setRegion(RegionStartRefArea.nullRegionStartRef(
                pageSeq, generatedBy, vport, sync));
        return vport;
    }

}
