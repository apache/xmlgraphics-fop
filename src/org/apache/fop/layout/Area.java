/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.layout;

// FOP
import org.apache.fop.datatypes.IDReferences;
import org.apache.fop.layout.inline.InlineSpace;

// Java
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Area extends Box {

    /*
     * nominal font size and nominal font family incorporated in
     * fontState
     */
    protected FontState fontState;
    protected BorderAndPadding bp = null;

    protected ArrayList children = new ArrayList();

    /* max size in line-progression-direction */
    protected int maxHeight;

    /**
     * Total height of content of this area.
     */
    protected int currentHeight = 0;

    // used to keep track of the current x position within a table.  Required for drawing rectangle links.
    protected int tableCellXOffset = 0;

    /** Stores position of top of this area relative to page column Ypos.
     *  Used to set the position of link hotspot rectangles.
     */
    private int absoluteYtop = 0;

    protected int contentRectangleWidth;

    protected int allocationWidth;

    /* the page this area is on */
    protected Page page;

    protected BackgroundProps background;

    private IDReferences idReferences;

//    protected ArrayList markers;

    // as defined in Section 6.1.1
    protected org.apache.fop.fo.FObj generatedBy;    // corresponds to 'generated-by' trait
//    protected HashMap returnedBy;

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
//        this.markers = new ArrayList();
//        this.returnedBy = new HashMap();
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
//        this.markers = new ArrayList();
//        this.returnedBy = new HashMap();
    }

    private void setFontState(FontState fontState) {
        // fontState.setFontInfo(this.page.getFontInfo());
        this.fontState = fontState;
    }

    public void addChild(Box child) {
        this.children.add(child);
        child.parent = this;
    }

    public void addChildAtStart(Box child) {
        this.children.add(0, child);
        child.parent = this;
    }

    public void addDisplaySpace(int size) {
        this.addChild(new DisplaySpace(size));
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

    public ArrayList getChildren() {
        return this.children;
    }

    public boolean hasChildren() {
        return (this.children.size() != 0);
    }

    /**
     * Tell whether this area contains any children which are not
     * DisplaySpace. This is used in determining whether to honor
     * keeps.
     */
    public boolean hasNonSpaceChildren() {
        if (this.children.size() > 0) {
            Iterator childIter = children.iterator();
            while (childIter.hasNext()) {
                if (! (childIter.next() instanceof DisplaySpace)) {
                    return true;
                }
            }
        }
        return false;
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

    public BackgroundProps getBackground() {
        return this.background;
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

    /**
     * Return absolute Y position of the current bottom of this area,
     * not counting any bottom padding or border. This is used
     * to set positions for link hotspots.
     * In fact, the position is not really absolute, but is relative
     * to the Ypos of the column-level AreaContainer, even when the
     * area is in a page header or footer!
     */
    public int getAbsoluteHeight() {
        return absoluteYtop + getPaddingTop() + getBorderTopWidth() +
                currentHeight;
    }

    /**
     * Set "absolute" Y position of the top of this area. In fact, the
     * position is not really absolute, but relative to the Ypos of
     * the column-level AreaContainer, even when the area is in a
     * page header or footer!
     * It is set from the value of getAbsoluteHeight() on the parent
     * area, just before adding this area.
     */
    public void setAbsoluteHeight(int value) {
        absoluteYtop = value;
    }

    public void increaseHeight(int amount) {
        this.currentHeight += amount;
    }

    // Remove allocation height of child
    public void removeChild(Area area) {
        this.currentHeight -= area.getHeight();
        this.children.remove(area);
    }

    public void removeChild(DisplaySpace spacer) {
        this.currentHeight -= spacer.getSize();
        this.children.remove(spacer);
    }

    public void remove() {
        this.parent.removeChild(this);
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public void setBackground(BackgroundProps bg) {
        this.background = bg;
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
        while (area != null && !(area instanceof AreaContainer)) {
            area = area.getParent();
        }
        return (AreaContainer)area;
    }

    public BorderAndPadding getBorderAndPadding() {
        return bp;
    }

//     public void addMarker(Marker marker) {
//         markers.add(marker);
//     }

//     public void addMarkers(ArrayList markers) {
//         markers.addAll(markers);
//     }

//     public ArrayList getMarkers() {
//         return markers;
//     }

//     public void addLineagePair(org.apache.fop.fo.FObj fo, int areaPosition) {
//         returnedBy.put(fo, new Integer(areaPosition));
//     }

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
