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
 * Created on 20/02/2004
 * $Id$
 */
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class RegionBodyVport extends RegionViewport {

    /**
     * Creates a <code>region-body-viewport</code> with no rectangular area
     * @param pageSeq the generating <code>page-sequence</code>
     * @param parent
     * @param sync
     */
    public RegionBodyVport(
            FoPageSequence pageSeq,
            Node parent,
            Object sync) {
        // the page-sequence is the generated-by node
        super(pageSeq, pageSeq, parent, sync);
    }

    /**
     * Creates and returns a <code>RegionBodyVport</code> with no rectangular
     * area. The area created references a null <code>RegionBodyRefArea</code>.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param parent the <code>page-reference-area</code>
     * @param sync
     * @return the created reference area
     */
    public static RegionBodyVport nullRegionBodyVport(
            FoPageSequence pageSeq, Node parent, Object sync) {
        RegionBodyVport vport =
            new RegionBodyVport(pageSeq, parent, sync);
        vport.setRegionRefArea(RegionBodyRefArea.nullRegionBodyRef(
                pageSeq, vport, sync));
        return vport;
    }
}
