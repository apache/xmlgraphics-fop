/*
   Copyright 2002-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 *  
 * $Id$
 */

package org.apache.fop.fo.pagination;

import java.util.Arrays;
import java.util.BitSet;
import java.util.NoSuchElementException;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.xml.XmlEventReader;
import org.apache.fop.xml.XmlEvent;

/**
 * Implements the fo:page-sequence-master flow object.  These Fos are
 * children of fo:layout-master-set FOs.  Their contents are specified by
 * (single-page-master-reference|repeatable-page-master-reference
 *                                |repeatable-page-master-alternatives)+
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 */
public class FoPageSequenceMaster extends FONode {

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
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        sparsePropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(sparsePropsMap, -1);
        numProps = 1;
        sparseIndices = new int[] { PropNames.MASTER_NAME };
        sparsePropsMap[PropNames.MASTER_NAME] = 0;
    }

    /** Constant values for FoSinglePageMasterReference.
        See {@link #sparsePropsMap sparsePropsMap}. */
    protected static final int[] s_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    protected static final int[] s_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    public static final int s_p_m_r_numProps;

    static {
        // applicableProps is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        s_p_m_r_PropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(s_p_m_r_PropsMap, -1);
        s_p_m_r_numProps = 1;
        s_p_m_r_Indices = new int[] { PropNames.MASTER_REFERENCE };
        s_p_m_r_PropsMap[PropNames.MASTER_REFERENCE] = 0;
    }

    /** See {@link #sparsePropsMap sparsePropsMap}. */
    protected static final int[] r_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    protected static final int[] r_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    public static final int r_p_m_r_numProps;

    static {
        // Collect the sets of properties that apply
        BitSet propsets = new BitSet();
        propsets.set(PropNames.MASTER_REFERENCE);
        propsets.set(PropNames.MAXIMUM_REPEATS);

        // Map these properties into sparsePropsSet
        // sparsePropsSet is a HashMap containing the indicies of the
        // sparsePropsSet array, indexed by the FO index of the FO slot
        // in sparsePropsSet.
        r_p_m_r_PropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(r_p_m_r_PropsMap, -1);
        r_p_m_r_numProps = propsets.cardinality();
        r_p_m_r_Indices = new int[r_p_m_r_numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            r_p_m_r_Indices[propx] = next;
            r_p_m_r_PropsMap[next] = propx++;
        }
    }

    /** See {@link #sparsePropsMap sparsePropsMap}.  */
    protected static final int[] r_p_m_a_PropsMap;

    /** See {@link #sparseIndices sparseIndices}.  */
    protected static final int[] r_p_m_a_Indices;

    /** See {@link #numProps numProps}.  */
    protected static final int r_p_m_a_numProps;

    static {
        r_p_m_a_PropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(r_p_m_a_PropsMap, -1);
        r_p_m_a_numProps = 1;
        r_p_m_a_Indices = new int[] { PropNames.MAXIMUM_REPEATS };
        r_p_m_a_PropsMap[PropNames.MAXIMUM_REPEATS] = 0;
    }

    /** See {@link #sparsePropsMap sparsePropsMap}. */
    protected static final int[] c_p_m_r_PropsMap;

    /** See {@link #sparseIndices sparseIndices}. */
    protected static final int[] c_p_m_r_Indices;

    /** See {@link #numProps numProps}. */
    public static final int c_p_m_r_numProps;

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
        c_p_m_r_PropsMap = new int[PropNames.LAST_PROPERTY_INDEX + 1];
        Arrays.fill(c_p_m_r_PropsMap, -1);
        c_p_m_r_numProps = propsets.cardinality();
        c_p_m_r_Indices = new int[c_p_m_r_numProps];
        int propx = 0;
        for (int next = propsets.nextSetBit(0);
                next >= 0;
                next = propsets.nextSetBit(next + 1)) {
            c_p_m_r_Indices[propx] = next;
            c_p_m_r_PropsMap[next] = propx++;
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

    public FoPageSequenceMaster(FOTree foTree, FONode parent, XmlEvent event)
        throws TreeException, FOPException, PropertyException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE_MASTER, parent, event,
              FONode.SEQ_MASTER_SET, sparsePropsMap, sparseIndices);
        // Process sequence members here
        try {
            do {
                XmlEvent ev = xmlevents.expectStartElement
                    (singleOrRepeatableMasterRefs, XmlEvent.DISCARD_W_SPACE);
                if (ev == null) break;  // page-sequence-masters exhausted
                int foType = ev.getFoType();
                if (foType == FObjectNames.SINGLE_PAGE_MASTER_REFERENCE) {
                    new FoSinglePageMasterReference(foTree, this, ev);
                } else if (foType ==
                            FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE) {
                    new FoRepeatablePageMasterReference(foTree, this, ev);
                } else if (foType ==
                        FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES) {
                    new FoRepeatablePageMasterAlternatives(foTree, this, ev);
                } else
                    throw new FOPException
                            ("Aargh! expectStartElement(events, list)");
                ev = xmlevents.getEndElement
                                    (XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
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
                            (FOTree foTree, FONode parent, XmlEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.SINGLE_PAGE_MASTER_REFERENCE, parent,
                    event, FONode.SEQ_MASTER_SET, s_p_m_r_PropsMap,
                    s_p_m_r_Indices);
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
                            (FOTree foTree, FONode parent, XmlEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE,
                    parent, event, FONode.SEQ_MASTER_SET,
                    r_p_m_r_PropsMap, r_p_m_r_Indices);
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
                            (FOTree foTree, FONode parent, XmlEvent event)
            throws TreeException, FOPException, PropertyException
        {
            super(foTree, FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES,
                    parent, event, FONode.SEQ_MASTER_SET,
                    r_p_m_a_PropsMap, r_p_m_a_Indices);

            // Process conditional-page-master-references here
            try {
                do {
                    XmlEvent ev = this.xmlevents.expectStartElement
                            (FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE,
                                                    XmlEvent.DISCARD_W_SPACE);
                    if (ev == null) break; // Sub-sequences exhausted
                    new FoConditionalPageMasterReference(foTree, this, ev);
                    ev = this.xmlevents.getEndElement
                                    (XmlEventReader.DISCARD_EV, ev);
                    this.namespaces.relinquishEvent(ev);
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
                            (FOTree foTree, FONode parent, XmlEvent event)
            throws TreeException, FOPException, PropertyException
            {
                super(foTree, FObjectNames.CONDITIONAL_PAGE_MASTER_REFERENCE,
                        parent, event, FONode.SEQ_MASTER_SET,
                        c_p_m_r_PropsMap, c_p_m_r_Indices);
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
