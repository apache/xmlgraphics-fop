/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
package org.apache.fop.fo.pagination;

import java.util.Collection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.PropNames;
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
                            ("Aargh! expectStartElement(events, list)");
                // Flush the master event
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
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
    }

    /**
     * Make the <tt>HashMap</tt> of <tt>PageSequenceMaster</tt>s available.
     * @return - the <i>pageSequenceMasters</i> <tt>HashMap</tt>.
     */
    public HashMap getPageSequenceMasters() {
        return pageSequenceMasters;
    }
        
}// FoLayoutMasterSet
