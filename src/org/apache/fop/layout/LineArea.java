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
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
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

import org.apache.fop.render.Renderer;
import org.apache.fop.messaging.MessageHandler;

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Rectangle;

import org.apache.fop.fo.properties.WrapOption; // for enumerated
// values 
import org.apache.fop.fo.properties.WhiteSpaceTreatment; // for
// enumerated values 
import org.apache.fop.fo.properties.TextAlign; // for enumerated
// values 
import org.apache.fop.fo.properties.TextAlignLast; // for enumerated
// values 

import org.apache.fop.datatypes.IDNode;

public class LineArea extends Area {
	
    protected int lineHeight;
    protected int halfLeading;
    protected int nominalFontSize;
    protected int nominalGlyphHeight;
    
    protected int allocationHeight;
    protected int startIndent;
    protected int endIndent;
    
    private int placementOffset;
    
    private FontState currentFontState; // not the nominal, which is
    // in this.fontState 
    private float red, green, blue;
    private int wrapOption;
    private int whiteSpaceTreatment;

    /* the width of text that has definitely made it into the line
       area */
    protected int finalWidth = 0;

    /* the position to shift a link rectangle in order to compensate for links embedded within a word*/
    protected int embeddedLinkStart=0;

    /* the width of the current word so far */
    protected int wordWidth = 0;

    /* values that prev (below) may take */
    protected static final int NOTHING = 0;
    protected static final int WHITESPACE = 1;
    protected static final int TEXT = 2;

    /* the character type of the previous character */
    protected int prev = NOTHING;

    /* the position in data[] of the start of the current word */
    protected int wordStart;

    /* the length (in characters) of the current word */
    protected int wordLength = 0;

    /* width of spaces before current word */
    protected int spaceWidth = 0;

    /* the inline areas that have not yet been added to the line
       because subsequent characters to come (in a different addText)
       may be part of the same word */
    protected Vector pendingAreas = new Vector();

    /* the width of the pendingAreas */
    protected int pendingWidth = 0;

    public LineArea(FontState fontState, int lineHeight, int
		    halfLeading, int allocationWidth, int startIndent,
		    int endIndent) { 
	super(fontState);        
	
	this.currentFontState = fontState;
	this.lineHeight = lineHeight;
	this.nominalFontSize = fontState.getFontSize();
	this.nominalGlyphHeight = fontState.getAscender() -
	    fontState.getDescender(); 
	
	this.placementOffset = fontState.getAscender();
	this.contentRectangleWidth = allocationWidth - startIndent -
	    endIndent; 
	this.fontState = fontState;
	
	this.allocationHeight = this.nominalGlyphHeight;
	this.halfLeading = this.lineHeight - this.allocationHeight;
	
	this.startIndent = startIndent;
	this.endIndent = endIndent;
	
    }
    
    public void render(Renderer renderer) {
	renderer.renderLineArea(this);
    }

    public int addPageNumberCitation(String refid, LinkSet ls)
    {
       
	/* We should add code here to handle the case where the page number doesn't fit on the current line
	*/

        //Space must be alloted to the page number, so currently we give it 3 spaces 
        int width= currentFontState.width(32) * 3;
        
        PageNumberInlineArea pia = new PageNumberInlineArea(currentFontState,
        this.red, this.green,
        this.blue,
        refid,
        width);                        
                
        pendingAreas.addElement(pia);
        pendingWidth += width;
        wordWidth = 0;
        prev = TEXT;

        return -1;
    }


