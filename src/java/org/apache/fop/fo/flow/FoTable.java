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
 * Implements the fo:table flow object.
 */
public class FoTable extends FOPageSeqNode {

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
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTable
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE, pageSequence, parent, event,
                                  stateFlags, sparsePropsMap, sparseIndices);
        XmlEvent ev;
        // Look for zero or more markers
        getMarkers();
        try {
            // Look for zero or more table-columns
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_COLUMN, XmlEvent.DISCARD_W_SPACE))
            != null) {
                new FoTableColumn(
                        getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                numColumns++;
                ev = xmlevents.getEndElement(
                        XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

            // Look for optional table-header
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_HEADER, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                headerOffset = numChildren();
                new FoTableHeader(
                        getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                ev = xmlevents.getEndElement(
                        XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

            // Look for optional table-footer
            if ((ev = xmlevents.expectStartElement
                    (FObjectNames.TABLE_FOOTER, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                footerOffset = numChildren();
                new FoTableFooter(
                        getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                ev = xmlevents.getEndElement(
                        XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

            // Look for one or more table-body
            // must have at least one
            ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_BODY, XmlEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No table-body found.");
            firstBodyOffset = numChildren();
            new FoTableBody(getFOTree(), pageSequence, this,
                    (FoXmlEvent)ev, stateFlags);
            numBodies++;
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            while ((ev = xmlevents.expectStartElement
                        (FObjectNames.TABLE_BODY, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:table-body's
                new FoTableBody(
                        getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                numBodies++;
                ev = xmlevents.getEndElement(
                        XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

        } catch(TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }

        makeSparsePropsSet();
    }

}
