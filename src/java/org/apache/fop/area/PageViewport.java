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

import java.awt.Rectangle;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.flow.AbstractRetrieveMarker;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.fo.flow.Markers;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.apache.fop.traits.WritingModeTraitsGetter;

import static org.apache.fop.fo.Constants.FO_REGION_BODY;

/**
 * Page viewport that specifies the viewport area and holds the page contents.
 * This is the top level object for a page and remains valid for the life
 * of the document and the area tree.
 * This object may be used as a key to reference a page.
 * This is the level that creates the page.
 * The page (reference area) is then rendered inside the page object
 */
public class PageViewport extends AreaTreeObject implements Resolvable {

    private Page page;
    private Rectangle viewArea;
    private String simplePageMasterName;

    /**
     * Unique key to identify the page. pageNumberString and pageIndex are both no option
     * for this.
     */
    private String pageKey;

    private int pageNumber = -1;
    private String pageNumberString = null;
    private int pageIndex = -1; //-1 = undetermined
    private boolean blank;

    private transient PageSequence pageSequence;

    // set of IDs that appear first (or exclusively) on this page:
    private Set<String> idFirsts = new java.util.HashSet<String>();

    // this keeps a list of currently unresolved areas or extensions
    // once an idref is resolved it is removed
    // when this is empty the page can be rendered
    private Map<String, List<Resolvable>> unresolvedIDRefs
            = new java.util.HashMap<String, List<Resolvable>>();

    private Map<String, List<PageViewport>> pendingResolved = null;

    private Markers pageMarkers;

    /**
     * logging instance
     */
    protected static final Log log = LogFactory.getLog(PageViewport.class);

    /**
     * Create a page viewport.
     * @param spm SimplePageMaster indicating the page and region dimensions
     * @param pageNumber the page number
     * @param pageStr String representation of the page number
     * @param blank true if this is a blank page
     * @param spanAll true if the first span area spans all columns
     */
    public PageViewport(SimplePageMaster spm, int pageNumber, String pageStr,
            boolean blank, boolean spanAll) {
        this.simplePageMasterName = spm.getMasterName();
        setExtensionAttachments(spm.getExtensionAttachments());
        setForeignAttributes(spm.getForeignAttributes());
        this.blank = blank;
        int pageWidth = spm.getPageWidth().getValue();
        int pageHeight = spm.getPageHeight().getValue();
        this.pageNumber = pageNumber;
        this.pageNumberString = pageStr;
        this.viewArea = new Rectangle(0, 0, pageWidth, pageHeight);
        this.page = new Page(spm);
        createSpan(spanAll);
    }

    /**
     * Create a page viewport.
     * @param spm SimplePageMaster indicating the page and region dimensions
     * @param pageNumber the page number
     * @param pageStr String representation of the page number
     * @param blank true if this is a blank page
     */
    public PageViewport(SimplePageMaster spm, int pageNumber, String pageStr, boolean blank) {
        this(spm, pageNumber, pageStr, blank, false);
    }

    /**
     * Copy constructor.
     * @param original the original PageViewport to copy from
     * @throws FOPException when cloning of the page is not supported
     */
    public PageViewport(PageViewport original) throws FOPException {
        if (original.extensionAttachments != null) {
            setExtensionAttachments(original.extensionAttachments);
        }
        if (original.foreignAttributes != null) {
            setForeignAttributes(original.foreignAttributes);
        }
        this.pageIndex = original.pageIndex;
        this.pageNumber = original.pageNumber;
        this.pageNumberString = original.pageNumberString;
        try {
            this.page = (Page) original.page.clone();
        } catch (CloneNotSupportedException e) {
            throw new FOPException(e);
        }
        this.viewArea = new Rectangle(original.viewArea);
        this.simplePageMasterName = original.simplePageMasterName;
        this.blank = original.blank;
    }

    /**
     * Constructor used by the area tree parser.
     * @param viewArea the view area
     * @param pageNumber the page number
     * @param pageStr String representation of the page number
     * @param simplePageMasterName name of the original simple-page-master that generated this page
     * @param blank true if this is a blank page
     */
    public PageViewport(Rectangle viewArea, int pageNumber, String pageStr,
            String simplePageMasterName, boolean blank) {
        this.viewArea = viewArea;
        this.pageNumber = pageNumber;
        this.pageNumberString = pageStr;
        this.simplePageMasterName = simplePageMasterName;
        this.blank = blank;
    }

    /**
     * Sets the page sequence this page belongs to
     * @param seq the page sequence
     */
    public void setPageSequence(PageSequence seq) {
        this.pageSequence = seq;
    }

    /** @return the page sequence this page belongs to */
    public PageSequence getPageSequence() {
        return this.pageSequence;
    }

