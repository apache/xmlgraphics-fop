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

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FODocumentParser.FOEventHandlerFactory;
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

/**
 * Tests that {@link DelegatingFOEventHandler} does forward every event to its delegate
 * event handler.
 */
public class DelegatingFOEventHandlerTestCase {

    private InputStream document;

    private List<String> expectedEvents;

    private List<String> actualEvents;

    private FODocumentParser documentParser;

    private class DelegatingFOEventHandlerTester extends FOEventHandler {

        DelegatingFOEventHandlerTester(FOUserAgent foUserAgent) {
            super(foUserAgent);
        }

        private final StringBuilder eventBuilder = new StringBuilder();

        @Override
        public void startDocument() throws SAXException {
            actualEvents.add("start document");
        }

        @Override
        public void endDocument() throws SAXException {
            actualEvents.add("end   document");
        }

        @Override
        public void startRoot(Root root) {
            startElement(root);
        }

        @Override
        public void endRoot(Root root) {
            endElement(root);
        }

        @Override
        public void startPageSequence(PageSequence pageSeq) {
            startElement(pageSeq);
        }

        @Override
        public void endPageSequence(PageSequence pageSeq) {
            endElement(pageSeq);
        }

        @Override
        public void startPageNumber(PageNumber pagenum) {
            startElement(pagenum);
        }

        @Override
        public void endPageNumber(PageNumber pagenum) {
            endElement(pagenum);
        }

        @Override
        public void startPageNumberCitation(PageNumberCitation pageCite) {
            startElement(pageCite);
        }

        @Override
        public void endPageNumberCitation(PageNumberCitation pageCite) {
            endElement(pageCite);
        }

        @Override
        public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
            startElement(pageLast);
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
        public void startColumn(TableColumn tc) {
            startElement(tc);
        }

        @Override
        public void endColumn(TableColumn tc) {
            endElement(tc);
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
            startElement(tc);
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
            startElement(basicLink);
        }

        @Override
        public void endLink(BasicLink basicLink) {
            endElement(basicLink);
        }

        @Override
        public void image(ExternalGraphic eg) {
            startElement(eg);
            endElement(eg);
        }

        @Override
        public void startInstreamForeignObject(InstreamForeignObject ifo) {
            startElement(ifo);
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
        public void startLeader(Leader l) {
            startElement(l);
        }

        @Override
        public void endLeader(Leader l) {
            endElement(l);
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
            startElement(c);
            endElement(c);
        }

        private void startElement(FObj node) {
            addEvent("start ", node);
        }

        private void endElement(FObj node) {
            addEvent("end   ", node);
        }

        private void addEvent(String event, FObj node) {
            eventBuilder.append(event);
            eventBuilder.append(node.getLocalName());
            addID(node);
            actualEvents.add(eventBuilder.toString());
            eventBuilder.setLength(0);
        }

        private void addID(FObj node) {
            String id = node.getId();
            if (id != null && id.length() > 0) {
                eventBuilder.append(" id=\"");
                eventBuilder.append(id);
                eventBuilder.append("\"");
            }
        }
    }

    @Before
    public void setUp() throws IOException {
        setUpEvents();
        loadDocument();
        createDocumentParser();
    }

    private void setUpEvents() throws IOException {
        loadDocument();
        loadExpectedEvents();
        actualEvents = new ArrayList<String>(expectedEvents.size());
    }

    private void loadDocument() {
        document = getClass().getResourceAsStream("complete_document.fo");
    }

    private void loadExpectedEvents() throws IOException {
        expectedEvents = new ArrayList<String>();
        InputStream xslt = getClass().getResourceAsStream("extract-events.xsl");
        try {
            runXSLT(xslt);
        } finally {
            closeStream(xslt);
            closeStream(document);
        }
    }

    private void runXSLT(InputStream xslt) {
        Transformer transformer = createTransformer(xslt);
        Source fo = new StreamSource(document);
        Result result = createTransformOutputHandler();
        try {
            transformer.transform(fo, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private Transformer createTransformer(InputStream xslt) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            return transformerFactory.newTransformer(new StreamSource(xslt));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private Result createTransformOutputHandler() {
        return new SAXResult(new DefaultHandler() {

            private final StringBuilder event = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                event.setLength(0);
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                event.append(ch, start, length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                expectedEvents.add(event.toString());
            }

        });
    }

    private void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDocumentParser() {
        documentParser = FODocumentParser.newInstance(new FOEventHandlerFactory() {

            public FOEventHandler newFOEventHandler(FOUserAgent foUserAgent) {
                return new DelegatingFOEventHandler(
                        new DelegatingFOEventHandlerTester(foUserAgent)) {
                };
            }
        });
    }

    @Test
    public void testFOEventHandler() throws Exception {
        documentParser.parse(document);
        assertArrayEquals(expectedEvents.toArray(), actualEvents.toArray());
    }

    @After
    public void unloadDocument() throws IOException {
        document.close();
    }

    /**
     * Prints the given list to {@code System.out}, each element on a new line. For
     * debugging purpose.
     *
     * @param list a list
     */
    public void printList(List<?> list) {
        for (Object element : list) {
            System.out.println(element);
        }
    }

}
