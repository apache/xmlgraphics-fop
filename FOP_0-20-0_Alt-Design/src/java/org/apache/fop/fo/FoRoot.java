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

package org.apache.fop.fo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.Area;
import org.apache.fop.datastructs.TreeException;
import org.apache.fop.fo.declarations.FoDeclarations;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.pagination.FoLayoutMasterSet;
import org.apache.fop.xml.FoXmlEvent;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.Namespaces;
import org.apache.fop.xml.XmlEventReader;

/**
 * <tt>FoRoot</tt> is the class which processes the fo:root start element
 * XML event.
 * The building of all of the fo tree, and the forwarding of FO tree events
 * on to further stages of processing, will all take place within the
 * <tt>buildFoTree()</tt> method of this class instance.
 * 
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */

public class FoRoot extends FONode {

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
        sparseIndices = new int[] { PropNames.MEDIA_USAGE };
        sparsePropsMap
                [PropNames.MEDIA_USAGE] = 0;
    }

    /** Offset of declarations child node. */
    private int declarations = -1;

    /** Offset of first page-sequence child node. */
    private int firstPageSeq = -1;

    /** The page number of the last laid-out page */
    private int lastPageNumber = 0;
    /**
     * Gets the last laid-out page number.  This is generally set by the
     * page-sequence.
     * @return the page number
     */
    public int getLastPageNumber() {
        return lastPageNumber;
    }
    /**
     * Sets the last generated page number.  This is generally set from the
     * page-sequence processing.
     * @param number the last generated number
     */
    public void setLastPageNumber(int number) {
        lastPageNumber = number; 
    }

    /**
     * @param foTree the FO tree being built
     * @param event the <tt>XmlEvent</tt> that triggered the creation of this
     * node
     */
    public FoRoot
        (FOTree foTree, XmlEvent event)
        throws TreeException, FOPException, PropertyException
    {
        // This is the root node of the tree; hence the null argument
        super(foTree, FObjectNames.ROOT, null, event, FONode.ROOT_SET,
                sparsePropsMap, sparseIndices);
    }

    /**
     * Process the FO tree, starting with this fo:root element.
     * N.B. the FO tree is a collection of trees.
     * Layout trees only occur with fo:flow and fo:static-content.  These will
     * be built at the appropriate places as part of the FO tree processing.
     * Terminates at the completion of FO tree processing.
     * <p>
     * <tt>fo:root</tt> contents are <br/>
     * (layout-master-set,declarations?,page-sequence+)
     * <p>
     * I.e. the <fo:root> element is the parent of a two-element sequence:
     * (layout-master-set-declarations),(page-sequence-sequence)
     * <p>
     * 'layout-master-set-declarations' is logically an unordered set of
     * (layout-master-set,declarations?), although this definition
     * determines an order of occurrence in the input tree.  It is 
     * unordered in the sense that there is no necessary order in the
     * set.  However, all of the elements of the page-sequence-sequence
     * are ordered.
     * <p>The contents of declarations must be available to all FO tree
     * processing; the contents of the layout-master-set must be available
     * during the page setup phase of the processing of each page-sequence
     * in the page-sequence-sequence.
     */
    public void buildFoTree() throws FOPException{
        XmlEvent ev;
        String nowProcessing;
        log.fine("buildFoTree");
        log.finer("layout-master-set");
        nowProcessing = "layout-master-set";
        try {
            // Look for layout-master-set.  Must be one.
            ev = xmlevents.expectStartElement
                (FObjectNames.LAYOUT_MASTER_SET, XmlEvent.DISCARD_W_SPACE);
            // Process the layout-master-set
            FoLayoutMasterSet layoutMasters =
                                new FoLayoutMasterSet(getFOTree(), this, ev);
            // Clean up the fo:layout-master-set event
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            layoutMasters.deleteSubTree();

            // Look for optional declarations
            log.finer("declarations");
            nowProcessing = "declarations";
            ev = xmlevents.expectStartElement
                        (FObjectNames.DECLARATIONS, XmlEvent.DISCARD_W_SPACE);
            if (ev != null) {
                // process the declarations
                declarations = numChildren();
                new FoDeclarations(getFOTree(), this, ev);
                ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
                namespaces.relinquishEvent(ev);
            }

            // Process page-sequences here
            // must have at least one
            log.finer("page-sequence");
            nowProcessing = "page-sequence";
            ev = xmlevents.expectStartElement
                        (FObjectNames.PAGE_SEQUENCE, XmlEvent.DISCARD_W_SPACE);
            if (ev == null)
                throw new FOPException("No page-sequence found.");
            firstPageSeq = numChildren();
            new FoPageSequence(getFOTree(), this, (FoXmlEvent)ev,
                    layoutMasters.getPageSequenceMasters());
            ev = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, ev);
            namespaces.relinquishEvent(ev);
            while ((ev = xmlevents.expectStartElement
                    (FObjectNames.PAGE_SEQUENCE, XmlEvent.DISCARD_W_SPACE))
                   != null) {
                // Loop over remaining fo:page-sequences
                new FoPageSequence(getFOTree(), this, (FoXmlEvent)ev,
                        layoutMasters.getPageSequenceMasters());
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
        // Clean up root's FO tree build environment
        makeSparsePropsSet();
        // Provide some stats
        for (int i = 0; i <= Namespaces.LAST_NS_INDEX; i++) {
            log.info("Namespace " + namespaces.getIndexURI(i));
            log.info("Size of event pool: " + namespaces.getNSPoolSize(i));
            log.info("Next event id     : " + namespaces.getSequenceValue(i));
        }
    }

    public Area getReferenceRectangle() throws FOPException {
        // TODO Reference rectangle is assumed to be equivalent to the
        // "auto" value on "page-height" and "page-width".  The
        // inline-progression-dimension and block-progression-dimension are
        // calculated according to the computed values of the
        // reference-orientation and writing-mode of the FO for which the
        // percentage is calculated.  See
        // 7.3 Reference Rectangle for Percentage Computations
        throw new FOPException("Called from FoRoot");
    }

}
