/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fo.pagination;

// java
import java.util.List;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeBuilderContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.destination.Destination;
import org.apache.fop.fo.pagination.bookmarks.BookmarkTree;

/**
 * Class modeling the <a href="http://www.w3.org/TR/xsl/#fo_root">
 * <code>fo:root</code></a> formatting object.
 * Contains page masters, page-sequences.
 */
public class Root extends FObj {
    // The value of properties relevant for fo:root.
    private int mediaUsage;
    // End of property values

    private LayoutMasterSet layoutMasterSet;
    private Declarations declarations;
    private BookmarkTree bookmarkTree = null;
    private List destinationList;
    private List pageSequences;

    // temporary until above list populated
    private boolean pageSequenceFound = false;

    /**
     * Keeps count of page number from over PageSequence instances
     */
    private int endingPageNumberOfPreviousSequence = 0;
    private int totalPagesGenerated = 0;

    /**
     * Context class used while building the FO tree.
     */
    private FOTreeBuilderContext builderContext;

    /**
     * FOEventHandler object for this FO Tree
     */
    private FOEventHandler foEventHandler = null;

    /**
     * Base constructor
     *
     * @param parent {@link FONode} that is the parent of this object
     * Note: parent should be null for the fo:root.
     */
    public Root(FONode parent) {
        super(parent);
        pageSequences = new java.util.ArrayList();
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        mediaUsage = pList.get(PR_MEDIA_USAGE).getEnum();
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (!pageSequenceFound || layoutMasterSet == null) {
            missingChildElementError("(layout-master-set, declarations?, "
                + "bookmark-tree?, (page-sequence|fox:external-document)+)");
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL 1.0 Spec: (layout-master-set,declarations?,page-sequence+)
     * <br>FOP: (layout-master-set, declarations?, fox:bookmarks?, page-sequence+)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
        throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("layout-master-set")) {
                if (layoutMasterSet != null) {
                    tooManyNodesError(loc, "fo:layout-master-set");
                }
            } else if (localName.equals("declarations")) {
                if (layoutMasterSet == null) {
                    nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:declarations");
                } else if (declarations != null) {
                    tooManyNodesError(loc, "fo:declarations");
                } else if (bookmarkTree != null) {
                    nodesOutOfOrderError(loc, "fo:declarations", "fo:bookmark-tree");
                } else if (pageSequenceFound) {
                    nodesOutOfOrderError(loc, "fo:declarations", "fo:page-sequence");
                }
            } else if (localName.equals("bookmark-tree")) {
                if (layoutMasterSet == null) {
                    nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:bookmark-tree");
                } else if (bookmarkTree != null) {
                    tooManyNodesError(loc, "fo:bookmark-tree");
                } else if (pageSequenceFound) {
                    nodesOutOfOrderError(loc, "fo:bookmark-tree", "fo:page-sequence");
                }
            } else if (localName.equals("page-sequence")) {
                if (layoutMasterSet == null) {
                    nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:page-sequence");
                } else {
                    pageSequenceFound = true;
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        } else {
            if (FOX_URI.equals(nsURI)) {
                if ("external-document".equals(localName)) {
                    pageSequenceFound = true;
                }
            }
            //invalidChildError(loc, nsURI, localName);
            //Ignore non-FO elements under root
        }
    }


    /** @inheritDoc */
    protected void validateChildNode(Locator loc, FONode child) throws ValidationException {
        if (child instanceof AbstractPageSequence) {
            pageSequenceFound = true;
        }
    }

    /**
     * Sets the FOEventHandler object that this Root is attached to
     * @param foEventHandler the FOEventHandler object
     */
    public void setFOEventHandler(FOEventHandler foEventHandler) {
        this.foEventHandler = foEventHandler;
    }

    /**
     * This method overrides the FONode version. The FONode version calls the
     * method by the same name for the parent object. Since Root is at the top
     * of the tree, it returns the actual FOEventHandler object. Thus, any FONode
     * can use this chain to find which FOEventHandler it is being built for.
     * @return the FOEventHandler implementation that this Root is attached to
     */
    public FOEventHandler getFOEventHandler() {
        return foEventHandler;
    }

    /**
     * Sets the builder context for this FO tree.
     * @param context the builder context to be used
     */
    public void setBuilderContext(FOTreeBuilderContext context) {
        this.builderContext = context;
    }

    /** {@inheritDoc} */
    public FOTreeBuilderContext getBuilderContext() {
        return this.builderContext;
    }

    /**
    * Gets the last page number generated by the previous page-sequence
    * @return the last page number, 0 if no page sequences yet generated
    */
    public int getEndingPageNumberOfPreviousSequence() {
        return endingPageNumberOfPreviousSequence;
    }

    /**
     * Returns the total number of pages generated by FOP
     * (May not equal endingPageNumberOfPreviousSequence due to
     * initial-page-number property on fo:page-sequences.)
     * @return the last page number, 0 if no page sequences yet generated
     */
    public int getTotalPagesGenerated() {
        return totalPagesGenerated;
    }

    /**
     * Notify additional pages generated to increase the totalPagesGenerated counter
     * @param lastPageNumber the last page number generated by the sequence
     * @param additionalPages the total pages generated by the sequence (for statistics)
     * @throws IllegalArgumentException for negative additional page counts
     */
    public void notifyPageSequenceFinished(int lastPageNumber, int additionalPages) {

        if (additionalPages >= 0) {
            totalPagesGenerated += additionalPages;
            endingPageNumberOfPreviousSequence = lastPageNumber;
        } else {
            throw new IllegalArgumentException(
                "Number of additional pages must be zero or greater.");
        }
    }

    /**
     * Returns the number of PageSequence instances.
     * @return the number of PageSequence instances
     */
    public int getPageSequenceCount() {
        return pageSequences.size();
    }

    /**
     * Some properties, such as 'force-page-count', require a
     * page-sequence to know about some properties of the next.
     * @param current the current PageSequence
     * @return succeeding PageSequence; null if none
     */
    public PageSequence getSucceedingPageSequence(PageSequence current) {
        int currentIndex = pageSequences.indexOf(current);
        if (currentIndex == -1) {
            return null;
        }
        if (currentIndex < (pageSequences.size() - 1)) {
            return (PageSequence)pageSequences.get(currentIndex + 1);
        } else {
            return null;
        }
    }

    /**
     * Returns the associated LayoutMasterSet.
     * @return the LayoutMasterSet instance
     */
    public LayoutMasterSet getLayoutMasterSet() {
        return this.layoutMasterSet;
    }

    /**
     * Sets the associated LayoutMasterSet.
     * @param layoutMasterSet the LayoutMasterSet to use
     */
    public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
        this.layoutMasterSet = layoutMasterSet;
    }

