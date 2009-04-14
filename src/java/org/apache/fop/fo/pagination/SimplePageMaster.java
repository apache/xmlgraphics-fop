/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.SimplePercentBaseContext;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonMarginBlock;

/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_simple-page-master">
 * <code>fo:simple-page-master</code></a> object.
 * This creates a simple page from the specified regions
 * and attributes.
 */
public class SimplePageMaster extends FObj {
    // The value of properties relevant for fo:simple-page-master.
    private CommonMarginBlock commonMarginBlock;
    private String masterName;
    private Length pageHeight;
    private Length pageWidth;
    private Numeric referenceOrientation;
    private int writingMode;
    // End of property values

    /**
     * Page regions (regionClass, Region)
     */
    private Map regions;

     // used for node validation
    private boolean hasRegionBody = false;
    private boolean hasRegionBefore = false;
    private boolean hasRegionAfter = false;
    private boolean hasRegionStart = false;
    private boolean hasRegionEnd = false;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     */
    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        commonMarginBlock = pList.getMarginBlockProps();
        masterName = pList.get(PR_MASTER_NAME).getString();
        pageHeight = pList.get(PR_PAGE_HEIGHT).getLength();
        pageWidth = pList.get(PR_PAGE_WIDTH).getLength();
        referenceOrientation = pList.get(PR_REFERENCE_ORIENTATION).getNumeric();
        writingMode = pList.getWritingMode();

        if (masterName == null || masterName.equals("")) {
            missingPropertyError("master-name");
        }
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        LayoutMasterSet layoutMasterSet = (LayoutMasterSet) parent;

        if (masterName == null) {
            missingPropertyError("master-name");
        } else {
            layoutMasterSet.addSimplePageMaster(this);
        }

        //Well, there are only 5 regions so we can save a bit of memory here
        regions = new HashMap(5);
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (!hasRegionBody) {
            missingChildElementError(
                    "(region-body, region-before?, region-after?, region-start?, region-end?)");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL Content Model: (region-body,region-before?,region-after?,region-start?,region-end?)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
            throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("region-body")) {
                if (hasRegionBody) {
                    tooManyNodesError(loc, "fo:region-body");
                } else {
                    hasRegionBody = true;
                }
            } else if (localName.equals("region-before")) {
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
            } else if (localName.equals("region-after")) {
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
            } else if (localName.equals("region-start")) {
                if (!hasRegionBody) {
                    nodesOutOfOrderError(loc, "fo:region-body", "fo:region-start");
                } else if (hasRegionStart) {
                    tooManyNodesError(loc, "fo:region-start");
                } else if (hasRegionEnd) {
                    nodesOutOfOrderError(loc, "fo:region-start", "fo:region-end");
                } else {
                    hasRegionStart = true;
                }
            } else if (localName.equals("region-end")) {
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
    }

    /** {@inheritDoc} */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode child) throws FOPException {
        if (child instanceof Region) {
            addRegion((Region)child);
        } else {
            super.addChildNode(child);
        }
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
     * Gets the context for the width of the page-reference-area,
     * taking into account the reference-orientation.
     *
     * @param lengthBase    the lengthBase to use to resolve percentages
     * @return context for the width of the page-reference-area
     */
    protected final PercentBaseContext getPageWidthContext(int lengthBase) {
        return (this.referenceOrientation.getValue() % 180 == 0)
                ? new SimplePercentBaseContext(
                        null,
                        lengthBase,
                        this.getPageWidth().getValue())
                : new SimplePercentBaseContext(
                        null,
                        lengthBase,
                        this.getPageHeight().getValue());
    }

    /**
     * Gets the context for the height of the page-reference-area,
     * taking into account the reference-orientation.
     *
     * @param lengthBase    the lengthBase to use to resolve percentages
     * @return the context for the height of the page-reference-area
     */
    protected final PercentBaseContext getPageHeightContext(int lengthBase) {
        return (this.referenceOrientation.getValue() % 180 == 0)
                ? new SimplePercentBaseContext(
                        null,
                        lengthBase,
                        this.getPageHeight().getValue())
                : new SimplePercentBaseContext(
                        null,
                        lengthBase,
                        this.getPageWidth().getValue());
    }

    /**
     * Returns the region for a given region class.
     * @param regionId Constants ID of the FO representing the region
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

    /** @return the Common Margin Properties-Block. */
    public CommonMarginBlock getCommonMarginBlock() {
        return commonMarginBlock;
    }

    /** @return "master-name" property. */
    public String getMasterName() {
        return masterName;
    }

    /** @return the "page-width" property. */
    public Length getPageWidth() {
        return pageWidth;
    }

    /** @return the "page-height" property. */
    public Length getPageHeight() {
        return pageHeight;
    }

    /** @return the "writing-mode" property. */
    public int getWritingMode() {
        return writingMode;
    }

    /** @return the "reference-orientation" property. */
    public int getReferenceOrientation() {
        return referenceOrientation.getValue();
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "simple-page-master";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_SIMPLE_PAGE_MASTER}
     */
    public int getNameId() {
        return FO_SIMPLE_PAGE_MASTER;
    }
}
