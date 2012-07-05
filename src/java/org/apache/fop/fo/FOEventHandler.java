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

package org.apache.fop.fo;

import org.xml.sax.SAXException;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;

/**
 * Abstract class defining what should be done with SAX events that map to
 * XSL-FO input. The events are actually captured by fo/FOTreeBuilder, passed
 * to the various fo Objects, which in turn, if needed, pass them to an instance
 * of FOEventHandler.
 *
 * Sub-classes will generally fall into one of two categories:
 * 1) a handler that actually builds an FO Tree from the events, or 2) a
 * handler that builds a structured (as opposed to formatted) document, such
 * as our MIF and RTF output targets.
 */
public abstract class FOEventHandler {

    /**
     * The FOUserAgent for this process
     */
    protected FOUserAgent foUserAgent;

    /**
     * The Font information relevant for this document
     */
    protected FontInfo fontInfo;

    /**
     * Main constructor
     * @param foUserAgent the apps.FOUserAgent instance for this process
     */
    public FOEventHandler(FOUserAgent foUserAgent) {
        this.foUserAgent = foUserAgent;
        this.fontInfo = new FontInfo();
        this.fontInfo.setEventListener(new FontEventAdapter(foUserAgent.getEventBroadcaster()));
    }

    /** Constructor for sub-classes that do not need an {@link FOUserAgent} instance. */
    protected FOEventHandler() {
    }

    /**
     * Returns the User Agent object associated with this FOEventHandler.
     * @return the User Agent object
     */
    public FOUserAgent getUserAgent() {
        return foUserAgent;
    }

    /**
     * Retrieve the font information for this document
     * @return the FontInfo instance for this document
     */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /**
     * This method is called to indicate the start of a new document run.
     * @throws SAXException In case of a problem
     */
    public void startDocument() throws SAXException {
    }

    /**
     * This method is called to indicate the end of a document run.
     * @throws SAXException In case of a problem
     */
    public void endDocument() throws SAXException {
    }

    /**
     * Called upon start of root element.
     * @param root element
     */
    public void startRoot(Root root) {
    }

    /**
     * Called upon end of root element.
     * @param root element
     */
    public void endRoot(Root root) {
    }

    /**
     *
     * @param pageSeq PageSequence that is starting.
     */
    public void startPageSequence(PageSequence pageSeq) {
    }

    /**
     * @param pageSeq PageSequence that is ending.
     */
    public void endPageSequence(PageSequence pageSeq) {
    }

    /**
     *
     * @param pagenum PageNumber that is starting.
     */
    public void startPageNumber(PageNumber pagenum) {
    }

    /**
     *
     * @param pagenum PageNumber that is ending.
     */
    public void endPageNumber(PageNumber pagenum) {
    }

    /**
     *
     * @param pageCite PageNumberCitation that is starting.
     */
    public void startPageNumberCitation(PageNumberCitation pageCite) {
    }

    /**
     *
     * @param pageCite PageNumberCitation that is ending.
     */
    public void endPageNumberCitation(PageNumberCitation pageCite) {
    }

