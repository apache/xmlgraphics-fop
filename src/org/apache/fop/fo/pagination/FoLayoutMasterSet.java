package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.fo.pagination.FoPageSequenceMaster;
import org.apache.fop.fo.pagination.FoPageSequenceMaster.SubSequenceSpecifier;
import org.apache.fop.fo.pagination
    .FoPageSequenceMaster.SubSequenceSpecifier.ConditionalPageMasterReference;

/*
 * FoLayoutMasterSet.java
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 * 
 * Created: Mon Nov 12 23:30:51 2001
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

    /**
     * Hash of SimplePageMaster and PageSequenceMaster objects,
     * indexed by master-name of the object.
     */
    private HashMap pageMasters;

    /**
     * Hash of SimplePageMaster objects,
     * indexed by master-name of the object.
     */
    private HashMap simplePageMasters;

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
        super(foTree, parent, event, FONode.LAYOUT);
    }

    /**
     * Set up all the page masters.  !!!Note that the masters are not entered
     * in the FO tree.!!!
     * fo:layout-master-set contents are
     * (simple-page-master|page-sequence-master)+
     */
    public void setupPageMasters() throws FOPException {
        // Set up a list with the two possibilities
        LinkedList list = new LinkedList();
        list.add((Object)(new XMLEvent.UriLocalName
                          (XMLEvent.XSLNSpaceIndex, "simple-page-master")));
        list.add((Object)(new XMLEvent.UriLocalName
                         (XMLEvent.XSLNSpaceIndex, "page-sequence-master")));
        try {
            do {
                FoSimplePageMaster simple;
                String simpleName;
                FoPageSequenceMaster pageSeq;
                XMLEvent ev =
                        XMLEvent.expectStartElement(xmlevents, list);
                if (ev.localName.equals("simple-page-master")) {
                    System.out.println("Found simple-page-master");
                    try {
                        simple = new FoSimplePageMaster(foTree, this, ev);
                    } catch (Tree.TreeException e) {
                        throw new FOPException
                                ("TreeException: " + e.getMessage());
                    }
                    simpleName = simple.getMasterName();
                    if (pageMasters == null)
                        pageMasters = new HashMap();
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
                    FoPageSequenceMaster seqMaster;
                    try {
                        // Construct a new PageSequenceMaster
                        seqMaster = new FoPageSequenceMaster
                                (foTree, this, simpleName);
                    } catch(Tree.TreeException e) {
                        throw new FOPException
                                ("TreeException: " + e.getMessage());
                    }
                    // Construct a SubSequence
                    SubSequenceSpecifier subSeq =
                            seqMaster.new SubSequenceSpecifier();
                    // Construct a default ConditionalPageMasterReference
                    ConditionalPageMasterReference cond = subSeq.new
                            ConditionalPageMasterReference(simpleName);
                    pageMasters.put(simpleName, seqMaster);
                } else if (ev.localName.equals("page-sequence-master")) {
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
            throw new FOPException("PropertyException: " + e.getMessage());
        }
    }
        
}// FoLayoutMasterSet
