package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.SyncedXmlEventsBuffer;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.fo.pagination.FoPageSequenceMaster;

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

    /**
     * An array with <tt>XMLEvent.UriLocalName</tt> objects identifying
     * <tt>simple-page-master</tt> and <tt>page-sequence-master</tt>
     * XML events.
     */
    private static final XMLEvent.UriLocalName[] simpleOrSequenceMaster = {
        new XMLEvent.UriLocalName
                      (XMLNamespaces.XSLNSpaceIndex, "simple-page-master"),
        new XMLEvent.UriLocalName
                     (XMLNamespaces.XSLNSpaceIndex, "page-sequence-master")
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
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoLayoutMasterSet
        (FOTree foTree, FONode parent, XMLEvent event)
        throws Tree.TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.LAYOUT_MASTER_SET, parent, event,
              FOPropertySets.LAYOUT_SET);
    }

    /**
     * Set up all the page masters.
     * fo:layout-master-set contents are
     * (simple-page-master|page-sequence-master)+
     */
    public void setupPageMasters() throws FOPException {
        // Use an array with the two possibilities
        try {
            do {
                FoSimplePageMaster simple;
                String simpleName;
                String localName;
                FoPageSequenceMaster pageSeq;
                XMLEvent ev =
                    xmlevents.expectStartElement
                        (simpleOrSequenceMaster, XMLEvent.DISCARD_W_SPACE);
                localName = ev.getLocalName();
                if (localName.equals("simple-page-master")) {
                    System.out.println("Found simple-page-master");
                    simple = new FoSimplePageMaster(foTree, this, ev);
                    simpleName = simple.getMasterName();
                    if (pageMasters.get
                        ((Object)(simpleName)) != null)
                        throw new FOPException
                                ("simple-page-master master-name clash in"
                                 + "pageMasters: "
                                 + simpleName);
                    //pageMasters.put(simpleName, simple);
                    if (simplePageMasters == null)
                        simplePageMasters = new HashMap();
                    if (simplePageMasters.get
                        ((Object)(simpleName)) != null)
                        throw new FOPException
                                ("simple-page-master master-name clash in "
                                 + "simplePageMasters: " + simpleName);
                    simplePageMasters.put(simpleName, simple);
                } else if (localName.equals("page-sequence-master")) {
                    System.out.println("Found page-sequence-master");
                    try {
                        pageSeq = new FoPageSequenceMaster(foTree, this, ev);
                    } catch (Tree.TreeException e) {
                        throw new FOPException
                                ("TreeException: " + e.getMessage());
                    }
                    if (pageMasters == null)
                        pageMasters = new HashMap();
                    if (pageMasters.get
                        ((Object)(pageSeq.getMasterName())) != null)
                        throw new FOPException
                                ("page-sequence-master master-name clash: "
                                 + pageSeq.getMasterName());
                    pageMasters.put
                            ((Object)(pageSeq.getMasterName()), pageSeq);
                } else
                    throw new FOPException
                            ("Aargh! expectStartElement(events, list)");
            } while (true);
        } catch (NoSuchElementException e) {
            // Masters exhausted
        }
        catch (PropertyException e) {
            throw new FOPException(e);
        }
        catch (Tree.TreeException e) {
            throw new FOPException(e);
        }
    }
        
}// FoLayoutMasterSet
