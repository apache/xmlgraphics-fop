/*-- $Id$ --

 ============================================================================
									 The Apache Software License, Version 1.1
 ============================================================================

		Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
		this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
		this list of conditions and the following disclaimer in the documentation
		and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
		include  the following  acknowledgment:  "This product includes  software
		developed  by the  Apache Software Foundation  (http://www.apache.org/)."
		Alternately, this  acknowledgment may  appear in the software itself,  if
		and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
		endorse  or promote  products derived  from this  software without  prior
		written permission. For written permission, please contact
		apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
		"Apache" appear  in their name,  without prior written permission  of the
		Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
import org.apache.fop.messaging.MessageHandler;

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

    /*hyphenation*/
    protected int hyphenate;
    protected char hyphenationChar;
    protected int hyphenationPushCharacterCount;
    protected int hyphenationRemainCharacterCount;
    protected String language;
    protected String country;

    protected Vector pendingFootnotes = null;

    public BlockArea(FontState fontState, int allocationWidth,
                     int maxHeight, int startIndent, int endIndent,
                     int textIndent, int align, int alignLastLine, int lineHeight) {
        super(fontState, allocationWidth, maxHeight);

        this.startIndent = startIndent;
        this.endIndent = endIndent;
        this.textIndent = textIndent;
        this.contentRectangleWidth =
          allocationWidth - startIndent - endIndent;
        this.align = align;
        this.alignLastLine = alignLastLine;
        this.lineHeight = lineHeight;

        if (fontState != null)
            this.halfLeading = (lineHeight - fontState.getFontSize()) / 2;
    }

    public void render(Renderer renderer) {
        renderer.renderBlockArea(this);
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
                    e.hasMoreElements();) {
                FootnoteBody fb = (FootnoteBody) e.nextElement();
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
        if (currentHeight + this.currentLineArea.getHeight() > maxHeight) {
            return null;
        }
        this.currentLineArea.changeHyphenation(language, country,
                                               hyphenate, hyphenationChar, hyphenationPushCharacterCount,
                                               hyphenationRemainCharacterCount);
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
        this.currentLineArea =
          new LineArea(fontState, lineHeight, halfLeading,
                       allocationWidth, startIndent, endIndent, currentLineArea);
        this.currentLineArea.changeHyphenation(language, country,
                                               hyphenate, hyphenationChar, hyphenationPushCharacterCount,
                                               hyphenationRemainCharacterCount);
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
                                       allocationWidth, startIndent + textIndent, endIndent, null);
    }

    public int getEndIndent() {
        return endIndent;
    }

    // KL: I think we should just return startIndent here!
    public int getStartIndent() {
        return startIndent + paddingLeft + borderWidthLeft;
    }

    public void setIndents(int startIndent, int endIndent) {
        this.startIndent = startIndent;
        this.endIndent = endIndent;
        this.contentRectangleWidth =
          allocationWidth - startIndent - endIndent;
    }

    public int spaceLeft() {
        return maxHeight - currentHeight;
    }

    public int getHalfLeading() {
        return halfLeading;
    }

    public void setHyphenation(String language, String country,
                               int hyphenate, char hyphenationChar,
                               int hyphenationPushCharacterCount,
                               int hyphenationRemainCharacterCount) {
        this.language = language;
        this.country = country;
        this.hyphenate = hyphenate;
        this.hyphenationChar = hyphenationChar;
        this.hyphenationPushCharacterCount = hyphenationPushCharacterCount;
        this.hyphenationRemainCharacterCount =
          hyphenationRemainCharacterCount;
    }

    public void addFootnote(FootnoteBody fb) {
        if (pendingFootnotes == null) {
            pendingFootnotes = new Vector();
        }
        pendingFootnotes.addElement(fb);
    }
}