    /**
     *
     * @param pageLast PageNumberCitationLast that is starting.
     */
    public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
    }

    /**
     *
     * @param pageLast PageNumberCitationLast that is ending.
     */
    public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
    }

    /**
     * This method is called to indicate the start of a new fo:flow
     * or fo:static-content.
     * This method also handles fo:static-content tags, because the
     * StaticContent class is derived from the Flow class.
     *
     * @param fl Flow that is starting.
     */
    public void startFlow(Flow fl) {
    }

    /**
     *
     * @param fl Flow that is ending.
     */
    public void endFlow(Flow fl) {
    }

    /**
     *
     * @param bl Block that is starting.
     */
    public void startBlock(Block bl) {
    }

    /**
     *
     * @param bl Block that is ending.
     */
    public void endBlock(Block bl) {
    }

    /**
    *
    * @param blc BlockContainer that is starting.
    */
    public void startBlockContainer(BlockContainer blc) {
    }

    /**
    *
    * @param blc BlockContainer that is ending.
    */
    public void endBlockContainer(BlockContainer blc) {
    }

    /**
     *
     * @param inl Inline that is starting.
     */
    public void startInline(Inline inl) {
    }

    /**
     *
     * @param inl Inline that is ending.
     */
    public void endInline(Inline inl) {
    }

    // Tables
    /**
     *
     * @param tbl Table that is starting.
     */
    public void startTable(Table tbl) {
    }

    /**
     *
     * @param tbl Table that is ending.
     */
    public void endTable(Table tbl) {
    }

    /**
     *
     * @param tc TableColumn that is starting;
     */
    public void startColumn(TableColumn tc) {
    }

    /**
     *
     * @param tc TableColumn that is ending;
     */
    public void endColumn(TableColumn tc) {
    }

    /**
     *
     * @param header TableHeader that is starting;
     */
    public void startHeader(TableHeader header) {
    }

    /**
     *
     * @param header TableHeader that is ending.
     */
    public void endHeader(TableHeader header) {
    }

    /**
     *
     * @param footer TableFooter that is starting.
     */
    public void startFooter(TableFooter footer) {
    }

    /**
     *
     * @param footer TableFooter that is ending.
     */
    public void endFooter(TableFooter footer) {
    }

    /**
     *
     * @param body TableBody that is starting.
     */
    public void startBody(TableBody body) {
    }

    /**
     *
     * @param body TableBody that is ending.
     */
    public void endBody(TableBody body) {
    }

    /**
     *
     * @param tr TableRow that is starting.
     */
    public void startRow(TableRow tr) {
    }

    /**
     *
     * @param tr TableRow that is ending.
     */
    public void endRow(TableRow tr) {
    }

    /**
     *
     * @param tc TableCell that is starting.
     */
    public void startCell(TableCell tc) {
    }

    /**
     *
     * @param tc TableCell that is ending.
     */
    public void endCell(TableCell tc) {
    }


    // Lists
    /**
     *
     * @param lb ListBlock that is starting.
     */
    public void startList(ListBlock lb) {
    }

    /**
     *
     * @param lb ListBlock that is ending.
     */
    public void endList(ListBlock lb) {
    }

    /**
     *
     * @param li ListItem that is starting.
     */
    public void startListItem(ListItem li) {
    }

    /**
     *
     * @param li ListItem that is ending.
     */
    public void endListItem(ListItem li) {
    }

    /**
     * Process start of a ListLabel.
     * @param listItemLabel ListItemLabel that is starting
     */
    public void startListLabel(ListItemLabel listItemLabel) {
    }

    /**
     * Process end of a ListLabel.
     * @param listItemLabel ListItemLabel that is ending
     */
    public void endListLabel(ListItemLabel listItemLabel) {
    }

    /**
     * Process start of a ListBody.
     * @param listItemBody ListItemBody that is starting
     */
    public void startListBody(ListItemBody listItemBody) {
    }

    /**
     * Process end of a ListBody.
     * @param listItemBody ListItemBody that is ending
     */
    public void endListBody(ListItemBody listItemBody) {
    }

    // Static Regions
    /**
     * Process start of a Static.
     * @param staticContent StaticContent that is starting
     */
    public void startStatic(StaticContent staticContent) {
    }

    /**
     * Process end of a Static.
     * @param staticContent StaticContent that is ending
     */
    public void endStatic(StaticContent staticContent) {
    }


    /**
     * Process start of a Markup.
     */
    public void startMarkup() {
    }

    /**
     * Process end of a Markup.
     */
    public void endMarkup() {
    }

    /**
     * Process start of a Link.
     * @param basicLink BasicLink that is starting
     */
    public void startLink(BasicLink basicLink) {
    }

    /**
     * Process end of a Link.
     * @param basicLink BasicLink that is ending
     */
    public void endLink(BasicLink basicLink) {
    }

    /**
     * Process an ExternalGraphic.
     * @param eg ExternalGraphic to process.
     */
    public void image(ExternalGraphic eg) {
    }

    /**
     * Process a pageRef.
     */
    public void pageRef() {
    }

    /**
     * Process the start of an InstreamForeignObject.
     * @param ifo InstreamForeignObject that is starting
     */
    public void startInstreamForeignObject(InstreamForeignObject ifo) {
    }

    /**
     * Process the end of an InstreamForeignObject.
     * @param ifo InstreamForeignObject that is ending
     */
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
    }

    /**
     * Process the start of a footnote.
     * @param footnote Footnote that is starting
     */
    public void startFootnote(Footnote footnote) {
    }

    /**
     * Process the ending of a footnote.
     * @param footnote Footnote that is ending
     */
    public void endFootnote(Footnote footnote) {
    }

    /**
     * Process the start of a footnote body.
     * @param body FootnoteBody that is starting
     */
    public void startFootnoteBody(FootnoteBody body) {
    }

    /**
     * Process the ending of a footnote body.
     * @param body FootnoteBody that is ending
     */
    public void endFootnoteBody(FootnoteBody body) {
    }

    /**
     * Process the start of a Leader.
     * @param l Leader that is starting
     */
    public void startLeader(Leader l) {
    }

    /**
     * Process the end of a Leader.
     * @param l Leader that is ending
     */
    public void endLeader(Leader l) {
    }

    /**
     * Process the start of a wrapper.
     *
     * @param wrapper wrapper that is starting
     */
    public void startWrapper(Wrapper wrapper) {
    }

    /**
     * Process the ending of a wrapper.
     *
     * @param wrapper wrapper that is ending
     */
    public void endWrapper(Wrapper wrapper) {
    }

    /**
     * Process a Character.
     * @param c Character to process.
     */
    public void character(Character c) {
    }

    /**
     * Process character data.
     * @param foText text to process
     */
    public void characters(FOText foText) {
    }

    /**
     * Process the start of the external-document extension.
     * @param document the external-document node
     */
    public void startExternalDocument(ExternalDocument document) {
    }

    /**
     * Process the end of the external-document extension.
     * @param document the external-document node
     */
    public void endExternalDocument(ExternalDocument document) {
    }

    /**
     * Get formatting results.
     * @return the FormattingResults instance for this document
     */
    public FormattingResults getResults() {
        return null;
    }

}

