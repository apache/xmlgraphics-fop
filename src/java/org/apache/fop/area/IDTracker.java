/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.area;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Used by the AreaTreeHandler to keep track of ID reference usage
 * on a PageViewport level.
 */
public class IDTracker {
    
    private static final Log log = LogFactory.getLog(IDTracker.class);

    // HashMap of ID's whose area is located on one or more consecutive
    // PageViewports. Each ID has an arraylist of PageViewports that
    // form the defined area of this ID
    private Map idLocations = new java.util.HashMap();

    // idref's whose target PageViewports have yet to be identified
    // Each idref has a HashSet of Resolvable objects containing that idref
    private Map unresolvedIDRefs = new java.util.HashMap();

    private Set unfinishedIDs = new java.util.HashSet();

    private Set alreadyResolvedIDs = new java.util.HashSet();
    
    /**
     * Tie a PageViewport with an ID found on a child area of the PV. Note that
     * an area with a given ID may be on more than one PV, hence an ID may have
     * more than one PV associated with it.
     * 
     * @param id the property ID of the area
     * @param pv a page viewport that contains the area with this ID
     */
    public void associateIDWithPageViewport(String id, PageViewport pv) {
        if (log.isDebugEnabled()) {
            log.debug("associateIDWithPageViewport(" + id + ", " + pv + ")");
        }
        List pvList = (List) idLocations.get(id);
        if (pvList == null) { // first time ID located
            pvList = new ArrayList();
            idLocations.put(id, pvList);
            pvList.add(pv);
            // signal the PageViewport that it is the first PV to contain this id:
            pv.setFirstWithID(id);
            /*
             * See if this ID is in the unresolved idref list, if so resolve
             * Resolvable objects tied to it.
             */
            if (!unfinishedIDs.contains(id)) {
                tryIDResolution(id, pv, pvList);
            }
        } else {
            /* TODO: The check is a quick-fix to avoid a waste 
             * when adding inline-ids to the page */
            if (!pvList.contains(pv)) {
                pvList.add(pv);
            }
        }
    }

    /**
     * This method tie an ID to the areaTreeHandler until this one is ready to
     * be processed. This is used in page-number-citation-last processing so we
     * know when an id can be resolved.
     * 
     * @param id the id of the object being processed
     */
    public void signalPendingID(String id) {
        if (log.isDebugEnabled()) {
            log.debug("signalPendingID(" + id + ")");
        }
        unfinishedIDs.add(id);
    }

    /**
     * Signals that all areas for the formatting object with the given ID have
     * been generated. This is used to determine when page-number-citation-last
     * ref-ids can be resolved.
     * 
     * @param id the id of the formatting object which was just finished
     */
    public void signalIDProcessed(String id) {
        if (log.isDebugEnabled()) {
            log.debug("signalIDProcessed(" + id + ")");
        }

        alreadyResolvedIDs.add(id);
        if (!unfinishedIDs.contains(id)) {
            return;
        }
        unfinishedIDs.remove(id);

        List pvList = (List) idLocations.get(id);
        Set todo = (Set) unresolvedIDRefs.get(id);
        if (todo != null) {
            for (Iterator iter = todo.iterator(); iter.hasNext();) {
                Resolvable res = (Resolvable) iter.next();
                res.resolveIDRef(id, pvList);
            }
            unresolvedIDRefs.remove(id);
        }
    }
    
    /**
     * Check if an ID has already been resolved
     * 
     * @param id the id to check
     * @return true if the ID has been resolved
     */
    public boolean alreadyResolvedID(String id) {
        return (alreadyResolvedIDs.contains(id));
    }
    
    /**
     * Tries to resolve all unresolved ID references on the given page.
     * 
     * @param id ID to resolve
     * @param pv page viewport whose ID refs to resolve
     * @param pvList of PageViewports
     */
    private void tryIDResolution(String id, PageViewport pv, List pvList) {
        Set todo = (Set) unresolvedIDRefs.get(id);
        if (todo != null) {
            for (Iterator iter = todo.iterator(); iter.hasNext();) {
                Resolvable res = (Resolvable) iter.next();
                if (!unfinishedIDs.contains(id)) {
                    res.resolveIDRef(id, pvList);
                } else {
                    return;
                }
            }
            alreadyResolvedIDs.add(id);
            unresolvedIDRefs.remove(id);
        }
    }

    /**
     * Tries to resolve all unresolved ID references on the given page.
     * 
     * @param pv page viewport whose ID refs to resolve
     */
    public void tryIDResolution(PageViewport pv) {
        String[] ids = pv.getIDRefs();
        if (ids != null) {
            for (int i = 0; i < ids.length; i++) {
                List pvList = (List) idLocations.get(ids[i]);
                if (pvList != null) {
                    tryIDResolution(ids[i], pv, pvList);
                }
            }
        }
    }
    
    /**
     * Get the list of page viewports that have an area with a given id.
     * 
     * @param id the id to lookup
     * @return the list of PageViewports
     */
    public List getPageViewportsContainingID(String id) {
        return (List) idLocations.get(id);
    }
    
    /**
     * Add an Resolvable object with an unresolved idref
     * 
     * @param idref the idref whose target id has not yet been located
     * @param res the Resolvable object needing the idref to be resolved
     */
    public void addUnresolvedIDRef(String idref, Resolvable res) {
        Set todo = (Set) unresolvedIDRefs.get(idref);
        if (todo == null) {
            todo = new java.util.HashSet();
            unresolvedIDRefs.put(idref, todo);
        }
        // add Resolvable object to this HashSet
        todo.add(res);
    }        
}
