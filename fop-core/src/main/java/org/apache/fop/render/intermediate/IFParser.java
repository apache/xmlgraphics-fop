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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

import org.apache.fop.accessibility.AccessibilityEventProducer;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.ElementMapping;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationExtensionConstants;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationHandler;
import org.apache.fop.render.intermediate.extensions.GoToXYAction;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.fop.util.DOMBuilderContentHandlerFactory;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.StringUtils;
import org.apache.fop.util.XMLUtil;
import org.apache.fop.utils.DefaultErrorListener;

/**
 * This is a parser for the intermediate format XML which converts the intermediate file into
 * {@link IFPainter} events.
 */
public class IFParser implements IFConstants {

    /** Logger instance */
    protected static final Log log = LogFactory.getLog(IFParser.class);

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    private static Set<String> handledNamespaces = new java.util.HashSet<String>();

    static {
        handledNamespaces.add(XMLNS_NAMESPACE_URI);
        handledNamespaces.add(XML_NAMESPACE);
        handledNamespaces.add(NAMESPACE);
        handledNamespaces.add(XLINK_NAMESPACE);
    }

    /**
     * Parses an intermediate file and paints it.
     * @param src the Source instance pointing to the intermediate file
     * @param documentHandler the intermediate format document handler used to process the IF events
     * @param userAgent the user agent
     * @throws TransformerException if an error occurs while parsing the area tree XML
     * @throws IFException if an IF-related error occurs inside the target document handler
     */
    public void parse(Source src, IFDocumentHandler documentHandler, FOUserAgent userAgent)
            throws TransformerException, IFException {
        try {
            Transformer transformer = tFactory.newTransformer();
            transformer.setErrorListener(new DefaultErrorListener(log));

            SAXResult res = new SAXResult(getContentHandler(documentHandler, userAgent));

            transformer.transform(src, res);
        } catch (TransformerException te) {
            Throwable cause = te.getCause();
            //Unpack original IFException if applicable
            if (cause instanceof SAXException) {
                SAXException se = (SAXException) cause;
                cause = se.getCause();
                if (cause instanceof IFException) {
                    throw (IFException) cause;
                }
            } else if (cause instanceof IFException) {
                throw (IFException) cause;
            }
            throw te;
        }
    }

    /**
     * Creates a new ContentHandler instance that you can send the area tree XML to. The parsed
     * pages are added to the AreaTreeModel instance you pass in as a parameter.
     * @param documentHandler the intermediate format document handler used to process the IF events
     * @param userAgent the user agent
     * @return the ContentHandler instance to receive the SAX stream from the area tree XML
     */
    public ContentHandler getContentHandler(IFDocumentHandler documentHandler,
                    FOUserAgent userAgent) {
        ElementMappingRegistry elementMappingRegistry
            = userAgent.getElementMappingRegistry();
        return new Handler(documentHandler, userAgent, elementMappingRegistry);
    }

    private static class Handler extends DefaultHandler {

        private Map<String, ElementHandler> elementHandlers = new HashMap<String, ElementHandler>();

        private IFDocumentHandler documentHandler;
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

        private ContentHandler navParser;

        private StructureTreeHandler structureTreeHandler;

        private Attributes pageSequenceAttributes;

        private Map<String, StructureTreeElement> structureTreeElements
                = new HashMap<String, StructureTreeElement>();
        private Map<String, GoToXYAction> unresolvedIds = new HashMap<>();

        private class StructureTreeHandler extends DefaultHandler {

            protected final StructureTreeEventHandler structureTreeEventHandler;

            StructureTreeHandler(StructureTreeEventHandler structureTreeEventHandler) {
                this.structureTreeEventHandler = structureTreeEventHandler;
            }

            void startStructureTree(String type) {
            }

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                if (!"structure-tree".equals(localName)) {
                    if (localName.equals("marked-content")) {
                        localName = "#PCDATA";
                    }
                    StructureTreeElement parent = getStructureTreeElement(attributes);
                    String structID = attributes.getValue(InternalElementMapping.URI,
                            InternalElementMapping.STRUCT_ID);
                    if (structID == null) {
                        structureTreeEventHandler.startNode(localName, attributes, parent);
                    } else if (localName.equals("external-graphic")
                            || localName.equals("instream-foreign-object")) {
                        StructureTreeElement structureTreeElement
                                = structureTreeEventHandler.startImageNode(localName, attributes, parent);
                        structureTreeElements.put(structID, structureTreeElement);
                    } else {
                        StructureTreeElement structureTreeElement = structureTreeEventHandler
                                    .startReferencedNode(localName, attributes, parent);
                        structureTreeElements.put(structID, structureTreeElement);
                    }
                }
            }

