/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
 
package org.apache.fop.render.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.transform.sax.TransformerHandler;

/**
 * XML handler for the XML renderer.
 */
public class XMLXMLHandler implements XMLHandler {
    
    /** Key for getting the TransformerHandler from the RendererContext */
    public static final String HANDLER = "handler";

    /** Logging instance */
    private static Log log = LogFactory.getLog(XMLXMLHandler.class);
    
    private AttributesImpl atts = new AttributesImpl();
    
    /** @see org.apache.fop.render.XMLHandler */
    public void handleXML(RendererContext context, 
                org.w3c.dom.Document doc, String ns) throws Exception {
        TransformerHandler handler = (TransformerHandler) context.getProperty(HANDLER);

        writeDocument(doc, handler);
    }

    /**
     * Writes the given document using the given TransformerHandler.
     * @param doc DOM document
     * @param handler TransformerHandler to write to
     * @throws SAXException In case of a problem while writing XML
     */
    public void writeDocument(Document doc,
                                     TransformerHandler handler) throws SAXException {
        for (Node n = doc.getFirstChild(); n != null;
                n = n.getNextSibling()) {
            writeNode(n, handler);
        }
    }

    /**
     * Writes a node using the given writer.
     * @param node node to serialize
     * @param handler TransformerHandler to write to
     * @throws SAXException In case of a problem while writing XML
     */
    public void writeNode(Node node, TransformerHandler handler) throws SAXException {
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
                        writeNode(c, handler);
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
                handler.startCDATA();
                handler.characters(ca, 0, ca.length);
                handler.endCDATA();
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
                handler.comment(ca, 0, ca.length);
                break;
            case Node.DOCUMENT_TYPE_NODE:
                break;
            default:
                throw new IllegalArgumentException("Unexpected node type (" 
                        + node.getNodeType() + ")");
        }
    }

    /** @see org.apache.fop.render.XMLHandler#supportsRenderer(org.apache.fop.render.Renderer) */
    public boolean supportsRenderer(Renderer renderer) {
        return (renderer instanceof XMLRenderer);
    }

    /** @see org.apache.fop.render.XMLHandler#getNamespace() */
    public String getNamespace() {
        return null; //Handle all XML content
    }

}

