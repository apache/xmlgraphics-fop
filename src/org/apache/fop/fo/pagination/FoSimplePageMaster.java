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
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Ints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoSimplePageMaster extends FONode {

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
        // Collect the sets of properties that apply
        BitSet propsets = PropertySets.marginBlockSetClone();
        propsets.set(PropNames.MASTER_NAME);
        propsets.set(PropNames.PAGE_HEIGHT);
        propsets.set(PropNames.PAGE_WIDTH);
        propsets.set(PropNames.REFERENCE_ORIENTATION);
        propsets.set(PropNames.WRITING_MODE);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = propsets.cardinality();
        sparseIndices = new int[numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            sparseIndices[propx] = next;
            sparsePropsMap[next] = propx++;
        }
    }

    private FoRegionBody regionBody;
    private FoRegionBefore regionBefore;
    private FoRegionAfter regionAfter;
    private FoRegionStart regionStart;
    private FoRegionEnd regionEnd;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoSimplePageMaster(FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.SIMPLE_PAGE_MASTER, parent, event,
              FONode.LAYOUT_SET, sparsePropsMap, sparseIndices);
        // Process regions here
        FoXMLEvent regionEv;
        if ((regionEv = xmlevents.expectStartElement
                (FObjectNames.REGION_BODY, XMLEvent.DISCARD_W_SPACE)) == null)
            throw new FOPException
                ("No fo:region-body in simple-page-master: "
                    + getMasterName());
        // Process region-body
        regionBody = new FoRegionBody(foTree, this, regionEv);
        regionEv = xmlevents.getEndElement(xmlevents.DISCARD_EV, regionEv);
        pool.surrenderEvent(regionEv);

        // Remaining regions are optional
        if ((regionEv = xmlevents.expectStartElement
                    (FObjectNames.REGION_BEFORE, XMLEvent.DISCARD_W_SPACE))
                != null)
        {
            regionBefore = new FoRegionBefore(foTree, this, regionEv);
            regionEv =
                xmlevents.getEndElement(xmlevents.DISCARD_EV, regionEv);
            pool.surrenderEvent(regionEv);
        }

        if ((regionEv = xmlevents.expectStartElement
                    (FObjectNames.REGION_AFTER, XMLEvent.DISCARD_W_SPACE))
                != null)
        {
            regionAfter = new FoRegionAfter(foTree, this, regionEv);
            regionEv =
                xmlevents.getEndElement(xmlevents.DISCARD_EV, regionEv);
            pool.surrenderEvent(regionEv);
        }

        if ((regionEv = xmlevents.expectStartElement
                    (FObjectNames.REGION_START, XMLEvent.DISCARD_W_SPACE))
                != null)
        {
            regionStart = new FoRegionStart(foTree, this, regionEv);
            regionEv =
                xmlevents.getEndElement(xmlevents.DISCARD_EV, regionEv);
            pool.surrenderEvent(regionEv);
        }

        if ((regionEv = xmlevents.expectStartElement
                    (FObjectNames.REGION_END, XMLEvent.DISCARD_W_SPACE))
                != null)
        {
            regionEnd = new FoRegionEnd(foTree, this, regionEv);
            regionEv =
                xmlevents.getEndElement(xmlevents.DISCARD_EV, regionEv);
            pool.surrenderEvent(regionEv);
        }

        // Clean up the build environment
        makeSparsePropsSet();
    }

    /**
     * @return a <tt>String</tt> with the "master-name" attribute value.
     */
    public String getMasterName() throws PropertyException {
        return ((NCName)getPropertyValue(PropNames.MASTER_NAME)).getNCName();
    }
}
