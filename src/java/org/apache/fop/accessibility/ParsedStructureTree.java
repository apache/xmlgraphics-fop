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

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.fop.util.DelegatingContentHandler;

/**
 * A StructureTree implementation re-created from the structure stored in an IF
 * XML document.
 */
public class ParsedStructureTree implements StructureTree {

    private SAXTransformerFactory factory;

    private List pageSequenceStructures = new ArrayList();

    /**
     * Creates a new instance.
     *
     * @param factory a factory internally used to build the structures of page
     * sequences
     */
    public ParsedStructureTree(SAXTransformerFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns a ContenHandler for parsing the structure of a new page sequence.
     * It is assumed that page sequences are being parsed in the document order.
     * This class will automatically number the structure trees.
     *
     * @return a handler for parsing the &lt;structure-tree&gt; element and its
     * descendants
     * @throws SAXException if there is an error when creating the handler
     */
    public ContentHandler getHandlerForNextPageSequence() throws SAXException {
        TransformerHandler structureTreeBuilder;
        try {
            structureTreeBuilder = factory.newTransformerHandler();
        } catch (TransformerConfigurationException e) {
            throw new SAXException(e);
        }
        final DOMResult domResult = new DOMResult();
        structureTreeBuilder.setResult(domResult);
        return new DelegatingContentHandler(structureTreeBuilder) {

            public void characters(char[] ch, int start, int length) throws SAXException {
                /*
                 * There's not text node in the structure tree. This is just
                 * whitespace => ignore
                 */
            }

            public void endDocument() throws SAXException {
                super.endDocument();
                pageSequenceStructures.add(domResult.getNode().getFirstChild().getChildNodes());
            }
        };
    }

    /** {@inheritDoc} */
    public NodeList getPageSequence(int number) {
        return (NodeList) pageSequenceStructures.get(number - 1);
    }

}
