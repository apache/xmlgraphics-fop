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
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.PageMaster;

// Java
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;

public class LayoutMasterSet extends FObj {

    private HashMap simplePageMasters;
    private HashMap pageSequenceMasters;
    private HashMap allRegions;

    private Root root;

    public LayoutMasterSet(FONode parent) {
        super(parent);
    }

    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
        this.simplePageMasters = new HashMap();
        this.pageSequenceMasters = new HashMap();

        if (parent.getName().equals("fo:root")) {
            this.root = (Root)parent;
            root.setLayoutMasterSet(this);
        } else {
            throw new FOPException("fo:layout-master-set must be child of fo:root, not "
                                   + parent.getName());
        }
        allRegions = new HashMap();

    }

    protected void addSimplePageMaster(SimplePageMaster simplePageMaster)
            throws FOPException {
        // check against duplication of master-name
        if (existsName(simplePageMaster.getMasterName()))
            throw new FOPException("'master-name' ("
                                   + simplePageMaster.getMasterName()
                                   + ") must be unique "
                                   + "across page-masters and page-sequence-masters");
        this.simplePageMasters.put(simplePageMaster.getMasterName(),
                                   simplePageMaster);
    }

    protected SimplePageMaster getSimplePageMaster(String masterName) {
        return (SimplePageMaster)this.simplePageMasters.get(masterName);
    }

    protected void addPageSequenceMaster(String masterName, PageSequenceMaster pageSequenceMaster)
            throws FOPException {
        // check against duplication of master-name
        if (existsName(masterName))
            throw new FOPException("'master-name' (" + masterName
                                   + ") must be unique "
                                   + "across page-masters and page-sequence-masters");
        this.pageSequenceMasters.put(masterName, pageSequenceMaster);
    }

    protected PageSequenceMaster getPageSequenceMaster(String masterName) {
        return (PageSequenceMaster)this.pageSequenceMasters.get(masterName);
    }

    private boolean existsName(String masterName) {
        if (simplePageMasters.containsKey(masterName)
                || pageSequenceMasters.containsKey(masterName))
            return true;
        else
            return false;
    }

    protected void resetPageMasters() {
        for (Iterator e = pageSequenceMasters.values().iterator();
                e.hasNext(); ) {
            ((PageSequenceMaster)e.next()).reset();
        }
    }

    protected void checkRegionNames() throws FOPException {
        // Section 7.33.15 check to see that if a region-name is a
        // duplicate, that it maps to the same region-class.
        for (Iterator spm = simplePageMasters.values().iterator();
                spm.hasNext(); ) {
            SimplePageMaster simplePageMaster =
                (SimplePageMaster)spm.next();
            HashMap spmRegions = simplePageMaster.getRegions();
            for (Iterator e = spmRegions.values().iterator();
                    e.hasNext(); ) {
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
     * Checks whether or not a region name exists in this master set
     * @returns true when the region name specified has a region in this LayoutMasterSet
     */
    protected boolean regionNameExists(String regionName) {
        boolean result = false;
        for (Iterator e = simplePageMasters.values().iterator();
                e.hasNext(); ) {
            result =
                ((SimplePageMaster)e.next()).regionNameExists(regionName);
            if (result) {
                return result;
            }
        }
        return result;
    }
}

