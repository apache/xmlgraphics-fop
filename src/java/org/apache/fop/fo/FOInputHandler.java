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

package org.apache.fop.fo;

// FOP
import org.apache.fop.apps.Document;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.area.AreaTree;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.flow.TableBody;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.commons.logging.Log;

import org.xml.sax.SAXException;

/**
 * Abstract class defining what should be done with SAX events that map to
 * XSL-FO input. The events are actually captured by fo/FOTreeBuilder, passed
 * to the various fo Objects, which in turn, if needed, pass them to an instance
 * of FOInputHandler.
 *
 * Sub-classes will generally fall into one of two categories:
 * 1) a handler that actually builds an FO Tree from the events, or 2) a
 * handler that builds a structured (as opposed to formatted) document, such
 * as our MIF and RTF output targets.
 */
public abstract class FOInputHandler {
    
    /**
     * The Document object that is controlling the FO Tree being built
     */
    public Document doc = null;

    /**
     * logging instance
     */
    protected Log logger = null;

    /**
     * Main constructor
     * @param document the apps.Document implementation that is controlling
     * the FO Tree being built
     */
    public FOInputHandler(Document document) {
        doc = document;
    }

    /**
     * Sets the Commons-Logging instance for this class
     * @param logger The Commons-Logging instance
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

    /**
     * Returns the Commons-Logging instance for this class
     * @return  The Commons-Logging instance
     */
    protected Log getLogger(Log logger) {
        return logger;
    }

    /**
     * Returns the FOTreeControl object associated with this FOInputHandler.
     * @return the FOTreeControl object
     */
    public Document getDocument() {
        return doc;
    }

    /**   
      * @return the current Area Tree object
      */   
     public AreaTree getAreaTree() {
        return doc.getAreaTree();
     } 
    
    /**
     * Returns the Driver object associated with this FOInputHandler.
     * @return the Driver object
     */
    public Driver getDriver() {
        return doc.getDriver();
    }

    /**
     * This method is called to indicate the start of a new document run.
     * @throws SAXException In case of a problem
     */
    public abstract void startDocument() throws SAXException;

    /**
     * This method is called to indicate the end of a document run.
     * @throws SAXException In case of a problem
     */
    public abstract void endDocument() throws SAXException;

    /**
     *
     * @param pageSeq PageSequence that is starting.
     */
    public abstract void startPageSequence(PageSequence pageSeq);

    /**
     *
     * @param pageSeq PageSequence that is ending.
     * @throws FOPException For errors encountered.
     */
    public abstract void endPageSequence(PageSequence pageSeq) throws FOPException;

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public abstract void startPageNumber(PageNumber pagenum);

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public abstract void endPageNumber(PageNumber pagenum);

    /**
     * This method is called to indicate the start of a new fo:flow or fo:static-content.
     * This method also handles fo:static-content tags, because the StaticContent class
     * is derived from the Flow class.
     *
     * @param fl Flow that is starting.
     */
    public abstract void startFlow(Flow fl);

    /**
     *
     * @param fl Flow that is ending.
     */
    public abstract void endFlow(Flow fl);

    /**
     *
     * @param bl Block that is starting.
     */
    public abstract void startBlock(Block bl);

    /**
     *
     * @param bl Block that is ending.
     */
    public abstract void endBlock(Block bl);

    /**
     *
     * @param inl Inline that is starting.
     */
    public abstract void startInline(Inline inl);

    /**
     *
     * @param inl Inline that is ending.
     */
    public abstract void endInline(Inline inl);

    // Tables
    /**
     *
     * @param tbl Table that is starting.
     */
    public abstract void startTable(Table tbl);

    /**
     *
     * @param tbl Table that is ending.
     */
    public abstract void endTable(Table tbl);

    /**
     *
     * @param tc TableColumn that is starting;
     */
    public abstract void startColumn(TableColumn tc);

