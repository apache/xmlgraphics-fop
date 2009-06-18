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
import org.apache.fop.area.Area;
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
 * Implements the fo:simple-page-master flow object
 */
public class FoTitle extends FOPageSeqNode {

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
        propsets.or(PropertySets.fontSet);
        propsets.or(PropertySets.marginInlineSet);
        propsets.set(PropNames.COLOR);
        propsets.set(PropNames.LINE_HEIGHT);
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

    /** The <tt>XmlEventReader</tt> from which events are drawn. */
    private XmlEventReader xmlevents;

    /**
     * Construct an fo:title node, and build the fo:title subtree.
     * <p>Content model for fo:title: (#PCDATA|%inline;)*
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoTitle(FOTree foTree, FoPageSequence parent, FoXmlEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.TITLE, parent, event,
              FONode.TITLE_SET, sparsePropsMap, sparseIndices);
        if (getMarkers() != 0) {
            throw new FOPException(
            "fo:marker illegal as child of fo:title");
        }
        XmlEvent ev = null;
        do {
            try {
                ev = xmlevents.expectOutOfLinePcdataOrInline();
                if (ev != null) {
                    // Generate the flow object
                    FObjects.makePageSeqFOChild(
                            foTree, parent, this, ev, FONode.TITLE_SET);
                    if (ev.getType() != XmlEvent.CHARACTERS)
                        ev = xmlevents.getEndElement(
                                XmlEventReader.DISCARD_EV, ev);
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

    public Area getReferenceRectangle() throws FOPException {
        // TODO Reference rectangle is assumed to be equivalent to the
        // "auto" value on "page-height" and "page-width".  The
        // inline-progression-dimension and block-progression-dimension are
        // calculated according to the computed values of the
        // reference-orientation and writing-mode of the FO for which the
        // percentage is calculated.  See
        // 7.3 Reference Rectangle for Percentage Computations
        // This applies to all descendants of fo:title, except that for
        // descendants, the relevant reference-orientation and writing-mode are
        // those applying to fo:title.
        throw new FOPException("Called from FoTitle");
    }

}
