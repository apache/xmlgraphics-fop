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

import java.util.Collections;
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

    private static final Log LOG = LogFactory.getLog(IDTracker.class);

    // Map of ID's whose area is located on one or more consecutive
    // PageViewports. Each ID has a list of PageViewports that
    // form the defined area of this ID
    private Map<String, List<PageViewport>> idLocations
            = new java.util.HashMap<String, List<PageViewport>>();

    // idref's whose target PageViewports have yet to be identified
    // Each idref has a HashSet of Resolvable objects containing that idref
    private Map<String, Set<Resolvable>> unresolvedIDRefs
            = new java.util.HashMap<String, Set<Resolvable>>();

    private Set<String> unfinishedIDs = new java.util.HashSet<String>();

    private Set<String> alreadyResolvedIDs = new java.util.HashSet<String>();

    /**
     * Tie a PageViewport with an ID found on a child area of the PV. Note that
     * an area with a given ID may be on more than one PV, hence an ID may have
     * more than one PV associated with it.
     *
     * @param id the property ID of the area
     * @param pv a page viewport that contains the area with this ID
     */
    public void associateIDWithPageViewport(String id, PageViewport pv) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("associateIDWithPageViewport(" + id + ", " + pv + ")");
        }
        List<PageViewport> pvList = idLocations.get(id);
        if (pvList == null) { // first time ID located
            pvList = new java.util.ArrayList<PageViewport>();
            idLocations.put(id, pvList);
            pvList.add(pv);
            // signal the PageViewport that it is the first PV to contain this id:
            pv.setFirstWithID(id);
            /*
             * See if this ID is in the unresolved idref list, if so resolve
             * Resolvable objects tied to it.
             */
            if (!unfinishedIDs.contains(id)) {
                tryIDResolution(id, pvList);
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("signalPendingID(" + id + ")");
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("signalIDProcessed(" + id + ")");
        }

        alreadyResolvedIDs.add(id);
        if (!unfinishedIDs.contains(id)) {
            return;
        }
        unfinishedIDs.remove(id);

        List<PageViewport> idLocs = idLocations.get(id);
        Set<Resolvable> todo = unresolvedIDRefs.get(id);
        if (todo != null) {
            for (Resolvable res : todo) {
                res.resolveIDRef(id, idLocs);
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
     * Tries to resolve all unresolved ID references on the given set of pages.
     *
     * @param id ID to resolve
     * @param pvList list of PageViewports
     */
    private void tryIDResolution(String id, List<PageViewport> pvList) {
        Set<Resolvable> todo = unresolvedIDRefs.get(id);
        if (todo != null) {
            for (Resolvable res : todo) {
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
            for (String id : ids) {
                List<PageViewport> pvList = idLocations.get(id);
                if (!(pvList == null || pvList.isEmpty())) {
                    tryIDResolution(id, pvList);
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
    public List<PageViewport> getPageViewportsContainingID(String id) {
        if (!(idLocations == null || idLocations.isEmpty())) {
            List<PageViewport> idLocs = idLocations.get(id);
            if (idLocs != null) {
                return idLocs;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get the first {@link PageViewport} containing content generated
     * by the FO with the given {@code id}.
     *
     * @param id    the id
     * @return  the first {@link PageViewport} for the id; {@code null} if
     *          no matching {@link PageViewport} was found
     */
    public PageViewport getFirstPageViewportContaining(String id) {
        List<PageViewport> list = getPageViewportsContainingID(id);
        if (!(list == null || list.isEmpty())) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Get the last {@link PageViewport} containing content generated
     * by the FO with the given {@code id}.
     *
     * @param id    the id
     * @return  the last {@link PageViewport} for the id; {@code null} if
     *          no matching {@link PageViewport} was found
     */
    public PageViewport getLastPageViewportContaining(String id) {
        List<PageViewport> list = getPageViewportsContainingID(id);
        if (!(list == null || list.isEmpty())) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * Add an Resolvable object with an unresolved idref
     *
     * @param idref the idref whose target id has not yet been located
     * @param res the Resolvable object needing the idref to be resolved
     */
    public void addUnresolvedIDRef(String idref, Resolvable res) {
        Set<Resolvable> todo = unresolvedIDRefs.get(idref);
        if (todo == null) {
            todo = new java.util.HashSet<Resolvable>();
            unresolvedIDRefs.put(idref, todo);
        }
        // add Resolvable object to this HashSet
        todo.add(res);
    }

    /**
     * Replace all id locations pointing to the old page view port with a new one. This is
     * necessary when a layouted page is replaced with a new one (e.g. last page handling).
     * @param oldPageViewPort old page view port
     * @param newPageViewPort new page view port
     */
    public void replacePageViewPort(PageViewport oldPageViewPort, PageViewport newPageViewPort) {

        for (List<PageViewport> viewPortList : idLocations.values()) {
            for (int i = 0, len = viewPortList.size(); i < len; i++) {
                PageViewport currPV = viewPortList.get(i);
                if (currPV == oldPageViewPort) {
                    viewPortList.set(i, newPageViewPort);
                }
            }
        }
    }
}
