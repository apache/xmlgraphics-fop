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

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FODocumentParser;
import org.apache.fop.fo.FODocumentParser.FOEventHandlerFactory;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.LoadingException;
import org.apache.fop.fotreetest.DummyFOEventHandler;

public class FO2StructureTreeConverterTestCase {

    private static class IgnoringPtrDifferenceListener implements DifferenceListener {

        public int differenceFound(Difference difference) {
            switch (difference.getId()) {
            case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
                return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
            case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
                String additionalAttribute = difference.getTestNodeDetail().getValue();
                if (additionalAttribute.equals("ptr")) {
                    return RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
                } else {
                    return RETURN_ACCEPT_DIFFERENCE;
                }
            default:
                return RETURN_ACCEPT_DIFFERENCE;
            }
        }

        public void skippedComparison(Node control, Node test) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    private static final String STRUCTURE_TREE_SEQUENCE_NAME = "structure-tree-sequence";

    @Test
    public void testConverter() throws Exception {
        DOMResult expectedStructureTree = loadExpectedStructureTree();
        DOMResult actualStructureTree = buildActualStructureTree();
        final Diff diff = createDiff(expectedStructureTree, actualStructureTree);
        assertTrue(diff.toString(), diff.similar());
    }

    private static DOMResult loadExpectedStructureTree() {
        DOMResult expectedStructureTree = new DOMResult();
        runXSLT(getXsltInputStream(), getFoInputStream(), expectedStructureTree);
        return expectedStructureTree;
    }

    private static InputStream getXsltInputStream() {
        return FO2StructureTreeConverterTestCase.class.getResourceAsStream("foToIfStructureTree.xsl");
    }

    private static InputStream getFoInputStream() {
        return FO2StructureTreeConverterTestCase.class.getResourceAsStream(
                "/org/apache/fop/fo/complete_document.fo");
    }

    private static void runXSLT(InputStream xslt, InputStream doc, Result result) {
        Source fo = new StreamSource(doc);
        try {
            Transformer transformer = TransformerFactory.newInstance()
                    .newTransformer(new StreamSource(xslt));
            transformer.transform(fo, result);
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        } finally {
            closeStream(xslt);
            closeStream(doc);
        }
    }

    private static void closeStream(InputStream stream) {
        try {
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static DOMResult buildActualStructureTree() throws Exception {
        DOMResult actualStructureTree = new DOMResult();
        createStructureTreeFromDocument(getFoInputStream(), actualStructureTree);
        return actualStructureTree;
    }

    private static void createStructureTreeFromDocument(InputStream foInputStream,
            DOMResult domResult) throws Exception {
        TransformerHandler tHandler = createTransformerHandler(domResult);
        startStructureTreeSequence(tHandler);
        StructureTreeEventHandler structureTreeEventHandler
                = StructureTree2SAXEventAdapter.newInstance(tHandler);
        FODocumentParser documentParser = createDocumentParser(structureTreeEventHandler);
        FOUserAgent userAgent = createFOUserAgent(documentParser);
        parseDocument(foInputStream, documentParser, userAgent);
        endStructureTreeSequence(tHandler);
    }

    private static TransformerHandler createTransformerHandler(DOMResult domResult)
            throws TransformerConfigurationException, TransformerFactoryConfigurationError {
        SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
        TransformerHandler transformerHandler = factory.newTransformerHandler();
        transformerHandler.setResult(domResult);
        return transformerHandler;
    }

    private static void startStructureTreeSequence(TransformerHandler tHandler) throws SAXException {
        tHandler.startDocument();
        tHandler.startElement("", STRUCTURE_TREE_SEQUENCE_NAME, STRUCTURE_TREE_SEQUENCE_NAME,
                new AttributesImpl());
    }

    private static FODocumentParser createDocumentParser(
            final StructureTreeEventHandler structureTreeEventHandler) {
        return FODocumentParser.newInstance(new FOEventHandlerFactory() {
            public FOEventHandler newFOEventHandler(FOUserAgent foUserAgent) {
                return new FO2StructureTreeConverter(structureTreeEventHandler,
                        new DummyFOEventHandler(foUserAgent));
            }
        });
    }

    private static FOUserAgent createFOUserAgent(FODocumentParser documentParser) {
        FOUserAgent userAgent = documentParser.createFOUserAgent();
        userAgent.setAccessibility(true);
        return userAgent;
    }

    private static void parseDocument(InputStream foInputStream, FODocumentParser documentParser,
            FOUserAgent userAgent) throws FOPException, LoadingException {
        try {
            documentParser.parse(foInputStream, userAgent);
        } finally {
            closeStream(foInputStream);
        }
    }

    private static void endStructureTreeSequence(TransformerHandler tHandler) throws SAXException {
        tHandler.endElement("", STRUCTURE_TREE_SEQUENCE_NAME, STRUCTURE_TREE_SEQUENCE_NAME);
        tHandler.endDocument();
    }

    private static Diff createDiff(DOMResult expected, DOMResult actual) {
        Diff diff = new Diff(getDocument(expected), getDocument(actual));
        diff.overrideDifferenceListener(new IgnoringPtrDifferenceListener());
        return diff;
    }

    private static Document getDocument(DOMResult result) {
        return (Document) result.getNode();
    }
}
