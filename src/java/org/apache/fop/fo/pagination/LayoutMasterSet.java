/*
 * $Id: LayoutMasterSet.java,v 1.19 2003/03/06 13:42:41 jeremias Exp $
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

// Java
import java.util.Iterator;
import java.util.Map;

// XML
import org.xml.sax.Attributes;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.apps.FOPException;

/**
 * The layout-master-set formatting object.
 * This class maintains the set of simple page master and
 * page sequence masters.
 * The masters are stored so that the page sequence can obtain
 * the required page master to create a page.
 * The page sequence masters can be reset as they hold state
 * information for a page sequence.
 */
public class LayoutMasterSet extends FObj {
    
    private Map simplePageMasters;
    private Map pageSequenceMasters;

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public LayoutMasterSet(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#handleAttrs(Attributes)
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);

        if (parent.getName().equals("fo:root")) {
            Root root = (Root)parent;
            root.setLayoutMasterSet(this);
        } else {
            throw new FOPException("fo:layout-master-set must be child of fo:root, not "
                                   + parent.getName());
        }

        this.simplePageMasters = new java.util.HashMap();
        this.pageSequenceMasters = new java.util.HashMap();
    }

    /**
     * Add a simple page master.
     * The name is checked to throw an error if already added.
     * @param simplePageMaster simple-page-master to add
     * @throws FOPException if there's a problem with name uniqueness
     */
    protected void addSimplePageMaster(SimplePageMaster simplePageMaster)
                throws FOPException {
        // check against duplication of master-name
        if (existsName(simplePageMaster.getMasterName())) {
            throw new FOPException("'master-name' ("
                                   + simplePageMaster.getMasterName()
                                   + ") must be unique "
                                   + "across page-masters and page-sequence-masters");
        }
        this.simplePageMasters.put(simplePageMaster.getMasterName(),
                                   simplePageMaster);
    }

    /**
     * Get a simple page master by name.
     * This is used by the page sequence to get a page master for
     * creating pages.
     * @param masterName the name of the page master
     * @return the requested simple-page-master
     */
    public SimplePageMaster getSimplePageMaster(String masterName) {
        return (SimplePageMaster)this.simplePageMasters.get(masterName);
    }

    /**
     * Add a page sequence master.
     * The name is checked to throw an error if already added.
     * @param masterName name for the master
     * @param pageSequenceMaster PageSequenceMaster instance
     * @throws FOPException if there's a problem with name uniqueness
     */
    protected void addPageSequenceMaster(String masterName, 
                                        PageSequenceMaster pageSequenceMaster)
                throws FOPException {
        // check against duplication of master-name
        if (existsName(masterName)) {
            throw new FOPException("'master-name' (" + masterName
                                   + ") must be unique "
                                   + "across page-masters and page-sequence-masters");
        }
        this.pageSequenceMasters.put(masterName, pageSequenceMaster);
    }

    /**
     * Get a page sequence master by name.
     * This is used by the page sequence to get a page master for
     * creating pages.
     * @param masterName name of the master
     * @return the requested PageSequenceMaster instance
     */
    public PageSequenceMaster getPageSequenceMaster(String masterName) {
        return (PageSequenceMaster)this.pageSequenceMasters.get(masterName);
    }

    private boolean existsName(String masterName) {
        if (simplePageMasters.containsKey(masterName)
                || pageSequenceMasters.containsKey(masterName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Section 7.33.15: check to see that if a region-name is a
     * duplicate, that it maps to the same region-class.
     * @throws FOPException if there's a name duplication
     */
    protected void checkRegionNames() throws FOPException {
        Map allRegions = new java.util.HashMap();
        for (Iterator spm = simplePageMasters.values().iterator();
                spm.hasNext();) {
            SimplePageMaster simplePageMaster =
                (SimplePageMaster)spm.next();
            Map spmRegions = simplePageMaster.getRegions();
            for (Iterator e = spmRegions.values().iterator();
                    e.hasNext();) {
                Region region = (Region)e.next();
                if (allRegions.containsKey(region.getRegionName())) {
                    String localClass =
                        (String)allRegions.get(region.getRegionName());
                    if (!localClass.equals(region.getRegionClass())) {
                        throw new FOPException("Duplicate region-names ("
                                               + region.getRegionName()
                                               + ") must map "
                                               + "to the same region-class ("
                                               + localClass + "!="
                                               + region.getRegionClass()
                                               + ")");
                    }
                }
                allRegions.put(region.getRegionName(),
                               region.getRegionClass());
            }
        }
    }

    /**
     * Checks whether or not a region name exists in this master set.
     * @param regionName name of the region
     * @return true when the region name specified has a region in this LayoutMasterSet
     */
    protected boolean regionNameExists(String regionName) {
        for (Iterator e = simplePageMasters.values().iterator();
                e.hasNext();) {
            if (((SimplePageMaster)e.next()).regionNameExists(regionName)) {
                return true;
            }
        }
        return false;
    }
}

