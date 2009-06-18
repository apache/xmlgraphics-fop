/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.xml;

import org.apache.fop.fo.FOUserAgent;
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
 */
public class XMLXMLHandler implements XMLHandler {
public static final String WRITER = "writer";

    public XMLXMLHandler() {
    }

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
     */
    public static void writeDocument(Document doc,
                                     Writer w) throws IOException {
        for (Node n = doc.getFirstChild(); n != null;
                n = n.getNextSibling()) {
            writeNode(n, w);
        }
    }

    /**
     * Writes a node using the given writer.
     */
    public static void writeNode(Node n, Writer w) throws IOException {
        switch (n.getNodeType()) {
            case Node.ELEMENT_NODE:
                w.write("<");
                w.write(n.getNodeName());

                if (n.hasAttributes()) {
                    NamedNodeMap attr = n.getAttributes();
                    int len = attr.getLength();
                    for (int i = 0; i < len; i++) {
                        Attr a = (Attr) attr.item(i);
                        w.write(" ");
                        w.write(a.getNodeName());
                        w.write("=\"");
                        w.write(contentToString(a.getNodeValue()));
                        w.write("\"");
                    }
                }

                Node c = n.getFirstChild();
                if (c != null) {
                    w.write(">");
                    for (; c != null; c = c.getNextSibling()) {
                        writeNode(c, w);
                    }
                    w.write("</");
                    w.write(n.getNodeName());
                    w.write(">");
                } else {
                    w.write("/>");
                }
                break;
            case Node.TEXT_NODE:
                w.write(contentToString(n.getNodeValue()));
                break;
            case Node.CDATA_SECTION_NODE:
                w.write("<![CDATA[");
                w.write(n.getNodeValue());
                w.write("]]>");
                break;
            case Node.ENTITY_REFERENCE_NODE:
                w.write("&");
                w.write(n.getNodeName());
                w.write(";");
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                w.write("<?");
                w.write(n.getNodeName());
                w.write(n.getNodeValue());
                w.write("?>");
                break;
            case Node.COMMENT_NODE:
                w.write("<!--");
                w.write(n.getNodeValue());
                w.write("-->");
                break;
            case Node.DOCUMENT_TYPE_NODE:
                break;
            default:
                throw new Error("Internal error (" + n.getNodeType() + ")");
        }
    }

    /**
     * Returns the given content value transformed to replace invalid
     * characters with entities.
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

