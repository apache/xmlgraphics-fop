/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// FOP

import org.apache.fop.datatypes.FODimension;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.CTM;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.RegionReference;

import org.xml.sax.Attributes;

/**
 * This is an abstract base class for pagination regions
 */
public abstract class Region extends FObj {
    public static final String PROP_REGION_NAME = "region-name";

    final static String BEFORE = "before";
    final static String START =  "start";
    final static String END =    "end";
    final static String AFTER =  "after";
    final static String BODY =   "body";

    private SimplePageMaster _layoutMaster;
    private String _regionName;

    protected int overflow;


    protected Region(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        // regions may have name, or default
        if (null == this.properties.get(PROP_REGION_NAME)) {
            setRegionName(getDefaultRegionName());
        } else if (this.properties.get(PROP_REGION_NAME).getString().equals("")) {
            setRegionName(getDefaultRegionName());
        } else {
            setRegionName(this.properties.get(PROP_REGION_NAME).getString());
            // check that name is OK. Not very pretty.
            if (isReserved(getRegionName())
                    &&!getRegionName().equals(getDefaultRegionName())) {
                throw new FOPException(PROP_REGION_NAME + " '" + _regionName
                                       + "' for " + this.name
                                       + " not permitted.");
            }
        }

	if (parent instanceof SimplePageMaster) {
	    _layoutMaster = (SimplePageMaster)parent;
	}
	else {
            throw new FOPException(this.name + " must be child "
                                   + "of simple-page-master, not "
                                   + parent.getName());
        }
    }

    /**
     * Creates a RegionViewport Area object for this pagination Region.
     */
    public RegionViewport makeRegionViewport(FODimension reldims, CTM pageCTM) {
        Rectangle2D relRegionRect = getViewportRectangle(reldims);
        Rectangle2D absRegionRect = pageCTM.transform(relRegionRect);
        // Get the region viewport rectangle in absolute coords by
        // transforming it using the page CTM
	return new RegionViewport(absRegionRect);
    }


    abstract protected Rectangle getViewportRectangle(FODimension pageRefRect);

    /**
     * Create the region reference area for this region master.
     * @param absRegVPRect The region viewport rectangle is "absolute" coordinates
     * where x=distance from left, y=distance from bottom, width=right-left
     * height=top-bottom
     */
    public RegionReference makeRegionReferenceArea(Rectangle2D absRegVPRect) {
	RegionReference r = new RegionReference(getRegionAreaClass());
	setRegionTraits(r, absRegVPRect);
	return r;
    }

    protected void setRegionTraits(RegionReference r, Rectangle2D absRegVPRect) {
        // Common Border, Padding, and Background Properties
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();
	/*        this.backgroundColor =
		  this.properties.get("background-color").getColorType();*/

        // this.properties.get("clip");
        // this.properties.get("display-align");
        this.overflow = this.properties.get("overflow").getEnum();
	FODimension reldims = new FODimension(0,0);
	r.setCTM(propMgr.getCTMandRelDims(absRegVPRect, reldims));

	//r.setBackground(bProps);
    }

    /**
     * Return the enumerated value designating this type of region in the
     * Area tree.
     */
    abstract protected int getRegionAreaClass();

    /**
     * Returns the default region name (xsl-region-before, xsl-region-start,
     * etc.)
     */
    protected abstract String getDefaultRegionName();


    public abstract String getRegionClass();


    /**
     * Returns the name of this region
     */
    public String getRegionName() {
        return _regionName;
    }

    private void setRegionName(String name) {
        _regionName = name;
    }

    protected SimplePageMaster getPageMaster() {
        return _layoutMaster;
    }

    /**
     * Checks to see if a given region name is one of the reserved names
     *
     * @param name a region name to check
     * @return true if the name parameter is a reserved region name
     */
    protected boolean isReserved(String name) throws FOPException {
        return (name.equals("xsl-region-before")
                || name.equals("xsl-region-start")
                || name.equals("xsl-region-end")
                || name.equals("xsl-region-after")
                || name.equals("xsl-before-float-separator")
                || name.equals("xsl-footnote-separator"));
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

    protected Region getSiblingRegion(String regionClass) {
	// Ask parent for region
	return  _layoutMaster.getRegion(regionClass);
    }

    boolean getPrecedence() {
        return false;
    }

    int getExtent() {
        return 0;
    }

}
