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

package org.apache.fop.accessibility.fo;

import java.util.Stack;

import org.xml.sax.SAXException;

import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.DelegatingFOEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOText;
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
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;

/**
 * Allows to create the structure tree of an FO document, by converting FO
 * events into appropriate structure tree events.
 */
public class FO2StructureTreeConverter extends DelegatingFOEventHandler {

    /** The top of the {@link converters} stack. */
    private FOEventHandler converter;

    private final Stack<FOEventHandler> converters = new Stack<FOEventHandler>();

    private final Stack<FOEventRecorder> tableFooterRecorders = new Stack<FOEventRecorder>();

    private final FOEventHandler structureTreeEventTrigger;

    /** The descendants of some elements like fo:leader must be ignored. */
    private final FOEventHandler eventSwallower = new FOEventHandler() {
    };

    /**
     * Creates a new instance.
     *
     * @param structureTreeEventHandler the object that will hold the structure tree
     * @param delegate the FO event handler that must be wrapped by this instance
     */
    public FO2StructureTreeConverter(StructureTreeEventHandler structureTreeEventHandler,
            FOEventHandler delegate) {
        super(delegate);
        this.structureTreeEventTrigger = new StructureTreeEventTrigger(structureTreeEventHandler);
        this.converter = structureTreeEventTrigger;
    }

    @Override
    public void startDocument() throws SAXException {
        converter.startDocument();
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        converter.endDocument();
        super.endDocument();
    }

    @Override
    public void startRoot(Root root) {
       converter.startRoot(root);
       super.startRoot(root);
    }

    @Override
    public void endRoot(Root root) {
        converter.endRoot(root);
        super.endRoot(root);
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        converter.startPageSequence(pageSeq);
        super.startPageSequence(pageSeq);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        converter.endPageSequence(pageSeq);
        super.endPageSequence(pageSeq);
    }

    @Override
    public void startPageNumber(PageNumber pagenum) {
        converter.startPageNumber(pagenum);
        super.startPageNumber(pagenum);
    }

    @Override
    public void endPageNumber(PageNumber pagenum) {
        converter.endPageNumber(pagenum);
        super.endPageNumber(pagenum);
    }

    @Override
    public void startPageNumberCitation(PageNumberCitation pageCite) {
        converter.startPageNumberCitation(pageCite);
        super.startPageNumberCitation(pageCite);
    }

    @Override
    public void endPageNumberCitation(PageNumberCitation pageCite) {
        converter.endPageNumberCitation(pageCite);
        super.endPageNumberCitation(pageCite);
    }

