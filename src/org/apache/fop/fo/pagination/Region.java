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
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.RegionArea;

/**
 * Abstract base class for pagination regions.
 */
public abstract class Region extends FObj {
    public static final String PROP_REGION_NAME = "region-name";

    private SimplePageMaster _layoutMaster;
    private String _regionName;

    protected Region(FObj parent, PropertyList propertyList,
                     String systemId, int line, int column)
        throws FOPException {
        super(parent, propertyList, systemId, line, column);

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
                                       + "' for " + this.getName()
                                       + " not permitted.", systemId, line, column);
            }
        }

        if (parent.getName().equals("fo:simple-page-master")) {
            _layoutMaster = (SimplePageMaster)parent;
            getPageMaster().addRegion(this);
        } else {
            throw new FOPException(getName() + " must be child "
                                   + "of simple-page-master, not "
                                   + parent.getName(), systemId, line, column);
        }
    }

    /**
     * Creates a Region layout object for this pagination Region.
     */
    abstract RegionArea makeRegionArea(int allocationRectangleXPosition,
                                       int allocationRectangleYPosition,
                                       int allocationRectangleWidth,
                                       int allocationRectangleHeight);

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

}
