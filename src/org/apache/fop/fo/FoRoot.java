/**
 * $Id$
 * <br/>Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * <br/>For details on use and redistribution please refer to the
 * <br/>LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

package org.apache.fop.fo;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FObjects;
import org.apache.fop.datastructs.SyncedCircularBuffer;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.pagination.FoLayoutMasterSet;
import org.apache.fop.xml.XMLEvent;

import org.xml.sax.Attributes;

import java.util.NoSuchElementException;

/** <p>
 * <tt>FoRoot</tt> is the class which processes the fo:root start element
 * XML event.
 * </p><p>
 * The building of all of the fo tree, and the forwarding of FO tree events
 * on to further stages of processing, will all take place within the
 * <tt>buildFoTree()</tt> method of this class instance.
 * </p>
 */

public class FoRoot extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    private FoLayoutMasterSet layoutMasters;

    /**
     * @param foTree the FO tree being built
     * @param event the <tt>XMLEvent</tt> that triggered the creation of this
     * node
     */
    public FoRoot
        (FOTree foTree, XMLEvent event)
        throws Tree.TreeException, FOPException, PropertyException
    {
        // This is the root node of the tree; hence the null argument
        super(foTree, null, event, FONode.ROOT);
    }

    /** <p>
     * Process the FO tree, starting with this fo:root element.
     * N.B. the FO tree is not really a tree.
     * Trees only occur with fo:flow and fo:static-content.  These will
     * be built at the appropriate places as part of the FO tree processing.
     * Terminates at the completion of FO tree processing.
     * </p><p>
     * <tt>fo:root</tt> contents are <br/>
     * (layout-master-set,declarations?,page-sequence+)
     * </p><p>
     * I.e. the <fo:root> element is the parent of a two-element sequence:
     * (layout-master-set-declarations),(page-sequence-sequence)
     * </p><p>
     * 'layout-master-set-declarations' is logically an unordered set of
     * (layout-master-set,declarations?), although this definition
     * determines an order of occurrence in the input tree.  It is 
     * unordered in the sense that there is no logical precedence in the
     * set.  However, there is a logical precedence of the set with respect
     * to the page-sequences.
     * </p><p>
     * The contents of declarations must be available to all FO tree
     * processing; the contents of the layout-master-set must be available
     * during the page setup phase of the processing of each page-sequence
     * in the page-sequence-sequence.
     */
    public void buildFoTree() throws FOPException{
        XMLEvent ev;
        //System.out.println("buildFoTree: " + event);
        // Look for layout-master-set
        try {
            ev = XMLEvent.expectStartElement
                    (foTree.xmlevents, XMLEvent.XSLNSpaceIndex,
                     "layout-master-set");
        } catch (NoSuchElementException e) {
            throw new FOPException(e.getMessage());
        }
        // Process the layout-master-set
        try {
            layoutMasters = new FoLayoutMasterSet(foTree, this, ev);
        } catch(Tree.TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }
        layoutMasters.setupPageMasters();
        // Stub - flush the layout masters
        ev = XMLEvent.getEndElement
                (foTree.xmlevents, XMLEvent.XSLNSpaceIndex,
                 "layout-master-set");
        // Look for optional declarations
        try {
            XMLEvent.expectStartElement
                    (foTree.xmlevents, XMLEvent.XSLNSpaceIndex,
                     "declarations");
            // process the declarations
            XMLEvent.getEndElement
                    (foTree.xmlevents, XMLEvent.XSLNSpaceIndex,
                     "declarations");
        } catch (NoSuchElementException e) {
            // Take no notice - declarations is optional
        }
        
    }
}