    /**
     * Returns the associated Declarations.
     * @return the Declarations instance
     */
    public Declarations getDeclarations() {
        return this.declarations;
    }

    /**
     * Sets the associated Declarations.
     * @param declarations the Declarations to use
     */
    public void setDeclarations(Declarations declarations) {
        this.declarations = declarations;
    }

    /**
     * Set the BookmarkTree object for this FO
     * @param bookmarkTree the BookmarkTree object
     */
    public void setBookmarkTree(BookmarkTree bookmarkTree) {
        this.bookmarkTree = bookmarkTree;
    }

    /**
     * Add a Destination object to this FO
     * @param destination the Destination object to add
     */
    public void addDestination(Destination destination) {
        if (destinationList == null) {
          destinationList = new java.util.ArrayList();
        }
        destinationList.add(destination);
    }

    /**
     * Public accessor for the list of Destination objects for this FO
     * @return the Destination object
     */
    public List getDestinationList() {
        return destinationList;
    }

    /**
     * Public accessor for the BookmarkTree object for this FO
     * @return the BookmarkTree object
     */
    public BookmarkTree getBookmarkTree() {
        return bookmarkTree;
    }

    /** {@inheritDoc} */
    public Root getRoot() {
        return this;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "root";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_ROOT}
     */
    public int getNameId() {
        return FO_ROOT;
    }

}
