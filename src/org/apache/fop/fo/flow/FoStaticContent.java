/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.UnexpectedStartElementException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoStaticContent extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array. See
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
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.set(PropNames.FLOW_NAME);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap(1);
        numProps = 1;
        sparsePropsMap.put
            (Ints.consts.get(PropNames.FLOW_NAME), Ints.consts.get(0));
        sparseIndices = new int[] { PropNames.FLOW_NAME };
    }

    /**
     * Construct an fo:static-content node, and build the fo:static-content
     * subtree.
     * <p>Content model for fo:static-content: (%block;)+
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoStaticContent(FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.STATIC_CONTENT, parent, event,
              FONode.STATIC_SET, sparsePropsMap, sparseIndices);
        FoXMLEvent ev;
        try {
            // Get at least one %block;
            if ((ev = xmlevents.expectBlock()) == null)
                throw new FOPException
                        ("%block; not found in fo:static-content");
            // Generate the flow object
            //System.out.println("Generating first block for static-content.");
            FObjects.fobjects.makeFlowObject
                                    (foTree, this, ev, FONode.STATIC_SET);
            // Clear the blockage
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);
            // Get the rest of the %block;s
            while ((ev = xmlevents.expectBlock()) != null) {
                // Generate the flow object
                //System.out.println
                    //("Generating subsequent block for static-content.");
                FObjects.fobjects.makeFlowObject
                                    (foTree, this, ev, FONode.STATIC_SET);
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }
        } catch(UnexpectedStartElementException e) {
            throw new FOPException
            ("Block not found or unexpected non-block in fo:static-content");
        }

        System.out.println("Making sparsePropsSet for static-content.");
        makeSparsePropsSet();
    }

}
