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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A reduced version of the document's FO tree, containing only its logical
 * structure. Used by accessible output formats.
 */
public final class StructureTree {

    private final List pageSequenceStructures = new ArrayList();

    /**
     * Package-private default constructor.
     */
    public StructureTree() { }

    private static boolean flowOrStaticContentNodes(NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                return false;
            }
            String name = node.getLocalName();
            if (!(name.equals("flow") || name.equals("static-content"))) {
                return false;
            }
        }
        return true;
    }

    void addPageSequenceStructure(NodeList structureTree) {
        assert flowOrStaticContentNodes(structureTree);
        pageSequenceStructures.add(structureTree);
    }

    /**
     * Returns the list of nodes that are the children of the given page sequence.
     *
     * @param index index of the page sequence, 0-based
     * @return its children nodes
     */
    public NodeList getPageSequence(int index) {
        return (NodeList) pageSequenceStructures.get(index);
    }

    /**
     * Returns an XML-like representation of the structure trees.
     * <p>
     * <strong>Note:</strong> use only for debugging purpose, as this method
     * performs non-trivial operations.
     * </p>
     * @return a string representation of this object
     */
    public String toString() {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            Writer str = new StringWriter();
            for (Iterator iter = pageSequenceStructures.iterator(); iter.hasNext();) {
                NodeList nodes = (NodeList) iter.next();
                for (int i = 0, c = nodes.getLength(); i < c; i++) {
                    t.transform(new DOMSource(nodes.item(i)), new StreamResult(str));
                }
            }
            return str.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }

}
