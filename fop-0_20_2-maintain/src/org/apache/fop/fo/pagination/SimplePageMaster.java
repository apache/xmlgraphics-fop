/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.layout.PageMaster;
import org.apache.fop.layout.BodyRegionArea;
import org.apache.fop.layout.MarginProps;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.Length;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Class modeling the fo:simple-page-master object.
 *
 * @see <a href="@XSLFO-STD@#fo_simple-page-master"
 *     target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.12</a>
 */
public class SimplePageMaster extends FObj {
    // Fallback values for "auto" page size: 8x11in
    private static final int FALLBACK_PAGE_HEIGHT = 792000;
    private static final int FALLBACK_PAGE_WIDTH = 576000;

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                        String systemId, int line, int column)
            throws FOPException {
            return new SimplePageMaster(parent, propertyList,
                                        systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new SimplePageMaster.Maker();
    }

    /**
     * Page regions (regionClass, Region)
     */
    private HashMap _regions;


    LayoutMasterSet layoutMasterSet;
    PageMaster pageMaster;
    String masterName;

    // before and after data as required by start and end
    boolean beforePrecedence = false;
    int beforeExtent, afterExtent, startExtent, endExtent;
    boolean afterPrecedence = false;

    protected SimplePageMaster(FObj parent, PropertyList propertyList,
                               String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);

        if (parent.getName().equals("fo:layout-master-set")) {
            this.layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.properties.get("master-name").getString();
            if (masterName == null) {
                log.warn("simple-page-master does not have "
                                       + "a master-name and so is being ignored");
            } else {
                this.layoutMasterSet.addSimplePageMaster(this);
            }
        } else {
            throw new FOPException("fo:simple-page-master must be child "
                                   + "of fo:layout-master-set, not "
                                   + parent.getName(), systemId, line, column);
        }
        _regions = new HashMap();

    }

    public String getName() {
        return "fo:simple-page-master";
    }

    protected void end() {
        Length pageWidthLen = this.properties.get("page-width").getLength();
        int pageWidth = pageWidthLen.isAuto() ? FALLBACK_PAGE_WIDTH : pageWidthLen.mvalue();
        Length pageHeightLen = this.properties.get("page-height").getLength();
        int pageHeight = pageHeightLen.isAuto() ? FALLBACK_PAGE_HEIGHT : pageHeightLen.mvalue();
        // this.properties.get("reference-orientation");
        // this.properties.get("writing-mode");

        // Common Margin Properties-Block
        MarginProps mProps = propMgr.getMarginProps();

        int contentRectangleXPosition = mProps.marginLeft;
        int contentRectangleYPosition = pageHeight - mProps.marginTop;
        int contentRectangleWidth = pageWidth - mProps.marginLeft
                                    - mProps.marginRight;
        int contentRectangleHeight = pageHeight - mProps.marginTop
                                     - mProps.marginBottom;
        this.pageMaster = new PageMaster(pageWidth, pageHeight);
        Region body = getRegion(RegionBody.REGION_CLASS);
        RegionBefore before = (RegionBefore)getRegion(RegionBefore.REGION_CLASS);
        RegionAfter after = (RegionAfter)getRegion(RegionAfter.REGION_CLASS);
        RegionStart start = (RegionStart)getRegion(RegionStart.REGION_CLASS);
        RegionEnd end = (RegionEnd)getRegion(RegionEnd.REGION_CLASS);
        if (before != null) {
            beforePrecedence = before.getPrecedence();
            beforeExtent = before.getExtent();
        }
        if (after != null) {
            afterPrecedence = after.getPrecedence();
            afterExtent = after.getExtent();
        }
        if (start != null)
            startExtent = start.getExtent();
        if (end != null)
            endExtent = end.getExtent();

        if (body != null)
            this.pageMaster.addBody((BodyRegionArea)body.makeRegionArea(
                    contentRectangleXPosition,
                    contentRectangleYPosition,
                    contentRectangleWidth,
                    contentRectangleHeight));
        else
            log.error("simple-page-master must have a region of class "
                                   + RegionBody.REGION_CLASS);

        if (before != null)
            this.pageMaster.addBefore(before.makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight, startExtent, endExtent));

        if (after != null)
            this.pageMaster.addAfter(after.makeRegionArea(contentRectangleXPosition,
                          contentRectangleYPosition, contentRectangleWidth,
                          contentRectangleHeight, startExtent, endExtent));

        if (start != null)
            this.pageMaster.addStart(start.makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeExtent, afterExtent));

        if (end != null)
            this.pageMaster.addEnd(end.makeRegionArea(contentRectangleXPosition,
                    contentRectangleYPosition, contentRectangleWidth,
                    contentRectangleHeight, beforePrecedence,
                    afterPrecedence, beforeExtent, afterExtent));
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


    protected void addRegion(Region region) throws FOPException {
        if (_regions.containsKey(region.getRegionClass())) {
            throw new FOPException("Only one region of class "
                                   + region.getRegionClass()
                                   + " allowed within a simple-page-master.",
                                   systemId, line, column);
        } else {
            _regions.put(region.getRegionClass(), region);
        }
    }

    protected Region getRegion(String regionClass) {
        return (Region)_regions.get(regionClass);
    }

    protected HashMap getRegions() {
        return _regions;
    }

    protected boolean regionNameExists(String regionName) {
        for (Iterator i = _regions.values().iterator(); i.hasNext(); ) {
            Region r = (Region)i.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }

}
