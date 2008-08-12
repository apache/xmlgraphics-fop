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

package org.apache.fop.render.intermediate;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.fop.util.DOMBuilderContentHandlerFactory;
import org.apache.fop.util.DefaultErrorListener;
import org.apache.fop.util.XMLUtil;

/**
 * This is a parser for the intermediate format XML which converts the intermediate file into
 * {@code IFPainter} events.
 */
public class IFParser implements IFConstants {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(IFParser.class);

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /**
     * Parses an intermediate file and paints it.
     * @param src the Source instance pointing to the intermediate file
     * @param painter the intermediate format painter used to process the IF events
     * @param userAgent the user agent
     * @throws TransformerException if an error occurs while parsing the area tree XML
     */
    public void parse(Source src, IFPainter painter, FOUserAgent userAgent)
            throws TransformerException {
        Transformer transformer = tFactory.newTransformer();
        transformer.setErrorListener(new DefaultErrorListener(log));

        SAXResult res = new SAXResult(getContentHandler(painter, userAgent));

        transformer.transform(src, res);
    }

    /**
     * Creates a new ContentHandler instance that you can send the area tree XML to. The parsed
     * pages are added to the AreaTreeModel instance you pass in as a parameter.
     * @param painter the intermediate format painter used to process the IF events
     * @param userAgent the user agent
     * @return the ContentHandler instance to receive the SAX stream from the area tree XML
     */
    public ContentHandler getContentHandler(IFPainter painter, FOUserAgent userAgent) {
        ElementMappingRegistry elementMappingRegistry
            = userAgent.getFactory().getElementMappingRegistry();
        return new Handler(painter, userAgent, elementMappingRegistry);
    }

    private static class Handler extends DefaultHandler {

        private Map elementHandlers = new java.util.HashMap();

        private IFPainter painter;
        private FOUserAgent userAgent;
        private ElementMappingRegistry elementMappingRegistry;

        private Attributes lastAttributes;

        private StringBuffer content = new StringBuffer();
        private boolean ignoreCharacters = true;

        //private Stack delegateStack = new Stack();
        private int delegateDepth;
        private ContentHandler delegate;
        private boolean inForeignObject;
        private Document foreignObject;

        public Handler(IFPainter painter, FOUserAgent userAgent,
                ElementMappingRegistry elementMappingRegistry) {
            this.painter = painter;
            this.userAgent = userAgent;
            this.elementMappingRegistry = elementMappingRegistry;
            elementHandlers.put(EL_DOCUMENT, new DocumentHandler());
            elementHandlers.put(EL_HEADER, new DocumentHeaderHandler());
            elementHandlers.put(EL_PAGE_SEQUENCE, new PageSequenceHandler());
            elementHandlers.put(EL_PAGE, new PageHandler());
            elementHandlers.put(EL_PAGE_HEADER, new PageHeaderHandler());
            elementHandlers.put(EL_PAGE_CONTENT, new PageContentHandler());
            elementHandlers.put(EL_PAGE_TRAILER, new PageTrailerHandler());
            //Page content
            elementHandlers.put(EL_VIEWPORT, new ViewportHandler());
            elementHandlers.put(EL_GROUP, new GroupHandler());
            elementHandlers.put("font", new FontHandler());
            elementHandlers.put("text", new TextHandler());
            elementHandlers.put("rect", new RectHandler());
            elementHandlers.put(EL_IMAGE, new ImageHandler());
        }


        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
            if (delegate != null) {
                //delegateStack.push(qName);
                delegateDepth++;
                delegate.startElement(uri, localName, qName, attributes);
            } else {
                boolean handled = true;
                if (NAMESPACE.equals(uri)) {
                    lastAttributes = new AttributesImpl(attributes);
                    ElementHandler elementHandler = (ElementHandler)elementHandlers.get(localName);
                    content.setLength(0);
                    ignoreCharacters = true;
                    if (elementHandler != null) {
                        ignoreCharacters = elementHandler.ignoreCharacters();
                        try {
                            elementHandler.startElement(attributes);
                        } catch (IFException ife) {
                            handleIFException(ife);
                        }
                    } else if ("extension-attachments".equals(localName)) {
                        //TODO implement me
                    } else {
                        handled = false;
                    }
                } else {
                    ContentHandlerFactoryRegistry registry
                            = userAgent.getFactory().getContentHandlerFactoryRegistry();
                    ContentHandlerFactory factory = registry.getFactory(uri);
                    if (factory == null) {
                        DOMImplementation domImplementation
                            = elementMappingRegistry.getDOMImplementationForNamespace(uri);
                        if (domImplementation == null) {
                            domImplementation = ElementMapping.getDefaultDOMImplementation();
                            /*
                            throw new SAXException("No DOMImplementation could be"
                                    + " identified to handle namespace: " + uri);
                                    */
                        }
                        factory = new DOMBuilderContentHandlerFactory(uri, domImplementation);
                    }
                    delegate = factory.createContentHandler();
                    delegateDepth++;
                    delegate.startDocument();
                    delegate.startElement(uri, localName, qName, attributes);
                }
                if (!handled) {
                    if (uri == null || uri.length() == 0) {
                        throw new SAXException("Unhandled element " + localName
                                + " in namespace: " + uri);
                    } else {
                        log.warn("Unhandled element " + localName
                                + " in namespace: " + uri);
                    }
                }
            }
        }

