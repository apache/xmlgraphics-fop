/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.apps;

// Java
import java.util.Map;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;


// FOP
import org.apache.fop.apps.FOUserAgent;

import org.apache.fop.area.AreaTree;
import org.apache.fop.area.AreaTreeControl;
import org.apache.fop.area.AreaTreeModel;

import org.apache.fop.fo.extensions.Bookmarks;
import org.apache.fop.fo.FOInputHandler;
import org.apache.fop.fo.FOTreeControl;
import org.apache.fop.fo.FOTreeEvent;
import org.apache.fop.fo.FOTreeListener;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.layout.LayoutStrategy;

import org.apache.commons.logging.Log;

// SAX
import org.xml.sax.SAXException;

/**
 * Class storing information for the FOP Document being processed, and managing
 * the processing of it.
 */
public class Document implements FOTreeControl, FOTreeListener,
        AreaTreeControl {
            
    /** The parent Driver object */
    private Driver driver;

    /** The Font information relevant for this document */
    private FontInfo fontInfo;
    
    /**
     * the LayoutStrategy to be used to process this document
     * TODO: this actually belongs in the RenderContext class, when it is
     * created
     */
    private LayoutStrategy layoutStrategy = null;

    /** The current AreaTree for the PageSequence being rendered. */
    public AreaTree areaTree;

    /** The AreaTreeModel for the PageSequence being rendered. */
    public AreaTreeModel atModel;

    private Bookmarks bookmarks = null;

    /**
     * The current set of id's in the FO tree.
     * This is used so we know if the FO tree contains duplicates.
     */
    private Set idReferences = new HashSet();

    /**
     * Structure handler used to notify structure events
     * such as start end element.
     */
    public FOInputHandler foInputHandler;

    /**
     * Main constructor
     * @param driver the Driver object that is the "parent" of this Document
     */
    public Document(Driver driver) {
        this.driver = driver;
        this.fontInfo = new FontInfo();
    }

    /**
     * Retrieve the font information for this document
     * @return the FontInfo instance for this document
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * Set the LayoutStrategy to be used to process this Document
     * @param ls the LayoutStrategy object to be used to process this Document
     */
    public void setLayoutStrategy(LayoutStrategy ls) {
        this.layoutStrategy = ls;
    }

    /**
     * @return this Document's LayoutStrategy object
     */
    public LayoutStrategy getLayoutStrategy () {
        return layoutStrategy;
    }

    /**
     * Public accessor for the parent Driver of this Document
     * @return the parent Driver for this Document
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * Required by the FOTreeListener interface. It handles an
     * FOTreeEvent that is fired when a PageSequence object has been completed.
     * @param event the FOTreeEvent that was fired
     * @throws FOPException for errors in building the PageSequence
     */
    public void foPageSequenceComplete (FOTreeEvent event) throws FOPException {
        PageSequence pageSeq = event.getPageSequence();
        areaTree.addBookmarksToAreaTree();
        layoutStrategy.format(pageSeq, areaTree);
    }

    /**
     * Required by the FOTreeListener interface. It handles an FOTreeEvent that
     * is fired when the Document has been completely parsed.
     * @param event the FOTreeEvent that was fired
     * @throws SAXException for parsing errors
     */
    public void foDocumentComplete (FOTreeEvent event) throws SAXException {
        //processAreaTree(atModel);
        try {
            areaTree.endDocument();
            driver.getRenderer().stopRenderer();
        } catch (IOException ex) {
            throw new SAXException(ex);
        }
    }

    /**
     * Get the area tree for this layout handler.
     *
     * @return the area tree for this document
     */
    public AreaTree getAreaTree() {
        return areaTree;
    }

    /**
     * Set the Bookmarks object for this Document
     * @param bookmarks the Bookmarks object containing the bookmarks for this
     * Document
     */
    public void setBookmarks(Bookmarks bookmarks) {
        this.bookmarks = bookmarks;
    }

    /**
     * Public accessor for the Bookmarks for this Document
     * @return the Bookmarks for this Document
     */
    public Bookmarks getBookmarks() {
        return bookmarks;
    }

    /**
     * Retuns the set of ID references.
     * @return the ID references
     */
    public Set getIDReferences() {
        return idReferences;
    }

    /**
     * @return the FOInputHandler for parsing this FO Tree
     */
    public FOInputHandler getFOInputHandler() {
        return foInputHandler;
    }

}