            @Override
            public void endElement(String uri, String localName, String arqNameg2)
                    throws SAXException {
                if (!"structure-tree".equals(localName)) {
                    structureTreeEventHandler.endNode(localName);
                }
            }
        }

        private class MainStructureTreeHandler extends StructureTreeHandler {

            private final Locale pageSequenceLanguage;

            MainStructureTreeHandler(StructureTreeEventHandler structureTreeEventHandler,
                    Locale pageSequenceLanguage) throws SAXException {
                super(structureTreeEventHandler);
                this.pageSequenceLanguage = pageSequenceLanguage;
            }

            @Override
            void startStructureTree(String type) {
                structureTreeEventHandler.startPageSequence(pageSequenceLanguage, type);
            }

            public void endDocument() throws SAXException {
                startIFElement(EL_PAGE_SEQUENCE, pageSequenceAttributes);
                pageSequenceAttributes = null;
            }

        }

        public Handler(IFDocumentHandler documentHandler, FOUserAgent userAgent,
                ElementMappingRegistry elementMappingRegistry) {
            this.documentHandler = documentHandler;
            this.userAgent = userAgent;
            this.elementMappingRegistry = elementMappingRegistry;
            elementHandlers.put(EL_DOCUMENT, new DocumentHandler());
            elementHandlers.put(EL_HEADER, new DocumentHeaderHandler());
            elementHandlers.put(EL_LOCALE, new LocaleHandler());
            elementHandlers.put(EL_TRAILER, new DocumentTrailerHandler());
            elementHandlers.put(EL_PAGE_SEQUENCE, new PageSequenceHandler());
            elementHandlers.put(EL_PAGE, new PageHandler());
            elementHandlers.put(EL_PAGE_HEADER, new PageHeaderHandler());
            elementHandlers.put(EL_PAGE_CONTENT, new PageContentHandler());
            elementHandlers.put(EL_PAGE_TRAILER, new PageTrailerHandler());
            //Page content
            elementHandlers.put(EL_VIEWPORT, new ViewportHandler());
            elementHandlers.put(EL_GROUP, new GroupHandler());
            elementHandlers.put(EL_ID, new IDHandler());
            elementHandlers.put(EL_FONT, new FontHandler());
            elementHandlers.put(EL_TEXT, new TextHandler());
            elementHandlers.put(EL_CLIP_RECT, new ClipRectHandler());
            elementHandlers.put(EL_RECT, new RectHandler());
            elementHandlers.put(EL_LINE, new LineHandler());
            elementHandlers.put(EL_BORDER_RECT, new BorderRectHandler());
            elementHandlers.put(EL_IMAGE, new ImageHandler());
        }

        private void establishForeignAttributes(Map<QName, String> foreignAttributes) {
            documentHandler.getContext().setForeignAttributes(foreignAttributes);
        }

