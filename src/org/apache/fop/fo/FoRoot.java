/**
 * $Id$
 * 
 *  ============================================================================
 *                    The Apache Software License, Version 1.1
 *  ============================================================================
 *  
 *  Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *  
 *  Redistribution and use in source and binary forms, with or without modifica-
 *  tion, are permitted provided that the following conditions are met:
 *  
 *  1. Redistributions of  source code must  retain the above copyright  notice,
 *     this list of conditions and the following disclaimer.
 *  
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  
 *  3. The end-user documentation included with the redistribution, if any, must
 *     include  the following  acknowledgment:  "This product includes  software
 *     developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *     Alternately, this  acknowledgment may  appear in the software itself,  if
 *     and wherever such third-party acknowledgments normally appear.
 *  
 *  4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *     endorse  or promote  products derived  from this  software without  prior
 *     written permission. For written permission, please contact
 *     apache@apache.org.
 *  
 *  5. Products  derived from this software may not  be called "Apache", nor may
 *     "Apache" appear  in their name,  without prior written permission  of the
 *     Apache Software Foundation.
 *  
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 *  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 *  APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 *  DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 *  ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 *  (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 *  This software  consists of voluntary contributions made  by many individuals
 *  on  behalf of the Apache Software  Foundation and was  originally created by
 *  James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 *  Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.pagination.FoLayoutMasterSet;
import org.apache.fop.fo.declarations.FoDeclarations;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;

import org.xml.sax.Attributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * <tt>FoRoot</tt> is the class which processes the fo:root start element
 * XML event.
 * <p>
 * The building of all of the fo tree, and the forwarding of FO tree events
 * on to further stages of processing, will all take place within the
 * <tt>buildFoTree()</tt> method of this class instance.
 */

public class FoRoot extends FONode {

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
        numProps = 1;
        sparseIndices = new int[] { PropNames.MEDIA_USAGE };
        sparsePropsMap
                [PropNames.MEDIA_USAGE] = 0;
    }

    /** 
     * The HashMap of PageSequenceMaster objects produced from the
     * layout-master-set.
     */
    private HashMap pageSequenceMasters;

    /** Offset of declarations child node. */
    private int declarations = -1;

    /** Offset of first page-sequence child node. */
    private int firstPageSeq = -1;

    /**
     * @param foTree the FO tree being built
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of this
     * node
     */
    public FoRoot
        (FOTree foTree, FoXMLEvent event)
        throws TreeException, FOPException, PropertyException
    {
        // This is the root node of the tree; hence the null argument
        super(foTree, FObjectNames.ROOT, null, event, FONode.ROOT_SET,
                sparsePropsMap, sparseIndices);
    }

    /**
     * Process the FO tree, starting with this fo:root element.
     * N.B. the FO tree is a collection of trees.
     * Layout trees only occur with fo:flow and fo:static-content.  These will
     * be built at the appropriate places as part of the FO tree processing.
     * Terminates at the completion of FO tree processing.
     * <p>
     * <tt>fo:root</tt> contents are <br/>
     * (layout-master-set,declarations?,page-sequence+)
     * <p>
     * I.e. the <fo:root> element is the parent of a two-element sequence:
     * (layout-master-set-declarations),(page-sequence-sequence)
     * <p>
     * 'layout-master-set-declarations' is logically an unordered set of
     * (layout-master-set,declarations?), although this definition
     * determines an order of occurrence in the input tree.  It is 
     * unordered in the sense that there is no necessary order in the
     * set.  However, all of the elements of the page-sequence-sequence
     * are ordered.
     * <p>The contents of declarations must be available to all FO tree
     * processing; the contents of the layout-master-set must be available
     * during the page setup phase of the processing of each page-sequence
     * in the page-sequence-sequence.
     */
    public void buildFoTree() throws FOPException{
        FoXMLEvent ev;
        String nowProcessing;
        //System.out.println("buildFoTree: " + event);
        nowProcessing = "layout-master-set";
        try {
            // Look for layout-master-set.  Must be one.
            ev = xmlevents.expectStartElement
                (FObjectNames.LAYOUT_MASTER_SET, XMLEvent.DISCARD_W_SPACE);
            // Process the layout-master-set
            FoLayoutMasterSet layoutMasters =
                                new FoLayoutMasterSet(getFOTree(), this, ev);
            // Clean up the fo:layout-master-set event
            pageSequenceMasters = layoutMasters.getPageSequenceMasters();
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);
            layoutMasters.deleteSubTree();

            // Look for optional declarations
            nowProcessing = "declarations";
            ev = xmlevents.expectStartElement
                        (FObjectNames.DECLARATIONS, XMLEvent.DISCARD_W_SPACE);
            if (ev != null) {
                // process the declarations
                declarations = numChildren();
                new FoDeclarations(getFOTree(), this, ev);
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }

            // Process page-sequences here
            // must have at least one
            nowProcessing = "page-sequence";
            ev = xmlevents.expectStartElement
                        (FObjectNames.PAGE_SEQUENCE, XMLEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No page-sequence found.");
            firstPageSeq = numChildren();
            new FoPageSequence(getFOTree(), this, ev);
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.PAGE_SEQUENCE, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:page-sequences
                new FoPageSequence(getFOTree(), this, ev);
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }
        } catch (NoSuchElementException e) {
            throw new FOPException
                ("Unexpected EOF while processing " + nowProcessing + ".");
        } catch(TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }
        // Clean up root's FO tree build environment
        makeSparsePropsSet();
        // Provide some stats
        System.out.println("Size of event pool: " + pool.getPoolSize());
        System.out.println("Next event id     : " + 
                           xmlevents.getNamespaces().getSequence());
    }
}
