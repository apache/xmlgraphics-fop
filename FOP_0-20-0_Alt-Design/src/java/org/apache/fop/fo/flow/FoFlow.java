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
import org.apache.fop.area.Page;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.PropNames;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.UnexpectedStartElementException;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoFlow extends FOPageSeqNode {

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
        propsets.set(PropNames.FLOW_NAME);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparsePropsMap[PropNames.FLOW_NAME] = 0;
        sparseIndices = new int[] { PropNames.FLOW_NAME };
    }
    
    /**
     * Construct an fo:flow node, and build the fo:flow subtree.
     * <p>Content model for fo:flow (%block;)+
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoFlow(FOTree foTree, FoPageSequence parent, FoXmlEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.FLOW, parent, event,
              FONode.FLOW_SET, sparsePropsMap, sparseIndices);
        getMarkers();
        XmlEvent ev;
        try {
            // Get at least one %block;
            if ((ev = xmlevents.expectBlock()) == null)
                throw new FOPException("%block; not found in fo:flow");
            // Generate the flow object
            FObjects.makePageSeqFOChild(
                    foTree, pageSequence, this, (FoXmlEvent)ev,
                    FONode.FLOW_SET);
            // Clear the blockage
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            // Get the rest of the %block;s
            do {
                ev = xmlevents.expectBlock();
                if (ev != null) {
                    // Generate the flow object
                    FObjects.makePageSeqFOChild(
                            foTree, parent, this, (FoXmlEvent)ev,
                            FONode.FLOW_SET);
                    ev = xmlevents.getEndElement(
                            XmlEventReader.DISCARD_EV, ev);
                    namespaces.relinquishEvent(ev);
                }
            } while (ev != null);
        } catch(UnexpectedStartElementException e) {
            throw new FOPException
                    ("Block not found or unexpected non-block in fo:flow");
        }
        makeSparsePropsSet();
    }

    public Area getReferenceRectangle() {
        // TODO Reference rectangle is assumed to be the content rectangle of
        // the first region into which the content is flowed.  For region-body
        // it is normal-flow reference-area; for other regions it is the
        // region-reference-area.  See
        // 7.3 Reference Rectangle for Percentage Computations
        // The difficulty is that there may be multiple attempts to layout the
        // flow.  Each attempt will generate its own page set, only the first
        // of which contains a region-body-reference-area which qualifies as
        // the reference rectangle for percentages defined on the flow.
        //
        // Get the first page of the page-sequence
        // TODO check whether the current page from the page-sequence will be
        // enough
        Page page = pageSequence.getCurr1stPage();
        if (page == null) return null;
        return page.getNormalFlowRefArea();
    }

    public Area getLayoutContext() {
        // The layout context for fo:flow is
        // the first normal-flow-reference-area.
        return getReferenceRectangle();
    }

    public Area getChildrensLayoutContext() {
        // The layout context for the &block; children of fo:flow is
        // the current normal-flow-reference-area.
        return pageSequence.getPage().getNormalFlowRefArea();
    }

}