    /**
     * Get the view area rectangle of this viewport.
     * @return the rectangle for this viewport
     */
    public Rectangle getViewArea() {
        return viewArea;
    }

    /**
     * Get the page reference area with the contents.
     * @return the page reference area
     */
    public Page getPage() {
        return page;
    }

    /**
     * Sets the page object for this PageViewport.
     * @param page the page
     */
    public void setPage(Page page) {
        this.page = page;
    }

    /**
     * Get the page number of this page.
     * @return the integer value that represents this page
     */
    public int getPageNumber() {
        return pageNumber;
    }

    /**
     * Get the page number of this page.
     * @return the string that represents this page
     */
    public String getPageNumberString() {
        return pageNumberString;
    }

    /**
     * Sets the page index of the page in this rendering run.
     * (This is not the same as the page number!)
     * @param index the page index (zero-based), -1 if it is undetermined
     */
    public void setPageIndex(int index) {
        this.pageIndex = index;
    }

    /**
     * @return the overall page index of the page in this rendering run (zero-based,
     *         -1 if it is undetermined).
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    /**
     * Sets the unique key for this PageViewport that will be used to reference this page.
     * @param key the unique key.
     */
    public void setKey(String key) {
        this.pageKey = key;
    }

    /**
     * Get the key for this page viewport.
     * This is used so that a serializable key can be used to
     * lookup the page or some other reference.
     *
     * @return a unique page viewport key for this area tree
     */
    public String getKey() {
        if (this.pageKey == null) {
            throw new IllegalStateException("No page key set on the PageViewport: " + toString());
        }
        return this.pageKey;
    }

    /**
     * Add an "ID-first" to this page.
     * This is typically called by the {@link AreaTreeHandler} when associating
     * an ID with a {@link PageViewport}.
     *
     * @param id the id to be registered as first appearing on this page
     */
    public void setFirstWithID(String id) {
        if (id != null) {
            idFirsts.add(id);
        }
    }

    /**
     * Check whether a certain id first appears on this page
     *
     * @param id the id to be checked
     * @return true if this page is the first where the id appears
     */
    public boolean isFirstWithID(String id) {
        return idFirsts.contains(id);
    }

    /**
     * Add an idref to this page.
     * All idrefs found for child areas of this {@link PageViewport} are added
     * to unresolvedIDRefs, for subsequent resolution by {@link AreaTreeHandler}
     * calls to this object's {@code resolveIDRef()}.
     *
     * @param idref the idref
     * @param res the child element of this page that needs this
     *      idref resolved
     */
    public void addUnresolvedIDRef(String idref, Resolvable res) {
        if (unresolvedIDRefs == null) {
            unresolvedIDRefs = new HashMap<String, List<Resolvable>>();
        }
        List<Resolvable> pageViewports = unresolvedIDRefs.get(idref);
        if (pageViewports == null) {
            pageViewports = new ArrayList<Resolvable>();
            unresolvedIDRefs.put(idref, pageViewports);
        }
        pageViewports.add(res);
    }

    /**
     * Check if this page has been fully resolved.
     * @return true if the page is resolved and can be rendered
     */
    public boolean isResolved() {
        return unresolvedIDRefs == null
            || unresolvedIDRefs.size() == 0;
    }

    /**
     * Get the unresolved idrefs for this page.
     * @return String array of idref's that still have not been resolved
     */
    public String[] getIDRefs() {
        return (unresolvedIDRefs == null) ? null
            : unresolvedIDRefs.keySet().toArray(
                new String[unresolvedIDRefs.keySet().size()]);
    }

    /** {@inheritDoc} */
    public void resolveIDRef(String id, List<PageViewport> pages) {
        if (page == null) {
            if (pendingResolved == null) {
                pendingResolved = new HashMap<String, List<PageViewport>>();
            }
            pendingResolved.put(id, pages);
        } else {
            if (unresolvedIDRefs != null) {
                List<Resolvable> todo = unresolvedIDRefs.get(id);
                if (todo != null) {
                    for (Resolvable res : todo) {
                        res.resolveIDRef(id, pages);
                    }
                }
            }
        }
        if (unresolvedIDRefs != null && pages != null) {
            unresolvedIDRefs.remove(id);
            if (unresolvedIDRefs.isEmpty()) {
                unresolvedIDRefs = null;
            }
        }
    }

    /**
     * Register the markers for this page.
     *
     * @param marks the map of markers to add
     * @param starting if the area being added is starting or ending
     * @param isfirst if the area being added has is-first trait
     * @param islast if the area being added has is-last trait
     */
    public void registerMarkers(Map<String, Marker> marks, boolean starting, boolean isfirst, boolean islast) {
        if (pageMarkers == null) {
            pageMarkers = new Markers();
        }
        pageMarkers.register(marks, starting, isfirst, islast);
    }