    /**
     *
     * @param tc TableColumn that is ending;
     */
    public abstract void endColumn(TableColumn tc);

    /**
     *
     * @param th TableBody that is starting;
     */
    public abstract void startHeader(TableBody th);

    /**
     *
     * @param th TableBody that is ending.
     */
    public abstract void endHeader(TableBody th);

    /**
     *
     * @param tf TableFooter that is starting.
     */
    public abstract void startFooter(TableBody tf);

    /**
     *
     * @param tf TableFooter that is ending.
     */
    public abstract void endFooter(TableBody tf);

    /**
     *
     * @param tb TableBody that is starting.
     */
    public abstract void startBody(TableBody tb);

    /**
     *
     * @param tb TableBody that is ending.
     */
    public abstract void endBody(TableBody tb);

    /**
     *
     * @param tr TableRow that is starting.
     */
    public abstract void startRow(TableRow tr);

    /**
     *
     * @param tr TableRow that is ending.
     */
    public abstract void endRow(TableRow tr);

    /**
     *
     * @param tc TableCell that is starting.
     */
    public abstract void startCell(TableCell tc);

    /**
     *
     * @param tc TableCell that is ending.
     */
    public abstract void endCell(TableCell tc);


    // Lists
    /**
     *
     * @param lb ListBlock that is starting.
     */
    public abstract void startList(ListBlock lb);

    /**
     *
     * @param lb ListBlock that is ending.
     */
    public abstract void endList(ListBlock lb);

    /**
     *
     * @param li ListItem that is starting.
     */
    public abstract void startListItem(ListItem li);

    /**
     *
     * @param li ListItem that is ending.
     */
    public abstract void endListItem(ListItem li);

    /**
     * Process start of a ListLabel.
     */
    public abstract void startListLabel();

    /**
     * Process end of a ListLabel.
     */
    public abstract void endListLabel();

    /**
     * Process start of a ListBody.
     */
    public abstract void startListBody();

    /**
     * Process end of a ListBody.
     */
    public abstract void endListBody();

    // Static Regions
    /**
     * Process start of a Static.
     */
    public abstract void startStatic();

    /**
     * Process end of a Static.
     */
    public abstract void endStatic();

    /**
     * Process start of a Markup.
     */
    public abstract void startMarkup();

    /**
     * Process end of a Markup.
     */
    public abstract void endMarkup();

    /**
     * Process start of a Link.
     * @param basicLink BasicLink that is ending
     */
    public abstract void startLink(BasicLink basicLink);

    /**
     * Process end of a Link.
     */
    public abstract void endLink();

    /**
     * Process an ExternalGraphic.
     * @param eg ExternalGraphic to process.
     */
    public abstract void image(ExternalGraphic eg);

    /**
     * Process a pageRef.
     */
    public abstract void pageRef();

    /**
     * Process an InstreamForeignObject.
     * @param ifo InstreamForeignObject to process.
     */
    public abstract void foreignObject(InstreamForeignObject ifo);

    /**
     * Process the start of a footnote.
     * @param footnote Footnote that is starting
     */
    public abstract void startFootnote(Footnote footnote);
    
    /**
     * Process the ending of a footnote.
     * @param footnote Footnote that is ending
     */
    public abstract void endFootnote(Footnote footnote);

    /**
     * Process the start of a footnote body.
     * @param body FootnoteBody that is starting
     */
    public abstract void startFootnoteBody(FootnoteBody body);
    
    /**
     * Process the ending of a footnote body.
     * @param body FootnoteBody that is ending
     */
    public abstract void endFootnoteBody(FootnoteBody body);

    /**
     * Process a Leader.
     * @param l Leader to process.
     */
    public abstract void leader(Leader l);

    /**
     * Process character data.
     * @param data Array of characters to process.
     * @param start Offset for characters to process.
     * @param length Portion of array to process.
     */
    public abstract void characters(char data[], int start, int length);

}