        private void handleIFException(IFException ife) throws SAXException {
            if (ife.getCause() instanceof SAXException) {
                //unwrap
                throw (SAXException)ife.getCause();
            } else {
                //wrap
                throw new SAXException(ife);
            }
        }


        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (delegate != null) {
                delegate.endElement(uri, localName, qName);
                delegateDepth--;
                if (delegateDepth == 0) {
                    delegate.endDocument();
                    if (delegate instanceof ContentHandlerFactory.ObjectSource) {
                        Object obj = ((ContentHandlerFactory.ObjectSource)delegate).getObject();
                        if (inForeignObject) {
                            this.foreignObject = (Document)obj;
                        } else {
                            handleExternallyGeneratedObject(obj);
                        }
                    }
                    delegate = null; //Sub-document is processed, return to normal processing
                }
            } else {
                if (NAMESPACE.equals(uri)) {
                    ElementHandler elementHandler = (ElementHandler)elementHandlers.get(localName);
                    if (elementHandler != null) {
                        try {
                            elementHandler.endElement();
                        } catch (IFException ife) {
                            handleIFException(ife);
                        }
                        content.setLength(0);
                    }
                    ignoreCharacters = true;
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace("Ignoring " + localName + " in namespace: " + uri);
                    }
                }
            }
        }

        // ============== Element handlers for the intermediate format =============

        private static interface ElementHandler {
            void startElement(Attributes attributes) throws IFException, SAXException;
            void endElement() throws IFException;
            boolean ignoreCharacters();
        }

        private abstract class AbstractElementHandler implements ElementHandler {

            public void startElement(Attributes attributes) throws IFException, SAXException {
                //nop
            }

            public void endElement() throws IFException {
                //nop
            }

            public boolean ignoreCharacters() {
                return true;
            }
        }

        private class DocumentHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter.startDocument();
            }

            public void endElement() throws IFException {
                painter.endDocument();
            }

        }

        private class DocumentHeaderHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter.startDocumentHeader();
            }

            public void endElement() throws IFException {
                painter.endDocumentHeader();
            }

        }

        private class PageSequenceHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                String id = attributes.getValue("id");
                painter.startPageSequence(id);
            }

            public void endElement() throws IFException {
                painter.endPageSequence();
            }

        }

        private class PageHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int index = Integer.parseInt(attributes.getValue("index"));
                String name = attributes.getValue("name");
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                painter.startPage(index, name, new Dimension(width, height));
            }

            public void endElement() throws IFException {
                painter.endPage();
            }

        }

        private class PageHeaderHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter.startPageHeader();
            }

            public void endElement() throws IFException {
                painter.endPageHeader();
            }

        }

        private class PageContentHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter.startPageContent();
            }

            public void endElement() throws IFException {
                painter.endPageContent();
            }

        }

        private class PageTrailerHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter.startPageTrailer();
            }

            public void endElement() throws IFException {
                painter.endPageTrailer();
            }

        }

        private class ViewportHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                String transform = attributes.getValue("transform");
                AffineTransform[] transforms
                    = AffineTransformArrayParser.createAffineTransform(transform);
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                Rectangle clipRect = XMLUtil.getAttributeAsRectangle(attributes, "clip-rect");
                painter.startViewport(transforms, new Dimension(width, height), clipRect);
            }

            public void endElement() throws IFException {
                painter.endViewport();
            }

        }

        private class GroupHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                String transform = attributes.getValue("transform");
                AffineTransform[] transforms
                    = AffineTransformArrayParser.createAffineTransform(transform);
                painter.startGroup(transforms);
            }

            public void endElement() throws IFException {
                painter.endGroup();
            }

        }

        private class FontHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                String family = attributes.getValue("family");
                String style = attributes.getValue("style");
                Integer weight = XMLUtil.getAttributeAsInteger(attributes, "weight");
                String variant = attributes.getValue("variant");
                Integer size = XMLUtil.getAttributeAsInteger(attributes, "size");
                Color color;
                try {
                    color = getAttributeAsColor(attributes, "color");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the color attribute", pe);
                }
                painter.setFont(family, style, weight, variant, size, color);
            }

        }

        private class TextHandler extends AbstractElementHandler {

            public void endElement() throws IFException {
                int x = Integer.parseInt(lastAttributes.getValue("x"));
                int y = Integer.parseInt(lastAttributes.getValue("y"));
                int[] dx = XMLUtil.getAttributeAsIntArray(lastAttributes, "dx");
                int[] dy = XMLUtil.getAttributeAsIntArray(lastAttributes, "dy");
                painter.drawText(x, y, dx, dy, content.toString());
            }

            public boolean ignoreCharacters() {
                return false;
            }

        }

        private class RectHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int x = Integer.parseInt(lastAttributes.getValue("x"));
                int y = Integer.parseInt(lastAttributes.getValue("y"));
                int width = Integer.parseInt(lastAttributes.getValue("width"));
                int height = Integer.parseInt(lastAttributes.getValue("height"));
                Color fillColor;
                try {
                    fillColor = getAttributeAsColor(attributes, "fill");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the fill attribute", pe);
                }
                Color strokeColor;
                try {
                    strokeColor = getAttributeAsColor(attributes, "stroke");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the stroke attribute", pe);
                }
                painter.drawRect(new Rectangle(x, y, width, height), fillColor, strokeColor);
            }

        }

        private class ImageHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                inForeignObject = true;
            }

            public void endElement() throws IFException {
                int x = Integer.parseInt(lastAttributes.getValue("x"));
                int y = Integer.parseInt(lastAttributes.getValue("y"));
                int width = Integer.parseInt(lastAttributes.getValue("width"));
                int height = Integer.parseInt(lastAttributes.getValue("height"));
                Map foreignAttributes = getForeignAttributes(lastAttributes);
                if (foreignObject != null) {
                    painter.drawImage(foreignObject,
                            new Rectangle(x, y, width, height), foreignAttributes);
                    foreignObject = null;
                } else {
                    String uri = lastAttributes.getValue(
                            XLINK_HREF.getNamespaceURI(), XLINK_HREF.getLocalName());
                    if (uri == null) {
                        throw new IFException("xlink:href is missing on image", null);
                    }
                    painter.drawImage(uri, new Rectangle(x, y, width, height), foreignAttributes);
                }
                inForeignObject = false;
            }

            public boolean ignoreCharacters() {
                return false;
            }
        }


        // ====================================================================


        private void assertObjectOfClass(Object obj, Class clazz) {
            if (!clazz.isInstance(obj)) {
                throw new IllegalStateException("Object is not an instance of "
                        + clazz.getName() + " but of " + obj.getClass().getName());
            }
        }

        /**
         * Handles objects created by "sub-parsers" that implement the ObjectSource interface.
         * An example of object handled here are ExtensionAttachments.
         * @param obj the Object to be handled.
         * @throws SAXException if an error occurs while handling the extension object
         */
        protected void handleExternallyGeneratedObject(Object obj) throws SAXException {
            try {
                painter.handleExtensionObject(obj);
            } catch (IFException ife) {
                handleIFException(ife);
            }
        }

        private Color getAttributeAsColor(Attributes attributes, String name)
                    throws PropertyException {
            String s = attributes.getValue(name);
            if (s == null) {
                return null;
            } else {
                return ColorUtil.parseColorString(userAgent, s);
            }
        }

        private static Map getForeignAttributes(Attributes atts) {
            Map foreignAttributes = null;
            for (int i = 0, c = atts.getLength(); i < c; i++) {
                String ns = atts.getURI(i);
                if (ns.length() > 0) {
                    if ("http://www.w3.org/2000/xmlns/".equals(ns)) {
                        continue;
                    } else if (NAMESPACE.equals(ns)) {
                        continue;
                    } else if (XLINK_NAMESPACE.equals(ns)) {
                        continue;
                    }
                    if (foreignAttributes == null) {
                        foreignAttributes = new java.util.HashMap();
                    }
                    QName qname = new QName(ns, atts.getQName(i));
                    foreignAttributes.put(qname, atts.getValue(i));
                }
            }
            return foreignAttributes;
        }

        /** {@inheritDoc} */
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (delegate != null) {
                delegate.characters(ch, start, length);
            } else if (!ignoreCharacters) {
                this.content.append(ch, start, length);
            }
        }
    }
}
