/*
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
 * $Id$
 */

package org.apache.fop.fo.flow;

// FOP
import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.pagination.FoLayoutMasterSet;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;

/**
 * Implements the fo:simple-page-master flow object
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class FoPageSequence extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array. See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
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
        propsets.set(PropNames.COUNTRY);
        propsets.set(PropNames.FORMAT);
        propsets.set(PropNames.LANGUAGE);
        propsets.set(PropNames.LETTER_VALUE);
        propsets.set(PropNames.GROUPING_SEPARATOR);
        propsets.set(PropNames.GROUPING_SIZE);
        propsets.set(PropNames.ID);
        propsets.set(PropNames.INITIAL_PAGE_NUMBER);
        propsets.set(PropNames.FORCE_PAGE_COUNT);
        propsets.set(PropNames.MASTER_REFERENCE);
        propsets.set(PropNames.FLOW_MAP_REFERENCE);
        
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

    /** Child index of fo:title child. */
    private int title = -1;
    /** Child index of first fo:static-content child. */
    private int firstStaticContent = -1;
    /** Child index of fo:flow child. */
    private int flowChild = -1;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param layoutMasters the layout master set
     */
    public FoPageSequence(FOTree foTree, FONode parent, FoXmlEvent event,
            FoLayoutMasterSet layoutMasters)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE, parent, event,
              FONode.PAGESEQ_SET, sparsePropsMap, sparseIndices);
        XmlEvent ev;
        // Look for optional title
        String nowProcessing = "title";
        try {
            ev = xmlevents.expectStartElement
                        (FObjectNames.TITLE, XmlEvent.DISCARD_W_SPACE);
            if (ev != null) {
                // process the title
                title = numChildren();
                new FoTitle(getFOTree(), this, (FoXmlEvent)ev);
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            } // else ignore

            // Look for zero or more static-content
            nowProcessing = "static-content";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.STATIC_CONTENT, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:static-content
                if (firstStaticContent == -1)
                    firstStaticContent = numChildren();
                new FoStaticContent(getFOTree(), this, (FoXmlEvent)ev);
                namespaces.relinquishEvent(ev);
            }

            // Look for one or more page-sequence
            // must have at least one
            nowProcessing = "flow";
            ev = xmlevents.expectStartElement
                        (FObjectNames.FLOW, XmlEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No flow found.");
            flowChild = numChildren();
            new FoFlow(getFOTree(), this, (FoXmlEvent)ev);
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            while ((ev = xmlevents.expectStartElement
                            (FObjectNames.FLOW, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:page-sequences
                new FoFlow(getFOTree(), this, (FoXmlEvent)ev);
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }
        } catch (NoSuchElementException e) {
            throw new FOPException
                ("Unexpected EOF while processing " + nowProcessing + ".");
        } catch(TreeException e) {
            throw new FOPException("TreeException: " + e.getMessage());
        } catch(PropertyException e) {
            throw new FOPException("PropertyException: " + e.getMessage());
        }

        makeSparsePropsSet();
    }

}
