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
import java.util.Vector;
import java.util.Enumeration;

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

    private Vector rootExtensions;

    private PageSequence pageSequence;

    protected int pageNumber = 0;
    protected String formattedPageNumber;

    protected Vector linkSets = new Vector();

    private Vector idList = new Vector();

    private Vector footnotes = null;

    private Vector markers = null;

    Page(AreaTree areaTree, int height, int width) {
        this.areaTree = areaTree;
        this.height = height;
        this.width = width;
        markers = new Vector();
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
        this.linkSets.addElement(linkSet);
    }

    public Vector getLinkSets() {
        return this.linkSets;
    }

    public boolean hasLinks() {
        return (!this.linkSets.isEmpty());
    }

    public void addToIDList(String id) {
        idList.addElement(id);
    }

    public Vector getIDList() {
        return idList;
    }

    public Vector getPendingFootnotes() {
        return footnotes;
    }

    public Vector getExtensions() {
        return rootExtensions;
    }

    public void setExtensions(Vector extensions) {
        this.rootExtensions = extensions;
    }

    public void setPendingFootnotes(Vector v) {
        footnotes = v;
        if (footnotes != null) {
            for (Enumeration e = footnotes.elements();
                    e.hasMoreElements(); ) {
                FootnoteBody fb = (FootnoteBody)e.nextElement();
                if (!Footnote.layoutFootnote(this, fb, null)) {
                    // footnotes are too large to fit on empty page
                }

            }
            footnotes = null;
        }
    }

    public void addPendingFootnote(FootnoteBody fb) {
        if (footnotes == null) {
            footnotes = new Vector();
        }
        footnotes.addElement(fb);
    }

    public void registerMarker(Marker marker) {
        markers.addElement(marker);
    }

    public Vector getMarkers() {
        return this.markers;
    }

}
