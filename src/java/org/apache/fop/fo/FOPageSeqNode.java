/*
 *
 * Copyright 2004 The Apache Software Foundation.
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
 * Created on 1/02/2004
 * $Id$
 */
package org.apache.fop.fo;

import java.util.ArrayList;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoMarker;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public class FOPageSeqNode extends FONode {

    /** The <code>FoPageSequence</code> ancestor of this node. */
    protected final FoPageSequence pageSequence;
    /**
     * Comment for <code>childContext</code>
     */
    protected Area currentArea = null;
    protected Area myContext = null;
    protected ArrayList generated = null;
    
    /**
     * @param foTree the FO tree to which this node is added
     * @param type of FO node
     * @param parent node
     * @param event that triggered the creation of this node
     * @param stateFlags the set of states relevant at this point in the
     * tree.  Includes the state information necessary to select an attribute
     * set for this node.
     * @param sparsePropsMap maps the property indices
     * to their offsets in the set of properties applicable to this node
     * @param sparseIndices holds the set of property
     * indices applicable to this node, in ascending order.
     * <code>sparsePropsMap</code> maps property indices to a position in this
     * array.  Together they provide a sparse array facility for this node's
     * properties.
     * @throws TreeException
     * @throws FOPException
     * @throws PropertyException
     */
    public FOPageSeqNode(
        FOTree foTree,
        int type,
        FONode pageSequence,
        FONode parent,
        XmlEvent event,
        int stateFlags,
        int[] sparsePropsMap,
        int[] sparseIndices)
        throws TreeException, FOPException, PropertyException {
        super(
            foTree,
            type,
            parent,
            event,
            stateFlags,
            sparsePropsMap,
            sparseIndices);
        if (pageSequence.type != FObjectNames.PAGE_SEQUENCE) {
            throw new RuntimeException(
                    "FOPageSeqNode constructor expects FoPageSequence; got " +
                    nodeType());
        }
        this.pageSequence = (FoPageSequence)pageSequence;
    }
    
    
    /**
     * Constructor for the immediate children of a page-sequence, whose
     * parent page-sequence is not an FOPageSeqNode.
     * 
     * @param foTree the FO tree to which this node is added
     * @param type of FO node
     * @param event that triggered the creation of this node
     * @param stateFlags the set of states relevant at this point in the
     * tree.  Includes the state information necessary to select an attribute
     * set for this node.
     * @param sparsePropsMap maps the property indices
     * to their offsets in the set of properties applicable to this node
     * @param sparseIndices holds the set of property
     * indices applicable to this node, in ascending order.
     * <code>sparsePropsMap</code> maps property indices to a position in this
     * array.  Together they provide a sparse array facility for this node's
     * properties.
     * @throws TreeException
     * @throws FOPException
     * @throws PropertyException
     */
    public FOPageSeqNode(
            FOTree foTree,
            int type,
            FONode pageSequence,
            XmlEvent event,
            int stateFlags,
            int[] sparsePropsMap,
            int[] sparseIndices)
    throws TreeException, FOPException, PropertyException {
        this(
                foTree,
                type,
                pageSequence,
                pageSequence,
                event,
                stateFlags,
                sparsePropsMap,
                sparseIndices);
    }
    
    public FoPageSequence getPageSequence() {
        return pageSequence;
    }

    /**
     * Gets the fo:marker elements (if any) defined in the this node.  Any
     * fo:marker events found are relinquished.
     * @return the number of markers found
     * @throws FOPException
     */
    public int getMarkers() throws FOPException {
        XmlEvent ev;
        try {
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.MARKER, XmlEvent.DISCARD_W_SPACE))
            != null) {
                new FoMarker(getFOTree(), pageSequence, this,
                        (FoXmlEvent)ev, stateFlags);
                numMarkers++;
                // Relinquish the original event
                namespaces.relinquishEvent(ev);
            }
        } catch (TreeException e) {
            throw new FOPException(e);
        } catch (FOPException e) {
            throw new FOPException(e);
        }
        return numMarkers;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.fo.FONode#getReferenceRectangle()
     */
    public Area getReferenceRectangle() throws FOPException {
        throw new FOPException("Called from FOPageSeqNode");
    }

    /**
     * @return
     * @throws FOPException
     */
    public Area getLayoutContext() throws FOPException {
        // The default layout context is provided by the parent
            return ((FONode)parent).getChildrensLayoutContext();
    }

    /**
     * Gets the layout context for the children of this <code>FONode</code>.
     * Subclasses with special requirements must override this method.
     * The default context is the current area generated by the node.
     */
    public Area getChildrensLayoutContext()
            throws FOPException {
        return currentArea;
    }

}
