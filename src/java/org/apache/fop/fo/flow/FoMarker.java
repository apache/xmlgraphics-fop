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

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEventsArrayBuffer;

/**
 * Implements the fo:marker flow object.
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */
public class FoMarker extends FOPageSeqNode {

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
    
    /**
     * The marker-class-name for this fo:marker
     */
    public final String markerClassName;
    
    /**
     * The buffer which will hold the contents of the fo:marker subtree
     */
    private XmlEventsArrayBuffer buffer;
    
    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.MARKER_CLASS_NAME };
        sparsePropsMap[PropNames.MARKER_CLASS_NAME] = 0;
    }

    /**
     * Construct an fo:marker node, and buffer the contents for later parsing.
     * @param foTree the FO tree being built
     * @param pageSequence ancestor of this node
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoMarker
            (FOTree foTree, FoPageSequence pageSequence, FOPageSeqNode parent,
                    FoXmlEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.MARKER, pageSequence, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        if ((stateFlags & FONode.FLOW) == 0)
            throw new FOPException
                    ("fo:marker must be descendent of fo:flow.");
        NCName ncName;
            try {
            ncName = (NCName)(getPropertyValue(PropNames.MARKER_CLASS_NAME));
        } catch (PropertyException e) {
            throw new FOPException(
                    "Cannot find marker-class-name in fo:marker", e);
        } catch (ClassCastException e) {
            throw new FOPException("Wrong PropertyValue type in fo:marker", e);
        }
        markerClassName = ncName.getNCName();

        // all but the marker-class-name property are irrelevant on fo:marker
        makeSparsePropsSet();
        
        // Collect the contents of fo:marker for future processing
        buffer = new XmlEventsArrayBuffer(namespaces);
        XmlEvent ev = xmlevents.getEndElement(
                buffer, XmlEventReader.RETAIN_EV, event);
        // The original START_ELEMENT event is still known to the parent
        // node, which will arrange to relinquish it.  Relinquish
        // the just-returned END_ELEMENT event
        namespaces.relinquishEvent(ev);
        
    }
    
    /**
     * @return the marker subtree buffer
     */
    public XmlEventsArrayBuffer getEventBuffer() {
        return buffer;
    }

}
