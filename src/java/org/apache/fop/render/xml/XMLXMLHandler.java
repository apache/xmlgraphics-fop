/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.fop.render.XMLHandler;
import org.apache.fop.render.RendererContext;

import org.apache.batik.dom.util.DOMUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

import java.io.Writer;
import java.io.IOException;

/**
 * XML handler for the XML renderer.
 */
public class XMLXMLHandler implements XMLHandler {
    
    /** Key for getting the Writer from the RendererContext */
    public static final String WRITER = "writer";

    /**
     * @see org.apache.fop.render.XMLHandler#handleXML(RendererContext, Document, String)
     */
    public void handleXML(RendererContext context, Document doc,
                          String ns) throws Exception {
        Writer writer = (Writer) context.getProperty(WRITER);

        String svg = "http://www.w3.org/2000/svg";
        // actually both do the same thing but one requires
        // batik
        if (svg.equals(ns)) {
            DOMUtilities.writeDocument(doc, writer);
        } else {
            writeDocument(doc, writer);
        }
        writer.write("\n");
    }

    /**
     * Writes the given document using the given writer.
     * @param doc DOM document
     * @param writer Writer to write to
     * @throws IOException In case of an I/O problem
     */
    public static void writeDocument(Document doc,
                                     Writer writer) throws IOException {
        for (Node n = doc.getFirstChild(); n != null;
                n = n.getNextSibling()) {
            writeNode(n, writer);
        }
    }

    /**
     * Writes a node using the given writer.
     * @param node node to serialize
     * @param writer Writer to write to
     * @throws IOException In case of an I/O problem
     */
    public static void writeNode(Node node, Writer writer) throws IOException {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                writer.write("<");
                writer.write(node.getNodeName());

                if (node.hasAttributes()) {
                    NamedNodeMap attr = node.getAttributes();
                    int len = attr.getLength();
                    for (int i = 0; i < len; i++) {
                        Attr a = (Attr) attr.item(i);
                        writer.write(" ");
                        writer.write(a.getNodeName());
                        writer.write("=\"");
                        writer.write(contentToString(a.getNodeValue()));
                        writer.write("\"");
                    }
                }

                Node c = node.getFirstChild();
                if (c != null) {
                    writer.write(">");
                    for (; c != null; c = c.getNextSibling()) {
                        writeNode(c, writer);
                    }
                    writer.write("</");
                    writer.write(node.getNodeName());
                    writer.write(">");
                } else {
                    writer.write("/>");
                }
                break;
            case Node.TEXT_NODE:
                writer.write(contentToString(node.getNodeValue()));
                break;
            case Node.CDATA_SECTION_NODE:
                writer.write("<![CDATA[");
                writer.write(node.getNodeValue());
                writer.write("]]>");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                writer.write("&");
                writer.write(node.getNodeName());
                writer.write(";");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                writer.write("<?");
                writer.write(node.getNodeName());
                writer.write(node.getNodeValue());
                writer.write("?>");
                break;
            case Node.COMMENT_NODE:
                writer.write("<!--");
                writer.write(node.getNodeValue());
                writer.write("-->");
                break;
            case Node.DOCUMENT_TYPE_NODE:
                break;
            default:
                throw new IllegalArgumentException("Unexpected node type (" 
                        + node.getNodeType() + ")");
        }
    }

    /**
     * Returns the given content value transformed to replace invalid
     * characters with entities.
     * @param s content value
     * @return encoded value
     */
    public static String contentToString(String s) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                case '\'':
                    result.append("&apos;");
                    break;
                default:
                    result.append(c);
            }
        }

        return result.toString();
    }

}

