/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FObjects;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.UnexpectedStartElementException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;
import org.apache.fop.messaging.MessageHandler;

import java.util.HashMap;
import java.util.BitSet;

/**
 * Implements the fo:marker flow object.
 */
public class FoMarker extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array. See
        {@link org.apache.fop.fo.FONode#sparsePropsSet FONode.sparsePropsSet}.
     */
    private static final HashMap sparsePropsMap;

    /** An <tt>int</tt> array of of the applicable property indices, in
        property index order. */
    private static final int[] sparseIndices;

    /** The number of applicable properties.  This is the size of the
        <i>sparsePropsSet</i> array. */
    private static final int numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap(1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.MARKER_CLASS_NAME };
        sparsePropsMap.put
            (Ints.consts.get(PropNames.MARKER_CLASS_NAME), Ints.consts.get(0));
    }

    /**
     * Construct an fo:wrapper node, and build the fo:wrapper subtree.
     * <p>Content model for fo:inline: (marker*,(#PCDATA|%inline;|%wrapper;)*)
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     * @param stateFlags - passed down from the parent.  Includes the
     * attribute set information.
     */
    public FoMarker
            (FOTree foTree, FONode parent, FoXMLEvent event, int stateFlags)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.MARKER, parent, event,
                          stateFlags, sparsePropsMap, sparseIndices);
        if ((stateFlags & FONode.FLOW) == 0)
            throw new FOPException
                    ("fo:marker must be descendent of fo:flow.");
        FoXMLEvent ev = null;
        do {
            try {
                if ((stateFlags & FONode.MC_OUT_OF_LINE) == 0)
                    ev = xmlevents.expectPcdataOrInlineOrBlock();
                else
                    ev = xmlevents.expectOutOfLinePcdataOrInlineOrBlock();
                if (ev != null) {
                    // Generate the flow object
                    //System.out.println("Generating flow object for " + ev);
                    FObjects.fobjects.makeFlowObject
                                (foTree, this, ev, stateFlags);
                    if (ev.getFoType() != FObjectNames.PCDATA)
                        ev = xmlevents.getEndElement(ev);
                }
            } catch(UnexpectedStartElementException e) {
                ev = xmlevents.getStartElement();
                MessageHandler.logln
                        ("Ignoring unexpected Start Element: "
                                                         + ev.getQName());
                ev = xmlevents.getEndElement(ev);
            }
        } while (ev != null);

        makeSparsePropsSet();
    }

}
