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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * This holds the contents of the page.
 * 
 * @author The Apache XML-FOP sub-project
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PageRefArea
extends AbstractReferenceArea
implements ReferenceArea, Serializable {
    // contains before, start, body, end and after regions
    private RegionBodyVport regionBody = null;
    private RegionBeforeVport regionBefore = null;
    private RegionAfterVport regionAfter = null;
    private RegionStartVport regionStart = null;
    private RegionEndVport regionEnd = null;

//    // temporary map of unresolved objects used when serializing the page
//    private Map unresolved = null;
    
    public PageRefArea(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(pageSeq, generatedBy, parent, sync);
    }

//    /**
//     * Set the unresolved references on this page for serializing.
//     *
//     * @param unres the map of unresolved objects
//     */
//    public void setUnresolvedReferences(Map unres) {
//        unresolved = unres;
//    }
//
//    /**
//     * Get the map unresolved references from this page.
//     * This should be called after deserializing to retrieve
//     * the map of unresolved references that were serialized.
//     *
//     * @return the de-serialized map of unresolved objects
//     */
//    public Map getUnresolvedReferences() {
//        return unresolved;
//    }
    
    /**
     * @return the regionAfter
     */
    public RegionAfterVport getRegionAfter() {
        return regionAfter;
    }

    /**
     * @param regionAfter to set
     */
    public void setRegionAfter(RegionAfterVport regionAfter) {
        this.regionAfter = regionAfter;
    }

    /**
     * @return the regionBefore
     */
    public RegionBeforeVport getRegionBefore() {
        return regionBefore;
    }

    /**
     * @param regionBefore to set
     */
    public void setRegionBefore(RegionBeforeVport regionBefore) {
        this.regionBefore = regionBefore;
    }

    /**
     * @return the regionBody
     */
    public RegionBodyVport getRegionBody() {
        return regionBody;
    }

    /**
     * @param regionBody to set
     */
    public void setRegionBody(RegionBodyVport regionBody) {
        this.regionBody = regionBody;
    }

    /**
     * @return the regionEnd
     */
    public RegionEndVport getRegionEnd() {
        return regionEnd;
    }

    /**
     * @param regionEnd to set
     */
    public void setRegionEnd(RegionEndVport regionEnd) {
        this.regionEnd = regionEnd;
    }

    /**
     * @return the regionStart
     */
    public RegionStartVport getRegionStart() {
        return regionStart;
    }

    /**
     * @param regionStart to set
     */
    public void setRegionStart(RegionStartVport regionStart) {
        this.regionStart = regionStart;
    }

    /**
     * Clone this page.
     * This returns a new page with a clone of all the regions.
     *
     * @return a new clone of this page
     */
    public Object clone() {
        PageRefArea p = (PageRefArea)(this.clone());
        if (regionBody != null) {
            p.regionBody = (RegionBodyVport)regionBody.clone();
        }
        if (regionBefore != null) {
            p.regionBefore = (RegionBeforeVport)regionBefore.clone();
        }
        if (regionAfter != null) {
            p.regionAfter = (RegionAfterVport)regionAfter.clone();
        }
        if (regionStart != null) {
            p.regionStart = (RegionStartVport)regionStart.clone();
        }
        if (regionEnd != null) {
            p.regionEnd = (RegionEndVport)regionEnd.clone();
        }

        return p;
    }
    
}

