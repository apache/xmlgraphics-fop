/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
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

