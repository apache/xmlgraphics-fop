/*-- $Id$ --
 *
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */
 
package org.apache.fop.layout;

public class PageMaster {

    private int width;
    private int height;
	
    private BodyRegionArea body;
    private RegionArea before;
    private RegionArea after;
    private RegionArea start;
    private RegionArea end;

    public PageMaster(int pageWidth, int pageHeight) {
	this.width = pageWidth;
	this.height = pageHeight;
    }

    public void addAfter(RegionArea region) {
	this.after = region;
    }

    public void addBefore(RegionArea region) {
	this.before = region;
    }

    public void addBody(BodyRegionArea region) {
	this.body = region;
    }

    public void addEnd(RegionArea region) {
	this.end = region;
    }
	
    public void addStart(RegionArea region) {
	this.start = region;
    }

    public int getHeight() {
	return this.height;
    }

    public int getWidth() {
	return this.width;
    }

    public Page makePage(AreaTree areaTree) {
	Page p = new Page(areaTree, this.height, this.width);
	if (this.body != null) {
	    p.addBody(body.makeBodyAreaContainer());
	}
	if (this.before != null) {
	    p.addBefore(before.makeAreaContainer());
	}
	if (this.after != null) {
	    p.addAfter(after.makeAreaContainer());
	}
	if (this.start != null) {
	    p.addStart(start.makeAreaContainer());
	}
	if (this.end != null) {
	    p.addEnd(end.makeAreaContainer());
	}
	
	return p;
    }
}
