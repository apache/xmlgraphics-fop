/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.flow.Marker;
import org.apache.fop.layout.inline.InlineSpace;

// Java
import java.util.Vector;
import java.util.Hashtable;

abstract public class Area extends Box {

    /*
     * nominal font size and nominal font family incorporated in
     * fontState
     */
    protected FontState fontState;
    protected BorderAndPadding bp = null;

    protected Vector children = new Vector();

    /* max size in line-progression-direction */
    protected int maxHeight;

    /**
     * Total height of content of this area.
     */
    protected int currentHeight = 0;

    // used to keep track of the current x position within a table.  Required for drawing rectangle links.
    protected int tableCellXOffset = 0;

    // used to keep track of the absolute height on the page.  Required for drawing rectangle links.
    private int absoluteHeight = 0;

    protected int contentRectangleWidth;

    protected int allocationWidth;

    /* the page this area is on */
    protected Page page;

    protected ColorType backgroundColor;

    private IDReferences idReferences;

    protected Vector markers;

    // as defined in Section 6.1.1
    protected org.apache.fop.fo.FObj generatedBy;    // corresponds to 'generated-by' trait
    protected Hashtable returnedBy;

    // as defined in Section 6.1.1
    protected String areaClass;

    // as defined in Section 4.2.2
    protected boolean isFirst = false;
    protected boolean isLast = false;

    /*
     * author : Seshadri G
     * * the fo which created it
     */
    // This is deprecated and should be phased out in
    // favour of using 'generatedBy'
    public org.apache.fop.fo.FObj foCreator;

    public Area(FontState fontState) {
        setFontState(fontState);
        this.markers = new Vector();
        this.returnedBy = new Hashtable();
    }

    /**
     * Creates a new <code>Area</code> instance.
     *
     * @param fontState a <code>FontState</code> value
     * @param allocationWidth the inline-progression dimension of the content
     * rectangle of the Area
     * @param maxHeight the maximum block-progression dimension available
     * for this Area (its allocation rectangle)
     */
    public Area(FontState fontState, int allocationWidth, int maxHeight) {
        setFontState(fontState);
        this.allocationWidth = allocationWidth;
        this.contentRectangleWidth = allocationWidth;
        this.maxHeight = maxHeight;
        this.markers = new Vector();
        this.returnedBy = new Hashtable();
    }

    private void setFontState(FontState fontState) {
        // fontState.setFontInfo(this.page.getFontInfo());
        this.fontState = fontState;
    }

    public void addChild(Box child) {
        this.children.addElement(child);
        child.parent = this;
    }

    public void addChildAtStart(Box child) {
        this.children.insertElementAt(child, 0);
        child.parent = this;
    }

    public void addDisplaySpace(int size) {
        this.addChild(new DisplaySpace(size));
        this.absoluteHeight += size;
        this.currentHeight += size;
    }

    public void addInlineSpace(int size) {
        this.addChild(new InlineSpace(size));
        // other adjustments...
    }

    public FontInfo getFontInfo() {
        return this.page.getFontInfo();
    }

    public void end() {}

    public int getAllocationWidth() {
        /*
         * ATTENTION: this may change your output!! (Karen Lease, 4mar2001)
         * return this.allocationWidth - getPaddingLeft() - getPaddingRight()
         * - getBorderLeftWidth() - getBorderRightWidth();
         */
        return this.allocationWidth;
    }

    /**
     * Set the allocation width.
     * @param w The new allocation width.
     * This sets content width to the same value.
     * Currently only called during layout of Table to set the width
     * to the total width of all the columns. Note that this assumes the
     * column widths are explicitly specified.
     */
    public void setAllocationWidth(int w) {
        this.allocationWidth = w;
        this.contentRectangleWidth = this.allocationWidth;
    }

    public Vector getChildren() {
        return this.children;
    }

    public boolean hasChildren() {
        return (this.children.size() != 0);
    }

    public int getContentWidth() {
        /*
         * ATTENTION: this may change your output!! (Karen Lease, 4mar2001)
         * return contentRectangleWidth  - getPaddingLeft() - getPaddingRight()
         * - getBorderLeftWidth() - getBorderRightWidth();
         */
        return contentRectangleWidth;
    }

    public FontState getFontState() {
        return this.fontState;
    }

    /**
     * Returns content height of the area.
     *
     * @return Content height in millipoints
     */
    public int getContentHeight() {
        return this.currentHeight;
    }

    /**
     * Returns allocation height of this area.
     * The allocation height is the sum of the content height plus border
     * and padding in the vertical direction.
     *
     * @return allocation height in millipoints
     */
    public int getHeight() {
        return this.currentHeight + getPaddingTop() + getPaddingBottom()
               + getBorderTopWidth() + getBorderBottomWidth();
    }

    public int getMaxHeight() {
        // Change KDL: return max height of content rectangle
        return this.maxHeight;
        /*
         * return this.maxHeight - getPaddingTop() - getPaddingBottom() -
         * getBorderTopWidth() - getBorderBottomWidth();
         */
    }

