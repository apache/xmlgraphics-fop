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
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.UnexpectedStartElementException;

/**
 * Implements the fo:table-caption flow object.
 */
public class FoTableCaption extends FOPageSeqNode {

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
        propsets.or(PropertySets.paddingSet);
        propsets.or(PropertySets.relativePositionSet);
        propsets.set(PropNames.BLOCK_PROGRESSION_DIMENSION);
        propsets.set(PropNames.HEIGHT);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.INLINE_PROGRESSION_DIMENSION);
        propsets.set(PropNames.INTRUSION_DISPLACE);
        propsets.set(PropNames.KEEP_TOGETHER);
        propsets.set(PropNames.WIDTH);

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

    /**
     * Construct an fo:table-caption node, and build the
     * fo:table-caption subtree.
     * <p>Content model for fo:table-caption (%block;)+
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoTableCaption
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TABLE_CAPTION, pageSequence, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        getMarkers();
        XmlEvent ev = null;
        try {
            // Get at least one %block;
            if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                ev = xmlevents.expectBlock();
            else
                ev = xmlevents.expectOutOfLineBlock();
            if (ev == null)
                throw new FOPException
                        ("%block; not found in fo:table-caption");
            // Generate the flow object
            FObjects.makePageSeqFOChild(
                    foTree, pageSequence, this, (FoXmlEvent)ev, stateFlags);
            // Clear the blockage
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            // Get the rest of the %block;s
            do {
                if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                    ev = xmlevents.expectBlock();
                else
                    ev = xmlevents.expectOutOfLineBlock();
                if (ev != null) {
                    // Generate the flow object
                    FObjects.makePageSeqFOChild(
                            foTree, pageSequence, this,
                            (FoXmlEvent)ev, stateFlags);
                    ev = xmlevents.getEndElement(
                            XmlEventReader.DISCARD_EV, ev);
                    namespaces.relinquishEvent(ev);
                }
            } while (ev != null);
        } catch(UnexpectedStartElementException e) {
            throw new FOPException
            ("Block not found or unexpected non-block in fo:table-caption");
        }

        makeSparsePropsSet();
    }

}
