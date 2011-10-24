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

package org.apache.fop.accessibility;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.DelegatingFOEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.fo.flow.AbstractGraphics;
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
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.util.XMLUtil;

/**
 * A class that builds the document's structure tree.
 */
public class StructureTreeBuildingFOEventHandler extends DelegatingFOEventHandler {

    private int idCounter;

    private final StructureTree structureTree;

    private TransformerHandler structureTreeDOMBuilder;

    private DOMResult result;

    /** Delegates to either {@link #actualStructureTreeBuilder} or {@link #eventSwallower}. */
    private FOEventHandler structureTreeBuilder;

    private FOEventHandler actualStructureTreeBuilder;

    /** The descendants of some elements like fo:leader must be ignored. */
    private final FOEventHandler eventSwallower;

    private final class StructureTreeBuilder extends FOEventHandler {

        public StructureTreeBuilder(FOUserAgent foUserAgent) {
            super(foUserAgent);
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPageSequence(PageSequence pageSeq) {
            SAXTransformerFactory transformerFactory =
                    (SAXTransformerFactory) TransformerFactory.newInstance();
            try {
                structureTreeDOMBuilder = transformerFactory.newTransformerHandler();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }
            result = new DOMResult();
            structureTreeDOMBuilder.setResult(result);
            try {
                structureTreeDOMBuilder.startDocument();
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
            startElement(pageSeq);
        }

        @Override
        public void endPageSequence(PageSequence pageSeq) {
            endElement(pageSeq);
            try {
                structureTreeDOMBuilder.endDocument();
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
            structureTree.addPageSequenceStructure(
                    result.getNode().getFirstChild().getChildNodes());
        }

        @Override
        public void startPageNumber(PageNumber pagenum) {
            startElementWithID(pagenum);
        }

        @Override
        public void endPageNumber(PageNumber pagenum) {
            endElement(pagenum);
        }

        @Override
        public void startPageNumberCitation(PageNumberCitation pageCite) {
            startElementWithID(pageCite);
        }

        @Override
        public void endPageNumberCitation(PageNumberCitation pageCite) {
            endElement(pageCite);
        }

        @Override
        public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
            startElementWithID(pageLast);
        }

        @Override
        public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
            endElement(pageLast);
        }

        @Override
        public void startFlow(Flow fl) {
            startElement(fl);
        }

        @Override
        public void endFlow(Flow fl) {
            endElement(fl);
        }

        @Override
        public void startBlock(Block bl) {
            startElementWithID(bl);
        }

        @Override
        public void endBlock(Block bl) {
            endElement(bl);
        }

        @Override
        public void startBlockContainer(BlockContainer blc) {
            startElement(blc);
        }

        @Override
        public void endBlockContainer(BlockContainer blc) {
            endElement(blc);
        }

        @Override
        public void startInline(Inline inl) {
            startElementWithID(inl);
        }

        @Override
        public void endInline(Inline inl) {
            endElement(inl);
        }

        @Override
        public void startTable(Table tbl) {
            startElementWithID(tbl);
        }

        @Override
        public void endTable(Table tbl) {
            endElement(tbl);
        }

        @Override
        public void startHeader(TableHeader header) {
            startElementWithID(header);
        }

        @Override
        public void endHeader(TableHeader header) {
            endElement(header);
        }

        @Override
        public void startFooter(TableFooter footer) {
            startElementWithID(footer);
        }

        @Override
        public void endFooter(TableFooter footer) {
            endElement(footer);
        }

        @Override
        public void startBody(TableBody body) {
            startElementWithID(body);
        }

        @Override
        public void endBody(TableBody body) {
            endElement(body);
        }

        @Override
        public void startRow(TableRow tr) {
            startElementWithID(tr);
        }

        @Override
        public void endRow(TableRow tr) {
            endElement(tr);
        }

        @Override
        public void startCell(TableCell tc) {
            AttributesImpl attributes = new AttributesImpl();
            int colSpan = tc.getNumberColumnsSpanned();
            if (colSpan > 1) {
                addNoNamespaceAttribute(attributes, "number-columns-spanned",
                        Integer.toString(colSpan));
            }
            startElementWithID(tc, attributes);
        }

        @Override
        public void endCell(TableCell tc) {
            endElement(tc);
        }

        @Override
        public void startList(ListBlock lb) {
            startElement(lb);
        }

        @Override
        public void endList(ListBlock lb) {
            endElement(lb);
        }

        @Override
        public void startListItem(ListItem li) {
            startElement(li);
        }

        @Override
        public void endListItem(ListItem li) {
            endElement(li);
        }

        @Override
        public void startListLabel(ListItemLabel listItemLabel) {
            startElement(listItemLabel);
        }

        @Override
        public void endListLabel(ListItemLabel listItemLabel) {
            endElement(listItemLabel);
        }

        @Override
        public void startListBody(ListItemBody listItemBody) {
            startElement(listItemBody);
        }

        @Override
        public void endListBody(ListItemBody listItemBody) {
            endElement(listItemBody);
        }

        @Override
        public void startStatic(StaticContent staticContent) {
            startElement(staticContent);
        }

        @Override
        public void endStatic(StaticContent statisContent) {
            endElement(statisContent);
        }

        @Override
        public void startLink(BasicLink basicLink) {
            startElementWithID(basicLink);
        }

        @Override
        public void endLink(BasicLink basicLink) {
            endElement(basicLink);
        }

        @Override
        public void image(ExternalGraphic eg) {
            startElementWithIDAndAltText(eg);
            endElement(eg);
        }

        @Override
        public void startInstreamForeignObject(InstreamForeignObject ifo) {
            startElementWithIDAndAltText(ifo);
        }

        @Override
        public void endInstreamForeignObject(InstreamForeignObject ifo) {
            endElement(ifo);
        }

        @Override
        public void startFootnote(Footnote footnote) {
            startElement(footnote);
        }

        @Override
        public void endFootnote(Footnote footnote) {
            endElement(footnote);
        }

        @Override
        public void startFootnoteBody(FootnoteBody body) {
            startElement(body);
        }

        @Override
        public void endFootnoteBody(FootnoteBody body) {
            endElement(body);
        }

        @Override
        public void startWrapper(Wrapper wrapper) {
            startElement(wrapper);
        }

        @Override
        public void endWrapper(Wrapper wrapper) {
            endElement(wrapper);
        }

        @Override
        public void character(Character c) {
            startElementWithID(c);
            endElement(c);
        }

        private void startElement(FONode node) {
            startElement(node, new AttributesImpl());
        }

        private void startElementWithID(FONode node) {
            startElementWithID(node, new AttributesImpl());
        }

        private void startElementWithIDAndAltText(AbstractGraphics node) {
            AttributesImpl attributes = new AttributesImpl();
            addAttribute(attributes, ExtensionElementMapping.URI, "alt-text",
                    ExtensionElementMapping.STANDARD_PREFIX, node.getAltText());
            startElementWithID(node, attributes);
        }

        private void startElementWithID(FONode node, AttributesImpl attributes) {
            String id = Integer.toHexString(idCounter++);
            node.setPtr(id);
            addAttribute(attributes,
                    InternalElementMapping.URI, "ptr", InternalElementMapping.STANDARD_PREFIX, id);
            startElement(node, attributes);
        }

        private void startElement(FONode node, AttributesImpl attributes) {
            String localName = node.getLocalName();
            if (node instanceof CommonAccessibilityHolder) {
                addRole((CommonAccessibilityHolder) node, attributes);
            }
            try {
                structureTreeDOMBuilder.startElement(node.getNamespaceURI(), localName,
                        node.getNormalNamespacePrefix() + ":" + localName,
                        attributes);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

        private void addNoNamespaceAttribute(AttributesImpl attributes, String name, String value) {
            attributes.addAttribute("", name, name, XMLUtil.CDATA, value);
        }

        private void addAttribute(AttributesImpl attributes,
                String namespace, String localName, String prefix, String value) {
            assert namespace.length() > 0 && prefix.length() > 0;
            String qualifiedName = prefix + ":" + localName;
            attributes.addAttribute(namespace, localName, qualifiedName, XMLUtil.CDATA, value);
        }

        private void addRole(CommonAccessibilityHolder node, AttributesImpl attributes) {
            String role = node.getCommonAccessibility().getRole();
            if (role != null) {
                addNoNamespaceAttribute(attributes, "role", role);
            }
        }

        private void endElement(FONode node) {
            String localName = node.getLocalName();
            try {
                structureTreeDOMBuilder.endElement(node.getNamespaceURI(), localName,
                        node.getNormalNamespacePrefix() + ":" + localName);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Creates a new instance.
     *
     * @param structureTree the object that will hold the structure tree
     * @param delegate the FO event handler that must be wrapped by this instance
     */
    public StructureTreeBuildingFOEventHandler(StructureTree structureTree,
            FOEventHandler delegate) {
        super(delegate);
        this.structureTree = structureTree;
        this.actualStructureTreeBuilder = new StructureTreeBuilder(foUserAgent);
        this.structureTreeBuilder = actualStructureTreeBuilder;
        this.eventSwallower = new FOEventHandler(foUserAgent) { };
    }

    @Override
    public void startDocument() throws SAXException {
        structureTreeBuilder.startDocument();
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        structureTreeBuilder.endDocument();
        super.endDocument();
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        structureTreeBuilder.startPageSequence(pageSeq);
        super.startPageSequence(pageSeq);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        structureTreeBuilder.endPageSequence(pageSeq);
        super.endPageSequence(pageSeq);
    }

    @Override
    public void startPageNumber(PageNumber pagenum) {
        structureTreeBuilder.startPageNumber(pagenum);
        super.startPageNumber(pagenum);
    }

    @Override
    public void endPageNumber(PageNumber pagenum) {
        structureTreeBuilder.endPageNumber(pagenum);
        super.endPageNumber(pagenum);
    }

    @Override
    public void startPageNumberCitation(PageNumberCitation pageCite) {
        structureTreeBuilder.startPageNumberCitation(pageCite);
        super.startPageNumberCitation(pageCite);
    }

    @Override
    public void endPageNumberCitation(PageNumberCitation pageCite) {
        structureTreeBuilder.endPageNumberCitation(pageCite);
        super.endPageNumberCitation(pageCite);
    }

    @Override
    public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
        structureTreeBuilder.startPageNumberCitationLast(pageLast);
        super.startPageNumberCitationLast(pageLast);
    }

    @Override
    public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
        structureTreeBuilder.endPageNumberCitationLast(pageLast);
        super.endPageNumberCitationLast(pageLast);
    }

    @Override
    public void startFlow(Flow fl) {
        structureTreeBuilder.startFlow(fl);
        super.startFlow(fl);
    }

    @Override
    public void endFlow(Flow fl) {
        structureTreeBuilder.endFlow(fl);
        super.endFlow(fl);
    }

    @Override
    public void startBlock(Block bl) {
        structureTreeBuilder.startBlock(bl);
        super.startBlock(bl);
    }

    @Override
    public void endBlock(Block bl) {
        structureTreeBuilder.endBlock(bl);
        super.endBlock(bl);
    }

    @Override
    public void startBlockContainer(BlockContainer blc) {
        structureTreeBuilder.startBlockContainer(blc);
        super.startBlockContainer(blc);
    }

    @Override
    public void endBlockContainer(BlockContainer blc) {
        structureTreeBuilder.endBlockContainer(blc);
        super.endBlockContainer(blc);
    }

    @Override
    public void startInline(Inline inl) {
        structureTreeBuilder.startInline(inl);
        super.startInline(inl);
    }

    @Override
    public void endInline(Inline inl) {
        structureTreeBuilder.endInline(inl);
        super.endInline(inl);
    }

    @Override
    public void startTable(Table tbl) {
        structureTreeBuilder.startTable(tbl);
        super.startTable(tbl);
    }

    @Override
    public void endTable(Table tbl) {
        structureTreeBuilder.endTable(tbl);
        super.endTable(tbl);
    }

    @Override
    public void startColumn(TableColumn tc) {
        structureTreeBuilder.startColumn(tc);
        super.startColumn(tc);
    }

    @Override
    public void endColumn(TableColumn tc) {
        structureTreeBuilder.endColumn(tc);
        super.endColumn(tc);
    }

    @Override
    public void startHeader(TableHeader header) {
        structureTreeBuilder.startHeader(header);
        super.startHeader(header);
    }

    @Override
    public void endHeader(TableHeader header) {
        structureTreeBuilder.endHeader(header);
        super.endHeader(header);
    }

    @Override
    public void startFooter(TableFooter footer) {
        structureTreeBuilder.startFooter(footer);
        super.startFooter(footer);
    }

    @Override
    public void endFooter(TableFooter footer) {
        structureTreeBuilder.endFooter(footer);
        super.endFooter(footer);
    }

    @Override
    public void startBody(TableBody body) {
        structureTreeBuilder.startBody(body);
        super.startBody(body);
    }

    @Override
    public void endBody(TableBody body) {
        structureTreeBuilder.endBody(body);
        super.endBody(body);
    }

    @Override
    public void startRow(TableRow tr) {
        structureTreeBuilder.startRow(tr);
        super.startRow(tr);
    }

    @Override
    public void endRow(TableRow tr) {
        structureTreeBuilder.endRow(tr);
        super.endRow(tr);
    }

    @Override
    public void startCell(TableCell tc) {
        structureTreeBuilder.startCell(tc);
        super.startCell(tc);
    }

    @Override
    public void endCell(TableCell tc) {
        structureTreeBuilder.endCell(tc);
        super.endCell(tc);
    }

    @Override
    public void startList(ListBlock lb) {
        structureTreeBuilder.startList(lb);
        super.startList(lb);
    }

    @Override
    public void endList(ListBlock lb) {
        structureTreeBuilder.endList(lb);
        super.endList(lb);
    }

    @Override
    public void startListItem(ListItem li) {
        structureTreeBuilder.startListItem(li);
        super.startListItem(li);
    }

    @Override
    public void endListItem(ListItem li) {
        structureTreeBuilder.endListItem(li);
        super.endListItem(li);
    }

    @Override
    public void startListLabel(ListItemLabel listItemLabel) {
        structureTreeBuilder.startListLabel(listItemLabel);
        super.startListLabel(listItemLabel);
    }

    @Override
    public void endListLabel(ListItemLabel listItemLabel) {
        structureTreeBuilder.endListLabel(listItemLabel);
        super.endListLabel(listItemLabel);
    }

    @Override
    public void startListBody(ListItemBody listItemBody) {
        structureTreeBuilder.startListBody(listItemBody);
        super.startListBody(listItemBody);
    }

    @Override
    public void endListBody(ListItemBody listItemBody) {
        structureTreeBuilder.endListBody(listItemBody);
        super.endListBody(listItemBody);
    }

    @Override
    public void startStatic(StaticContent staticContent) {
        structureTreeBuilder.startStatic(staticContent);
        super.startStatic(staticContent);
    }

    @Override
    public void endStatic(StaticContent statisContent) {
        structureTreeBuilder.endStatic(statisContent);
        super.endStatic(statisContent);
    }

    @Override
    public void startMarkup() {
        structureTreeBuilder.startMarkup();
        super.startMarkup();
    }

    @Override
    public void endMarkup() {
        structureTreeBuilder.endMarkup();
        super.endMarkup();
    }

    @Override
    public void startLink(BasicLink basicLink) {
        structureTreeBuilder.startLink(basicLink);
        super.startLink(basicLink);
    }

    @Override
    public void endLink(BasicLink basicLink) {
        structureTreeBuilder.endLink(basicLink);
        super.endLink(basicLink);
    }

    @Override
    public void image(ExternalGraphic eg) {
        structureTreeBuilder.image(eg);
        super.image(eg);
    }

    @Override
    public void pageRef() {
        structureTreeBuilder.pageRef();
        super.pageRef();
    }

    @Override
    public void startInstreamForeignObject(InstreamForeignObject ifo) {
        structureTreeBuilder.startInstreamForeignObject(ifo);
        super.startInstreamForeignObject(ifo);
    }

    @Override
    public void endInstreamForeignObject(InstreamForeignObject ifo) {
        structureTreeBuilder.endInstreamForeignObject(ifo);
        super.endInstreamForeignObject(ifo);
    }

    @Override
    public void startFootnote(Footnote footnote) {
        structureTreeBuilder.startFootnote(footnote);
        super.startFootnote(footnote);
    }

    @Override
    public void endFootnote(Footnote footnote) {
        structureTreeBuilder.endFootnote(footnote);
        super.endFootnote(footnote);
    }

    @Override
    public void startFootnoteBody(FootnoteBody body) {
        structureTreeBuilder.startFootnoteBody(body);
        super.startFootnoteBody(body);
    }

    @Override
    public void endFootnoteBody(FootnoteBody body) {
        structureTreeBuilder.endFootnoteBody(body);
        super.endFootnoteBody(body);
    }

    @Override
    public void startLeader(Leader l) {
        structureTreeBuilder = eventSwallower;
        structureTreeBuilder.startLeader(l);
        super.startLeader(l);
    }

    @Override
    public void endLeader(Leader l) {
        structureTreeBuilder.endLeader(l);
        structureTreeBuilder = actualStructureTreeBuilder;
        super.endLeader(l);
    }

    @Override
    public void startWrapper(Wrapper wrapper) {
        structureTreeBuilder.startWrapper(wrapper);
        super.startWrapper(wrapper);
    }

    @Override
    public void endWrapper(Wrapper wrapper) {
        structureTreeBuilder.endWrapper(wrapper);
        super.endWrapper(wrapper);
    }

    @Override
    public void character(Character c) {
        structureTreeBuilder.character(c);
        super.character(c);
    }

    @Override
    public void characters(char[] data, int start, int length) {
        structureTreeBuilder.characters(data, start, length);
        super.characters(data, start, length);
    }

    @Override
    public void startExternalDocument(ExternalDocument document) {
        structureTreeBuilder.startExternalDocument(document);
        super.startExternalDocument(document);
    }

    @Override
    public void endExternalDocument(ExternalDocument document) {
        structureTreeBuilder.endExternalDocument(document);
        super.endExternalDocument(document);
    }

}
