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
 * Implements the fo:table flow object.
 */
public class FoTable extends FONode {

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
        propsets.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        propsets.set(PropNames.BORDER_AFTER_PRECEDENCE);
        propsets.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        propsets.set(PropNames.BORDER_COLLAPSE);
        propsets.set(PropNames.BORDER_END_PRECEDENCE);
        propsets.set(PropNames.BORDER_SEPARATION);
        propsets.set(PropNames.BORDER_START_PRECEDENCE);
        propsets.set(PropNames.BREAK_AFTER);
        propsets.set(PropNames.BREAK_BEFORE);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        propsets.set(PropNames.INTRUSION_DISPLACE);
        propsets.set(PropNames.HEIGHT);
        propsets.set(PropNames.KEEP_TOGETHER);
        propsets.set(PropNames.KEEP_WITH_NEXT);
        propsets.set(PropNames.KEEP_WITH_PREVIOUS);
        propsets.set(PropNames.TABLE_LAYOUT);
        propsets.set(PropNames.TABLE_OMIT_FOOTER_AT_BREAK);
        propsets.set(PropNames.TABLE_OMIT_HEADER_AT_BREAK);
        propsets.set(PropNames.WIDTH);
        propsets.set(PropNames.WRITING_MODE);

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

    /** The number of table-columns on this FO. */
    private int numColumns = 0;

    /** The offset of table-header within the children. */
    private int headerOffset = -1;

    /** The offset of table-footer within the children. */
    private int footerOffset = -1;

    /** The offset of 1st table-body within the children. */
    private int firstBodyOffset = -1;

    /** The number of "table-body"s on this FO. */
    private int numBodies = 0;

    /**
     * Construct an fo:table node, and build the fo:table subtree.
     * <p>Content model for fo:table:<br>
     * (marker*, table-column*, table-header?, table-footer?, table-body+)
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTable
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE, parent, event,
                                  stateFlags, sparsePropsMap, sparseIndices);
        FoXMLEvent ev;
        xmlevents = foTree.getXmlevents();
        // Look for zero or more markers
        String nowProcessing = "marker";
        try {
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.MARKER, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoMarker(getFOTree(), this, ev, stateFlags);
                numMarkers++;
                xmlevents.getEndElement(FObjectNames.MARKER);
            }

            // Look for zero or more table-columns
            nowProcessing = "table-column";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_COLUMN, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoTableColumn(getFOTree(), this, ev, stateFlags);
                numColumns++;
                xmlevents.getEndElement(FObjectNames.TABLE_COLUMN);
            }

            // Look for optional table-header
            nowProcessing = "table-header";
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_HEADER, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                headerOffset = numChildren();
                new FoTableHeader(getFOTree(), this, ev, stateFlags);
                xmlevents.getEndElement(FObjectNames.TABLE_HEADER);
            }

            // Look for optional table-footer
            nowProcessing = "table-footer";
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_FOOTER, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                footerOffset = numChildren();
                new FoTableFooter(getFOTree(), this, ev, stateFlags);
                xmlevents.getEndElement(FObjectNames.TABLE_FOOTER);
            }

            // Look for one or more table-body
            // must have at least one
            nowProcessing = "table-body";
            ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_BODY, XMLEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No table-body found.");
            firstBodyOffset = numChildren();
            new FoTableBody(getFOTree(), this, ev, stateFlags);
            numBodies++;
            xmlevents.getEndElement(FObjectNames.TABLE_BODY);
            while ((ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_BODY, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:table-body's
                new FoTableBody(getFOTree(), this, ev, stateFlags);
                numBodies++;
                xmlevents.getEndElement(FObjectNames.TABLE_BODY);
            }

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
