/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// Java
import java.util.HashSet;

import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.FontInfo;

import org.xml.sax.SAXException;

/**
 * This class receives structure events from the FO Tree.
 * Sub-classes can then implement various methods to handle
 * the FO Tree when the SAX events occur.
 */
public class StructureHandler {
    /**
       The current set of id's in the FO tree
       This is used so we know if the FO tree contains duplicates
     */
    private HashSet idReferences = new HashSet();
    protected Logger log;

    public StructureHandler() {
    }

    public void setLogger(Logger logger) {
        log = logger;
    }

    public HashSet getIDReferences() {
        return idReferences;
    }

    public FontInfo getFontInfo() {
        return null;
    }

    public void startDocument() throws SAXException {

    }

    public void endDocument() throws SAXException {

    }

    public void startPageSequence() {

    }

    public void endPageSequence(PageSequence pageSeq) throws FOPException {

    }

    public void setPageInfo() {

    }

    public void startBlock() {

    }

    public void endBlock() {

    }


    // Tables
    public void startTable() {

    }

    public void endTable() {

    }

    public void startHeader() {

    }

    public void endHeader() {

    }

    public void startFooter() {

    }

    public void endFooter() {

    }

    public void startBody() {

    }

    public void endBody() {

    }

    public void startRow() {

    }

    public void endRow() {

    }

    public void startCell() {

    }

    public void endCell() {

    }


    // Lists
    public void startList() {

    }

    public void endList() {

    }

    public void startListItem() {

    }

    public void endListItem() {

    }

    public void startListLabel() {

    }

    public void endListLabel() {

    }

    public void startListBody() {

    }

    public void endListBody() {

    }


    // Static Regions
    public void startStatic() {

    }

    public void endStatic() {

    }


    public void startMarkup() {

    }

    public void endMarkup() {

    }


    public void startLink() {

    }

    public void endLink() {

    }


    public void image() {

    }

    public void pageRef() {

    }

    public void foreignObject() {

    }

    public void footnote() {

    }

    public void leader() {

    }


    public void characters() {

    }

    public void pageBreak() {

    }


}

