/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.*;
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.fo.pagination.PageSequence;

// Java
import java.util.ArrayList;

/*Modified by Mark Lillywhite mark-fop@inomial.com. Added getIDReferences.
  This is just a convenience method for renderers who no longer have access
  to the AreaTree when rendering.
  */

public class Page {

    private int height;
    private int width;

    private BodyAreaContainer body;
    private AreaContainer before;
    private AreaContainer after;
    private AreaContainer start;
    private AreaContainer end;

    private AreaTree areaTree;

    private ArrayList rootExtensions;

    private PageSequence pageSequence;

    protected int pageNumber = 0;
    protected String formattedPageNumber;

    protected ArrayList linkSets = new ArrayList();

    private ArrayList idList = new ArrayList();

    private ArrayList footnotes = null;

    private ArrayList markers = null;

    Page(AreaTree areaTree, int height, int width) {
        this.areaTree = areaTree;
        this.height = height;
        this.width = width;
        markers = new ArrayList();
    }

    public IDReferences getIDReferences() {
        return areaTree.getIDReferences();
    }

    public void setPageSequence(PageSequence pageSequence) {
        this.pageSequence = pageSequence;
    }

    public PageSequence getPageSequence() {
        return pageSequence;
    }

    public AreaTree getAreaTree() {
        return areaTree;
    }

    public void setNumber(int number) {
        this.pageNumber = number;
    }

    public int getNumber() {
        return this.pageNumber;
    }

    public void setFormattedNumber(String number) {
        this.formattedPageNumber = number;
    }

    public String getFormattedNumber() {
        return this.formattedPageNumber;
    }

    void addAfter(AreaContainer area) {
        this.after = area;
        area.setPage(this);
    }

    void addBefore(AreaContainer area) {
        this.before = area;
        area.setPage(this);
    }

    /**
     * Ensure that page is set not only on B.A.C. but also on the
     * three top-level reference areas.
     * @param area The region-body area container (special)
     */
    public void addBody(BodyAreaContainer area) {
        this.body = area;
        area.setPage(this);
        ((BodyAreaContainer)area).getMainReferenceArea().setPage(this);
        ((BodyAreaContainer)area).getBeforeFloatReferenceArea().setPage(this);
        ((BodyAreaContainer)area).getFootnoteReferenceArea().setPage(this);
    }

    void addEnd(AreaContainer area) {
        this.end = area;
        area.setPage(this);
    }

    void addStart(AreaContainer area) {
        this.start = area;
        area.setPage(this);
    }

    public void render(Renderer renderer) {
        renderer.renderPage(this);
    }

    public AreaContainer getAfter() {
        return this.after;
    }

    public AreaContainer getBefore() {
        return this.before;
    }

    public AreaContainer getStart() {
        return this.start;
    }

    public AreaContainer getEnd() {
        return this.end;
    }

    public BodyAreaContainer getBody() {
        return this.body;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public FontInfo getFontInfo() {
        return this.areaTree.getFontInfo();
    }

    public void addLinkSet(LinkSet linkSet) {
        this.linkSets.add(linkSet);
    }

    public ArrayList getLinkSets() {
        return this.linkSets;
    }

    public boolean hasLinks() {
        return (!this.linkSets.isEmpty());
    }

    public void addToIDList(String id) {
        idList.add(id);
    }

    public ArrayList getIDList() {
        return idList;
    }

    public ArrayList getPendingFootnotes() {
        return footnotes;
    }

    public ArrayList getExtensions() {
        return rootExtensions;
    }

    public void setExtensions(ArrayList extensions) {
        this.rootExtensions = extensions;
    }

    public void setPendingFootnotes(ArrayList v) {
        footnotes = v;
        if (footnotes != null) {
          for (int i = 0; i < footnotes.size(); i++ ) {
                FootnoteBody fb = (FootnoteBody)footnotes.get(i);
                if (!Footnote.layoutFootnote(this, fb, null)) {
                    // footnotes are too large to fit on empty page
                }

            }
            footnotes = null;
        }
    }

    public void addPendingFootnote(FootnoteBody fb) {
        if (footnotes == null) {
            footnotes = new ArrayList();
        }
        footnotes.add(fb);
    }

    public void registerMarker(Marker marker) {
        markers.add(marker);
    }

    public ArrayList getMarkers() {
        return this.markers;
    }

}
