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
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
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
import org.apache.fop.area.PageListElement;
import org.apache.fop.area.PageSet;
import org.apache.fop.area.PageSetElement;
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
    /** The <code>PageList</code> for this page-sequence */
    private PageList pagelist = null;
    /** The index of the current element in the pagelist */
    private int pgListIndex = -1;
    /** The page currently being processed by this page-sequence */
    private Page page = null;
    /**
     * Gets the current page of this page-sequence
     * @return the page
     */
    public Page getPage() {
        return page;
    }

    /**
     * @return the pagelist
     */
    public PageList getPagelist() {
        return pagelist;
    }
    /**
     * @param pagelist to set
     */
    public void setPagelist(PageList pagelist) {
        this.pagelist = pagelist;
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

    /**
     * Gets the current first page
     * @return the first page
     */
    public Page getCurr1stPage() {
        PageListElement firstPage = pagelist.get(0);
        while (firstPage.isPageSet()) {
            PageSet pageset = (PageSet)firstPage;
            PageSetElement setEl = pageset.get(pageset.getCurrentElement());
            if (setEl.isPageList()) {
                firstPage = ((PageList)setEl).get(0);
            } else {
                firstPage = (Page)setEl;
            }
        }
        return (Page)firstPage;
    }
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
        // Set up the graphics environment
        pageSpread =
            new BufferedImage(20*72, 12*72, BufferedImage.TYPE_INT_RGB);
        g2D = pageSpread.createGraphics();
        frcontext = g2D.getFontRenderContext();

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
            pagelist = new PageList(page);
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

    private GraphicsEnvironment gEnv = null;
    public GraphicsEnvironment getGraphicsEnvironment() {
        return gEnv;
    }
    private BufferedImage pageSpread = null;
    public BufferedImage getPageSpread() {
        return pageSpread;
    }
    private Graphics2D g2D = null;
    public Graphics2D getGraphics2D() {
        return g2D;
    }
    private FontRenderContext frcontext = null;
    public FontRenderContext getFontRenderContext() {
        return frcontext;
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
