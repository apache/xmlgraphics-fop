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
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.fop.util.DelegatingContentHandler;

/**
 * Helper class that re-builds a structure tree from what is stored in an
 * intermediate XML file (IF XML or Area Tree XML).
 */
public final class StructureTreeBuilder {

    private final SAXTransformerFactory factory;

    private final StructureTree structureTree = new StructureTree();

    /**
     * Creates a new instance.
     *
     * @param factory a factory internally used to build the structures of page
     * sequences
     */
    public StructureTreeBuilder(SAXTransformerFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the structure tree that will result from the parsing.
     *
     * @return the structure tree built by this object
     */
    public StructureTree getStructureTree() {
        return structureTree;
    }

    /**
     * Returns a ContenHandler for parsing the structure of a new page sequence.
     * It is assumed that page sequences are being parsed in the document order.
     *
     * @return a handler for parsing the &lt;structure-tree&gt; or
     * &lt;structureTree&gt; element and its descendants
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
                 * There's no text node in the structure tree. This is just
                 * whitespace => ignore
                 */
            }

            public void endDocument() throws SAXException {
                super.endDocument();
                structureTree.addPageSequenceStructure(domResult.getNode().getFirstChild()
                        .getChildNodes());
            }
        };
    }

}
