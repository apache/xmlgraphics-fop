package org.apache.fop.fo.pagination;

import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.UriLocalName;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.pagination.FoPageSequenceMaster;
import org.apache.fop.fo.pagination.PageSequenceMaster;

/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
/**
 * <tt>FoLayoutMasterSet</tt> is the class which processes the
 * <i>layout-master-set</i> element.  This is the compulsory first element
 * under the <i>root</i> element in an FO document.
 */

public class FoLayoutMasterSet extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.  See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
     */
    private static final HashMap sparsePropsMap;

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
        sparsePropsMap = new HashMap(0);
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

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoLayoutMasterSet
        (FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.LAYOUT_MASTER_SET, parent, event,
              FOPropertySets.LAYOUT_SET, sparsePropsMap, sparseIndices,
              numProps);
        setupPageMasters(event);
        // No need to clean up the build tree, because the whole subtree
        // will be deleted.
        // This is problematical: while Node is obliged to belong to a Tree,
        // any remaining references to elements of the subtree will keep the
        // whole subtree from being GCed.
        makeSparsePropsSet();
    }

    /**
     * Set up all the page masters.
     * fo:layout-master-set contents are
     * (simple-page-master|page-sequence-master)+
     * @param event - the layout page-master-set STARTELEMENT event.
     * @throws <tt>FOPException</tt>.
     */
    public void setupPageMasters(FoXMLEvent event)
            throws FOPException, PropertyException
    {
        FoSimplePageMaster simple;
        String masterName;
        int foType;
        FoPageSequenceMaster foPageSeq;
        try {
            do {
                FoXMLEvent ev =
                    xmlevents.expectStartElement
                        (simpleOrSequenceMaster, XMLEvent.DISCARD_W_SPACE);
                if (ev == null) break; // No instance of these elements found
                foType = ev.getFoType();
                if (foType == FObjectNames.SIMPLE_PAGE_MASTER) {
                    //System.out.println("Found simple-page-master");
                    simple = new FoSimplePageMaster(foTree, this, ev);
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
                } else if (foType == FObjectNames.PAGE_SEQUENCE_MASTER) {
                    //System.out.println("Found page-sequence-master");
                    try {
                        foPageSeq =
                                new FoPageSequenceMaster(foTree, this, ev);
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
                } else
                    throw new FOPException
                            ("Aargh! expectStartElement(events, list)");
                // Flush the master event
                xmlevents.getEndElement(ev);
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
        if (pageMasters.size() == 0)
            throw new FOPException
                        ("No page masters defined in layout-master-set.");
        // Create the master set structures.
        // Scan the page-sequence-masters
        // N.B. Processing of the page-sequence-masters must be deferred until
        // now because contained master-references may be to
        // simple-page-masters which follow the page-sequence-master in the
        // input tree.
        Set pageSeqSet = pageMasters.keySet();
        Iterator pageSeqNames = pageSeqSet.iterator();
        while (pageSeqNames.hasNext()) {
            masterName = (String)(pageSeqNames.next());
            // Get the FoPageSequenceMaster
            foPageSeq = (FoPageSequenceMaster)(pageMasters.get(masterName));
            // Create a new PageSequenceMaster object - NOT an foPageSeqM
            PageSequenceMaster pageSeq = new PageSequenceMaster
                                (masterName, foPageSeq, simplePageMasters);
            pageSequenceMasters.put(masterName, pageSeq);
        }
    }

    /**
     * Make the <tt>HashMap</tt> of <tt>PageSequenceMaster</tt>s available.
     * @return - the <i>pageSequenceMasters</i> <tt>HashMap</tt>.
     */
    public HashMap getPageSequenceMasters() {
        return pageSequenceMasters;
    }
        
}// FoLayoutMasterSet
