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
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.PropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.Ints;

import java.util.HashMap;
import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * Implements the fo:simple-page-master flow object
 */
public class FoPageSequence extends FONode {

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

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap();
        numProps = propsets.cardinality();
        sparseIndices = new int[numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            sparseIndices[propx] = next;
            sparsePropsMap.put
                        (Ints.consts.get(next), Ints.consts.get(propx++));
        }
    }

    /** Child index of fo:title child. */
    private int title = -1;
    /** Child index of first fo:static-content child. */
    private int firstStaticContent = -1;
    /** Child index of first fo:flow child. */
    private int firstFlow = -1;

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>FoXMLEvent</tt> that triggered the creation of
     * this node
     */
    public FoPageSequence(FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE, parent, event,
              FOPropertySets.PAGESEQ_SET, sparsePropsMap, sparseIndices,
              numProps);
        FoXMLEvent ev;
        String nowProcessing;
        // Look for optional title
        nowProcessing = "title";
        try {
            ev = xmlevents.expectStartElement
                        (FObjectNames.TITLE, XMLEvent.DISCARD_W_SPACE);
            if (ev != null) {
                // process the title
                title = numChildren();
                new FoTitle(getFOTree(), this, ev);
                xmlevents.getEndElement(FObjectNames.TITLE);
            } // else ignore

            // Look for zero or more static-content
            nowProcessing = "static-content";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.STATIC_CONTENT, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:static-content
                if (firstStaticContent != -1)
                    firstStaticContent = numChildren();
                new FoStaticContent(getFOTree(), this, ev);
                xmlevents.getEndElement(FObjectNames.STATIC_CONTENT);
            }

            // Look for one or more page-sequence
            // must have at least one
            nowProcessing = "flow";
            ev = xmlevents.expectStartElement
                        (FObjectNames.FLOW, XMLEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No flow found.");
            firstFlow = numChildren();
            new FoFlow(getFOTree(), this, ev);
            xmlevents.getEndElement(FObjectNames.FLOW);
            while ((ev = xmlevents.expectStartElement
                            (FObjectNames.FLOW, XMLEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:page-sequences
                new FoFlow(getFOTree(), this, ev);
                xmlevents.getEndElement(FObjectNames.FLOW);
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
