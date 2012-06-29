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

import java.util.Locale;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
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
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.util.XMLUtil;

/**
 * A bridge between {@link FOEventHandler} and {@link StructureTreeEventHandler}.
 */
class StructureTreeEventTrigger extends FOEventHandler {

    private StructureTreeEventHandler structureTreeEventHandler;

    public StructureTreeEventTrigger(StructureTreeEventHandler structureTreeEventHandler) {
        this.structureTreeEventHandler = structureTreeEventHandler;
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPageSequence(PageSequence pageSeq) {
        Locale locale = null;
        if (pageSeq.getLanguage() != null) {
            if (pageSeq.getCountry() != null) {
                locale = new Locale(pageSeq.getLanguage(), pageSeq.getCountry());
            } else {
                locale = new Locale(pageSeq.getLanguage());
            }
        }
        String role = pageSeq.getCommonAccessibility().getRole();
        structureTreeEventHandler.startPageSequence(locale, role);
    }

    @Override
    public void endPageSequence(PageSequence pageSeq) {
        structureTreeEventHandler.endPageSequence();
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
        startElement(bl);
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
        startElement(inl);
    }

    @Override
    public void endInline(Inline inl) {
        endElement(inl);
    }

    @Override
    public void startTable(Table tbl) {
        startElement(tbl);
    }

    @Override
    public void endTable(Table tbl) {
        endElement(tbl);
    }

    @Override
    public void startHeader(TableHeader header) {
        startElement(header);
    }

    @Override
    public void endHeader(TableHeader header) {
        endElement(header);
    }

    @Override
    public void startFooter(TableFooter footer) {
        startElement(footer);
    }

    @Override
    public void endFooter(TableFooter footer) {
        endElement(footer);
    }

    @Override
    public void startBody(TableBody body) {
        startElement(body);
    }

    @Override
    public void endBody(TableBody body) {
        endElement(body);
    }

    @Override
    public void startRow(TableRow tr) {
        startElement(tr);
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
        startElement(tc, attributes);
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

    @Override
    public void characters(FOText foText) {
        startElementWithID(foText);
        endElement(foText);
    }


    private void startElement(FONode node) {
        startElement(node, new AttributesImpl());
    }

    private void startElementWithID(FONode node) {
        AttributesImpl attributes = new AttributesImpl();
        String localName = node.getLocalName();
        if (node instanceof CommonAccessibilityHolder) {
            addRole((CommonAccessibilityHolder) node, attributes);
        }
        node.setStructureTreeElement(
                structureTreeEventHandler.startReferencedNode(localName, attributes));
    }

    private void startElementWithIDAndAltText(AbstractGraphics node) {
        AttributesImpl attributes = new AttributesImpl();
        String localName = node.getLocalName();
        addRole(node, attributes);
        addAttribute(attributes, ExtensionElementMapping.URI, "alt-text",
                ExtensionElementMapping.STANDARD_PREFIX, node.getAltText());
        node.setStructureTreeElement(
                structureTreeEventHandler.startImageNode(localName, attributes));
    }

    private StructureTreeElement startElement(FONode node, AttributesImpl attributes) {
        String localName = node.getLocalName();
        if (node instanceof CommonAccessibilityHolder) {
            addRole((CommonAccessibilityHolder) node, attributes);
        }
        return structureTreeEventHandler.startNode(localName, attributes);
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
        structureTreeEventHandler.endNode(localName);
    }

}
