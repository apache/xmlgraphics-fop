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
package org.apache.fop.area;

import java.io.Serializable;
import java.util.Map;

/**
 * The page.
 * This holds the contents of the page. Each region is added.
 * The unresolved references area added so that if the page is
 * serialized then it will handle the resolving properly after
 * being reloaded.
 * This is serializable so it can be saved to cache to save
 * memory if there are forward references.
 * The page is cloneable so the page master can make copies of
 * the top level page and regions.
 */
public class Page implements Serializable, Cloneable {
    // contains before, start, body, end and after regions
    private RegionViewport regionBefore = null;
    private RegionViewport regionStart = null;
    private RegionViewport regionBody = null;
    private RegionViewport regionEnd = null;
    private RegionViewport regionAfter = null;

    // temporary map of unresolved objects used when serializing the page
    private Map unresolved = null;

    /**
     * Set the region on this page.
     *
     * @param areaclass the area class of the region to set
     * @param port the region viewport to set
     */
    public void setRegion(int areaclass, RegionViewport port) {
        if (areaclass == RegionReference.BEFORE) {
            regionBefore = port;
        } else if (areaclass == RegionReference.START) {
            regionStart = port;
        } else if (areaclass == RegionReference.BODY) {
            regionBody = port;
        } else if (areaclass == RegionReference.END) {
            regionEnd = port;
        } else if (areaclass == RegionReference.AFTER) {
            regionAfter = port;
        }
    }

    /**
     * Get the region from this page.
     *
     * @param areaclass the region area class
     * @return the region viewport or null if none
     */
    public RegionViewport getRegion(int areaclass) {
        if (areaclass == RegionReference.BEFORE) {
            return regionBefore;
        } else if (areaclass == RegionReference.START) {
            return regionStart;
        } else if (areaclass == RegionReference.BODY) {
            return regionBody;
        } else if (areaclass == RegionReference.END) {
            return regionEnd;
        } else if (areaclass == RegionReference.AFTER) {
            return regionAfter;
        }
        return null;
    }

    /**
     * Clone this page.
     * This returns a new page with a clone of all the regions.
     *
     * @return a new clone of this page
     */
    public Object clone() {
        Page p = new Page();
        if (regionBefore != null) {
            p.regionBefore = (RegionViewport)regionBefore.clone();
        }
        if (regionStart != null) {
            p.regionStart = (RegionViewport)regionStart.clone();
        }
        if (regionBody != null) {
            p.regionBody = (RegionViewport)regionBody.clone();
        }
        if (regionEnd != null) {
            p.regionEnd = (RegionViewport)regionEnd.clone();
        }
        if (regionAfter != null) {
            p.regionAfter = (RegionViewport)regionAfter.clone();
        }

        return p;
    }

    /**
     * Set the unresolved references on this page for serializing.
     *
     * @param unres the map of unresolved objects
     */
    public void setUnresolvedReferences(Map unres) {
        unresolved = unres;
    }

    /**
     * Get the map unresolved references from this page.
     * This should be called after deserializing to retrieve
     * the map of unresolved references that were serialized.
     *
     * @return the de-serialized map of unresolved objects
     */
    public Map getUnresolvedReferences() {
        return unresolved;
    }
}