        private void resetForeignAttributes() {
            documentHandler.getContext().resetForeignAttributes();
        }

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
            if (delegate != null) {
                delegateDepth++;
                delegate.startElement(uri, localName, qName, attributes);
            } else {
                boolean handled = true;
                if (NAMESPACE.equals(uri)) {
                    if (localName.equals(EL_PAGE_SEQUENCE) && userAgent.isAccessibilityEnabled()) {
                        pageSequenceAttributes = new AttributesImpl(attributes);
                        Locale language = getLanguage(attributes);
                        structureTreeHandler = new MainStructureTreeHandler(
                                userAgent.getStructureTreeEventHandler(), language);

                    } else if (localName.equals(EL_STRUCTURE_TREE)) {
                        if (userAgent.isAccessibilityEnabled()) {
                            String type = attributes.getValue("type");
                            structureTreeHandler.startStructureTree(type);
                            delegate = structureTreeHandler;
                        } else {
                            /* Delegate to a handler that does nothing */
                            delegate = new DefaultHandler();
                        }
                        delegateDepth++;
                        delegate.startDocument();
                        delegate.startElement(uri, localName, qName, attributes);
                    } else {
                        if (pageSequenceAttributes != null) {
                            /*
                             * This means that no structure-element tag was
                             * found in the XML, otherwise a
                             * StructureTreeBuilderWrapper object would have
                             * been created, which would have reset the
                             * pageSequenceAttributes field.
                             */
                            AccessibilityEventProducer.Provider
                                    .get(userAgent.getEventBroadcaster())
                                    .noStructureTreeInXML(this);
                        }
                        handled = startIFElement(localName, attributes);

                        if (EL_ID.equals(localName) && documentHandler.getDocumentNavigationHandler() != null) {
                            if (this.navParser == null) {
                                this.navParser = new DocumentNavigationHandler(
                                        documentHandler.getDocumentNavigationHandler(),
                                        structureTreeElements, unresolvedIds);
                            }
                            delegate = this.navParser;
                            delegateDepth++;
                            delegate.startDocument();
                            delegate.startElement(uri, localName, qName, attributes);
                        }
                    }
                } else if (DocumentNavigationExtensionConstants.NAMESPACE.equals(uri)) {
                    if (this.navParser == null) {
                        this.navParser = new DocumentNavigationHandler(
                                this.documentHandler.getDocumentNavigationHandler(),
                                        structureTreeElements, unresolvedIds);
                    }
                    delegate = this.navParser;
                    delegateDepth++;
                    delegate.startDocument();
                    delegate.startElement(uri, localName, qName, attributes);
                } else {
                    ContentHandlerFactoryRegistry registry
                            = userAgent.getContentHandlerFactoryRegistry();
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

        private static Locale getLanguage(Attributes attributes) {
            String xmllang = attributes.getValue(XML_NAMESPACE, "lang");
            return (xmllang == null) ? null : LanguageTags.toLocale(xmllang);
        }

        private boolean startIFElement(String localName, Attributes attributes)
                throws SAXException {
            lastAttributes = new AttributesImpl(attributes);
            ElementHandler elementHandler = elementHandlers.get(localName);
            content.setLength(0);
            ignoreCharacters = true;
            if (elementHandler != null) {
                ignoreCharacters = elementHandler.ignoreCharacters();
                try {
                    elementHandler.startElement(attributes);
                } catch (IFException ife) {
                    handleIFException(ife);
                }
                return true;
            } else {
                return false;
            }
        }

        private void handleIFException(IFException ife) throws SAXException {
            Throwable cause = ife.getCause();
            if (cause instanceof SAXException) {
                //unwrap
                throw (SAXException) cause;
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
                    ElementHandler elementHandler = elementHandlers.get(localName);
                    if (elementHandler != null) {
                        try {
                            if (elementHandler instanceof DocumentHandler) {
                                addUnresolvedIds();
                            }
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

        private void addUnresolvedIds() throws IFException {
            for (GoToXYAction action : unresolvedIds.values()) {
                if (action != null) {
                    documentHandler.getDocumentNavigationHandler().addResolvedAction(action);
                }
            }
        }

        // ============== Element handlers for the intermediate format =============

        private interface ElementHandler {
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
                documentHandler.startDocument();
            }

            public void endElement() throws IFException {
                documentHandler.endDocument();
            }

        }

        private class DocumentHeaderHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                documentHandler.startDocumentHeader();
            }

            public void endElement() throws IFException {
                documentHandler.endDocumentHeader();
            }

        }

        private class LocaleHandler extends AbstractElementHandler {
            public void startElement(Attributes attributes) throws IFException {
                documentHandler.setDocumentLocale(getLanguage(attributes));
            }
        }

        private class DocumentTrailerHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                documentHandler.startDocumentTrailer();
            }

            public void endElement() throws IFException {
                documentHandler.endDocumentTrailer();
            }

        }

        private class PageSequenceHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                String id = attributes.getValue("id");
                Locale language = getLanguage(attributes);
                if (language != null) {
                    documentHandler.getContext().setLanguage(language);
                }
                Map<QName, String> foreignAttributes = getForeignAttributes(lastAttributes);
                establishForeignAttributes(foreignAttributes);
                documentHandler.startPageSequence(id);
                resetForeignAttributes();
            }