    public int addText(char odata[], int start, int end, LinkSet ls) {
	boolean overrun = false;

	wordStart = start;
	wordLength = 0;
	wordWidth = 0;
	char[] data = new char[odata.length];
	for(int count=0;count <odata.length; count++) {
		data[count] = odata[count];
	}

	/* iterate over each character */
	for (int i = start; i < end; i++) {
	    int charWidth;
	    /* get the character */
	    char c = data[i];
	    
	    if (c > 127) {
		/* this class shouldn't be hard coded */
		char d =
		    org.apache.fop.render.pdf.CodePointMapping.map[c];
		if (d != 0) {
		    c = data[i] = d;
		} else {
		    MessageHandler.error("ch"
				       + (int)c + "?");
		    c = data[i] = '#';
		}
	    }
	    
	    charWidth = currentFontState.width(c);
	    
	    if ((c == ' ') ||
		(c == '\n') ||
		(c == '\r') ||
		(c == '\t')) { // whitespace
		
		if (prev == WHITESPACE) {

		    // if current & previous are WHITESPACE

		    if (this.whiteSpaceTreatment ==
			WhiteSpaceTreatment.PRESERVE) { 
			if (c == ' ') {
			    spaceWidth += currentFontState.width(32);
			} else if (c == '\n') {
			    // force line break
			    return i;
			} else if (c == '\t') {
			    spaceWidth += 8 * currentFontState.width(32);
			}
		    } // else ignore it
		    
		} else if (prev == TEXT) {
		    
		    // if current is WHITESPACE and previous TEXT

		    // the current word made it, so

		    // add the space before the current word (if there
		    // was some)
    
		    if (spaceWidth > 0) {
			addChild(new InlineSpace(spaceWidth));
			finalWidth += spaceWidth;
			spaceWidth = 0;
		    }
		    
		    // add any pending areas

		    Enumeration e = pendingAreas.elements();
		    while (e.hasMoreElements()) {
			InlineArea inlineArea = (InlineArea) e.nextElement();
			if (ls != null) {
			    Rectangle lr =
				new Rectangle(finalWidth,
					      0,
					      inlineArea.getContentWidth(),
					      fontState.getFontSize());
			    ls.addRect(lr, this);
			}

			addChild(inlineArea);
		    }
		    finalWidth += pendingWidth;

		    // reset pending areas array
		    pendingWidth = 0;
		    pendingAreas = new Vector();

		    // add the current word

		    if (wordLength > 0) {
			InlineArea ia = new InlineArea(currentFontState,
						       this.red, this.green,
						       this.blue, new
						       String(data, wordStart,
							      wordLength),
						       wordWidth);
			addChild(ia);
			if (ls != null) {
			    Rectangle lr =
				new Rectangle(finalWidth,
					      0,
					      ia.getContentWidth(),
					      fontState.getFontSize());                            
			    ls.addRect(lr, this);
			}
			finalWidth += wordWidth;
			
			// reset word width
			wordWidth = 0;
		    }

		    // deal with this new whitespace following the
		    // word we just added

		    prev = WHITESPACE;

                    embeddedLinkStart=0; //reset embeddedLinkStart since a space was encountered                    
		    
		    if (this.whiteSpaceTreatment ==
			WhiteSpaceTreatment.IGNORE) {
			// do nothing
		    } else {
		        spaceWidth = currentFontState.width(32);
		    }
		    if (this.whiteSpaceTreatment ==
			WhiteSpaceTreatment.PRESERVE) { 
			if (c == '\n') {
			    // force a line break
			    return i;
			} else if (c == '\t') {
			    spaceWidth = currentFontState.width(32);
			}
		    }
		    
		} else {
		    
		    // if current is WHITESPACE and no previous

		    if (this.whiteSpaceTreatment ==
			WhiteSpaceTreatment.PRESERVE) { 
			prev = WHITESPACE;
			spaceWidth = currentFontState.width(32);
		    } else {
			// skip over it
			start++;
		    }
		}
		
	    } else { // current is TEXT

		if (prev == WHITESPACE) {
		    
		    // if current is TEXT and previous WHITESPACE

		    wordWidth = charWidth;
		    if ((finalWidth + spaceWidth + wordWidth) >
			this.getContentWidth()) { 
			if (overrun)
			    MessageHandler.error(">");
			if (this.wrapOption == WrapOption.WRAP)
			    return i;
		    }
		    prev = TEXT;
		    wordStart = i;
		    wordLength = 1;
		} else if (prev == TEXT) {

		    wordLength++;
		    wordWidth += charWidth;
		} else { // nothing previous

		    prev = TEXT;
		    wordStart = i;
		    wordLength = 1;
		    wordWidth = charWidth;
		}

		if ((finalWidth + spaceWidth + pendingWidth + wordWidth) >
		    this.getContentWidth()) { 
		    
		    // BREAK MID WORD
		    if (wordStart == start) { // if couldn't even fit
			// first word 
			overrun = true;
			// if not at start of line, return word start
			// to try again on a new line
			if (finalWidth > 0) {
			    return wordStart;
			}
		    } else if (this.wrapOption == WrapOption.WRAP) {
			return wordStart;
		    }
		}

	    }
	} // end of iteration over text

	if (prev == TEXT) {

	    InlineArea pia = new InlineArea(currentFontState, this.red,
				    this.green, this.blue, new
				    String(data, wordStart,
					   wordLength), wordWidth); 
	    
	    if (ls != null) {
		Rectangle lr =
		    new Rectangle(finalWidth + spaceWidth + embeddedLinkStart,
				  spaceWidth,
				  pia.getContentWidth(),
			          fontState.getFontSize());                		
		ls.addRect(lr, this);
	    }
	    
	    embeddedLinkStart += wordWidth;
	    pendingAreas.addElement(pia);
	    pendingWidth += wordWidth;
	    wordWidth = 0;
	}

	if (overrun)
	    MessageHandler.error(">");
	return -1;
    }

