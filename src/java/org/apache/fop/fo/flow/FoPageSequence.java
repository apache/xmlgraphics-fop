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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.area.Page;
import org.apache.fop.area.PageList;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPageSeqNode;
import org.apache.fop.fo.FOTree;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.FoRoot;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.InitialPageNumber;
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
    /**
     * Private map of <code>List</code>s of static-content subtrees hashed on
     * flow-name.  Note that there is no restriction on multiple
     * <code>fo:static-content</code> elements being assigned to a single
     * <code>flow-name</code>, so the objects keyed by <code>flow-name</code>
     * must be able to hold more than one element; hence <code>List</code>s.
     * 
     * Each element of the <code>HashMap</code>, keyed on the
     * <code>flow-name</code> is an <code>ArrayList</code> containing one or
     * more instances of <code>FoStaticContent</code>.
     */
    private HashMap staticSubtrees = null;
    /** Unmodifiable version of <code>staticSubtrees</code> */
    public Map staticContents = null;
    /** Child index of fo:flow child. */
    private int flowChild = -1;
    /** The page currently being processed by this page-sequence */
    private Page page = null;
    
    /**
     * Gets the current page of this page-sequence
     * @return the page
     */
    public Page getPage() {
        return page;
    }

    /** The <code>PageList</code> containing the flattened
     * <code>pageTree</code> for this page-sequence.  This PageList contains
     * only <code>Page</code> elements. */
    private ArrayList pageArray = new ArrayList();
    /** The index of the current element in the pageList */
    private int pgListIndex = -1;

    /** The tree of all layout attempts for this page-sequence */
    private PageList pageList = null;
    /** An array of indicies mapping the path through the
     * <code>pageTree</code> to the current element  */
    private ArrayList pageTreeMap = null;

    /**
     * @return the pageList
     */
    public PageList getPageList() {
        return pageList;
    }
    /**
     * @param pageList to set
     */
    public void setPagelist(PageList pageList) {
        this.pageList = pageList;
    }
    /**
     * @return the pgListIndex
     */
    public int getPgListIndex() {
        return pgListIndex;
    }
    /**
     * @param pgListIndex to set
     */
    public void setPgListIndex(int pgListIndex) {
        this.pgListIndex = pgListIndex;
    }

    public Page getCurr1stPage() {
        if (pageArray == null) {
            return null;
        }
        return (Page)(pageArray.get(0));
    }

    /**
     * The number of the page being laid out
     */
    private int currPageNumber = 0;

    private FoRoot root;

    private void getInitialPageNumber() {
        PropertyValue pv;
        try {
            pv = getPropertyValue(PropNames.INITIAL_PAGE_NUMBER);
        } catch (PropertyException e) {
            throw new RuntimeException(
                    "Unable to obtain InitialPageNumber value");
        }
        int i = 0;
        int lastnum = root.getLastPageNumber();
        switch (pv.getType()) {
        case PropertyValue.AUTO:
            currPageNumber = lastnum + 1;
            break;
        case PropertyValue.ENUM:
            i = ((EnumType)pv).getEnumValue();
            switch (i) {
            case InitialPageNumber.AUTO_ODD:
                currPageNumber = 
                    ((lastnum % 2 == 0) ? lastnum + 1 : lastnum + 2);
                break;
            case InitialPageNumber.AUTO_EVEN:
                currPageNumber = 
                    ((lastnum % 2 == 0) ? lastnum + 2 : lastnum + 1);
                break;
            default:
                throw new RuntimeException(
                        "Unknown InitialPageNumber enum value: " + i);
            }
        case PropertyValue.INTEGER:
            i = ((IntegerType)pv).getInt();
            if (i < 0) {
                currPageNumber = 1;
            } else {
                currPageNumber = i;
            }
            break;
        case PropertyValue.NUMERIC:
            i = ((Numeric)pv).asInt();
            if (i < 0) {
                currPageNumber = 1;
            } else {
                currPageNumber = i;
            }
            break;
        default:
            throw new RuntimeException("Invalid property value type "
                    + PropertyValue.propertyTypes.get(pv.getType()));
        }
    }

    /** Maps flownames to fo:flow and fo:static-content objects */
    private HashMap flowMap = new HashMap(10);
    /**
     * Maps a flow name to a <code>FoFlow</code> or <code>FoStaticContent</code>
     * object.
     * @param flowname the name of the flow
     * @param flow the flow or static-content object
     * @throws FOPException if object that the name is being mapped to is
     * not a flow or static-content
     */
    public void mapFlowName(String flowname, FOPageSeqNode flow)
    throws FOPException {
        synchronized (flowMap) {
            if ( ! (flow.type == FObjectNames.FLOW
                    || flow.type == FObjectNames.STATIC_CONTENT)) {
                throw new FOPException(
                "Only fo:flow or fo:static-content allowed in flowmap");
            }
            flowMap.put(flowname, flow);
        }
    }


    /**
     * @param flowname
     * @return
     */
    public FOPageSeqNode unmapFlowName(String flowname) {
        synchronized (flowMap) {
            return (FOPageSeqNode)(flowMap.get(flowname));
        }
    }
    /** An image on which to draw areas */
    private BufferedImage pageSpread = null;
    /**
     * Gets the page spread image from which the <code>Graphics2D</code> and
     * <code>FontRenderContext</code> have been derived.
     * @return the page spread
     */
    public BufferedImage getPageSpread() {
        return pageSpread;
    }

    /**
     * @param foTree the FO tree being built
     * @param parent the parent FONode of this node
     * @param event the <tt>XmlEvent</tt> that triggered the creation of
     * this node
     * @param pageSeqMasters a <code>Map</code> of the page sequence masters
     * from the layout master set
     */
    public FoPageSequence(FOTree foTree, FONode parent, FoXmlEvent event,
            Map pageSeqMasters)
        throws TreeException, FOPException
    {
        super(foTree, FObjectNames.PAGE_SEQUENCE, parent, event,
              FONode.PAGESEQ_SET, sparsePropsMap, sparseIndices);
        root = (FoRoot)parent;
        // Set up the graphics environment
        pageSpread =
            new BufferedImage(20*72, 12*72, BufferedImage.TYPE_INT_RGB);

        XmlEvent ev;
        // Look for optional title
        log.finer("page-sequence title");
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

            // Look for zero or more static-content subtrees
            log.finer("static-content");
            nowProcessing = "static-content";
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.STATIC_CONTENT, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:static-content
                if (firstStaticContent == -1) {
                    firstStaticContent = numChildren();
                    staticSubtrees = new HashMap();
                }
                FoStaticContent statContent  =
                    new FoStaticContent(getFOTree(), this, (FoXmlEvent)ev);
                namespaces.relinquishEvent(ev);
                // Collect the static-content subtrees for this page-sequence
                String flowname = statContent.getFlowName();
                if (! staticSubtrees.containsKey(flowname)) {
                    // Create a new list for this key
                    staticSubtrees.put(flowname, new ArrayList(1));
                }
                // Add an entry to an existing List
                ArrayList statconsList =
                    (ArrayList)(staticSubtrees.get(flowname));
                statconsList.add(statContent);
            }
            // Create the unmodifiable map of unmodifiable lists
            // TODO make the contents of the events buffer unmodifiable
            // Each value in the Map is an ArrayList.  Iterate over all of the
            // entries, replacing the ArrayList value in each Map.Entry with an
            // unmodifiableList constructed from the ArrayList
            if (staticSubtrees != null) {
                Set entries = staticSubtrees.entrySet();
                Iterator iter = entries.iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)(iter.next());
                    entry.setValue(
                            Collections.unmodifiableList(
                                    (List)(entry.getValue())));
                }
                // Now make an unmodifiableMap from the overall HashMap
                // of flow-name indexed ArrayLists
                staticContents = Collections.unmodifiableMap(staticSubtrees);
            }
            // Generate a null page for the flow(s)
            page = Page.setupNullPage(this, foTree.getNextPageId());
            // Intialize the PageList for this page-sequence
            pageList = new PageList(page);
            pgListIndex = 0;

            // Look for one or more fo:flow
            // must have at least one: N.B. in 1.0, only one is allowed,
            // but in 1.1. multiple flows are allowed with different 
            // flow maps
            log.finer("flow");
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
                // Loop over remaining fo:flow elements
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

    public Area getReferenceRectangle() throws FOPException {
        // TODO Reference rectangle is assumed to be equivalent to the
        // "auto" value on "page-height" and "page-width".  The
        // inline-progression-dimension and block-progression-dimension are
        // calculated according to the computed values of the
        // reference-orientation and writing-mode of the FO for which the
        // percentage is calculated.  See
        // 7.3 Reference Rectangle for Percentage Computations
        throw new FOPException("Called from FoPageSequence");
    }

}
