/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

// FOP
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoRegionBeforeAfter extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array. See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
     */
    protected static final HashMap sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    protected static final int[] sparseIndices;
 
    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    protected static final int numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = PropertySets.backgroundSetClone();
        propsets.or(PropertySets.borderSet);
        propsets.or(PropertySets.paddingSet);
        propsets.set(PropNames.CLIP);
        propsets.set(PropNames.DISPLAY_ALIGN);
        propsets.set(PropNames.OVERFLOW);
        propsets.set(PropNames.REGION_NAME);
        propsets.set(PropNames.REFERENCE_ORIENTATION);
        propsets.set(PropNames.WRITING_MODE);
        propsets.set(PropNames.EXTENT);
        propsets.set(PropNames.PRECEDENCE);

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
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoRegionBeforeAfter
                (FOTree foTree, int foType, FONode parent, FoXMLEvent event)
        throws Tree.TreeException, FOPException
    {
        super(foTree, foType, parent, event, FOPropertySets.LAYOUT_SET,
                sparsePropsMap, sparseIndices, numProps);
    }

}
