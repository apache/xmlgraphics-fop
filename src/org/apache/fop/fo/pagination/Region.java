/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.RegionArea;

/**
 * This is an abstract base class for pagination regions
 */
public abstract class Region extends FObj {
    public static final String PROP_REGION_NAME = "region-name";

    private SimplePageMaster _layoutMaster;
    private String _regionName;

    protected Region(FObj parent,
                     PropertyList propertyList) throws FOPException {
        super(parent, propertyList);
        this.name = getElementName();

        // regions may have name, or default
        if (null == this.properties.get(PROP_REGION_NAME)) {
            setRegionName(getDefaultRegionName());
        } else if ( this.properties.get(
              PROP_REGION_NAME).getString().equals("")) {
            setRegionName(getDefaultRegionName());
        } else {
            setRegionName(
              this.properties.get(PROP_REGION_NAME).getString());
            // check that name is OK. Not very pretty.
            if (isReserved(getRegionName()) &&
                    !getRegionName().equals(getDefaultRegionName())) {
                throw new FOPException(PROP_REGION_NAME + " '" +
                                       _regionName + "' for "+this.name + " not permitted.");
            }
        }

        if (parent.getName().equals("fo:simple-page-master")) {
            _layoutMaster = (SimplePageMaster) parent;
            getPageMaster().addRegion(this);
        } else {
            throw new FOPException(getElementName() + " must be child " +
                                   "of simple-page-master, not " + parent.getName());
        }
    }

    /**
     * Creates a Region layout object for this pagination Region.
     */
    abstract RegionArea makeRegionArea( int allocationRectangleXPosition,
                                        int allocationRectangleYPosition,
                                        int allocationRectangleWidth, int allocationRectangleHeight);

    /**
     * Returns the default region name (xsl-region-before, xsl-region-start,
     * etc.)
     */
    protected abstract String getDefaultRegionName();

    /**
     * Returns the element name ("fo:region-body", "fo:region-start",
     * etc.)
     */
    protected abstract String getElementName();

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
        return (name.equals("xsl-region-before") ||
                name.equals("xsl-region-start") ||
                name.equals("xsl-region-end") ||
                name.equals("xsl-region-after") ||
                name.equals("xsl-before-float-separator") ||
                name.equals("xsl-footnote-separator"));
    }

    public boolean generatesReferenceAreas() {
        return true;
    }

}
