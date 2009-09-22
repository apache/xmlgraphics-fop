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

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A reduced version of the document's FO tree, containing only its logical
 * structure. Used by accessible output formats.
 */
public final class StructureTree {

    private final Node reducedFOTree;

    private static class NamespaceContextImpl implements NamespaceContext {

        private String uri;
        private String prefix;

        public NamespaceContextImpl() {
        }

        public NamespaceContextImpl(String prefix, String uri) {
            this.uri = uri;
            this.prefix = prefix;
        }

        public String getNamespaceURI(String prefix) {
            return uri;
        }

        public void setNamespaceURI(String uri) {
            this.uri = uri;
        }

        public String getPrefix(String uri) {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public Iterator getPrefixes(String uri) {
            return null;
        }
    }

    StructureTree(Node reducedFOTree) {
        this.reducedFOTree = reducedFOTree;
    }

    /**
     * Returns the list of nodes that are the children of the given page sequence.
     *
     * @param number number of the page sequence, 1-based
     * @return its children nodes
     */
    public NodeList getPageSequence(int number) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NamespaceContext namespaceContext = new NamespaceContextImpl("fo",
                "http://www.w3.org/1999/XSL/Format");
        xpath.setNamespaceContext(namespaceContext);
        String xpathExpr = "/fo:root/fo:page-sequence[" + Integer.toString(number) + "]/*";

        try {
            return (NodeList) xpath.evaluate(xpathExpr, reducedFOTree, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }
}
