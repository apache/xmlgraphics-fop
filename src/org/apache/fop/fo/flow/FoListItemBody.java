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
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.UnexpectedStartElementException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:list-item-body flow object.
 */
public class FoListItemBody extends FONode {

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
        propsets.or(PropertySets.accessibilitySet);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.KEEP_TOGETHER);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap();
        numProps = propsets.cardinality();
        sparseIndices = new int[numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            sparseIndices[propx] = next;
            sparsePropsMap.put
                        (Ints.consts.get(next), Ints.consts.get(propx++));
        }
    }

    /**
     * Construct an fo:list-item-body node, and build the
     * fo:list-item-body subtree.
     * <p>Content model for fo:list-item-body: (marker*, %block;+)
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoListItemBody
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.LIST_ITEM_BODY, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        FoXMLEvent ev = null;
        try {
            // Get at least one %block;
            if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                ev = xmlevents.expectBlock();
            else
                ev = xmlevents.expectOutOfLineBlock();
            if (ev == null)
                throw new FOPException
                        ("%block; not found in fo:list-item-body");
            // Generate the flow object
            FObjects.fobjects.makeFlowObject(foTree, this, ev, stateFlags);
            // Clear the blockage
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);
            // Get the rest of the %block;s
            do {
                if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                    ev = xmlevents.expectBlock();
                else
                    ev = xmlevents.expectOutOfLineBlock();
                if (ev != null) {
                    // Generate the flow object
                    FObjects.fobjects.makeFlowObject
                                            (foTree, this, ev, stateFlags);
                    ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                    pool.surrenderEvent(ev);
                }
            } while (ev != null);
        } catch(UnexpectedStartElementException e) {
            throw new FOPException
            ("Block not found or unexpected non-block in fo:list-item-body");
        }

        makeSparsePropsSet();
    }

}
