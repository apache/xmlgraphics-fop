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

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;
import org.apache.fop.apps.FOPException;

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

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public SimplePageMaster(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);

        if (parent.getName().equals("fo:layout-master-set")) {
            LayoutMasterSet layoutMasterSet = (LayoutMasterSet)parent;
            masterName = this.propertyList.get(PR_MASTER_NAME).getString();
            if (masterName == null) {
                getLogger().warn("simple-page-master does not have "
                        + "a master-name and so is being ignored");
            } else {
                layoutMasterSet.addSimplePageMaster(this);
            }
        } else {
            throw new FOPException("fo:simple-page-master must be child "
                    + "of fo:layout-master-set, not "
                    + parent.getName());
        }
        //Well, there are only 5 regions so we can save a bit of memory here
        regions = new HashMap(5);
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
        return masterName;
    }

    /**
     * @see org.apache.fop.fo.FONode#addChild(FONode)
     */
    protected void addChild(FONode child) {
        if (child instanceof Region) {
            addRegion((Region)child);
        } else {
            getLogger().error("SimplePageMaster cannot have child of type "
                    + child.getName());
        }
    }

    /**
     * Adds a region to this simple-page-master.
     * @param region region to add
     */
    protected void addRegion(Region region) {
        String key = String.valueOf(region.getRegionClassCode());
        if (regions.containsKey(key)) {
            getLogger().error("Only one region of class " + region.getRegionName()
                    + " allowed within a simple-page-master. The duplicate"
                    + " region (" + region.getName() + ") is ignored.");
        } else {
            regions.put(key, region);
        }
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
            Region r = (Region)regenum.next();
            if (r.getRegionName().equals(regionName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveSimplePageMaster(this);
    }

}
