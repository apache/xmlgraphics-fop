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

// Java
import java.util.Vector;

abstract public class Area extends Box {

    /* nominal font size and nominal font family incorporated in
       fontState */
    protected FontState fontState;

    protected Vector children = new Vector();
	
    /* max size in line-progression-direction */
    protected int maxHeight;

    protected int currentHeight = 0;

    protected int contentRectangleWidth;

    protected int allocationWidth;

    /* the inner-most area container the area is in */
    protected AreaContainer areaContainer;

    /* the page this area is on */
    protected Page page;

    public Area (FontState fontState) {
	this.fontState = fontState;
    }

    public Area (FontState fontState, int allocationWidth, int maxHeight) {
	this.fontState = fontState;
	this.allocationWidth = allocationWidth;
	this.maxHeight = maxHeight;
    }

    public void addChild(Box child) {
	this.children.addElement(child);
	child.parent = this;
    }
    
    public void addChildAtStart(Box child) {
	this.children.insertElementAt(child,0);
	child.parent = this;
    }
	
    public void addDisplaySpace(int size) {
	this.addChild(new DisplaySpace(size));
	this.currentHeight += size;
    }

    public FontInfo getFontInfo() {
	return this.page.getFontInfo();
    }
	
    public void end() {
    }
    
    public int getAllocationWidth() {
	return this.allocationWidth;
    }
    
    public Vector getChildren() {
	return this.children;
    }
	
    public int getContentWidth() {
	return this.contentRectangleWidth;
    }

    public FontState getFontState() {
	return this.fontState;
    }

    public int getHeight() {
	return this.currentHeight;
    }

    public int getMaxHeight() {
	return this.maxHeight;
    }

    public Page getPage() {
	return this.page;
    }

    public void increaseHeight(int amount) {
	this.currentHeight += amount;
    }

    protected void removeChild(Area area) {
	this.currentHeight -= area.getHeight();
	this.children.removeElement(area);
    }
	
    public void remove() {
	this.parent.removeChild(this);
    }

    public void setPage(Page page) {
	this.page = page;
    }

    public int spaceLeft() {
	return maxHeight - currentHeight;
    }

    public void start() {
    }
}
