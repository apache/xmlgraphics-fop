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

import org.apache.fop.datastructs.Node;

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
public class PageRefArea
extends AbstractReferenceArea
implements ReferenceArea, Serializable {
    // contains before, start, body, end and after regions
    private RegionBodyVport regionBody = null;
    private RegionBeforeVport regionBefore = null;
    private RegionAfterVport regionAfter = null;
    private RegionStartVport regionStart = null;
    private RegionEndVport regionEnd = null;

    // temporary map of unresolved objects used when serializing the page
    private Map unresolved = null;
    
    public PageRefArea(Node parent, Object sync) {
        super(parent, sync);
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
    
    /**
     * @return the regionAfter
     */
    public RegionAfterVport getRegionAfter() {
        return regionAfter;
    }

    /**
     * @param regionAfter to set
     */
    public void setRegionAfter(RegionAfterVport regionAfter) {
        this.regionAfter = regionAfter;
    }

    /**
     * @return the regionBefore
     */
    public RegionBeforeVport getRegionBefore() {
        return regionBefore;
    }

    /**
     * @param regionBefore to set
     */
    public void setRegionBefore(RegionBeforeVport regionBefore) {
        this.regionBefore = regionBefore;
    }

    /**
     * @return the regionBody
     */
    public RegionBodyVport getRegionBody() {
        return regionBody;
    }

    /**
     * @param regionBody to set
     */
    public void setRegionBody(RegionBodyVport regionBody) {
        this.regionBody = regionBody;
    }

    /**
     * @return the regionEnd
     */
    public RegionEndVport getRegionEnd() {
        return regionEnd;
    }

    /**
     * @param regionEnd to set
     */
    public void setRegionEnd(RegionEndVport regionEnd) {
        this.regionEnd = regionEnd;
    }

    /**
     * @return the regionStart
     */
    public RegionStartVport getRegionStart() {
        return regionStart;
    }

    /**
     * @param regionStart to set
     */
    public void setRegionStart(RegionStartVport regionStart) {
        this.regionStart = regionStart;
    }

    /**
     * Clone this page.
     * This returns a new page with a clone of all the regions.
     *
     * @return a new clone of this page
     */
    public Object clone() {
        PageRefArea p = (PageRefArea)(this.clone());
        if (regionBody != null) {
            p.regionBody = (RegionBodyVport)regionBody.clone();
        }
        if (regionBefore != null) {
            p.regionBefore = (RegionBeforeVport)regionBefore.clone();
        }
        if (regionAfter != null) {
            p.regionAfter = (RegionAfterVport)regionAfter.clone();
        }
        if (regionStart != null) {
            p.regionStart = (RegionStartVport)regionStart.clone();
        }
        if (regionEnd != null) {
            p.regionEnd = (RegionEndVport)regionEnd.clone();
        }

        return p;
    }
    
}

