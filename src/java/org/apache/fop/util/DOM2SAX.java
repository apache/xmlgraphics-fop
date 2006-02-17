/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Helper class that produces a SAX stream from a DOM Document.
 */
public class DOM2SAX {

    /** Logging instance */
    private static Log log = LogFactory.getLog(DOM2SAX.class);

    /**
     * Writes the given document using the given TransformerHandler.
     * @param doc DOM document
     * @param handler TransformerHandler to write to
     * @throws SAXException In case of a problem while writing XML
     */
    public static void writeDocument(Document doc,
                                     ContentHandler handler) throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        for (Node n = doc.getFirstChild(); n != null;
                n = n.getNextSibling()) {
            writeNode(n, handler, atts);
        }
    }

    /**
     * Writes a node using the given writer.
     * @param node node to serialize
     * @param handler ContentHandler to write to
     * @param atts AttributesImpl instance that is reused during SAX event generation
     * @throws SAXException In case of a problem while writing XML
     */
    private static void writeNode(Node node, ContentHandler handler, AttributesImpl atts) 
                throws SAXException {
        char[] ca;
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                atts.clear();

                if (node.hasAttributes()) {
                    NamedNodeMap attr = node.getAttributes();
                    int len = attr.getLength();
                    for (int i = 0; i < len; i++) {
                        Attr a = (Attr) attr.item(i);
                        atts.addAttribute("", a.getNodeName(), a.getNodeName(),
                                "CDATA", a.getNodeValue());
                    }
                }
                handler.startElement(node.getNamespaceURI(),
                        node.getLocalName(), node.getLocalName(), atts);

                Node c = node.getFirstChild();
                if (c != null) {
                    for (; c != null; c = c.getNextSibling()) {
                        writeNode(c, handler, atts);
                    }
                }
                handler.endElement(node.getNamespaceURI(), node.getNodeName(), node.getNodeName());
                break;
            case Node.TEXT_NODE:
                ca = node.getNodeValue().toCharArray();
                handler.characters(ca, 0, ca.length);
                break;
            case Node.CDATA_SECTION_NODE:
                ca = node.getNodeValue().toCharArray();
                if (handler instanceof LexicalHandler) {
                    LexicalHandler lh = (LexicalHandler)handler;
                    lh.startCDATA();
                    handler.characters(ca, 0, ca.length);
                    lh.endCDATA();
                } else {
                    handler.characters(ca, 0, ca.length);
                }
                break;
            case Node.ENTITY_REFERENCE_NODE:
                log.warn("Ignoring ENTITY_REFERENCE_NODE. NYI");
                /*
                writer.write("&");
                writer.write();
                writer.write(";");
                */
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                handler.processingInstruction(node.getNodeName(), node.getNodeValue());
                break;
            case Node.COMMENT_NODE:
                ca = node.getNodeValue().toCharArray();
                if (handler instanceof LexicalHandler) {
                    LexicalHandler lh = (LexicalHandler)handler;
                    lh.comment(ca, 0, ca.length);
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                break;
            default:
                throw new IllegalArgumentException("Unexpected node type ("
                        + node.getNodeType() + ")");
        }
    }

    
}
