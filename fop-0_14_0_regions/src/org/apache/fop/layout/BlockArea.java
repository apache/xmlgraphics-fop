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

// Java
import java.util.Vector;
import java.util.Enumeration;

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

    public BlockArea(FontState fontState, int allocationWidth,
		     int maxHeight, int startIndent, int endIndent,
		     int textIndent, int align, int alignLastLine,
		     int lineHeight) {
	super(fontState, allocationWidth, maxHeight);

	this.startIndent = startIndent;
	this.endIndent = endIndent;
	this.textIndent = textIndent;
	this.contentRectangleWidth = allocationWidth - startIndent - endIndent;
	this.align = align;
	this.alignLastLine = alignLastLine;
	this.lineHeight = lineHeight;

        if (fontState != null)
	  this.halfLeading = (lineHeight - fontState.getFontSize())/2;
    }

    public void render(Renderer renderer) {
	renderer.renderBlockArea(this);
    }

    public void addLineArea(LineArea la) {
	if (!la.isEmpty()) {
	    this.addDisplaySpace(this.halfLeading);
	    int size = la.getHeight();
	    this.addChild(la);
	    this.increaseHeight(size);
	    this.addDisplaySpace(this.halfLeading);
	}
    }

    public int addPageNumberCitation(FontState fontState, float red, float green,
		       float blue, int wrapOption, LinkSet ls,
		       int whiteSpaceTreatment, String refid) {

        this.currentLineArea.changeFont(fontState);
	this.currentLineArea.changeColor(red, green, blue);
	this.currentLineArea.changeWrapOption(wrapOption);
	this.currentLineArea.changeWhiteSpaceTreatment(whiteSpaceTreatment);

        if (ls != null) {
            this.currentLinkSet = ls;
	    ls.setYOffset(currentHeight);
	}

	this.currentLineArea.addPageNumberCitation(refid,ls);            
	this.hasLines = true;
        
        return -1;

    }

    public int addText(FontState fontState, float red, float green,
		       float blue, int wrapOption, LinkSet ls,
		       int whiteSpaceTreatment, char data[],
		       int start, int end) { 
	int ts, te;
	char[] ca;
	
	ts = start;
	te = end;
	ca = data;

	if (currentHeight + currentLineArea.getHeight() > maxHeight) {
	    return start;
	}
		
	this.currentLineArea.changeFont(fontState);
	this.currentLineArea.changeColor(red, green, blue);
	this.currentLineArea.changeWrapOption(wrapOption);
	this.currentLineArea.changeWhiteSpaceTreatment(whiteSpaceTreatment);

	if (ls != null) {
            this.currentLinkSet = ls;
	    ls.setYOffset(currentHeight);
	}

	ts = this.currentLineArea.addText(ca, ts, te, ls);
	this.hasLines = true;
		
	while (ts != -1) {
	    this.currentLineArea.align(this.align);
	    this.addLineArea(this.currentLineArea);
	    this.currentLineArea = new
		LineArea(fontState, lineHeight, halfLeading,
			 allocationWidth, startIndent, endIndent);  
	    if (currentHeight + currentLineArea.getHeight() >
		this.maxHeight) {
		return ts;
	    }
	    this.currentLineArea.changeFont(fontState);
	    this.currentLineArea.changeColor(red, green, blue);
	    this.currentLineArea.changeWrapOption(wrapOption);
	    this.currentLineArea.changeWhiteSpaceTreatment(whiteSpaceTreatment);
	    if (ls != null) {
		ls.setYOffset(currentHeight);
	    }

	    ts = this.currentLineArea.addText(ca, ts, te, ls);
	}
	return -1;
    }

    public void end() {
	if (this.hasLines) {
	    this.currentLineArea.addPending();
	    this.currentLineArea.align(this.alignLastLine);
	    this.addLineArea(this.currentLineArea);
	}
    }

    public void start() {
	currentLineArea = new LineArea(fontState, lineHeight,
				       halfLeading, allocationWidth,
				       startIndent + textIndent,
				       endIndent);
    }

    public int getEndIndent() {
	return endIndent;
    }

    public int getStartIndent() {
	return startIndent + paddingLeft + borderWidthLeft;
    }

    public void setIndents(int startIndent, int endIndent) {
	this.startIndent = startIndent;
	this.endIndent = endIndent;
	this.contentRectangleWidth = allocationWidth - startIndent - endIndent;
    }
    
    public int spaceLeft() {
	return maxHeight - currentHeight;
    }
    
    public int getHalfLeading()
    {
        return halfLeading;
    }

}
