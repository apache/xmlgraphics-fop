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
 * Implements the fo:simple-page-master flow object
 */
public class FoStaticContent extends FOPageSeqNode {

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
     * The flow-name for this fo:static-content
     */
    public final String flowName;
    
    /**
     * The buffer which will hold the contents of the fo:static-content subtree
     */
    private XmlEventsArrayBuffer buffer;
    
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
     * Construct an fo:static-content node, and buffer the contents for later
     * parsing.
     * <p>Content model for fo:static-content: (%block;)+
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     */
    public FoStaticContent(FOTree foTree, FoPageSequence parent, FoXmlEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.STATIC_CONTENT, parent, event,
              FONode.STATIC_SET, sparsePropsMap, sparseIndices);
        NCName ncName;
        try {
            ncName = (NCName)(getPropertyValue(PropNames.FLOW_NAME));
        } catch (PropertyException e) {
            throw new FOPException(
                    "Cannot find flow-name in fo:static-content", e);
        } catch (ClassCastException e) {
            throw new FOPException
            ("Wrong PropertyValue type for flow-name in fo:static-content",
                    e);
        }
        flowName = ncName.getNCName();
        
        // sparsePropsSet cannot be made for static content, because the full
        // ancestor tree of properties must be available for the later
        // resolution of properties in the static-content subtree and any
        // markers which are later attached to the subtree.
        // makeSparsePropsSet();
        
        // Collect the contents of fo:static-content for future processing
        buffer = new XmlEventsArrayBuffer(namespaces);
        XmlEvent ev = xmlevents.getEndElement(
                buffer, XmlEventReader.RETAIN_EV, event);
        // The original START_ELEMENT event is still known to the parent
        // page-sequence, which will arrange to relinquish it.  Relinquish
        // the just-returned END_ELEMENT event
        namespaces.relinquishEvent(ev);
    }
    
    /**
     * Gets the buffer of <code>XmlEvent</code>s from this
     * <b>fo:static-content</b> subtree
     * 
     * @return the static-content subtree buffer
     */
    public XmlEventsArrayBuffer getEventBuffer() {
        return buffer;
    }

    /**
     * Gets the <b>flow-name</b> with which this <b>fo:static-content</b>
     * is associated
     * 
     * @return the flow-name
     */
    public String getFlowName() {
        return flowName;
    }
//
//    public Area getReferenceRectangle() throws FOPException {
//        // TODO Reference rectangle is assumed to be the content rectangle of
//        // the first region into which the content is flowed.  For region-body
//        // it is normal-flow reference-area; for other regions it is the
//        // region-reference-area.  See
//        // 7.3 Reference Rectangle for Percentage Computations
//        throw new FOPException("Called from FoStaticContent");
//    }

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
