/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.area.CTM;
import org.apache.fop.datatypes.FODimension;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Page;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.RegionReference;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.apps.FOPException;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import org.xml.sax.Attributes;

/**
 * A simple-page-master formatting object.
 * This creates a simple page from the specified regions
 * and attributes.
 */
public class SimplePageMaster extends FObj {
    /**
     * Page regions (regionClass, Region)
     */
    private HashMap _regions;

    PageMaster pageMaster;
    String masterName;

    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        if (parent.getName().equals("fo:layout-master-set")) {
            LayoutMasterSet layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.properties.get("master-name").getString();
            if (masterName == null) {
                log.warn("simple-page-master does not have "
                                       + "a master-name and so is being ignored");
            } else {
                layoutMasterSet.addSimplePageMaster(this);
            }
        } else {
            throw new FOPException("fo:simple-page-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName());
        }
        _regions = new HashMap();
    }

    /**
     * At the end of this element read all the information and create
     * the page master.
     */
    protected void end() {
        int pageWidth =
            this.properties.get("page-width").getLength().mvalue();
        int pageHeight =
            this.properties.get("page-height").getLength().mvalue();
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        // Get absolute margin properties (top, left, bottom, right)
        MarginProps mProps = propMgr.getMarginProps();

	/* Create the page reference area rectangle in first quadrant coordinates
	 * (ie, 0,0 is at bottom,left of the "page media" and y increases
	 * when moving towards the top of the page.
	 * The media rectangle itself is (0,0,pageWidth,pageHeight).
	 */
	Rectangle pageRefRect =
	    new Rectangle(mProps.marginLeft, mProps.marginTop,
			  pageWidth - mProps.marginLeft - mProps.marginRight,
			  pageHeight - mProps.marginTop - mProps.marginBottom);

	// ??? KL shouldn't this take the viewport too???
	Page page = new Page();  // page reference area

        // Set up the CTM on the page reference area based on writing-mode
        // and reference-orientation
	FODimension reldims=new FODimension(0,0);
	CTM pageCTM = propMgr.getCTMandRelDims(pageRefRect, reldims);

	// Create a RegionViewport/ reference area pair for each page region

	boolean bHasBody=false;

        for (Iterator regenum = _regions.values().iterator();
                regenum.hasNext(); ) {
            Region r = (Region)regenum.next();
	    RegionViewport rvp = r.makeRegionViewport(reldims, pageCTM);
	    rvp.setRegion(r.makeRegionReferenceArea(rvp.getViewArea()));
	    page.setRegion(r.getRegionAreaClass(), rvp);
	    if (r.getRegionAreaClass() == RegionReference.BODY) {
		bHasBody = true;
	    }
        }

	if (!bHasBody) {
            log.error("simple-page-master has no region-body");
        }

	this.pageMaster = new PageMaster(new PageViewport(page,
					   new Rectangle(0,0,
							 pageWidth,pageHeight)));

	//  _regions = null; // PageSequence access SimplePageMaster....
        children = null;
        properties = null;
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    public PageMaster getPageMaster() {
        return this.pageMaster;
    }

    public PageMaster getNextPageMaster() {
        return this.pageMaster;
    }

    public String getMasterName() {
        return masterName;
    }

    protected void addChild(FONode child) {
	if (child instanceof Region) {
	    addRegion((Region)child);
	}
	else {
	    log.error("SimplePageMaster cannot have child of type " +
		      child.getName());
	}
    }

    protected void addRegion(Region region) {
	String key = region.getRegionClass();
        if (_regions.containsKey(key)) {
            log.error("Only one region of class "
                                   + key
                                   + " allowed within a simple-page-master.");
            // throw new FOPException("Only one region of class "
//                                    + key
//                                    + " allowed within a simple-page-master.");
        } else {
            _regions.put(key, region);
        }
    }

    protected Region getRegion(String regionClass) {
        return (Region)_regions.get(regionClass);
    }

    protected HashMap getRegions() {
        return _regions;
    }

    protected boolean regionNameExists(String regionName) {
        for (Iterator regenum = _regions.values().iterator();
                regenum.hasNext(); ) {
            Region r = (Region)regenum.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }
}

