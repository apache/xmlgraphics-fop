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
 * Created on 16/04/2004
 * $Id$
 */
package org.apache.fop.area;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.pagination.FoSimplePageMaster;
import org.apache.fop.fo.properties.RetrievePosition;

/**
 * This class gathers all of of the components necessary to set up the basic
 * page area precursors for the resolution of <code>fo:flow</code> and
 * <code>fo:static-content</code> elements.
 * 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Page extends AreaNode
implements PageListElement, PageSetElement, Cloneable {

    // Implementation of PageList interface.
    // N.B. getId is required for both interfaces.

    /* (non-Javadoc)
     * @see org.apache.fop.area.PageSetElement#isPageList()
     */
    public boolean isPageList() {
        return false;
    }

    // Implementation of PageSet interface
    // N.B. getId is required for both interfaces.

    /* (non-Javadoc)
     * @see org.apache.fop.area.PageListElement#isPageSet()
     */
    public boolean isPageSet() {
        return false;
    }

    /** Unique ID for this page.  0 is an invalid ID.  */
    private long pageId = 0;

    /**
     * @return the pageId
     */
    public long getId() {
        synchronized (sync) {
            return pageId;
        }
    }
    
    /**
     * Create a page at the root of a tree, synchronized on itself,
     * with a given page reference area and viewport dimensions
     * @param pageId
     */
    public Page(
            FoPageSequence pageSeq,
            long pageId) {
        // the page-sequence is the generated-by node
        super(pageSeq, pageSeq);
        this.pageId = pageId;
    }

    /**
     * Create a page.
     * @param parent node of this viewport
     * @param sync object on which the Area is synchronized
     * @param pageId the unique identifier of this page
     */
    public Page(
            FoPageSequence pageSeq,
            long pageId,
            Node parent,
            Object sync) {
        // the page-sequence is the generated-by node
        super(pageSeq, pageSeq, parent, sync);
        this.pageId = pageId;
    }

    /**
     * @param pageId to set
     */
    public void setId(long pageId) {
        synchronized (sync) {
            this.pageId = pageId;
        }
    }

    public NormalFlowRefArea getNormalFlowRefArea() {
        RegionBodyRefArea bodyRefArea = getRegionBodyRefArea();
        if (bodyRefArea == null) return null;
        MainReferenceArea main = bodyRefArea.getMainReference();
        if (main == null) return null;
        SpanReferenceArea span = main.getCurrSpanRefArea();
        if (span == null) return null;
        return span.getCurrNormalFlowRefArea();
    }

    /**
     * Creates a null page, consisting of
     * <ul>
     * <li>a <code>PageViewport</code>/<code>PageRefArea</code> pair</li>
     * <li>a set of region viewport/reference-area pairs
     *   <ul>
     *     <li><code>RegionBodyVPort</code>/<code>RegionBodyRefArea</code>
     *       <ul>
     *         <li><code>MainReferenceArea</code></li>
     *         <li><code>SpanReferenceArea</code></li>
     *         <li><code>NormalFlowRefArea</code></li>
     *       </ul>
     *     </li>
     *     <li><code>RegionBeforeVPort</code>/<code>RegionBeforeRefArea</code>
     *     </li>
     *     <li><code>RegionAfterVPort</code>/<code>RegionAfterRefArea</code>
     *     </li>
     *     <li><code>RegionStartVPort</code>/<code>RegionStartRefArea</code>
     *     </li>
     *     <li><code>RegionEndVPort</code>/<code>RegionEndRefArea</code>
     *     </li>
     *   </ul>
     * </li>
     * </ul>
     * 
     */
    public static Page setupNullPage(FoPageSequence pageSeq, long id) {
        Page page = new Page(pageSeq, id);
        PageViewport pageVport =
            PageViewport.nullPageVport(pageSeq, page, page);
        page.setVport(pageVport);
        PageRefArea refArea = pageVport.getPageRefArea();
        page.setPageRefArea(refArea);
        return page;
    }
    /** The <code>simple-page-master</code> that generated this page. */
    protected FoSimplePageMaster pageMaster = null;
    /** The single <code>page-viewport</code> child of this page */
    protected PageViewport vport = null;
    /** The single <code>page-reference-area</code> child of the viewport */
    protected PageRefArea pageRefArea = null;

    /**
     * @return the vport
     */
    public PageViewport getVport() {
        return vport;
    }
    /**
     * @param vport to set
     */
    public void setVport(PageViewport vport) {
        this.vport = vport;
    }
    /**
     * @param pageRefArea to set
     */
    public void setPageRefArea(PageRefArea pageRefArea) {
        this.pageRefArea = pageRefArea;
    }
    public PageRefArea getPageRefArea() {
        return pageRefArea;
    }
    /** The formatted page number */
    private String pageNumber = null;

    /**
     * Gets the <code>region-body-reference-area</code> associated with this
     * page
     * @return the <code>region-body-reference-area</code>
     */
    public RegionBodyRefArea getRegionBodyRefArea() {
        return (RegionBodyRefArea)(
                pageRefArea.getRegionBodyVport().getRegionRefArea());
    }
    /**
     * Set the page number for this page.
     * @param num the string representing the page number
     */
    public void setPageNumber(String num) {
        synchronized (sync) {
            pageNumber = num;
        }
    }

    /**
     * Get the page number of this page.
     * @return the string that represents this page
     */
    public String getPageNumber() {
        synchronized (sync) {
            return pageNumber;
        }
    }

    // list of id references and the rectangle on the page
    private Map idReferences = null;

    // this keeps a list of currently unresolved areas or extensions
    // once the thing is resolved it is removed
    // when this is empty the page can be rendered
    private Map unresolved = null;

    private Map pendingResolved = null;

    /**
     * Add an unresolved id to this page.
     * All unresolved ids for the contents of this page are
     * added to this page. This is so that the resolvers can be
     * serialized with the pageRefArea to preserve the proper function.
     * @param id the id of the reference
     * @param res the resolver of the reference
     */
    public void addUnresolvedID(String id, Resolveable res) {
        synchronized (sync) {
            if (unresolved == null) {
                unresolved = new HashMap();
            }
            List list = (List)unresolved.get(id);
            if (list == null) {
                list = new ArrayList();
                unresolved.put(id, list);
            }
            list.add(res);
        }
    }

    /**
     * Check if this page has been fully resolved.
     * @return true if the page is resolved and can be rendered
     */
    public boolean isResolved() {
        synchronized (sync) {
            return unresolved == null;
        }
    }

    // hashmap of markers for this page
    // start and end are added by the fo that contains the markers
    private Map markerFirstStart = null;
    private Map markerLastStart = null;
    private Map markerFirstAny = null;
    private Map markerLastEnd = null;
    private Map markerLastAny = null;

    /**
     * Add the markers for this page.
     * Only the required markers are kept.
     * For "first-starting-within-page" it adds the markers
     * that are starting only if the marker class name is not
     * already added.
     * For "first-including-carryover" it adds any starting marker
     * if the marker class name is not already added.
     * For "last-starting-within-page" it adds all marks that
     * are starting, replacing earlier markers.
     * For "last-ending-within-page" it adds all markers that
     * are ending, replacing earlier markers.
     * 
     * Should this logic be placed in the page layout manager.
     *
     * @param marks the map of markers to add
     * @param start if the area being added is starting or ending
     * @param isfirst isfirst or islast flag
     */
    public void addMarkers(Map marks, boolean start, boolean isfirst) {
        synchronized (sync) {
            if (start) {
                if (isfirst) {
                    if (markerFirstStart == null) {
                        markerFirstStart = new HashMap();
                    }
                    if (markerFirstAny == null) {
                        markerFirstAny = new HashMap();
                    }
                    // only put in new values, leave current
                    for (
                            Iterator iter = marks.keySet().iterator();
                            iter.hasNext();
                            ) {
                        Object key = iter.next();
                        if (!markerFirstStart.containsKey(key)) {
                            markerFirstStart.put(key, marks.get(key));
                        }
                        if (!markerFirstAny.containsKey(key)) {
                            markerFirstAny.put(key, marks.get(key));
                        }
                    }
                    if (markerLastStart == null) {
                        markerLastStart = new HashMap();
                    }
                    // replace all
                    markerLastStart.putAll(marks);
                    
                } else {
                    if (markerFirstAny == null) {
                        markerFirstAny = new HashMap();
                    }
                    // only put in new values, leave current
                    for (Iterator iter = marks.keySet().iterator(); iter.hasNext();) {
                        Object key = iter.next();
                        if (!markerFirstAny.containsKey(key)) {
                            markerFirstAny.put(key, marks.get(key));
                        }
                    }
                }
            } else {
                if (!isfirst) {
                    if (markerLastEnd == null) {
                        markerLastEnd = new HashMap();
                    }
                    // replace all
                    markerLastEnd.putAll(marks);
                }
                if (markerLastAny == null) {
                    markerLastAny = new HashMap();
                }
                // replace all
                markerLastAny.putAll(marks);
            }
        }
    }

    /**
     * Get a marker from this page.
     * This will retrieve a marker with the class name
     * and position.
     *
     * @param name The class name of the marker to retrieve 
     * @param pos the position to retrieve
     * @return Object the marker found or null
     */
    public Object getMarker(String name, int pos) {
        synchronized (sync) {
            Object mark = null;
            switch (pos) {
                case RetrievePosition.FIRST_STARTING_WITHIN_PAGE:
                    if (markerFirstStart != null) {
                        mark = markerFirstStart.get(name);
                    }
                if (mark == null && markerFirstAny != null) {
                    mark = markerFirstAny.get(name);
                }
                break;
                case RetrievePosition.FIRST_INCLUDING_CARRYOVER:
                    if (markerFirstAny != null) {
                        mark = markerFirstAny.get(name);
                    }
                break;
                case RetrievePosition.LAST_STARTING_WITHIN_PAGE:
                    if (markerLastStart != null) {
                        mark = markerLastStart.get(name);
                    }
                if (mark == null && markerLastAny != null) {
                    mark = markerLastAny.get(name);
                }
                break;
                case RetrievePosition.LAST_ENDING_WITHIN_PAGE:
                    if (markerLastEnd != null) {
                        mark = markerLastEnd.get(name);
                    }
                if (mark == null && markerLastAny != null) {
                    mark = markerLastAny.get(name);
                }
                break;
            }
            return mark;
        }
    }

}
