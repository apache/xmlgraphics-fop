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
import java.util.Iterator;
import java.util.Map;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.FOTreeVisitor;
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
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
        XSL/FOP: (simple-page-master|page-sequence-master)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) {
        if (nsURI == FOElementMapping.URI) {
            if (!localName.equals("simple-page-master") 
                && !localName.equals("page-sequence-master")) {   
                    invalidChildError(loc, nsURI, localName);
            }
        } else {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#end
     */
    protected void end() {
        if (children == null) {
           missingChildElementError("(simple-page-master|page-sequence-master)+");
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);

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
     * Section 7.25.7: check to see that if a region-name is a
     * duplicate, that it maps to the same fo region-class.
     * @throws FOPException if there's a name duplication
     */
    public void checkRegionNames() throws FOPException {
        // (user-entered) region-name to default region map.
        Map allRegions = new java.util.HashMap();
        for (Iterator spm = simplePageMasters.values().iterator();
                spm.hasNext();) {
            SimplePageMaster simplePageMaster =
                (SimplePageMaster)spm.next();
            Map spmRegions = simplePageMaster.getRegions();
            for (Iterator e = spmRegions.values().iterator();
                    e.hasNext();) {
                Region region = (Region) e.next();
                if (allRegions.containsKey(region.getRegionName())) {
                    String defaultRegionName =
                        (String) allRegions.get(region.getRegionName());
                    if (!defaultRegionName.equals(region.getDefaultRegionName())) {
                        throw new FOPException("Region-name ("
                                               + region.getRegionName()
                                               + ") is being mapped to multiple "
                                               + "region-classes ("
                                               + defaultRegionName + " and "
                                               + region.getDefaultRegionName()
                                               + ")");
                    }
                }
                allRegions.put(region.getRegionName(),
                               region.getDefaultRegionName());
            }
        }
    }

    /**
     * Checks whether or not a region name exists in this master set.
     * @param regionName name of the region
     * @return true when the region name specified has a region in this LayoutMasterSet
     */
    public boolean regionNameExists(String regionName) {
        for (Iterator e = simplePageMasters.values().iterator();
                e.hasNext();) {
            if (((SimplePageMaster)e.next()).regionNameExists(regionName)) {
                return true;
            }
        }
        return false;
    }

    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveLayoutMasterSet(this);
    }

    public String getName() {
        return "fo:layout-master-set";
    }
}

