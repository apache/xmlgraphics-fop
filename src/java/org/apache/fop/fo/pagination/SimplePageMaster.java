/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.pagination;

// Java
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * A simple-page-master formatting object.
 * This creates a simple page from the specified regions
 * and attributes.
 */
public class SimplePageMaster extends FObj {
    /**
     * Page regions (regionClass, Region)
     */
    private Map regions;

    private String masterName;

    // used for node validation
    private boolean hasRegionBody = false;
    private boolean hasRegionBefore = false;
    private boolean hasRegionAfter = false;
    private boolean hasRegionStart = false;
    private boolean hasRegionEnd = false;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);

        LayoutMasterSet layoutMasterSet = (LayoutMasterSet) parent;

        if (getPropString(PR_MASTER_NAME) == null) {
            missingPropertyError("master-name");
        } else {
            layoutMasterSet.addSimplePageMaster(this);
        }

        //Well, there are only 5 regions so we can save a bit of memory here
        regions = new HashMap(5);
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: (region-body,region-before?,region-after?,region-start?,region-end?)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
            if (nsURI == FO_URI && localName.equals("region-body")) {
                if (hasRegionBody) {
                    tooManyNodesError(loc, "fo:region-body");
                } else {
                    hasRegionBody = true;
                }
            } else if (nsURI == FO_URI && localName.equals("region-before")) {
                if (!hasRegionBody) {
                    nodesOutOfOrderError(loc, "fo:region-body", "fo:region-before");
                } else if (hasRegionBefore) {
                    tooManyNodesError(loc, "fo:region-before");
                } else if (hasRegionAfter) {
                    nodesOutOfOrderError(loc, "fo:region-before", "fo:region-after");
                } else if (hasRegionStart) {
                    nodesOutOfOrderError(loc, "fo:region-before", "fo:region-start");
                } else if (hasRegionEnd) {
                    nodesOutOfOrderError(loc, "fo:region-before", "fo:region-end");
                } else {
                    hasRegionBody = true;
                }
            } else if (nsURI == FO_URI && localName.equals("region-after")) {
                if (!hasRegionBody) {
                    nodesOutOfOrderError(loc, "fo:region-body", "fo:region-after");
                } else if (hasRegionAfter) {
                    tooManyNodesError(loc, "fo:region-after");
                } else if (hasRegionStart) {
                    nodesOutOfOrderError(loc, "fo:region-after", "fo:region-start");
                } else if (hasRegionEnd) {
                    nodesOutOfOrderError(loc, "fo:region-after", "fo:region-end");
                } else {
                    hasRegionAfter = true;
                }
            } else if (nsURI == FO_URI && localName.equals("region-start")) {
                if (!hasRegionBody) {
                    nodesOutOfOrderError(loc, "fo:region-body", "fo:region-start");
                } else if (hasRegionStart) {
                    tooManyNodesError(loc, "fo:region-start");
                } else if (hasRegionEnd) {
                    nodesOutOfOrderError(loc, "fo:region-start", "fo:region-end");
                } else {
                    hasRegionStart = true;
                }
            } else if (nsURI == FO_URI && localName.equals("region-end")) {
                if (!hasRegionBody) {
                    nodesOutOfOrderError(loc, "fo:region-body", "fo:region-end");
                } else if (hasRegionEnd) {
                    tooManyNodesError(loc, "fo:region-end");
                } else {
                    hasRegionEnd = true;
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
    }

    /**
     * Make sure content model satisfied.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        if (!hasRegionBody) {
            missingChildElementError("(region-body, region-before?," +
                " region-after?, region-start?, region-end?)");
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#generatesReferenceAreas()
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * Returns the name of the simple-page-master.
     * @return the page master name
     */
    public String getMasterName() {
        return getPropString(PR_MASTER_NAME);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) {
        addRegion((Region)child);
    }

    /**
     * Adds a region to this simple-page-master.
     * @param region region to add
     */
    protected void addRegion(Region region) {
        String key = String.valueOf(region.getNameId());
        regions.put(key, region);
    }

    /**
     * Returns the region for a given region class.
     * @param regionClass region class to lookup
     * @return the region, null if it doesn't exist
     */
    public Region getRegion(int regionId) {
        return (Region) regions.get(String.valueOf(regionId));
    }

    /**
     * Returns a Map of regions associated with this simple-page-master
     * @return the regions
     */
    public Map getRegions() {
        return regions;
    }

    /**
     * Indicates if a region with a given name exists in this
     * simple-page-master.
     * @param regionName name of the region to lookup
     * @return True if a region with this name exists
     */
    protected boolean regionNameExists(String regionName) {
        for (Iterator regenum = regions.values().iterator();
                regenum.hasNext();) {
            Region r = (Region) regenum.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:simple-page-master";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_SIMPLE_PAGE_MASTER;
    }
}
