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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.area.BlockArea;
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
 * Implements the fo:block flow object.
 */
public class FoBlock extends FOPageSeqNode {

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
        propsets.or(PropertySets.fontSet);
        propsets.or(PropertySets.hyphenationSet);
        propsets.or(PropertySets.marginBlockSet);
        propsets.or(PropertySets.paddingSet);
        propsets.or(PropertySets.relativePositionSet);
        propsets.set(PropNames.BREAK_AFTER);
        propsets.set(PropNames.BREAK_BEFORE);
        propsets.set(PropNames.COLOR);
        propsets.set(PropNames.TEXT_DEPTH);
        propsets.set(PropNames.TEXT_ALTITUDE);
        propsets.set(PropNames.HYPHENATION_KEEP);
        propsets.set(PropNames.HYPHENATION_LADDER_COUNT);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.INTRUSION_DISPLACE);
        propsets.set(PropNames.KEEP_TOGETHER);
        propsets.set(PropNames.KEEP_WITH_NEXT);
        propsets.set(PropNames.KEEP_WITH_PREVIOUS);
        propsets.set(PropNames.LAST_LINE_END_INDENT);
        propsets.set(PropNames.LINEFEED_TREATMENT);
        propsets.set(PropNames.LINE_HEIGHT);
        propsets.set(PropNames.LINE_HEIGHT_SHIFT_ADJUSTMENT);
        propsets.set(PropNames.LINE_STACKING_STRATEGY);
        propsets.set(PropNames.ORPHANS);
        propsets.set(PropNames.WHITE_SPACE_TREATMENT);
        propsets.set(PropNames.SPAN);
        propsets.set(PropNames.TEXT_ALIGN);
        propsets.set(PropNames.TEXT_ALIGN_LAST);
        propsets.set(PropNames.TEXT_INDENT);
        propsets.set(PropNames.VISIBILITY);
        propsets.set(PropNames.WHITE_SPACE_COLLAPSE);
        propsets.set(PropNames.WIDOWS);
        propsets.set(PropNames.WRAP_OPTION);

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
     * Construct an fo:block node, and build the fo:block subtree.
     * <p>Content model for fo:inline: (#PCDATA|%inline;|%block;)*
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoBlock
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.BLOCK, pageSequence, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        getMarkers();
        // Generate a block area
        currentArea = new BlockArea(
                pageSequence, this, layoutContext, layoutContext.getSync());
        generated = new ArrayList();
        generated.add(currentArea);
        XmlEvent ev = null;
        do {
            try {
                if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                    ev = xmlevents.expectPcdataOrInlineOrBlock();
                else
                    ev = xmlevents.expectOutOfLinePcdataOrInlineOrBlock();
                if (ev != null) {
                    // Generate the flow object
                    
                    FObjects.makePageSeqFOChild(
                            foTree, pageSequence, this, ev, stateFlags);
                    // Area generation happening here
                    // Note that while the child is being processed, callbacks
                    // involving requests for and allocation of page space will
                    // be occurring
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

    public Area getReferenceRectangle() {
        // TODO
        return null;
    }

}
