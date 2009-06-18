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
import org.apache.fop.fo.properties.*;

// Java
import java.util.Vector;
import java.util.Enumeration;

/**
 * This class represents a Block Area.
 * A block area is made up of a sequence of Line Areas.
 *
 * This class is used to organise the sequence of line areas as
 * inline areas are added to this block it creates and ands line areas
 * to hold the inline areas.
 * This uses the line-height and line-stacking-strategy to work
 * out how to stack the lines.
 */
public class BlockArea extends Area {

    /* relative to area container */
    protected int startIndent;
    protected int endIndent;

    /* first line startIndent modifier */
    protected int textIndent;

    protected int lineHeight;

    protected int halfLeading;


    /* text-align of all but the last line */
    protected int align;

    /* text-align of the last line */
    protected int alignLastLine;

    protected LineArea currentLineArea;
    protected LinkSet currentLinkSet;

    /* have any line areas been used? */
    protected boolean hasLines = false;

    /* hyphenation */
    protected HyphenationProps hyphProps;

    protected Vector pendingFootnotes = null;

    public BlockArea(FontState fontState, int allocationWidth, int maxHeight,
                     int startIndent, int endIndent, int textIndent,
                     int align, int alignLastLine, int lineHeight) {
        super(fontState, allocationWidth, maxHeight);

        this.startIndent = startIndent;
        this.endIndent = endIndent;
        this.textIndent = textIndent;
        this.contentRectangleWidth = allocationWidth - startIndent
                                     - endIndent;
        this.align = align;
        this.alignLastLine = alignLastLine;
        this.lineHeight = lineHeight;

        if (fontState != null)
            this.halfLeading = (lineHeight - fontState.getFontSize()) / 2;
    }

    /**
     * Add a Line Area to this block area.
     * Used internally to add a completed line area to this block area
     * when either a new line area is created or this block area is
     * completed.
     *
     * @param la the LineArea to add
     */
    protected void addLineArea(LineArea la) {
        if (!la.isEmpty()) {
            la.verticalAlign();
            this.addDisplaySpace(this.halfLeading);
            int size = la.getHeight();
            this.addChild(la);
            this.increaseHeight(size);
            this.addDisplaySpace(this.halfLeading);
        }
        // add pending footnotes
        if (pendingFootnotes != null) {
            for (Enumeration e = pendingFootnotes.elements();
                    e.hasMoreElements(); ) {
                FootnoteBody fb = (FootnoteBody)e.nextElement();
                Page page = getPage();
                if (!Footnote.layoutFootnote(page, fb, this)) {
                    page.addPendingFootnote(fb);
                }
            }
            pendingFootnotes = null;
        }
    }

    /**
     * Get the current line area in this block area.
     * This is used to get the current line area for adding
     * inline objects to.
     * This will return null if there is not enough room left
     * in the block area to accomodate the line area.
     *
     * @return the line area to be used to add inlie objects
     */
    public LineArea getCurrentLineArea() {
        if (currentHeight + lineHeight > maxHeight) {
            return null;
        }
        this.currentLineArea.changeHyphenation(hyphProps);
        this.hasLines = true;
        return this.currentLineArea;
    }

    /**
     * Create a new line area to add inline objects.
     * This should be called after getting the current line area
     * and discovering that the inline object will not fit inside the current
     * line. This method will create a new line area to place the inline
     * object into.
     * This will return null if the new line cannot fit into the block area.
     *
     * @return the new current line area, which will be empty.
     */
    public LineArea createNextLineArea() {
        if (this.hasLines) {
            this.currentLineArea.align(this.align);
            this.addLineArea(this.currentLineArea);
        }
        this.currentLineArea = new LineArea(fontState, lineHeight,
                                            halfLeading, allocationWidth,
                                            startIndent, endIndent,
                                            currentLineArea);
        this.currentLineArea.changeHyphenation(hyphProps);
        if (currentHeight + lineHeight > maxHeight) {
            return null;
        }
        return this.currentLineArea;
    }

    public void setupLinkSet(LinkSet ls) {
        if (ls != null) {
            this.currentLinkSet = ls;
            ls.setYOffset(currentHeight);
        }
    }

    /**
     * Notify this block that the area has completed layout.
     * Indicates the the block has been fully laid out, this will
     * add (if any) the current line area.
     */
    public void end() {
        if (this.hasLines) {
            this.currentLineArea.addPending();
            this.currentLineArea.align(this.alignLastLine);
            this.addLineArea(this.currentLineArea);
        }
    }

    public void start() {
        currentLineArea = new LineArea(fontState, lineHeight, halfLeading,
                                       allocationWidth,
                                       startIndent + textIndent, endIndent,
                                       null);
    }

    public int getEndIndent() {
        return endIndent;
    }

    // KL: I think we should just return startIndent here!
    public int getStartIndent() {
        // return startIndent + paddingLeft + borderWidthLeft;
        return startIndent;
    }

    public void setIndents(int startIndent, int endIndent) {
        this.startIndent = startIndent;
        this.endIndent = endIndent;
        this.contentRectangleWidth = allocationWidth - startIndent
                                     - endIndent;
    }

    /**
     * Return the maximum space remaining for this area's content in
     * the block-progression-dimension.
     * Remove top and bottom padding and spacing since these reduce
     * available space for content and they are not yet accounted for
     * in the positioning of the object.
     */
    public int spaceLeft() {
        // return maxHeight - currentHeight ;
        return maxHeight - currentHeight -
	    (getPaddingTop() + getPaddingBottom()
	     + getBorderTopWidth() + getBorderBottomWidth());
    }

    public int getHalfLeading() {
        return halfLeading;
    }

    public void setHyphenation(HyphenationProps hyphProps) {
        this.hyphProps = hyphProps;
    }

    public void addFootnote(FootnoteBody fb) {
        if (pendingFootnotes == null) {
            pendingFootnotes = new Vector();
        }
        pendingFootnotes.addElement(fb);
    }

}
