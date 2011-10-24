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

/* $Id $ */

package org.apache.fop.fo.pagination.bookmarks;

import java.util.ArrayList;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;


/**
 * Class modelling the <a href="http://www.w3.org/TR/xsl/#fo_bookmark">
 * <code>fo:bookmark</code></a> object, first introduced in the
 * XSL 1.1 WD.
 */
public class Bookmark extends FObj implements CommonAccessibilityHolder {
    private BookmarkTitle bookmarkTitle;
    private ArrayList childBookmarks = new ArrayList();

    // The value of properties relevant for this FO
    private CommonAccessibility commonAccessibility;
    private String internalDestination;
    private String externalDestination;
    private boolean bShow = true; // from starting-state property

    // Valid, but unused properties. Commented out for performance
    // private CommonAccessibility commonAccessibility;


    /**
     * Create a new Bookmark object that is a child of the
     * given {@link FONode}.
     *
     * @param parent the parent fo node
     */
    public Bookmark(FONode parent) {
        super(parent);
    }

    /** {@inheritDoc} */
    public void bind(PropertyList pList) throws FOPException {
        commonAccessibility = CommonAccessibility.getInstance(pList);
        externalDestination = pList.get(PR_EXTERNAL_DESTINATION).getString();
        internalDestination = pList.get(PR_INTERNAL_DESTINATION).getString();
        bShow = (pList.get(PR_STARTING_STATE).getEnum() == EN_SHOW);

        // per spec, internal takes precedence if both specified
        if (internalDestination.length() > 0) {
            externalDestination = null;
        } else if (externalDestination.length() == 0) {
            // slightly stronger than spec "should be specified"
            getFOValidationEventProducer().missingLinkDestination(this, getName(), locator);
        } else {
            getFOValidationEventProducer().unimplementedFeature(this, getName(),
                    "external-destination", getLocator());
        }
    }

    /**
     * {@inheritDoc}
     * <br>XSL/FOP: (bookmark-title, bookmark*)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName)
                throws ValidationException {
        if (FO_URI.equals(nsURI)) {
            if (localName.equals("bookmark-title")) {
                if (bookmarkTitle != null) {
                    tooManyNodesError(loc, "fo:bookmark-title");
                }
            } else if (localName.equals("bookmark")) {
                if (bookmarkTitle == null) {
                    nodesOutOfOrderError(loc, "fo:bookmark-title", "fo:bookmark");
                }
            } else {
                invalidChildError(loc, nsURI, localName);
            }
        }
    }

    /** {@inheritDoc} */
    protected void endOfNode() throws FOPException {
        if (bookmarkTitle == null) {
           missingChildElementError("(bookmark-title, bookmark*)");
        }
    }

    /** {@inheritDoc} */
    protected void addChildNode(FONode obj) {
        if (obj instanceof BookmarkTitle) {
            bookmarkTitle = (BookmarkTitle)obj;
        } else if (obj instanceof Bookmark) {
            childBookmarks.add(obj);
        }
    }

    public CommonAccessibility getCommonAccessibility() {
        return commonAccessibility;
    }

    /**
     * Get the bookmark title for this bookmark
     *
     * @return the bookmark title string or an empty string if not found
     */
    public String getBookmarkTitle() {
        return bookmarkTitle == null ? "" : bookmarkTitle.getTitle();
    }

    /**
     * Returns the value of the internal-destination property.
     * @return the internal-destination
     */
    public String getInternalDestination() {
        return internalDestination;
    }

    /**
     * Returns the value of the external-destination property.
     * @return the external-destination
     */
    public String getExternalDestination() {
        return externalDestination;
    }

    /**
     * Determines if this fo:bookmark's subitems should be initially displayed
     * or hidden, based on the starting-state property set on this FO.
     *
     * @return true if this bookmark's starting-state is "show", false if "hide".
     */
    public boolean showChildItems() {
        return bShow;
    }

    /**
     * Get the child <code>Bookmark</code>s in an <code>java.util.ArrayList</code>.
     * @return an <code>ArrayList</code> containing the child Bookmarks
     */
    public ArrayList getChildBookmarks() {
        return childBookmarks;
    }

    /** {@inheritDoc} */
    public String getLocalName() {
        return "bookmark";
    }

    /**
     * {@inheritDoc}
     * @return {@link org.apache.fop.fo.Constants#FO_BOOKMARK}
     */
    public int getNameId() {
        return FO_BOOKMARK;
    }
}
