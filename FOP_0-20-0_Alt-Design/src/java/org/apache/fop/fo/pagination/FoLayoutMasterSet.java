/*
   Copyright 2002-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * $Id$
 */
package org.apache.fop.fo.pagination;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEvent;

/**
 * <tt>FoLayoutMasterSet</tt> is the class which processes the
 * <i>layout-master-set</i> element.  This is the compulsory first element
 * under the <i>root</i> element in an FO document.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FoLayoutMasterSet extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.
     */
    private static final int[] sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    private static final int[] sparseIndices;

    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    private static final int numProps;

    static {
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 0;
        sparseIndices = new int[] {};
    }

    /**
     * An array with <tt>int</tt>s identifying
     * <tt>simple-page-master</tt> and <tt>page-sequence-master</tt>
     * XML events.
     */
    private static final int[] simpleOrSequenceMaster = {
        FObjectNames.SIMPLE_PAGE_MASTER,
        FObjectNames.PAGE_SEQUENCE_MASTER
    };

    /**
     * Hash of SimplePageMaster and PageSequenceMaster objects,
     * indexed by master-name of the object.
     */
    private HashMap pageMasters = new HashMap();

    /**
     * Hash of SimplePageMaster objects,
     * indexed by master-name of the object.
     */
    private HashMap simplePageMasters = new HashMap();

    /**
     * Hash of the set of PageSequenceMaster objects derived from the set of
     * <tt>FoSimplePageMaster</tt>s and the set of
     * <tt>FoPageSequenceMaster</tt>s.
     */
    private HashMap pageSequenceMasters = new HashMap();
    
    public Map finalPageSequenceMasters;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoLayoutMasterSet
        (FOTree foTree, FONode parent, XmlEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.LAYOUT_MASTER_SET, parent, event,
              FONode.LAYOUT_SET, sparsePropsMap, sparseIndices);
        setupPageMasters(event);
        // No need to clean up the build tree, because the whole subtree
        // will be deleted.
        makeSparsePropsSet();
    }

    /**
     * Set up all the page masters.
     * fo:layout-master-set contents are
     * (simple-page-master|page-sequence-master)+
     * @param event - the layout page-master-set STARTELEMENT event.
     * @throws FOPException
     */
    public void setupPageMasters(XmlEvent event)
            throws FOPException, PropertyException
    {
        FoSimplePageMaster simple;
        String masterName;
        int foType;
        FoPageSequenceMaster foPageSeq;
        try {
            do {
                XmlEvent ev =
                    xmlevents.expectStartElement
                        (simpleOrSequenceMaster, XmlEvent.DISCARD_W_SPACE);
                if (ev == null) break; // No instance of these elements found
                foType = ev.getFoType();
                if (foType == FObjectNames.SIMPLE_PAGE_MASTER) {
                    //System.out.println("Found simple-page-master");
                    simple = new FoSimplePageMaster(getFOTree(), this, ev);
                    masterName = simple.getMasterName();
                    if (pageMasters.get(masterName) != null)
                        throw new FOPException
                                ("simple-page-master master-name clash in"
                                 + "pageMasters: "
                                 + masterName);
                    //pageMasters.put(masterName, simple);
                    if (simplePageMasters.get(masterName) != null)
                        throw new FOPException
                                ("simple-page-master master-name clash in "
                                 + "simplePageMasters: " + masterName);
                    simplePageMasters.put(masterName, simple);
                    System.out.println("simple-page-master ok");
                } else if (foType == FObjectNames.PAGE_SEQUENCE_MASTER) {
                    //System.out.println("Found page-sequence-master");
                    try {
                        foPageSeq =
                            new FoPageSequenceMaster(getFOTree(), this, ev);
                    } catch (TreeException e) {
                        throw new FOPException
                                ("TreeException: " + e.getMessage());
                    }
                    masterName = foPageSeq.getMasterName();
                    if (pageMasters.get(masterName) != null)
                        throw new FOPException
                                ("page-sequence-master master-name clash in"
                                 + "pageMasters: " + masterName);
                    if (simplePageMasters.get(masterName) != null)
                        throw new FOPException
                                ("page-sequence-master master-name clash in "
                                 + "simplePageMasters: " + masterName);
                    pageMasters.put(masterName, foPageSeq);
                    System.out.println("page-sequence-master ok");
                } else
                    throw new FOPException
                            ("Error seeking page-masters");
                // Flush the master event
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            } while (true);
        } catch (NoSuchElementException e) {
            // Unexpected end of file
            throw new FOPException("layout-master-set: unexpected EOF.");
        }
        catch (PropertyException e) {
            throw new FOPException(e);
        }
        catch (TreeException e) {
            throw new FOPException(e);
        }
        if (pageMasters.size() == 0 && simplePageMasters.size() == 0)
            throw new FOPException
                        ("No page masters defined in layout-master-set.");
        // Create the master set structures.
        // N.B. Processing of the page-sequence-masters must be deferred until
        // now because contained master-references may be to
        // simple-page-masters which follow the page-sequence-master in the
        // input tree.
        // Scan the simple-page-masters
        Collection set = simplePageMasters.values();
        Iterator values = set.iterator();
        while (values.hasNext()) {
            // Create a new PageSequenceMaster object - NOT an foPageSeqM
            PageSequenceMaster pageSeq = new PageSequenceMaster
                                ((FoSimplePageMaster)(values.next()));
            pageSequenceMasters.put(pageSeq.getMasterName(), pageSeq);
        }
        // Scan the page-sequence-masters
        set = pageMasters.values();
        values = set.iterator();
        while (values.hasNext()) {
            // Get the FoPageSequenceMaster
            // Create a new PageSequenceMaster object - NOT an foPageSeqM
            PageSequenceMaster pageSeq = new PageSequenceMaster
                ((FoPageSequenceMaster)(values.next()), simplePageMasters);
            pageSequenceMasters.put(pageSeq.getMasterName(), pageSeq);
        }
        //Create the unmodifiable Map
        finalPageSequenceMasters =
            Collections.unmodifiableMap(pageSequenceMasters);
        // Nullify the now-redundant HashMaps
        simplePageMasters = null;
        pageMasters = null;
    }

    /**
     * Make the unmodifiable <tt>Map</tt> of <tt>PageSequenceMaster</tt>s
     * available.
     * @return - the <i>finalPageSequenceMasters</i> <tt>Map</tt>.
     */
    public Map getPageSequenceMasters() {
        return finalPageSequenceMasters;
    }

    /**
     * The genrator field for page ids.
     * These page ids generated within any given instance of
     * <code>FoLayoutMasterSet</code> increase monotonically from 1 through
     * the range of values of <code>long</code>.  They wrap around when that
     * range is exhausted, but the value 0 is never returned.
     */
    private long pageId = 0;
    
    /**
     * @return a <code>long</code> page id not equal to 0.
     */
    public long makePageId() {
        if (++pageId == 0) {
            ++pageId;   // 0 is invalid
            log.warning("Page ID rollover.");
        }
        return pageId;
    }
    
}// FoLayoutMasterSet
