/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
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

/**
 * Class modeling the fo:layout-master-set object.
 *
 * @see <a href="@XSLFO-STD@#fo_layout-master-set"
       target="_xslfostd">@XSLFO-STDID@
 *     &para;6.4.6</a>
 */
public class LayoutMasterSet extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent,
                         PropertyList propertyList) throws FOPException {
            return new LayoutMasterSet(parent, propertyList);
        }
    }

    public static FObj.Maker maker() {
        return new LayoutMasterSet.Maker();
    }

    private HashMap simplePageMasters;
    private HashMap pageSequenceMasters;
    private HashMap allRegions;

    private Root root;

    protected LayoutMasterSet(FObj parent,
                              PropertyList propertyList) throws FOPException {
        super(parent, propertyList);

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

    public String getName() {
        return "fo:layout-master-set";
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
     * @return true when the region name specified has a region in this LayoutMasterSet
     */
    protected boolean regionNameExists(String regionName) {
        for (Iterator i = simplePageMasters.values().iterator(); i.hasNext(); ) {
            if (((SimplePageMaster)i.next()).regionNameExists(regionName)) {
                return true;
            }
        }
        return false;
    }

}