    @Override
    public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
        converter.startPageNumberCitationLast(pageLast);
        super.startPageNumberCitationLast(pageLast);
    }

    @Override
    public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
        converter.endPageNumberCitationLast(pageLast);
        super.endPageNumberCitationLast(pageLast);
    }

    @Override
    public void startFlow(Flow fl) {
        converter.startFlow(fl);
        super.startFlow(fl);
    }

    @Override
    public void endFlow(Flow fl) {
        converter.endFlow(fl);
        super.endFlow(fl);
    }

    @Override
    public void startBlock(Block bl) {
        converter.startBlock(bl);
        super.startBlock(bl);
    }

    @Override
    public void endBlock(Block bl) {
        converter.endBlock(bl);
        super.endBlock(bl);
    }

    @Override
    public void startBlockContainer(BlockContainer blc) {
        converter.startBlockContainer(blc);
        super.startBlockContainer(blc);
    }

    @Override
    public void endBlockContainer(BlockContainer blc) {
        converter.endBlockContainer(blc);
        super.endBlockContainer(blc);
    }

    @Override
    public void startInline(Inline inl) {
        converter.startInline(inl);
        super.startInline(inl);
    }

    @Override
    public void endInline(Inline inl) {
        converter.endInline(inl);
        super.endInline(inl);
    }

    @Override
    public void startTable(Table tbl) {
        converter.startTable(tbl);
        tableFooterRecorders.push(null);
        super.startTable(tbl);
    }

    @Override
    public void endTable(Table tbl) {
        FOEventRecorder tableFooterRecorder = tableFooterRecorders.pop();
        if (tableFooterRecorder != null) {
            tableFooterRecorder.replay(converter);
        }
        converter.endTable(tbl);
        super.endTable(tbl);
    }

    @Override
    public void startColumn(TableColumn tc) {
        converter.startColumn(tc);
        super.startColumn(tc);
    }

    @Override
    public void endColumn(TableColumn tc) {
        converter.endColumn(tc);
        super.endColumn(tc);
    }

    @Override
    public void startHeader(TableHeader header) {
        converter.startHeader(header);
        super.startHeader(header);
    }

    @Override
    public void endHeader(TableHeader header) {
        converter.endHeader(header);
        super.endHeader(header);
    }

    @Override
    public void startFooter(TableFooter footer) {
        converters.push(converter);
        converter = new FOEventRecorder();
        converter.startFooter(footer);
        super.startFooter(footer);
    }

    @Override
    public void endFooter(TableFooter footer) {
        converter.endFooter(footer);
        /* Replace the dummy table footer with the real one. */
        tableFooterRecorders.pop();
        tableFooterRecorders.push((FOEventRecorder) converter);
        converter = converters.pop();
        super.endFooter(footer);
    }

    @Override
    public void startBody(TableBody body) {
        converter.startBody(body);
        super.startBody(body);
    }

    @Override
    public void endBody(TableBody body) {
        converter.endBody(body);
        super.endBody(body);
    }

    @Override
    public void startRow(TableRow tr) {
        converter.startRow(tr);
        super.startRow(tr);
    }

    @Override
    public void endRow(TableRow tr) {
        converter.endRow(tr);
        super.endRow(tr);
    }

    @Override
    public void startCell(TableCell tc) {
        converter.startCell(tc);
        super.startCell(tc);
    }

    @Override
    public void endCell(TableCell tc) {
        converter.endCell(tc);
        super.endCell(tc);
    }

    @Override
    public void startList(ListBlock lb) {
        converter.startList(lb);
        super.startList(lb);
    }

    @Override
    public void endList(ListBlock lb) {
        converter.endList(lb);
        super.endList(lb);
    }

    @Override
    public void startListItem(ListItem li) {
        converter.startListItem(li);
        super.startListItem(li);
    }

    @Override
    public void endListItem(ListItem li) {
        converter.endListItem(li);
        super.endListItem(li);
    }

    @Override
    public void startListLabel(ListItemLabel listItemLabel) {
        converter.startListLabel(listItemLabel);
        super.startListLabel(listItemLabel);
    }

    @Override
    public void endListLabel(ListItemLabel listItemLabel) {
        converter.endListLabel(listItemLabel);
        super.endListLabel(listItemLabel);
    }

    @Override
    public void startListBody(ListItemBody listItemBody) {
        converter.startListBody(listItemBody);
        super.startListBody(listItemBody);
    }

    @Override
    public void endListBody(ListItemBody listItemBody) {
        converter.endListBody(listItemBody);
        super.endListBody(listItemBody);
    }

    @Override
    public void startStatic(StaticContent staticContent) {
        handleStartArtifact(staticContent);
        converter.startStatic(staticContent);
        super.startStatic(staticContent);
    }

    @Override
    public void endStatic(StaticContent statisContent) {
        converter.endStatic(statisContent);
        handleEndArtifact(statisContent);
        super.endStatic(statisContent);
    }

    @Override
    public void startMarkup() {
        converter.startMarkup();
        super.startMarkup();
    }

    @Override
    public void endMarkup() {
        converter.endMarkup();
        super.endMarkup();
    }

    @Override
    public void startLink(BasicLink basicLink) {
        converter.startLink(basicLink);
        super.startLink(basicLink);
    }

    @Override
    public void endLink(BasicLink basicLink) {
        converter.endLink(basicLink);
        super.endLink(basicLink);
    }

    @Override
    public void image(ExternalGraphic eg) {
        converter.image(eg);
        super.image(eg);
    }

    @Override
    public void pageRef() {
        converter.pageRef();
        super.pageRef();
    }

    @Override
    public void startInstreamForeignObject(InstreamForeignObject ifo) {
        converter.startInstreamForeignObject(ifo);
        super.startInstreamForeignObject(ifo);
    }

    @Override
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
        converter.endInstreamForeignObject(ifo);
        super.endInstreamForeignObject(ifo);
    }

    @Override
    public void startFootnote(Footnote footnote) {
        converter.startFootnote(footnote);
        super.startFootnote(footnote);
    }

    @Override
    public void endFootnote(Footnote footnote) {
        converter.endFootnote(footnote);
        super.endFootnote(footnote);
    }

    @Override
    public void startFootnoteBody(FootnoteBody body) {
        converter.startFootnoteBody(body);
        super.startFootnoteBody(body);
    }

    @Override
    public void endFootnoteBody(FootnoteBody body) {
        converter.endFootnoteBody(body);
        super.endFootnoteBody(body);
    }

    @Override
    public void startLeader(Leader l) {
        converters.push(converter);
        converter = eventSwallower;
        converter.startLeader(l);
        super.startLeader(l);
    }

    @Override
    public void endLeader(Leader l) {
        converter.endLeader(l);
        converter = converters.pop();
        super.endLeader(l);
    }

    @Override
    public void startWrapper(Wrapper wrapper) {
        handleStartArtifact(wrapper);
        converter.startWrapper(wrapper);
        super.startWrapper(wrapper);
    }

    @Override
    public void endWrapper(Wrapper wrapper) {
        converter.endWrapper(wrapper);
        handleEndArtifact(wrapper);
        super.endWrapper(wrapper);
    }

    @Override
    public void character(Character c) {
        converter.character(c);
        super.character(c);
    }

    @Override
    public void characters(FOText foText) {
        converter.characters(foText);
        super.characters(foText);
    }

    @Override
    public void startExternalDocument(ExternalDocument document) {
        converter.startExternalDocument(document);
        super.startExternalDocument(document);
    }

    @Override
    public void endExternalDocument(ExternalDocument document) {
        converter.endExternalDocument(document);
        super.endExternalDocument(document);
    }

    private void handleStartArtifact(CommonAccessibilityHolder fobj) {
        if (isArtifact(fobj)) {
            converters.push(converter);
            converter = eventSwallower;
        }
    }

    private void handleEndArtifact(CommonAccessibilityHolder fobj) {
        if (isArtifact(fobj)) {
            converter = converters.pop();
        }
    }

    private boolean isArtifact(CommonAccessibilityHolder fobj) {
        CommonAccessibility accessibility = fobj.getCommonAccessibility();
        return Accessibility.ROLE_ARTIFACT.equalsIgnoreCase(accessibility.getRole());
    }

}