    /**
     * Resolve a marker from this page.
     * This will retrieve a marker with the class name
     * and position.
     *
     * @param name The class name of the marker to retrieve
     * @param pos the position to retrieve
     * @return Object the marker found or null
     */
    public Marker resolveMarker(AbstractRetrieveMarker rm) {
        if (pageMarkers == null) {
            return null;
        }
        return pageMarkers.resolve(rm);
    }

    /** Dumps the current marker data to the logger. */
    public void dumpMarkers() {
        if (pageMarkers != null) {
            pageMarkers.dump();
        }
    }

    /**
     * Save the page contents to an object stream.
     * The map of unresolved references are set on the page so that
     * the resolvers can be properly serialized and reloaded.
     * @param out the object output stream to write the contents
     * @throws IOException in case of an I/O error while serializing the page
     */
    public void savePage(ObjectOutputStream out) throws IOException {
        // set the unresolved references so they are serialized
        page.setUnresolvedReferences(unresolvedIDRefs);
        out.writeObject(page);
        page = null;
    }

    /**
     * Load the page contents from an object stream.
     * This loads the page contents from the stream and
     * if there are any unresolved references that were resolved
     * while saved they will be resolved on the page contents.
     * @param in the object input stream to read the page from
     * @throws ClassNotFoundException if a class was not found while loading the page
     * @throws IOException if an I/O error occurred while loading the page
     */
    public void loadPage(ObjectInputStream in) throws IOException, ClassNotFoundException {
        page = (Page) in.readObject();
        unresolvedIDRefs = page.getUnresolvedReferences();
        if (unresolvedIDRefs != null && pendingResolved != null) {
            for (String id : pendingResolved.keySet()) {
                resolveIDRef(id, pendingResolved.get(id));
            }
            pendingResolved = null;
        }
    }

    /** {@inheritDoc} */
    public Object clone() throws CloneNotSupportedException {
        PageViewport pvp = (PageViewport) super.clone();
        pvp.page = (Page) page.clone();
        pvp.viewArea = (Rectangle) viewArea.clone();
        return pvp;
    }

    /**
     * Clear the page contents to save memory.
     * This object is kept for the life of the area tree since
     * it holds id and marker information and is used as a key.
     */
    public void clear() {
        page = null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append("PageViewport: page=");
        sb.append(getPageNumberString());
        return sb.toString();
    }

    /** @return the name of the simple-page-master that created this page */
    public String getSimplePageMasterName() {
        return this.simplePageMasterName;
    }

    /** @return True if this is a blank page. */
    public boolean isBlank() {
        return this.blank;
    }

    /**
     * Convenience method to get BodyRegion of this PageViewport
     * @return BodyRegion object
     */
    public BodyRegion getBodyRegion() {
        return (BodyRegion) getPage().getRegionViewport(FO_REGION_BODY).getRegionReference();
    }

    /**
     * Convenience method to create a new Span for this
     * this PageViewport.
     *
     * @param spanAll whether this is a single-column span
     * @return Span object created
     */
    public Span createSpan(boolean spanAll) {
        return getBodyRegion().getMainReference().createSpan(spanAll);
    }

    /**
     * Convenience method to get the span-reference-area currently
     * being processed
     *
     * @return span currently being processed.
     */
    public Span getCurrentSpan() {
        return getBodyRegion().getMainReference().getCurrentSpan();
    }

    /**
     * Convenience method to get the normal-flow-reference-area
     * currently being processed
     *
     * @return span currently being processed.
     */
    public NormalFlow getCurrentFlow() {
        return getCurrentSpan().getCurrentFlow();
    }

    /**
     * Convenience method to increment the Span to the
     * next NormalFlow to be processed, and to return that flow.
     *
     * @return the next NormalFlow in the Span.
     */
    public NormalFlow moveToNextFlow() {
        return getCurrentSpan().moveToNextFlow();
    }

    /**
     * Convenience method to return a given region-reference-area,
     * keyed by the Constants class identifier for the corresponding
     * formatting object (ie. Constants.FO_REGION_BODY, FO_REGION_START,
     * etc.)
     *
     * @param id the Constants class identifier for the region.
     * @return the corresponding region-reference-area for this page.
     */
    public RegionReference getRegionReference(int id) {
        return getPage().getRegionViewport(id).getRegionReference();
    }

    /**
     * Sets the writing mode traits for the page associated with this viewport.
     * @param wmtg a WM traits getter
     */
    public void setWritingModeTraits(WritingModeTraitsGetter wmtg) {
        if (page != null) {
            page.setWritingModeTraits(wmtg);
        }
    }

}
