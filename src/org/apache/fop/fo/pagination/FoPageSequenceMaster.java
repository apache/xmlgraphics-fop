/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */

package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.BitSet;
import java.util.NoSuchElementException;

// FOP
import org.apache.fop.fo.FOAttributes;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.XMLEvent;
import org.apache.fop.xml.XMLNamespaces;
import org.apache.fop.xml.UriLocalName;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.FOPropertySets;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FONode;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Ints;

/**
 * Implements the fo:page-sequence-master flow object.  These Fos are
 * children of fo:layout-master-set FOs.  Their contents are specified by
 * (single-page-master-reference|repeatable-page-master-reference
 *                                |repeatable-page-master-alternatives)+
 */
public class FoPageSequenceMaster extends FONode {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /** Map of <tt>Integer</tt> indices of <i>sparsePropsSet</i> array.
        It is indexed by the FO index of the FO associated with a given
        position in the <i>sparsePropsSet</i> array.  See
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
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new HashMap(1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.MASTER_NAME };
        sparsePropsMap.put
            (Ints.consts.get(PropNames.MASTER_NAME), Ints.consts.get(0));
    }

    /** Constant values for FoSinglePageMasterReference.
        See {@link #sparsePropsMap sparsePropsMap}. */
    private static final HashMap s_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    private static final int[] s_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    private static final int s_p_m_r_numProps;

    static {
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        s_p_m_r_PropsMap = new HashMap(1);
        s_p_m_r_numProps = 1;
        s_p_m_r_Indices = new int[] { PropNames.MASTER_REFERENCE };
        s_p_m_r_PropsMap.put
            (Ints.consts.get(PropNames.MASTER_REFERENCE),
                                                Ints.consts.get(0));
    }

