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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:table-and-caption flow object.
 */
public class FoTableAndCaption extends FONode {

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
        propsets.or(PropertySets.auralSet);
        propsets.or(PropertySets.backgroundSet);
        propsets.or(PropertySets.borderSet);
        propsets.or(PropertySets.marginBlockSet);
        propsets.or(PropertySets.paddingSet);
        propsets.or(PropertySets.relativePositionSet);
        propsets.set(PropNames.BREAK_AFTER);
        propsets.set(PropNames.BREAK_BEFORE);
        propsets.set(PropNames.CAPTION_SIDE);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.INTRUSION_DISPLACE);
        propsets.set(PropNames.KEEP_TOGETHER);
        propsets.set(PropNames.KEEP_WITH_NEXT);
        propsets.set(PropNames.KEEP_WITH_PREVIOUS);
        propsets.set(PropNames.TEXT_ALIGN);

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

    /** The number of markers on this FO. */
    private int numMarkers = 0;

    /** The offset of table-caption within the children. */
    private int captionOffset = -1;

    /** The offset of table within the children. */
    private int tableOffset = 0;

    /**
     * Construct an fo:table-and-caption node, and build the
     * fo:table-and-caption subtree.
     * <p>Content model for fo:table-and-caption:<br>
     * (marker*, table-caption?, table)
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTableAndCaption
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE_AND_CAPTION, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        FoXMLEvent ev;
        // Look for zero or more markers
        String nowProcessing = "marker";
        try {
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.MARKER, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoMarker(getFOTree(), this, ev, stateFlags);
                numMarkers++;
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }

            // Look for optional table-caption
            nowProcessing = "table-caption";
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_CAPTION, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoTableCaption(getFOTree(), this, ev, stateFlags);
                captionOffset = numMarkers;
                ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
                pool.surrenderEvent(ev);
            }

            // Look for one table
            nowProcessing = "table";
            ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE, XMLEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No table found.");
            tableOffset = numChildren();
            new FoTable(getFOTree(), this, ev, stateFlags);
            ev = xmlevents.getEndElement(xmlevents.DISCARD_EV, ev);
            pool.surrenderEvent(ev);

            /*
        } catch (NoSuchElementException e) {
            throw new FOPException
                ("Unexpected EOF while processing " + nowProcessing + ".");
            */
        } catch(TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }

        makeSparsePropsSet();
    }

}
