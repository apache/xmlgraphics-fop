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
 * Implements the fo:multi-case flow object.
 */
public class FoMultiCase extends FOPageSeqNode {

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
        propsets.set(PropNames.CASE_NAME);
        propsets.set(PropNames.CASE_TITLE);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.STARTING_STATE);

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
     * Construct an fo:multi-case node, and build the
     * fo:multi-case subtree.
     * <p>Content model for fo:multi-case: (#PCDATA|%inline;|%block;)*
     * <p> N.B. no marker children, and the allowable children restricted to
     * FOs available in place of the outer multi-case's parent multi-switch.
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoMultiCase
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.MULTI_CASE, pageSequence, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        stateFlags |= FONode.MC_MULTI_CASE;
        if (getMarkers() != 0) {
            throw new FOPException(
            "fo:marker illegal as child of fo:multi-case");
        }
        XmlEvent ev = null;
        do {
            try {
                if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                    ev = xmlevents.expectPcdataOrInlineOrBlock();
                else
                    ev = xmlevents.expectOutOfLinePcdataOrInlineOrBlock();
                if (ev != null) {
                    // Generate the flow object
                    //System.out.println("Generating flow object for " + ev);
                    FObjects.makePageSeqFOChild(
                                foTree, pageSequence, this, ev, stateFlags);
                    if (ev.getType() != XmlEvent.CHARACTERS) {
                        ev = xmlevents.getEndElement(
                                XmlEventReader.DISCARD_EV, ev);
                    }
                    namespaces.relinquishEvent(ev);
                }
            } catch(UnexpectedStartElementException e) {
                ev = xmlevents.getStartElement();
                log.warning
                        ("Ignoring unexpected Start Element: "
                                                         + ev.getQName());
                ev = xmlevents.getEndElement(
                        XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }
        } while (ev != null);

        makeSparsePropsSet();
    }

}