    public void addPending() {
	if (spaceWidth > 0) {
	    addChild(new InlineSpace(spaceWidth));
	    finalWidth += spaceWidth;
	    spaceWidth = 0;
	}

	Enumeration e = pendingAreas.elements();
	while (e.hasMoreElements()) {
	    InlineArea inlineArea = (InlineArea) e.nextElement();
	    addChild(inlineArea);
	}
	finalWidth += pendingWidth;
	
	// reset pending areas array
	pendingWidth = 0;
	pendingAreas = new Vector();
    }

    public void align(int type) {
	int padding = 0;
	
	switch (type) {
	case TextAlign.START: // left
	    padding = this.getContentWidth() - finalWidth;
	    endIndent += padding;
	    break;
	case TextAlign.END: // right
	    padding = this.getContentWidth() - finalWidth;
	    startIndent += padding;
	    break;
	case TextAlign.CENTER: // center
	    padding = (this.getContentWidth() - finalWidth)/2;
	    startIndent += padding;
	    endIndent += padding;
	    break;
	case TextAlign.JUSTIFY: // justify
	    Vector spaceList = new Vector();

	    int spaceCount = 0;
	    Enumeration e = children.elements();
	    while (e.hasMoreElements()) {
		Box b = (Box)e.nextElement();
		if (b instanceof InlineSpace) { 
		    InlineSpace space = (InlineSpace)b;
		    spaceList.addElement(space);
		    spaceCount++;
		}
	    }
	    if (spaceCount > 0) {
		padding = (this.getContentWidth() - finalWidth) /
		    spaceCount; 
	    } else { // no spaces
		padding = 0;
	    }
	    Enumeration f = spaceList.elements();
	    while (f.hasMoreElements()) {
		InlineSpace space2 = (InlineSpace)f.nextElement();
		int i = space2.getSize();
		space2.setSize(i + padding);
	    }
	}
    }
    
    public void changeColor(float red, float green, float blue) {
	this.red = red;
	this.green = green;
	this.blue = blue;
    }
    
    public void changeFont(FontState fontState) {
	this.currentFontState = fontState;
    }
    
    public void changeWhiteSpaceTreatment(int whiteSpaceTreatment) {
	this.whiteSpaceTreatment = whiteSpaceTreatment;
    }
    
    public void changeWrapOption(int wrapOption) {
	this.wrapOption = wrapOption;
    }
    
    public int getEndIndent() {
	return endIndent;
    }
    
    public int getHeight() {
	return this.allocationHeight;
    }
    
    public int getPlacementOffset() {
	return this.placementOffset;
    }
    
    public int getStartIndent() {
	return startIndent;
    }
    
    public boolean isEmpty() {
	return (prev==0);
    }

    public Vector getPendingAreas() {
	return pendingAreas;
    }

    public int getPendingWidth() {
	return pendingWidth;
    }

    public void setPendingAreas(Vector areas) {
	pendingAreas = areas;
    }

    public void setPendingWidth(int width) {
	pendingWidth = width;
    }

}
