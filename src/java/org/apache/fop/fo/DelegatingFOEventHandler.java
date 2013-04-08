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
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
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
import org.apache.fop.fonts.FontInfo;

/**
 * This class delegates all FO events to another FOEventHandler instance.
 */
public abstract class DelegatingFOEventHandler extends FOEventHandler {

    private final FOEventHandler delegate;

    /**
     * Creates a new instance that delegates events to the given object.
     *
     * @param delegate the object to which all FO events will be forwarded
     */
    public DelegatingFOEventHandler(FOEventHandler delegate) {
        super(delegate.getUserAgent());
        this.delegate = delegate;
    }

    @Override
    public FOUserAgent getUserAgent() {
        return delegate.getUserAgent();
    }

    @Override
    public FontInfo getFontInfo() {
        return delegate.getFontInfo();
    }

    @Override
    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        delegate.endDocument();
    }

    @Override
    public void startRoot(Root root) {
        delegate.startRoot(root);
    }

    @Override
    public void endRoot(Root root) {
        delegate.endRoot(root);
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        delegate.startPageSequence(pageSeq);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        delegate.endPageSequence(pageSeq);
    }

    @Override
    public void startPageNumber(PageNumber pagenum) {
        delegate.startPageNumber(pagenum);
    }

    @Override
    public void endPageNumber(PageNumber pagenum) {
        delegate.endPageNumber(pagenum);
    }

    @Override
    public void startPageNumberCitation(PageNumberCitation pageCite) {
        delegate.startPageNumberCitation(pageCite);
    }

    @Override
    public void endPageNumberCitation(PageNumberCitation pageCite) {
        delegate.endPageNumberCitation(pageCite);
    }

    @Override
    public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
        delegate.startPageNumberCitationLast(pageLast);
    }

    @Override
    public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
        delegate.endPageNumberCitationLast(pageLast);
    }

    @Override
    public void startStatic(StaticContent staticContent) {
        delegate.startStatic(staticContent);
    }

    @Override
    public void endStatic(StaticContent statisContent) {
        delegate.endStatic(statisContent);
    }

    @Override
    public void startFlow(Flow fl) {
        delegate.startFlow(fl);
    }

    @Override
    public void endFlow(Flow fl) {
        delegate.endFlow(fl);
    }

    @Override
    public void startBlock(Block bl) {
        delegate.startBlock(bl);
    }

    @Override
    public void endBlock(Block bl) {
        delegate.endBlock(bl);
    }

    @Override
    public void startBlockContainer(BlockContainer blc) {
        delegate.startBlockContainer(blc);
    }

    @Override
    public void endBlockContainer(BlockContainer blc) {
        delegate.endBlockContainer(blc);
    }

    @Override
    public void startInline(Inline inl) {
        delegate.startInline(inl);
    }

    @Override
    public void endInline(Inline inl) {
        delegate.endInline(inl);
    }

    @Override
    public void startTable(Table tbl) {
        delegate.startTable(tbl);
    }

    @Override
    public void endTable(Table tbl) {
        delegate.endTable(tbl);
    }

    @Override
    public void startColumn(TableColumn tc) {
        delegate.startColumn(tc);
    }

    @Override
    public void endColumn(TableColumn tc) {
        delegate.endColumn(tc);
    }

    @Override
    public void startHeader(TableHeader header) {
        delegate.startHeader(header);
    }

    @Override
    public void endHeader(TableHeader header) {
        delegate.endHeader(header);
    }

    @Override
    public void startFooter(TableFooter footer) {
        delegate.startFooter(footer);
    }

    @Override
    public void endFooter(TableFooter footer) {
        delegate.endFooter(footer);
    }

    @Override
    public void startBody(TableBody body) {
        delegate.startBody(body);
    }

    @Override
    public void endBody(TableBody body) {
        delegate.endBody(body);
    }

    @Override
    public void startRow(TableRow tr) {
        delegate.startRow(tr);
    }

    @Override
    public void endRow(TableRow tr) {
        delegate.endRow(tr);
    }

    @Override
    public void startCell(TableCell tc) {
        delegate.startCell(tc);
    }

    @Override
    public void endCell(TableCell tc) {
        delegate.endCell(tc);
    }

    @Override
    public void startList(ListBlock lb) {
        delegate.startList(lb);
    }

    @Override
    public void endList(ListBlock lb) {
        delegate.endList(lb);
    }

    @Override
    public void startListItem(ListItem li) {
        delegate.startListItem(li);
    }

    @Override
    public void endListItem(ListItem li) {
        delegate.endListItem(li);
    }

    @Override
    public void startListLabel(ListItemLabel listItemLabel) {
        delegate.startListLabel(listItemLabel);
    }

    @Override
    public void endListLabel(ListItemLabel listItemLabel) {
        delegate.endListLabel(listItemLabel);
    }

    @Override
    public void startListBody(ListItemBody listItemBody) {
        delegate.startListBody(listItemBody);
    }

    @Override
    public void endListBody(ListItemBody listItemBody) {
        delegate.endListBody(listItemBody);
    }

    @Override
    public void startMarkup() {
        delegate.startMarkup();
    }

    @Override
    public void endMarkup() {
        delegate.endMarkup();
    }

    @Override
    public void startLink(BasicLink basicLink) {
        delegate.startLink(basicLink);
    }

    @Override
    public void endLink(BasicLink basicLink) {
        delegate.endLink(basicLink);
    }

    @Override
    public void image(ExternalGraphic eg) {
        delegate.image(eg);
    }

    @Override
    public void pageRef() {
        delegate.pageRef();
    }

    @Override
    public void startInstreamForeignObject(InstreamForeignObject ifo) {
        delegate.startInstreamForeignObject(ifo);
    }

    @Override
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
        delegate.endInstreamForeignObject(ifo);
    }

    @Override
    public void startFootnote(Footnote footnote) {
        delegate.startFootnote(footnote);
    }

    @Override
    public void endFootnote(Footnote footnote) {
        delegate.endFootnote(footnote);
    }

    @Override
    public void startFootnoteBody(FootnoteBody body) {
        delegate.startFootnoteBody(body);
    }

    @Override
    public void endFootnoteBody(FootnoteBody body) {
        delegate.endFootnoteBody(body);
    }

    @Override
    public void startLeader(Leader l) {
        delegate.startLeader(l);
    }

    @Override
    public void endLeader(Leader l) {
        delegate.endLeader(l);
    }

    @Override
    public void startWrapper(Wrapper wrapper) {
        delegate.startWrapper(wrapper);
    }

    @Override
    public void endWrapper(Wrapper wrapper) {
        delegate.endWrapper(wrapper);
    }

    @Override
    public void startRetrieveMarker(RetrieveMarker retrieveMarker) {
        delegate.startRetrieveMarker(retrieveMarker);
    }

    @Override
    public void endRetrieveMarker(RetrieveMarker retrieveMarker) {
        delegate.endRetrieveMarker(retrieveMarker);
    }

    @Override
    public void restoreState(RetrieveMarker retrieveMarker) {
        delegate.restoreState(retrieveMarker);
    }

    @Override
    public void startRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
        delegate.startRetrieveTableMarker(retrieveTableMarker);
    }

    @Override
    public void endRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
        delegate.endRetrieveTableMarker(retrieveTableMarker);
    }

    @Override
    public void restoreState(RetrieveTableMarker retrieveTableMarker) {
        delegate.restoreState(retrieveTableMarker);
    }

    @Override
    public void character(Character c) {
        delegate.character(c);
    }

    @Override
    public void characters(FOText foText) {
        delegate.characters(foText);
    }

    @Override
    public void startExternalDocument(ExternalDocument document) {
        delegate.startExternalDocument(document);
    }

    @Override
    public void endExternalDocument(ExternalDocument document) {
        delegate.endExternalDocument(document);
    }

    @Override
    public FormattingResults getResults() {
        return delegate.getResults();
    }

}
