/*
 * $Id$
 * 
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.flow;

// FOP
import java.util.Arrays;
import java.util.BitSet;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;

/**
 * Implements the fo:table-footer flow object.
 */
public class FoTableFooter extends FOPageSeqNode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.
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

    /** The number of table-rows on this FO. */
    private int numRows = 0;

    /** The offset of 1st table-row within the children. */
    private int firstRowOffset = -1;

    /** The number of table-cells on this FO. */
    private int numCells = 0;

    /** The offset of 1st table-cell within the children. */
    private int firstCellOffset = -1;

    /**
     * Construct an fo:table-footer node, and build the
     * fo:table-footer subtree.
     * <p>Content model for fo:table-footer
     * (marker*, (table-row+|table-cell+))
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTableFooter
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE_FOOTER, pageSequence, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        // Look for zero or more markers
        getMarkers();
        XmlEvent ev;
        String nowProcessing;
        try {
            // Look for one or more table-rows
            nowProcessing = "table-row";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_ROW, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                new FoTableRow(getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                numRows++;
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

            if (numRows > 0) {
                firstRowOffset = numMarkers;
            } else {
                // No rows - look for one or more table-cells
                nowProcessing = "table-cell";
                if ((ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_CELL, XmlEvent.DISCARD_W_SPACE))
                       != null) {
                    new FoTableCell(
                            getFOTree(), pageSequence, this,
                            (FoXmlEvent)ev, stateFlags);
                    numCells++;
                    ev = xmlevents.getEndElement(
                            XmlEventReader.DISCARD_EV, ev);
                    namespaces.relinquishEvent(ev);
                }
                if (numCells == 0)
                    throw new FOPException
                            ("No table-row or table-cell in table-footer.");
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
