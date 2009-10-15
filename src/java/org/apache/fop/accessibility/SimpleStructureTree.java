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

import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A StructureTree implementation created from the reduced FO tree, in the form
 * of a single DOM document obtained by XSL Transformation of the original FO
 * tree.
 */
final class SimpleStructureTree implements StructureTree {

    private final Node reducedFOTree;

    SimpleStructureTree(Node reducedFOTree) {
        this.reducedFOTree = reducedFOTree;
    }

    /** {@inheritDoc} */
    public NodeList getPageSequence(int number) {
        Node pageSequence = reducedFOTree.getFirstChild().getChildNodes().item(number - 1);
        assert pageSequence.getNodeType() == Node.ELEMENT_NODE
                && pageSequence.getLocalName().equals("page-sequence");
        return pageSequence.getChildNodes();
    }

    /** {@inheritDoc} */
    public String toString() {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Writer str = new StringWriter();
            t.transform(new DOMSource(reducedFOTree), new StreamResult(str));
            return str.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

}