    /** See {@link #sparsePropsMap sparsePropsMap}. */
    private static final HashMap r_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    private static final int[] r_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    private static final int r_p_m_r_numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.set(PropNames.MASTER_REFERENCE);
        propsets.set(PropNames.MAXIMUM_REPEATS);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        r_p_m_r_PropsMap = new HashMap();
        r_p_m_r_numProps = propsets.cardinality();
        r_p_m_r_Indices = new int[r_p_m_r_numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            r_p_m_r_Indices[propx] = next;
            r_p_m_r_PropsMap.put
                        (Ints.consts.get(next), Ints.consts.get(propx++));
        }
    }

    /** See {@link #sparsePropsMap sparsePropsMap}.  */
    private static final HashMap r_p_m_a_PropsMap;

    /** See {@link #sparseIndices sparseIndices}.  */
    private static final int[] r_p_m_a_Indices;

    /** See {@link #numProps numProps}.  */
    private static final int r_p_m_a_numProps;

    static {
        r_p_m_a_PropsMap = new HashMap(1);
        r_p_m_a_numProps = 1;
        r_p_m_a_Indices = new int[] { PropNames.MAXIMUM_REPEATS };
        r_p_m_a_PropsMap.put
            (Ints.consts.get(PropNames.MAXIMUM_REPEATS),
                                                Ints.consts.get(0));
    }

    /** See {@link #sparsePropsMap sparsePropsMap}. */
    private static final HashMap c_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    private static final int[] c_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    private static final int c_p_m_r_numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.set(PropNames.MASTER_REFERENCE);
        propsets.set(PropNames.PAGE_POSITION);
        propsets.set(PropNames.ODD_OR_EVEN);
        propsets.set(PropNames.BLANK_OR_NOT_BLANK);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        c_p_m_r_PropsMap = new HashMap();
        c_p_m_r_numProps = propsets.cardinality();
        c_p_m_r_Indices = new int[c_p_m_r_numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            c_p_m_r_Indices[propx] = next;
            c_p_m_r_PropsMap.put
                    (Ints.consts.get(next), Ints.consts.get(propx++));
        }
    }

    /**
     * An array with <tt>int</tt>s identifying
     * <tt>single-page-master-reference</tt>,
     * <tt>repeatable-page-master-reference</tt> and
     * <tt>repeatable-page-master-alternatives</tt> XML events.
     */
    private static final int[] singleOrRepeatableMasterRefs = {
        FObjectNames.SINGLE_PAGE_MASTER_REFERENCE,
        FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE,
        FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES
    };

    public FoPageSequenceMaster(FOTree foTree, FONode parent, FoXMLEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE_MASTER, parent, event,
              FOPropertySets.SEQ_MASTER_SET, sparsePropsMap, sparseIndices,
              numProps);
        // Process sequence members here
        try {
            do {
                FoXMLEvent ev = xmlevents.expectStartElement
                    (singleOrRepeatableMasterRefs, XMLEvent.DISCARD_W_SPACE);
                if (ev == null) break;  // page-sequence-masters exhausted
                int foType = ev.getFoType();
                if (foType == FObjectNames.SINGLE_PAGE_MASTER_REFERENCE) {
                    //System.out.println("Found single-page-master-reference");
                    new FoSinglePageMasterReference(foTree, this, ev);
                } else if (foType ==
                            FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE) {
                    //System.out.println
                    //        ("Found repeatable-page-master-reference");
                    new FoRepeatablePageMasterReference(foTree, this, ev);
                } else if (foType ==
                        FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES) {
                    //System.out.println
                    //        ("Found repeatable-page-master-alternatives");
                    new FoRepeatablePageMasterAlternatives(foTree, this, ev);
                } else
                    throw new FOPException
                            ("Aargh! expectStartElement(events, list)");
                xmlevents.getEndElement(ev);
            } while (true);
        } catch (NoSuchElementException e) {
            throw new FOPException("Unexpected EOF in page-sequence-master.");
        }
        if (this.numChildren() == 0)
            throw new FOPException("No children of page-sequence-master.");
        makeSparsePropsSet();
    }

    /**
     * @return a <tt>String</tt> with the "master-name" attribute value.
     */
    public String getMasterName() throws PropertyException {
        return ((NCName)getPropertyValue(PropNames.MASTER_NAME)).getNCName();
    }

    /**
     * Implements the fo:single-page-master-reference flow object.  It is
     * always a child of an fo:page-sequence-master.
     */
    public class FoSinglePageMasterReference extends FONode {

        public FoSinglePageMasterReference
                            (FOTree foTree, FONode parent, FoXMLEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.SINGLE_PAGE_MASTER_REFERENCE, parent,
                    event, FOPropertySets.SEQ_MASTER_SET, s_p_m_r_PropsMap,
                    s_p_m_r_Indices, s_p_m_r_numProps);
            this.makeSparsePropsSet();
        }

        public PropertyValue getMasterReference() throws PropertyException {
            return this.getPropertyValue(PropNames.MASTER_REFERENCE);
        }

    }// FoSinglePageMasterReference

    /**
     * Implements the fo:repeatable-page-master-reference flow object.  It is
     * always a child of an fo:page-sequence-master.
     */
    public class FoRepeatablePageMasterReference extends FONode {

        public FoRepeatablePageMasterReference
                            (FOTree foTree, FONode parent, FoXMLEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE,
                    parent, event, FOPropertySets.SEQ_MASTER_SET,
                    r_p_m_r_PropsMap, r_p_m_r_Indices, r_p_m_r_numProps);
            this.makeSparsePropsSet();
        }

        public PropertyValue getMasterReference() throws PropertyException {
            return this.getPropertyValue(PropNames.MASTER_REFERENCE);
        }

        public PropertyValue getMaximumRepeats() throws PropertyException {
            return this.getPropertyValue(PropNames.MAXIMUM_REPEATS);
        }

    }// FoRepeatablePageMasterReference

    /**
     * Implements the fo:repeatable-page-master-alternatives flow object.
     * It is always a child of an fo:page-sequence-master.
     */
    public class FoRepeatablePageMasterAlternatives extends FONode {

        public FoRepeatablePageMasterAlternatives
                            (FOTree foTree, FONode parent, FoXMLEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES,
                    parent, event, FOPropertySets.SEQ_MASTER_SET,
                    r_p_m_a_PropsMap, r_p_m_a_Indices, r_p_m_a_numProps);

            // Process conditional-page-master-references here
            try {
                do {
                    FoXMLEvent ev = this.xmlevents.expectStartElement
                            (FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE,
                                                    XMLEvent.DISCARD_W_SPACE);
                    if (ev == null) break; // Sub-sequences exhausted
                    //System.out.println
                    //    ("Found conditional-page-master-reference");
                    new FoConditionalPageMasterReference(foTree, this, ev);
                    this.xmlevents.getEndElement(ev);
                } while (true);
            } catch (NoSuchElementException e) {
                // End of file reached
                throw new FOPException("EOF in repeatable-page-masters.");
            }
            this.makeSparsePropsSet();
        }

        public PropertyValue getMaximumRepeats() throws PropertyException {
            return this.getPropertyValue(PropNames.MAXIMUM_REPEATS);
        }

        public class FoConditionalPageMasterReference extends FONode {

            public FoConditionalPageMasterReference
                            (FOTree foTree, FONode parent, FoXMLEvent event)
            throws TreeException, FOPException, PropertyException
            {
                super(foTree, FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE,
                        parent, event, FOPropertySets.SEQ_MASTER_SET,
                        c_p_m_r_PropsMap, c_p_m_r_Indices, c_p_m_r_numProps);
                this.makeSparsePropsSet();
            }

            public PropertyValue getMasterReference() throws PropertyException
            {
                return this.getPropertyValue(PropNames.MASTER_REFERENCE);
            }

            public PropertyValue getPagePosition() throws PropertyException {
                return this.getPropertyValue(PropNames.PAGE_POSITION);
            }

            public PropertyValue getOddOrEven() throws PropertyException {
                return this.getPropertyValue(PropNames.ODD_OR_EVEN);
            }

            public PropertyValue getBlankOrNotBlank() throws PropertyException
            {
                return this.getPropertyValue(PropNames.BLANK_OR_NOT_BLANK);
            }

        } // FoConditionalPageMasterReference

    }// FoRepeatablePageMasterAlternatives

}
