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
 * Implements the fo:table-body flow object.
 */
public class FoTableBody extends FONode {

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
        propsets.or(PropertySets.relativePositionSet);
        propsets.set(PropNames.BORDER_AFTER_PRECEDENCE);
        propsets.set(PropNames.BORDER_BEFORE_PRECEDENCE);
        propsets.set(PropNames.BORDER_END_PRECEDENCE);
        propsets.set(PropNames.BORDER_START_PRECEDENCE);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.VISIBILITY);

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

    /** The number of table-rows on this FO. */
    private int numRows = 0;

    /** The offset of 1st table-row within the children. */
    private int firstRowOffset = -1;

    /** The number of table-cells on this FO. */
    private int numCells = 0;

    /** The offset of 1st table-cell within the children. */
    private int firstCellOffset = -1;

    /**
     * Construct an fo:table-body node, and build the
     * fo:table-body subtree.
     * <p>Content model for fo:table-body
     * (marker*, (table-row+|table-cell+))
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTableBody
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE_BODY, parent, event,
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

            // Look for one or more table-rows
            nowProcessing = "table-row";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_ROW, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                new FoTableRow(getFOTree(), this, ev, stateFlags);
                numRows++;
                xmlevents.getEndElement(FObjectNames.TABLE_ROW);
            }

            if (numRows > 0) {
                firstRowOffset = numMarkers;
            } else {
                // No rows - look for one or more table-cells
                nowProcessing = "table-cell";
                if ((ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_CELL, XMLEvent.DISCARD_W_SPACE))
                       != null) {
                    new FoTableCell(getFOTree(), this, ev, stateFlags);
                    numCells++;
                    xmlevents.getEndElement(FObjectNames.TABLE_CELL);
                }
                if (numCells == 0)
                    throw new FOPException
                            ("No table-row or table-cell in table-body.");
                firstCellOffset = numMarkers;
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
