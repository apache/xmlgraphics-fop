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

package org.apache.fop.render.xml;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.BookmarkData;
import org.apache.fop.area.OffDocumentExtensionAttachment;
import org.apache.fop.area.OffDocumentItem;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.RendererContext;

/** Abstract xml renderer base class. */
public abstract class AbstractXMLRenderer extends PrintRenderer {

    /**
     * @param userAgent the user agent that contains configuration details. This cannot be null.
     */
    public AbstractXMLRenderer(FOUserAgent userAgent) {
        super(userAgent);
    }

    /** Main namespace in use. */
    public static final String NS = "";

    /** CDATA type */
    public static final String CDATA = "CDATA";

    /** An empty Attributes object used when no attributes are needed. */
    public static final Attributes EMPTY_ATTS = new AttributesImpl();

    /** AttributesImpl instance that can be used during XML generation. */
    protected AttributesImpl atts = new AttributesImpl();

    /** ContentHandler that the generated XML is written to */
    protected ContentHandler handler;

    /** The OutputStream to write the generated XML to. */
    protected OutputStream out;

    /** The renderer context. */
    protected RendererContext context;

    /** A list of ExtensionAttachements received through processOffDocumentItem() */
    protected List extensionAttachments;

    /**
     * Handles SAXExceptions.
     * @param saxe the SAXException to handle
     */
    protected void handleSAXException(SAXException saxe) {
        throw new RuntimeException(saxe.getMessage());
    }

    /**
     * Handles page extension attachments
     * @param page the page viewport
     */
    protected void handlePageExtensionAttachments(PageViewport page) {
        handleExtensionAttachments(page.getExtensionAttachments());
    }

    /**
     * Writes a comment to the generated XML.
     * @param comment the comment
     */
    protected void comment(String comment) {
        if (handler instanceof LexicalHandler) {
            try {
                ((LexicalHandler) handler).comment(comment.toCharArray(), 0, comment.length());
            } catch (SAXException saxe) {
                handleSAXException(saxe);
            }
        }
    }

    /**
     * Starts a new element (without attributes).
     * @param tagName tag name of the element
     */
    protected void startElement(String tagName) {
        startElement(tagName, EMPTY_ATTS);
    }

    /**
     * Starts a new element.
     * @param tagName tag name of the element
     * @param atts attributes to add
     */
    protected void startElement(String tagName, Attributes atts) {
        try {
            handler.startElement(NS, tagName, tagName, atts);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Ends an element.
     * @param tagName tag name of the element
     */
    protected void endElement(String tagName) {
        try {
            handler.endElement(NS, tagName, tagName);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Sends plain text to the XML
     * @param text the text
     */
    protected void characters(String text) {
        try {
            char[] ca = text.toCharArray();
            handler.characters(ca, 0, ca.length);
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(String name, String value) {
        atts.addAttribute(NS, name, name, CDATA, value);
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(QName name, String value) {
        atts.addAttribute(name.getNamespaceURI(), name.getLocalName(), name.getQName(),
                CDATA, value);
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param value value of the attribute
     */
    protected void addAttribute(String name, int value) {
        addAttribute(name, Integer.toString(value));
    }

    private String createString(Rectangle2D rect) {
        return "" + (int) rect.getX() + " " + (int) rect.getY() + " "
                  + (int) rect.getWidth() + " " + (int) rect.getHeight();
    }

    /**
     * Adds a new attribute to the protected member variable "atts".
     * @param name name of the attribute
     * @param rect a Rectangle2D to format and use as attribute value
     */
    protected void addAttribute(String name, Rectangle2D rect) {
        addAttribute(name, createString(rect));
    }

    /** {@inheritDoc} */
    public void startRenderer(OutputStream outputStream)
                throws IOException {
        if (this.handler == null) {
            SAXTransformerFactory factory
                = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
            try {
                TransformerHandler transformerHandler = factory.newTransformerHandler();
                setContentHandler(transformerHandler);
                StreamResult res = new StreamResult(outputStream);
                transformerHandler.setResult(res);
            } catch (TransformerConfigurationException tce) {
                throw new RuntimeException(tce.getMessage());
            }
            this.out = outputStream;
        }

        try {
            handler.startDocument();
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
    }

    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        try {
            handler.endDocument();
        } catch (SAXException saxe) {
            handleSAXException(saxe);
        }
        if (this.out != null) {
            this.out.flush();
        }
    }

    /** {@inheritDoc} */
    public void processOffDocumentItem(OffDocumentItem oDI) {
        if (oDI instanceof BookmarkData) {
            renderBookmarkTree((BookmarkData) oDI);
        } else if (oDI instanceof OffDocumentExtensionAttachment) {
            ExtensionAttachment attachment = ((OffDocumentExtensionAttachment)oDI).getAttachment();
            if (extensionAttachments == null) {
                extensionAttachments = new java.util.ArrayList();
            }
            extensionAttachments.add(attachment);
        } else {
            String warn = "Ignoring OffDocumentItem: " + oDI;
            log.warn(warn);
        }
    }

    /** Handle document extension attachments. */
    protected void handleDocumentExtensionAttachments() {
        if (extensionAttachments != null && extensionAttachments.size() > 0) {
            handleExtensionAttachments(extensionAttachments);
            extensionAttachments.clear();
        }
    }

    /**
     * Sets an outside TransformerHandler to use instead of the default one
     * create in this class in startRenderer().
     * @param handler Overriding TransformerHandler
     */
    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    /**
     * Handles a list of extension attachments
     * @param attachments a list of extension attachments
     */
    protected abstract void handleExtensionAttachments(List attachments);

    /**
     * Renders a bookmark tree
     * @param odi the bookmark data
     */
    protected abstract void renderBookmarkTree(BookmarkData odi);
}