    public Page getPage() {
        return this.page;
    }

    public ColorType getBackgroundColor() {
        return this.backgroundColor;
    }

    // Must handle conditionality here, depending on isLast/isFirst
    public int getPaddingTop() {
        return (bp == null ? 0 : bp.getPaddingTop(false));
    }

    public int getPaddingLeft() {
        return (bp == null ? 0 : bp.getPaddingLeft(false));
    }

    public int getPaddingBottom() {
        return (bp == null ? 0 : bp.getPaddingBottom(false));
    }

    public int getPaddingRight() {
        return (bp == null ? 0 : bp.getPaddingRight(false));
    }

    // Handle border-width, including conditionality
    // For now, just pass false everywhere!
    public int getBorderTopWidth() {
        return (bp == null ? 0 : bp.getBorderTopWidth(false));
    }

    public int getBorderRightWidth() {
        return (bp == null ? 0 : bp.getBorderRightWidth(false));
    }

    public int getBorderLeftWidth() {
        return (bp == null ? 0 : bp.getBorderLeftWidth(false));
    }

    public int getBorderBottomWidth() {
        return (bp == null ? 0 : bp.getBorderBottomWidth(false));
    }

    public int getTableCellXOffset() {
        return tableCellXOffset;
    }

    public void setTableCellXOffset(int offset) {
        tableCellXOffset = offset;
    }

    public int getAbsoluteHeight() {
        return absoluteHeight;
    }

    public void setAbsoluteHeight(int value) {
        absoluteHeight = value;
    }

    public void increaseAbsoluteHeight(int value) {
        absoluteHeight += value;
    }

    public void increaseHeight(int amount) {
        this.currentHeight += amount;
        this.absoluteHeight += amount;
    }

    // Remove allocation height of child
    public void removeChild(Area area) {
        this.currentHeight -= area.getHeight();
        this.absoluteHeight -= area.getHeight();
        this.children.removeElement(area);
    }

    public void removeChild(DisplaySpace spacer) {
        this.currentHeight -= spacer.getSize();
        this.absoluteHeight -= spacer.getSize();
        this.children.removeElement(spacer);
    }

    public void remove() {
        this.parent.removeChild(this);
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void setBackgroundColor(ColorType bgColor) {
        this.backgroundColor = bgColor;
    }

    public void setBorderAndPadding(BorderAndPadding bp) {
        this.bp = bp;
    }

    /**
     * Return space remaining in the vertical direction (height).
     * This returns maximum available space - current content height
     * Note: content height should be based on allocation height of content!
     * @return space remaining in base units (millipoints)
     */
    public int spaceLeft() {
        return maxHeight - currentHeight;
    }

    public void start() {}


    /**
     * Set the content height to the passed value if that value is
     * larger than current content height. If the new content height
     * is greater than the maximum available height, set the content height
     * to the max. available (!!!)
     *
     * @param height allocation height of content in millipoints
     */
    public void setHeight(int height) {
        int prevHeight = currentHeight;
        if (height > currentHeight) {
            currentHeight = height;
        }

        if (currentHeight > getMaxHeight()) {
            currentHeight = getMaxHeight();
        }
        absoluteHeight += (currentHeight - prevHeight);
    }

    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }

    public Area getParent() {
        return this.parent;
    }

    public void setParent(Area parent) {
        this.parent = parent;
    }

    public void setIDReferences(IDReferences idReferences) {
        this.idReferences = idReferences;
    }

    public IDReferences getIDReferences() {
        return idReferences;
    }

    /* Author seshadri */
    public org.apache.fop.fo.FObj getfoCreator() {
        return this.foCreator;
    }

    // Function not currently used! (KLease, 16mar01)

    public AreaContainer getNearestAncestorAreaContainer() {
        Area area = this.getParent();
        while (!(area instanceof AreaContainer)) {
            area = area.getParent();
        }
        return (AreaContainer)area;
    }

    public BorderAndPadding getBorderAndPadding() {
        return bp;
    }

    public void addMarker(Marker marker) {
        markers.addElement(marker);
    }

    public void addMarkers(Vector markers) {
        markers.addAll(markers);
    }

    public void addLineagePair(org.apache.fop.fo.FObj fo, int areaPosition) {
        returnedBy.put(fo, new Integer(areaPosition));
    }

    public Vector getMarkers() {
        return markers;
    }

    public void setGeneratedBy(org.apache.fop.fo.FObj generatedBy) {
        this.generatedBy = generatedBy;
    }

    public org.apache.fop.fo.FObj getGeneratedBy() {
        return generatedBy;
    }

    public void isFirst(boolean isFirst) {
        this.isFirst = isFirst;
    }

    public boolean isFirst() {
        return isFirst;
    }

    public void isLast(boolean isLast) {
        this.isLast = isLast;
    }

    public boolean isLast() {
        return isLast;
    }

}
