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

// FOP
import org.apache.fop.datatypes.*;

// Java
import java.util.Vector;

abstract public class Area extends Box {

    /* nominal font size and nominal font family incorporated in
       fontState */
    protected FontState fontState;
    protected BorderAndPadding bp=null;

    protected Vector children = new Vector();

    /* max size in line-progression-direction */
    protected int maxHeight;

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

    /* author : Seshadri G
    ** the fo which created it */	
    public org.apache.fop.fo.FObj foCreator;	
        
    public Area (FontState fontState) {
	setFontState(fontState);
    }

    public Area (FontState fontState, int allocationWidth, int maxHeight) {
	setFontState(fontState);
        this.allocationWidth = allocationWidth;
        this.contentRectangleWidth = allocationWidth;
        this.maxHeight = maxHeight;
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

    public FontInfo getFontInfo() {
        return this.page.getFontInfo();
    }

    public void end() {
    }

    public int getAllocationWidth() {
      /* ATTENTION: this may change your output!! (Karen Lease, 4mar2001)
	return this.allocationWidth - getPaddingLeft() - getPaddingRight()
      - getBorderLeftWidth() - getBorderRightWidth();
      */
      return this.allocationWidth ;
    }

    public void setAllocationWidth(int w) {
        this.allocationWidth = w;
        this.contentRectangleWidth = this.allocationWidth;
    }

    public Vector getChildren() {
        return this.children;
    }

    public int getContentWidth() {
      /* ATTENTION: this may change your output!! (Karen Lease, 4mar2001)
	return contentRectangleWidth  - getPaddingLeft() - getPaddingRight()
	- getBorderLeftWidth() - getBorderRightWidth();
      */
	return contentRectangleWidth ;
    }

    public FontState getFontState() {
        return this.fontState;
    }

    public int getContentHeight() {
        return this.currentHeight;
    }

    public int getHeight() {
        return this.currentHeight + getPaddingTop() + getPaddingBottom() +
               getBorderTopWidth() + getBorderBottomWidth();
    }

    public int getMaxHeight() {
        return this.maxHeight - getPaddingTop() - getPaddingBottom() -
               getBorderTopWidth() - getBorderBottomWidth();
    }

    public Page getPage() {
        return this.page;
    }

    public ColorType getBackgroundColor() {
        return this.backgroundColor;
    }

  // Must handle conditionality here, depending on isLast/isFirst
    public int getPaddingTop() {
        return (bp==null? 0 : bp.getPaddingTop(false));
    }

    public int getPaddingLeft() {
        return(bp==null? 0 :  bp.getPaddingLeft(false));
    }

    public int getPaddingBottom() {
        return (bp==null? 0 : bp.getPaddingBottom(false));
    }

    public int getPaddingRight() {
        return (bp==null? 0 : bp.getPaddingRight(false));
    }

  // Handle border-width, including conditionality
  // For now, just pass false everywhere!
    public int getBorderTopWidth() {
        return (bp==null? 0 : bp.getBorderTopWidth(false));
    }

    public int getBorderRightWidth() {
        return (bp==null? 0 :  bp.getBorderRightWidth(false));
    }

    public int getBorderLeftWidth() {
        return (bp==null? 0 : bp.getBorderLeftWidth(false));
    }

    public int getBorderBottomWidth() {
        return (bp==null? 0 : bp.getBorderBottomWidth(false));
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

    public int spaceLeft() {
        return maxHeight - currentHeight;
    }

    public void start() {
    }

    public void setHeight(int height) {
        if (height > currentHeight)
            currentHeight = height;
        absoluteHeight = height;

        if (currentHeight > getMaxHeight())
            currentHeight = getMaxHeight();
        absoluteHeight = getMaxHeight();

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

	public AreaContainer getNearestAncestorAreaContainer()
	{
		Area area = this.getParent();
		while (!(area instanceof AreaContainer))
		{
			area = area.getParent();
		} 
		return (AreaContainer)area;
	}

  public BorderAndPadding getBorderAndPadding() {
    return bp;
  }
}