            public void endElement() throws IFException {
                documentHandler.endPageSequence();
                documentHandler.getContext().setLanguage(null);
            }

        }

        private class PageHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int index = Integer.parseInt(attributes.getValue("index"));
                String name = attributes.getValue("name");
                String pageMasterName = attributes.getValue("page-master-name");
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                Map<QName, String> foreignAttributes = getForeignAttributes(lastAttributes);
                establishForeignAttributes(foreignAttributes);
                documentHandler.startPage(index, name, pageMasterName,
                        new Dimension(width, height));
                documentHandler.getContext().setPageNumber(index + 1);
                resetForeignAttributes();
            }

            public void endElement() throws IFException {
                documentHandler.endPage();
            }

        }

        private class PageHeaderHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                documentHandler.startPageHeader();
                structureTreeHandler = new StructureTreeHandler(userAgent.getStructureTreeEventHandler());
            }

            public void endElement() throws IFException {
                documentHandler.endPageHeader();
            }

        }

        private class PageContentHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                painter = documentHandler.startPageContent();
            }

            public void endElement() throws IFException {
                painter = null;
                documentHandler.getContext().setID("");
                documentHandler.endPageContent();
            }

        }

        private class PageTrailerHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                documentHandler.startPageTrailer();
            }

            public void endElement() throws IFException {
                documentHandler.endPageTrailer();
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
                documentHandler.getContext().setRegionType(attributes.getValue("region-type"));
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
                String layer = attributes.getValue("layer");
                painter.startGroup(transforms, layer);
            }

            public void endElement() throws IFException {
                painter.endGroup();
            }

        }

        private class IDHandler extends AbstractElementHandler {

            @Override
            public void startElement(Attributes attributes) throws IFException, SAXException {
                String id = attributes.getValue("name");
                documentHandler.getContext().setID(id);
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
                String s = lastAttributes.getValue("letter-spacing");
                int letterSpacing = (s != null ? Integer.parseInt(s) : 0);
                s = lastAttributes.getValue("word-spacing");
                int wordSpacing = (s != null ? Integer.parseInt(s) : 0);
                int[] dx = XMLUtil.getAttributeAsIntArray(lastAttributes, "dx");
                int[][] dp = XMLUtil.getAttributeAsPositionAdjustments(lastAttributes, "dp");
                // if only DX present, then convert DX to DP; otherwise use only DP,
                // effectively ignoring DX
                if ((dp == null) && (dx != null)) {
                    dp = IFUtil.convertDXToDP(dx);
                }
                establishStructureTreeElement(lastAttributes);
                boolean isHyphenated = Boolean.valueOf(lastAttributes.getValue("hyphenated"));
                if (isHyphenated) {
                    documentHandler.getContext().setHyphenated(isHyphenated);
                }
                boolean nextIsSpace = Boolean.valueOf(lastAttributes.getValue("next-is-space"));

                painter.drawText(x, y, letterSpacing, wordSpacing, dp,
                        StringUtils.processSoftHyphen(content.toString(), painter), nextIsSpace);
                documentHandler.getContext().setHyphenated(false);
                resetStructureTreeElement();
            }

            public boolean ignoreCharacters() {
                return false;
            }

        }

        private class ClipRectHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int x = Integer.parseInt(attributes.getValue("x"));
                int y = Integer.parseInt(attributes.getValue("y"));
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                BorderProps[] borders = new BorderProps[4];
                for (int i = 0; i < 4; i++) {
                    String b = attributes.getValue(SIDES[i]);
                    if (b != null) {
                        borders[i] = BorderProps.valueOf(userAgent, b);
                    }
                }

                if (!(borders[0] == null && borders[1] == null
                        && borders[2] == null && borders[3] == null)) {
                    painter.clipBackground(new Rectangle(x, y, width, height),
                            borders[0], borders[1], borders[2], borders[3]);
                }
                painter.clipRect(new Rectangle(x, y, width, height));
            }

        }

        private class RectHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int x = Integer.parseInt(attributes.getValue("x"));
                int y = Integer.parseInt(attributes.getValue("y"));
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                Color fillColor;
                try {
                    fillColor = getAttributeAsColor(attributes, "fill");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the fill attribute", pe);
                }

                Rectangle rectangularArea = new Rectangle(x, y, width, height);

                //TODO should rect be overloaded to include rounded corners
                // or should we introduce a new IF element?
                BorderProps[] borders = new BorderProps[4];
                for (int i = 0; i < 4; i++) {
                    String b = attributes.getValue(SIDES[i]);
                    if (b != null) {
                        borders[i] = BorderProps.valueOf(userAgent, b);
                    }
                }

                if (!(borders[0] == null && borders[1] == null
                        && borders[2] == null && borders[3] == null)) {
                    painter.clipBackground(rectangularArea,
                            borders[0], borders[1], borders[2], borders[3]);
                }

                painter.fillRect(rectangularArea , fillColor);
            }

        }

        private class LineHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int x1 = Integer.parseInt(attributes.getValue("x1"));
                int y1 = Integer.parseInt(attributes.getValue("y1"));
                int x2 = Integer.parseInt(attributes.getValue("x2"));
                int y2 = Integer.parseInt(attributes.getValue("y2"));
                int width = Integer.parseInt(attributes.getValue("stroke-width"));
                Color color;
                try {
                    color = getAttributeAsColor(attributes, "color");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the fill attribute", pe);
                }
                RuleStyle style = RuleStyle.valueOf(attributes.getValue("style"));
                painter.drawLine(new Point(x1, y1), new Point(x2, y2), width, color, style);
            }

        }

        private static final String[] SIDES = new String[] {"top", "bottom", "left", "right"};

        private class BorderRectHandler extends AbstractElementHandler {

            public void startElement(Attributes attributes) throws IFException {
                int x = Integer.parseInt(attributes.getValue("x"));
                int y = Integer.parseInt(attributes.getValue("y"));
                int width = Integer.parseInt(attributes.getValue("width"));
                int height = Integer.parseInt(attributes.getValue("height"));
                BorderProps[] borders = new BorderProps[4];
                for (int i = 0; i < 4; i++) {
                    String b = attributes.getValue(SIDES[i]);
                    if (b != null) {
                        borders[i] = BorderProps.valueOf(userAgent, b);
                    }
                }
                Color backgroundColor;

                try {
                    backgroundColor = getAttributeAsColor(attributes, "inner-background-color");
                } catch (PropertyException pe) {
                    throw new IFException("Error parsing the color attribute", pe);
                }

                painter.drawBorderRect(new Rectangle(x, y, width, height),
                        borders[0], borders[1], borders[2], borders[3], backgroundColor);
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
                Map<QName, String> foreignAttributes = getForeignAttributes(lastAttributes);
                establishForeignAttributes(foreignAttributes);
                establishStructureTreeElement(lastAttributes);
                if (foreignObject != null) {
                    painter.drawImage(foreignObject,
                            new Rectangle(x, y, width, height));
                    foreignObject = null;
                } else {
                    String uri = lastAttributes.getValue(
                            XLINK_HREF.getNamespaceURI(), XLINK_HREF.getLocalName());
                    if (uri == null) {
                        throw new IFException("xlink:href is missing on image", null);
                    }
                    painter.drawImage(uri, new Rectangle(x, y, width, height));
                }
                resetStructureTreeElement();
                resetForeignAttributes();
                inForeignObject = false;
            }

            public boolean ignoreCharacters() {
                return false;
            }
        }


        // ====================================================================

        /**
         * Handles objects created by "sub-parsers" that implement the ObjectSource interface.
         * An example of object handled here are ExtensionAttachments.
         * @param obj the Object to be handled.
         * @throws SAXException if an error occurs while handling the extension object
         */
        protected void handleExternallyGeneratedObject(Object obj) throws SAXException {
            try {
                documentHandler.handleExtensionObject(obj);
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

        private static Map<QName, String> getForeignAttributes(Attributes atts) {
            Map<QName, String> foreignAttributes = null;
            for (int i = 0, c = atts.getLength(); i < c; i++) {
                String ns = atts.getURI(i);
                if (ns.length() > 0) {
                    if (handledNamespaces.contains(ns)) {
                        continue;
                    }
                    if (foreignAttributes == null) {
                        foreignAttributes = new java.util.HashMap<QName, String>();
                    }
                    QName qname = new QName(ns, atts.getQName(i));
                    foreignAttributes.put(qname, atts.getValue(i));
                }
            }
            return foreignAttributes;
        }

        private void establishStructureTreeElement(Attributes attributes) {
            StructureTreeElement element = getStructureTreeElement(attributes);
            if (element != null) {
                documentHandler.getContext().setStructureTreeElement(element);
            }
        }

        private StructureTreeElement getStructureTreeElement(Attributes attributes) {
            String structRef = attributes.getValue(InternalElementMapping.URI, InternalElementMapping.STRUCT_REF);
            if (structRef != null && structRef.length() > 0) {
                assert structureTreeElements.containsKey(structRef);
                return structureTreeElements.get(structRef);
            } else {
                return null;
            }
        }

        private void resetStructureTreeElement() {
            documentHandler.getContext().resetStructureTreeElement();
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
