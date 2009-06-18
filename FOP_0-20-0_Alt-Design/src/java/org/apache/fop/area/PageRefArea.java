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

import java.io.Serializable;
//import java.util.Map;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * This holds the contents of the page.
 * 
 * @author The Apache XML-FOP sub-project
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PageRefArea
extends BlockReferenceArea
implements ReferenceArea, Serializable {
    // contains before, start, body, end and after regions
    private RegionBodyVport regionBodyVport = null;
    private RegionBeforeVport regionBeforeVport = null;
    private RegionAfterVport regionAfterVport = null;
    private RegionStartVport regionStartVport = null;
    private RegionEndVport regionEndVport = null;

//    // temporary map of unresolved objects used when serializing the page
//    private Map unresolved = null;
    
    /**
     * Create a <code>page-reference-area</code> with a null rectangular area
     * and <code>region-reference-area</code>s. 
     * @param pageSeq
     * @param parent
     * @param sync
     */
    public PageRefArea(
            FoPageSequence pageSeq,
            Node parent,
            Object sync) {
        // the page-sequence is the generated-by node
        super(pageSeq, pageSeq, parent, sync);
    }

    /**
     * Creates and returns a <code>PageRefArea</code> with no rectangular
     * area. The area created references null region viewports for
     * <code>region-body, region-before, region-after, region-start</code> and
     * <code>region-end</code>.
     * <b>N.B.</b> this is a <code>static</code> method.
     * @param pageSeq the <code>page-sequence</code> to which this area belongs
     * @param parent the <code>page-viewport-area</code>
     * @param sync
     * @return the created reference area
     */
    public static PageRefArea nullPageRef(
            FoPageSequence pageSeq, Node parent, Object sync) {
        PageRefArea pageRef =
            new PageRefArea(pageSeq, parent, sync);
        pageRef.setRegionBodyVport(RegionBodyVport.nullRegionBodyVport(
                pageSeq, pageRef, sync));
        pageRef.setRegionBeforeVport(RegionBeforeVport.nullRegionBeforeVport(
                pageSeq, pageRef, sync));
        pageRef.setRegionAfterVport(RegionAfterVport.nullRegionAfterVport(
                pageSeq, pageRef, sync));
        pageRef.setRegionStartVport(RegionStartVport.nullRegionStartVport(
                pageSeq, pageRef, sync));
        pageRef.setRegionEndVport(RegionEndVport.nullRegionEndVport(
                pageSeq, pageRef, sync));
        return pageRef;
    }

    /**
     * @return the regionAfterVport
     */
    public RegionAfterVport getRegionAfterVport() {
        return regionAfterVport;
    }

    /**
     * @param regionAfter viewport to set
     */
    public void setRegionAfterVport(RegionAfterVport regionAfter) {
        regionAfterVport = regionAfter;
    }

    /**
     * @return the regionBeforeVport
     */
    public RegionBeforeVport getRegionBeforeVport() {
        return regionBeforeVport;
    }

    /**
     * @param regionBefore viewport to set
     */
    public void setRegionBeforeVport(RegionBeforeVport regionBefore) {
        regionBeforeVport = regionBefore;
    }

    /**
     * @return the regionBodyVport
     */
    public RegionBodyVport getRegionBodyVport() {
        return regionBodyVport;
    }

    /**
     * @param regionBody viewport to set
     */
    public void setRegionBodyVport(RegionBodyVport regionBody) {
        regionBodyVport = regionBody;
    }

    /**
     * @return the regionEndVport
     */
    public RegionEndVport getRegionEndVport() {
        return regionEndVport;
    }

    /**
     * @param regionEnd viewport to set
     */
    public void setRegionEndVport(RegionEndVport regionEnd) {
        regionEndVport = regionEnd;
    }

    /**
     * @return the regionStartVport
     */
    public RegionStartVport getRegionStartVport() {
        return regionStartVport;
    }

    /**
     * @param regionStart viewport to set
     */
    public void setRegionStartVport(RegionStartVport regionStart) {
        regionStartVport = regionStart;
    }
    
}

