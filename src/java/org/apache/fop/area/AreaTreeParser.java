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

package org.apache.fop.area;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.util.QName;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Trait.Background;
import org.apache.fop.area.Trait.InternalLink;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineBlockParent;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.Leader;
import org.apache.fop.area.inline.Space;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.area.inline.WordArea;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.ElementMappingRegistry;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.fop.util.ConversionUtils;
import org.apache.fop.util.DefaultErrorListener;
import org.apache.fop.util.XMLUtil;

/**
 * This is a parser for the area tree XML (intermediate format) which is used to reread an area
 * tree (or part of it) into memory again for rendering to the final output format.
 */
public class AreaTreeParser {

    /** Logger instance */
    protected static Log log = LogFactory.getLog(AreaTreeParser.class);

    private static SAXTransformerFactory tFactory
        = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /**
     * Parses an intermediate file (area tree XML) into an AreaTreeModel instance by adding
     * pages to it.
     * @param src the Source instance pointing to the intermediate file
     * @param treeModel the AreaTreeModel that the parsed pages are added to
     * @param userAgent the user agent
     * @throws TransformerException if an error occurs while parsing the area tree XML
     */
    public void parse(Source src, AreaTreeModel treeModel, FOUserAgent userAgent)
            throws TransformerException {
        Transformer transformer = tFactory.newTransformer();
        transformer.setErrorListener(new DefaultErrorListener(log));

        SAXResult res = new SAXResult(getContentHandler(treeModel, userAgent));

        transformer.transform(src, res);
    }

    /**
     * Creates a new ContentHandler instance that you can send the area tree XML to. The parsed
     * pages are added to the AreaTreeModel instance you pass in as a parameter.
     * @param treeModel the AreaTreeModel that the parsed pages are added to
     * @param userAgent the user agent
     * @return the ContentHandler instance to receive the SAX stream from the area tree XML
     */
    public ContentHandler getContentHandler(AreaTreeModel treeModel, FOUserAgent userAgent) {
        ElementMappingRegistry elementMappingRegistry
            = userAgent.getFactory().getElementMappingRegistry();
        return new Handler(treeModel, userAgent, elementMappingRegistry);
    }

    private static class Handler extends DefaultHandler {

        private Map makers = new java.util.HashMap();

        private AreaTreeModel treeModel;
        private FOUserAgent userAgent;
        private ElementMappingRegistry elementMappingRegistry;

        private Attributes lastAttributes;

        private CharBuffer content = CharBuffer.allocate(64);
        private boolean ignoreCharacters = true;

        private PageViewport currentPageViewport;
        private Map pageViewportsByKey = new java.util.HashMap();
        // set of "ID firsts" that have already been assigned to a PV:
        private Set idFirstsAssigned = new java.util.HashSet();

        private Stack areaStack = new Stack();
        private boolean firstFlow;

        private Stack delegateStack = new Stack();
        private ContentHandler delegate;
        private DOMImplementation domImplementation;


        public Handler(AreaTreeModel treeModel, FOUserAgent userAgent,
                ElementMappingRegistry elementMappingRegistry) {
            this.treeModel = treeModel;
            this.userAgent = userAgent;
            this.elementMappingRegistry = elementMappingRegistry;
            makers.put("areaTree", new AreaTreeMaker());
            makers.put("page", new PageMaker());
            makers.put("pageSequence", new PageSequenceMaker());
            makers.put("title", new TitleMaker());
            makers.put("pageViewport", new PageViewportMaker());
            makers.put("regionViewport", new RegionViewportMaker());
            makers.put("regionBefore", new RegionBeforeMaker());
            makers.put("regionAfter", new RegionAfterMaker());
            makers.put("regionStart", new RegionStartMaker());
            makers.put("regionEnd", new RegionEndMaker());
            makers.put("regionBody", new RegionBodyMaker());
            makers.put("flow", new FlowMaker());
            makers.put("mainReference", new MainReferenceMaker());
            makers.put("span", new SpanMaker());
            makers.put("footnote", new FootnoteMaker());
            makers.put("beforeFloat", new BeforeFloatMaker());
            makers.put("block", new BlockMaker());
            makers.put("lineArea", new LineAreaMaker());
            makers.put("inline", new InlineMaker());
            makers.put("inlineparent", new InlineParentMaker());
            makers.put("inlineblockparent", new InlineBlockParentMaker());
            makers.put("text", new TextMaker());
            makers.put("word", new WordMaker());
            makers.put("space", new SpaceMaker());
            makers.put("leader", new LeaderMaker());
            makers.put("viewport", new ViewportMaker());
            makers.put("image", new ImageMaker());
            makers.put("foreignObject", new ForeignObjectMaker());
            makers.put("bookmarkTree", new BookmarkTreeMaker());
            makers.put("bookmark", new BookmarkMaker());
            makers.put("destination", new DestinationMaker());
        }

        private Area findAreaType(Class clazz) {
            if (areaStack.size() > 0) {
                int pos = areaStack.size() - 1;
                Object obj = null;
                while (pos >= 0 && !(clazz.isInstance(obj = areaStack.get(pos)))) {
                    pos--;
                }
                if (pos >= 0) {
                    return (Area)obj;
                }
            }
            return null;
        }

        private RegionViewport getCurrentRegionViewport() {
            return (RegionViewport)findAreaType(RegionViewport.class);
        }

        private BodyRegion getCurrentBodyRegion() {
            return (BodyRegion)findAreaType(BodyRegion.class);
        }

        private BlockParent getCurrentBlockParent() {
            return (BlockParent)findAreaType(BlockParent.class);
        }

        private AbstractTextArea getCurrentText() {
            return (AbstractTextArea)findAreaType(AbstractTextArea.class);
        }

        private Viewport getCurrentViewport() {
            return (Viewport)findAreaType(Viewport.class);
        }

        /** {@inheritDoc} */
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {
            if (delegate != null) {
                delegateStack.push(qName);
                delegate.startElement(uri, localName, qName, attributes);
            } else if (domImplementation != null) {
                //domImplementation is set so we need to start a new DOM building sub-process
                TransformerHandler handler;
                try {
                    handler = tFactory.newTransformerHandler();
                } catch (TransformerConfigurationException e) {
                    throw new SAXException("Error creating a new TransformerHandler", e);
                }
                Document doc = domImplementation.createDocument(uri, qName, null);
                //It's easier to work with an empty document, so remove the root element
                doc.removeChild(doc.getDocumentElement());
                handler.setResult(new DOMResult(doc));
                Area parent = (Area)areaStack.peek();
                ((ForeignObject)parent).setDocument(doc);

                //activate delegate for nested foreign document
                domImplementation = null; //Not needed anymore now
                this.delegate = handler;
                delegateStack.push(qName);
                delegate.startDocument();
                delegate.startElement(uri, localName, qName, attributes);
            } else {
                lastAttributes = new AttributesImpl(attributes);
                boolean handled = true;
                if ("".equals(uri)) {
                    Maker maker = (Maker)makers.get(localName);
                    content.clear();
                    ignoreCharacters = true;
                    if (maker != null) {
                        ignoreCharacters = maker.ignoreCharacters();
                        maker.startElement(attributes);
                    } else if ("extension-attachments".equals(localName)) {
                        //TODO implement me
                    } else {
                        handled = false;
                    }
                } else {
                    ContentHandlerFactoryRegistry registry
                            = userAgent.getFactory().getContentHandlerFactoryRegistry();
                    ContentHandlerFactory factory = registry.getFactory(uri);
                    if (factory != null) {
                        delegate = factory.createContentHandler();
                        delegateStack.push(qName);
                        delegate.startDocument();
                        delegate.startElement(uri, localName, qName, attributes);
                    } else {
                        handled = false;
                    }
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

        /** {@inheritDoc} */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (delegate != null) {
                delegate.endElement(uri, localName, qName);
                delegateStack.pop();
                if (delegateStack.size() == 0) {
                    delegate.endDocument();
                    if (delegate instanceof ContentHandlerFactory.ObjectSource) {
                        Object obj = ((ContentHandlerFactory.ObjectSource)delegate).getObject();
                        handleExternallyGeneratedObject(obj);
                    }
                    delegate = null; //Sub-document is processed, return to normal processing
                }
            } else {
                if ("".equals(uri)) {
                    Maker maker = (Maker)makers.get(localName);
                    if (maker != null) {
                        maker.endElement();
                        content.clear();
                    }
                    ignoreCharacters = true;
                } else {
                    //log.debug("Ignoring " + localName + " in namespace: " + uri);
                }
            }
        }

        // ============== Maker classes for the area tree objects =============

        private static interface Maker {
            void startElement(Attributes attributes) throws SAXException;
            void endElement();
            boolean ignoreCharacters();
        }

        private abstract class AbstractMaker implements Maker {

            public void startElement(Attributes attributes) throws SAXException {
                //nop
            }

            public void endElement() {
                //nop
            }

            public boolean ignoreCharacters() {
                return true;
            }
        }

        private class AreaTreeMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                // In case the Handler is reused:
                idFirstsAssigned.clear();
            }
        }

        private class PageSequenceMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                PageSequence pageSequence = new PageSequence(null);
                String lang = attributes.getValue("language");
                pageSequence.setLanguage(lang);
                String country = attributes.getValue("country");
                pageSequence.setCountry(country);
                transferForeignObjects(attributes, pageSequence);
                areaStack.push(pageSequence);
            }
        }

        private class TitleMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                LineArea line = new LineArea();
                transferForeignObjects(attributes, line);
                areaStack.push(line);
            }

            public void endElement() {
                LineArea line = (LineArea)areaStack.pop();
                PageSequence pageSequence = (PageSequence)areaStack.peek();
                pageSequence.setTitle(line);
            }

        }

        private class PageViewportMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                if (!areaStack.isEmpty()) {
                    PageSequence pageSequence = (PageSequence)areaStack.peek();
                    treeModel.startPageSequence(pageSequence);
                    areaStack.pop();
                }
                if (currentPageViewport != null) {
                    throw new IllegalStateException("currentPageViewport must be null");
                }
                Rectangle2D viewArea = XMLUtil.getAttributeAsRectangle2D(attributes, "bounds");
                int pageNumber = XMLUtil.getAttributeAsInt(attributes, "nr", -1);
                String key = attributes.getValue("key");
                String pageNumberString = attributes.getValue("formatted-nr");
                String pageMaster = attributes.getValue("simple-page-master-name");
                boolean blank = XMLUtil.getAttributeAsBoolean(attributes, "blank", false);
                currentPageViewport = new PageViewport(viewArea,
                        pageNumber, pageNumberString,
                        pageMaster, blank);
                transferForeignObjects(attributes, currentPageViewport);
                currentPageViewport.setKey(key);
                pageViewportsByKey.put(key, currentPageViewport);
            }

        }

        private class PageMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                Page p = new Page();
                currentPageViewport.setPage(p);
            }

            public void endElement() {
                treeModel.addPage(currentPageViewport);
                currentPageViewport = null;
            }
        }

        private class RegionViewportMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                RegionViewport rv = getCurrentRegionViewport();
                if (rv != null) {
                    throw new IllegalStateException("Current RegionViewport must be null");
                }
                Rectangle2D viewArea = XMLUtil.getAttributeAsRectangle2D(attributes, "rect");
                rv = new RegionViewport(viewArea);
                transferForeignObjects(attributes, rv);
                rv.setClip(XMLUtil.getAttributeAsBoolean(attributes, "clipped", false));
                setAreaAttributes(attributes, rv);
                setTraits(attributes, rv, SUBSET_COMMON);
                setTraits(attributes, rv, SUBSET_BOX);
                setTraits(attributes, rv, SUBSET_COLOR);
                areaStack.push(rv);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), RegionViewport.class);
            }
        }

        private class RegionBeforeMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                pushNewRegionReference(attributes, Constants.FO_REGION_BEFORE);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), RegionReference.class);
            }
        }

        private class RegionAfterMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                pushNewRegionReference(attributes, Constants.FO_REGION_AFTER);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), RegionReference.class);
            }
        }

        private class RegionStartMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                pushNewRegionReference(attributes, Constants.FO_REGION_START);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), RegionReference.class);
            }
        }

        private class RegionEndMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                pushNewRegionReference(attributes, Constants.FO_REGION_END);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), RegionReference.class);
            }
        }

        private class RegionBodyMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                BodyRegion body = getCurrentBodyRegion();
                if (body != null) {
                    throw new IllegalStateException("Current BodyRegion must be null");
                }
                String regionName = attributes.getValue("name");
                int columnCount = XMLUtil.getAttributeAsInt(attributes, "columnCount", 1);
                int columnGap = XMLUtil.getAttributeAsInt(attributes, "columnGap", 0);
                RegionViewport rv = getCurrentRegionViewport();
                body = new BodyRegion(Constants.FO_REGION_BODY,
                        regionName, rv, columnCount, columnGap);
                transferForeignObjects(attributes, body);
                body.setCTM(getAttributeAsCTM(attributes, "ctm"));
                setAreaAttributes(attributes, body);
                setTraits(attributes, body, SUBSET_BORDER_PADDING);
                rv.setRegionReference(body);
                currentPageViewport.getPage().setRegionViewport(
                        Constants.FO_REGION_BODY, rv);
                areaStack.push(body);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), BodyRegion.class);
            }
        }

        private class FlowMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                BodyRegion body = getCurrentBodyRegion();
                if (!firstFlow) {
                    body.getMainReference().getCurrentSpan().moveToNextFlow();
                } else {
                    firstFlow = false;
                }
                NormalFlow flow = body.getMainReference().getCurrentSpan().getCurrentFlow();
                transferForeignObjects(attributes, flow);
                setAreaAttributes(attributes, flow);
                areaStack.push(flow);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), NormalFlow.class);
            }
        }

        private class MainReferenceMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                //mainReference is created by the BodyRegion
                MainReference mr = getCurrentBodyRegion().getMainReference();
                transferForeignObjects(attributes, mr);
                setAreaAttributes(attributes, mr);
            }
        }

        private class SpanMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                int ipd = XMLUtil.getAttributeAsInt(attributes, "ipd", 0);
                int columnCount = XMLUtil.getAttributeAsInt(attributes, "columnCount", 1);
                BodyRegion body = getCurrentBodyRegion();
                Span span = new Span(columnCount,
                        body.getColumnGap(), ipd);
                transferForeignObjects(attributes, span);
                setAreaAttributes(attributes, span);
                body.getMainReference().getSpans().add(span);
                firstFlow = true;
            }
        }

        private class FootnoteMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                Footnote fn = getCurrentBodyRegion().getFootnote();
                transferForeignObjects(attributes, fn);
                fn.setTop(XMLUtil.getAttributeAsInt(attributes, "top-offset", 0));
                areaStack.push(fn);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), Footnote.class);
            }
        }

        private class BeforeFloatMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                BeforeFloat bf = getCurrentBodyRegion().getBeforeFloat();
                transferForeignObjects(attributes, bf);
                areaStack.push(bf);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), BeforeFloat.class);
            }
        }

        private class BlockMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                boolean isViewport = XMLUtil.getAttributeAsBoolean(attributes,
                        "is-viewport-area", false);
                Block block;
                if (isViewport) {
                    BlockViewport bv = new BlockViewport();
                    bv.setClip(XMLUtil.getAttributeAsBoolean(attributes, "clipped", false));
                    bv.setCTM(getAttributeAsCTM(attributes, "ctm"));
                    if (bv.getPositioning() != BlockViewport.RELATIVE) {
                        bv.setXOffset(
                                XMLUtil.getAttributeAsInt(attributes, "left-position", 0));
                        bv.setYOffset(
                                XMLUtil.getAttributeAsInt(attributes, "top-position", 0));
                    }
                    block = bv;
                } else {
                    block = new Block();
                }
                String positioning = attributes.getValue("positioning");
                if ("absolute".equalsIgnoreCase(positioning)) {
                    block.setPositioning(Block.ABSOLUTE);
                } else if ("fixed".equalsIgnoreCase(positioning)) {
                    block.setPositioning(Block.FIXED);
                } else if ("relative".equalsIgnoreCase(positioning)) {
                    block.setPositioning(Block.RELATIVE);
                } else {
                    block.setPositioning(Block.STACK);
                }
                if (attributes.getValue("left-offset") != null) {
                    block.setXOffset(XMLUtil.getAttributeAsInt(attributes, "left-offset", 0));
                }
                if (attributes.getValue("top-offset") != null) {
                    block.setYOffset(XMLUtil.getAttributeAsInt(attributes, "top-offset", 0));
                }
                transferForeignObjects(attributes, block);
                setAreaAttributes(attributes, block);
                setTraits(attributes, block, SUBSET_COMMON);
                setTraits(attributes, block, SUBSET_BOX);
                setTraits(attributes, block, SUBSET_COLOR);
                Area parent = (Area)areaStack.peek();
                //BlockParent parent = getCurrentBlockParent();
                parent.addChildArea(block);
                areaStack.push(block);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), Block.class);
            }
        }

        private class LineAreaMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                LineArea line = new LineArea();
                setAreaAttributes(attributes, line);
                setTraits(attributes, line, SUBSET_COMMON);
                setTraits(attributes, line, SUBSET_BOX);
                setTraits(attributes, line, SUBSET_COLOR);
                BlockParent parent = getCurrentBlockParent();
                parent.addChildArea(line);
                areaStack.push(line);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), LineArea.class);
            }
        }

        // Maker for "generic" inline areas
        private class InlineMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                InlineArea inl = new InlineArea();
                transferForeignObjects(attributes, inl);
                inl.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                setAreaAttributes(attributes, inl);
                setTraits(attributes, inl, SUBSET_COMMON);
                setTraits(attributes, inl, SUBSET_BOX);
                setTraits(attributes, inl, SUBSET_COLOR);
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(inl);
                areaStack.push(inl);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), InlineArea.class);
            }
        }

        private class InlineParentMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                InlineParent ip = new InlineParent();
                transferForeignObjects(attributes, ip);
                ip.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                setAreaAttributes(attributes, ip);
                setTraits(attributes, ip, SUBSET_COMMON);
                setTraits(attributes, ip, SUBSET_BOX);
                setTraits(attributes, ip, SUBSET_COLOR);
                setTraits(attributes, ip, SUBSET_LINK);
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(ip);
                areaStack.push(ip);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), InlineParent.class);
            }
        }

        private class InlineBlockParentMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                InlineBlockParent ibp = new InlineBlockParent();
                transferForeignObjects(attributes, ibp);
                ibp.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                setAreaAttributes(attributes, ibp);
                setTraits(attributes, ibp, SUBSET_COMMON);
                setTraits(attributes, ibp, SUBSET_BOX);
                setTraits(attributes, ibp, SUBSET_COLOR);
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(ibp);
                areaStack.push(ibp);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), InlineBlockParent.class);
            }
        }

        private class TextMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                if (getCurrentText() != null) {
                    throw new IllegalStateException("Current Text must be null");
                }
                TextArea text = new TextArea();
                setAreaAttributes(attributes, text);
                setTraits(attributes, text, SUBSET_COMMON);
                setTraits(attributes, text, SUBSET_BOX);
                setTraits(attributes, text, SUBSET_COLOR);
                setTraits(attributes, text, SUBSET_FONT);
                text.setBaselineOffset(XMLUtil.getAttributeAsInt(attributes, "baseline", 0));
                text.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                text.setTextLetterSpaceAdjust(XMLUtil.getAttributeAsInt(attributes,
                        "tlsadjust", 0));
                text.setTextWordSpaceAdjust(XMLUtil.getAttributeAsInt(attributes,
                        "twsadjust", 0));
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(text);
                areaStack.push(text);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), TextArea.class);
            }
        }

        private class WordMaker extends AbstractMaker {

            public void endElement() {
                int offset = XMLUtil.getAttributeAsInt(lastAttributes, "offset", 0);
                int[] letterAdjust
                        = ConversionUtils.toIntArray(
                            lastAttributes.getValue("letter-adjust"), "\\s");
                content.flip();
                WordArea word = new WordArea(content.toString().trim(), offset, letterAdjust);
                AbstractTextArea text = getCurrentText();
                word.setParentArea(text);
                text.addChildArea(word);
            }

            public boolean ignoreCharacters() {
                return false;
            }
        }

        private class SpaceMaker extends AbstractMaker {

            public void endElement() {
                int offset = XMLUtil.getAttributeAsInt(lastAttributes, "offset", 0);
                //TODO the isAdjustable parameter is currently not used/implemented
                if (content.position() > 0) {
                    content.flip();
                    boolean adjustable = XMLUtil.getAttributeAsBoolean(lastAttributes, "adj", true);
                    SpaceArea space = new SpaceArea(content.charAt(0), offset, adjustable);
                    AbstractTextArea text = getCurrentText();
                    space.setParentArea(text);
                    text.addChildArea(space);
                } else {
                    Space space = new Space();
                    setAreaAttributes(lastAttributes, space);
                    setTraits(lastAttributes, space, SUBSET_COMMON);
                    setTraits(lastAttributes, space, SUBSET_BOX);
                    setTraits(lastAttributes, space, SUBSET_COLOR);
                    space.setOffset(offset);
                    Area parent = (Area)areaStack.peek();
                    parent.addChildArea(space);
                }
            }

            public boolean ignoreCharacters() {
                return false;
            }
        }

        private class LeaderMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                Leader leader = new Leader();
                transferForeignObjects(attributes, leader);
                setAreaAttributes(attributes, leader);
                setTraits(attributes, leader, SUBSET_COMMON);
                setTraits(attributes, leader, SUBSET_BOX);
                setTraits(attributes, leader, SUBSET_COLOR);
                setTraits(attributes, leader, SUBSET_FONT);
                leader.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                String ruleStyle = attributes.getValue("ruleStyle");
                if (ruleStyle != null) {
                    leader.setRuleStyle(ruleStyle);
                }
                leader.setRuleThickness(
                        XMLUtil.getAttributeAsInt(attributes, "ruleThickness", 0));
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(leader);
            }
        }

        private class ViewportMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                Viewport viewport = new Viewport(null);
                transferForeignObjects(attributes, viewport);
                setAreaAttributes(attributes, viewport);
                setTraits(attributes, viewport, SUBSET_COMMON);
                setTraits(attributes, viewport, SUBSET_BOX);
                setTraits(attributes, viewport, SUBSET_COLOR);
                viewport.setContentPosition(XMLUtil.getAttributeAsRectangle2D(attributes, "pos"));
                viewport.setClip(XMLUtil.getAttributeAsBoolean(attributes, "clip", false));
                viewport.setOffset(XMLUtil.getAttributeAsInt(attributes, "offset", 0));
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(viewport);
                areaStack.push(viewport);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), Viewport.class);
            }
        }

        private class ImageMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                String url = attributes.getValue("url");
                Image image = new Image(url);
                transferForeignObjects(attributes, image);
                setAreaAttributes(attributes, image);
                setTraits(attributes, image, SUBSET_COMMON);
                getCurrentViewport().setContent(image);
            }
        }

        private class ForeignObjectMaker extends AbstractMaker {

            public void startElement(Attributes attributes) throws SAXException {
                String ns = attributes.getValue("ns");
                domImplementation
                    = elementMappingRegistry.getDOMImplementationForNamespace(ns);
                if (domImplementation == null) {
                    throw new SAXException("No DOMImplementation could be"
                            + " identified to handle namespace: " + ns);
                }
                ForeignObject foreign = new ForeignObject(ns);
                transferForeignObjects(attributes, foreign);
                setAreaAttributes(attributes, foreign);
                setTraits(attributes, foreign, SUBSET_COMMON);
                getCurrentViewport().setContent(foreign);
                areaStack.push(foreign);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), ForeignObject.class);
            }
        }

        private class BookmarkTreeMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                BookmarkData bm = new BookmarkData();
                areaStack.push(bm);
            }

            public void endElement() {
                Object tos = areaStack.pop();
                assertObjectOfClass(tos, BookmarkData.class);
                treeModel.handleOffDocumentItem((BookmarkData) tos);
                // as long as the bookmark tree comes after the last PageViewport in the
                // area tree XML, we don't have to worry about resolved/unresolved. The
                // only resolution needed is the mapping of the pvKey to the PV instance.
            }
        }

        private class BookmarkMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                String title = attributes.getValue("title");
                boolean showChildren = XMLUtil.getAttributeAsBoolean(
                        attributes, "show-children", false);
                String[] linkdata
                        = InternalLink.parseXMLAttribute(attributes.getValue("internal-link"));
                PageViewport pv = (PageViewport) pageViewportsByKey.get(linkdata[0]);
                BookmarkData bm = new BookmarkData(title, showChildren, pv, linkdata[1]);
                Object tos = areaStack.peek();
                if (tos instanceof BookmarkData) {
                    BookmarkData parent = (BookmarkData) tos;
                    parent.addSubData(bm);
                }
                areaStack.push(bm);
            }

            public void endElement() {
                assertObjectOfClass(areaStack.pop(), BookmarkData.class);
            }
        }

        private class DestinationMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                String[] linkdata
                    = InternalLink.parseXMLAttribute(lastAttributes.getValue("internal-link"));
                PageViewport pv = (PageViewport) pageViewportsByKey.get(linkdata[0]);
                DestinationData dest = new DestinationData(linkdata[1]);
                List pages = new java.util.ArrayList();
                pages.add(pv);
                dest.resolveIDRef(linkdata[1], pages);
                areaStack.push(dest);
            }

            public void endElement() {
                Object tos = areaStack.pop();
                assertObjectOfClass(tos, DestinationData.class);
                treeModel.handleOffDocumentItem((DestinationData) tos);
            }
        }

        // ====================================================================


        private void pushNewRegionReference(Attributes attributes, int side) {
            String regionName = attributes.getValue("name");
            RegionViewport rv = getCurrentRegionViewport();
            RegionReference reg = new RegionReference(side,
                    regionName, rv);
            transferForeignObjects(attributes, reg);
            reg.setCTM(getAttributeAsCTM(attributes, "ctm"));
            setAreaAttributes(attributes, reg);
            setTraits(attributes, reg, SUBSET_BORDER_PADDING);
            rv.setRegionReference(reg);
            currentPageViewport.getPage().setRegionViewport(
                    side, rv);
            areaStack.push(reg);
        }

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
         */
        protected void handleExternallyGeneratedObject(Object obj) {
            if (areaStack.size() == 0 && obj instanceof ExtensionAttachment) {
                ExtensionAttachment attachment = (ExtensionAttachment)obj;
                if (this.currentPageViewport == null) {
                    this.treeModel.handleOffDocumentItem(
                            new OffDocumentExtensionAttachment(attachment));
                } else {
                    this.currentPageViewport.addExtensionAttachment(attachment);
                }
            } else {
                Object o = areaStack.peek();
                if (o instanceof AreaTreeObject && obj instanceof ExtensionAttachment) {
                    AreaTreeObject ato = (AreaTreeObject)o;
                    ExtensionAttachment attachment = (ExtensionAttachment)obj;
                    ato.addExtensionAttachment(attachment);
                } else {
                    log.warn("Don't know how to handle externally generated object: " + obj);
                }
            }
        }

        private void setAreaAttributes(Attributes attributes, Area area) {
            area.setIPD(Integer.parseInt(attributes.getValue("ipd")));
            area.setBPD(Integer.parseInt(attributes.getValue("bpd")));
        }

        private static final Object[] SUBSET_COMMON = new Object[] {
            Trait.PROD_ID};
        private static final Object[] SUBSET_LINK = new Object[] {
            Trait.INTERNAL_LINK, Trait.EXTERNAL_LINK};
        private static final Object[] SUBSET_COLOR = new Object[] {
            Trait.BACKGROUND, Trait.COLOR};
        private static final Object[] SUBSET_FONT = new Object[] {
            Trait.FONT, Trait.FONT_SIZE, Trait.BLINK,
            Trait.OVERLINE, Trait.OVERLINE_COLOR,
            Trait.LINETHROUGH, Trait.LINETHROUGH_COLOR,
            Trait.UNDERLINE, Trait.UNDERLINE_COLOR};
        private static final Object[] SUBSET_BOX = new Object[] {
            Trait.BORDER_BEFORE, Trait.BORDER_AFTER, Trait.BORDER_START, Trait.BORDER_END,
            Trait.SPACE_BEFORE, Trait.SPACE_AFTER, Trait.SPACE_START, Trait.SPACE_END,
            Trait.PADDING_BEFORE, Trait.PADDING_AFTER, Trait.PADDING_START, Trait.PADDING_END,
            Trait.START_INDENT, Trait.END_INDENT,
            Trait.IS_REFERENCE_AREA, Trait.IS_VIEWPORT_AREA};
        private static final Object[] SUBSET_BORDER_PADDING = new Object[] {
            Trait.BORDER_BEFORE, Trait.BORDER_AFTER, Trait.BORDER_START, Trait.BORDER_END,
            Trait.PADDING_BEFORE, Trait.PADDING_AFTER, Trait.PADDING_START, Trait.PADDING_END};

        private void setTraits(Attributes attributes, Area area, Object[] traitSubset) {
            for (int i = traitSubset.length; --i >= 0;) {
                Object trait = traitSubset[i];
                String traitName = Trait.getTraitName(trait);
                String value = attributes.getValue(traitName);
                if (value != null) {
                    Class cl = Trait.getTraitClass(trait);
                    if (cl == Integer.class) {
                        area.addTrait(trait, new Integer(value));
                    } else if (cl == Boolean.class) {
                        area.addTrait(trait, Boolean.valueOf(value));
                    } else if (cl == String.class) {
                        area.addTrait(trait, value);
                        if (trait == Trait.PROD_ID
                                && !idFirstsAssigned.contains(value)
                                && currentPageViewport != null) {
                            currentPageViewport.setFirstWithID(value);
                            idFirstsAssigned.add(value);
                        }
                    } else if (cl == Color.class) {
                        try {
                            area.addTrait(trait, ColorUtil.parseColorString(this.userAgent, value));
                        } catch (PropertyException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    } else if (cl == InternalLink.class) {
                        area.addTrait(trait, new InternalLink(value));
                    } else if (cl == Trait.ExternalLink.class) {
                        area.addTrait(trait, Trait.ExternalLink.makeFromTraitValue(value));
                    } else if (cl == Background.class) {
                        Background bkg = new Background();
                        try {
                            Color col = ColorUtil.parseColorString(
                                        this.userAgent, attributes.getValue("bkg-color"));
                            bkg.setColor(col);
                        } catch (PropertyException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                        String uri = attributes.getValue("bkg-img");
                        if (uri != null) {
                            bkg.setURL(uri);

                            try {
                                ImageManager manager = userAgent.getFactory().getImageManager();
                                ImageSessionContext sessionContext
                                    = userAgent.getImageSessionContext();
                                ImageInfo info = manager.getImageInfo(uri, sessionContext);
                                bkg.setImageInfo(info);
                            } catch (Exception e) {
                                log.error("Background image not available: " + uri, e);
                            }

                            String repeat = attributes.getValue("bkg-repeat");
                            if (repeat != null) {
                                bkg.setRepeat(repeat);
                            }
                            bkg.setHoriz(XMLUtil.getAttributeAsInt(attributes,
                                    "bkg-horz-offset", 0));
                            bkg.setVertical(XMLUtil.getAttributeAsInt(attributes,
                                    "bkg-vert-offset", 0));
                        }
                        area.addTrait(trait, bkg);
                    } else if (cl == BorderProps.class) {
                        area.addTrait(trait, BorderProps.valueOf(this.userAgent, value));
                    }
                } else {
                    if (trait == Trait.FONT) {
                        String fontName = attributes.getValue("font-name");
                        if (fontName != null) {
                            String fontStyle = attributes.getValue("font-style");
                            int fontWeight = XMLUtil.getAttributeAsInt(
                                    attributes, "font-weight", Font.WEIGHT_NORMAL);
                            area.addTrait(trait,
                                    FontInfo.createFontKey(fontName, fontStyle, fontWeight));
                        }
                    }
                }
            }
        }

        private static CTM getAttributeAsCTM(Attributes attributes, String name) {
            String s = attributes.getValue(name).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1);
                double[] values = ConversionUtils.toDoubleArray(s, "\\s");
                if (values.length != 6) {
                    throw new IllegalArgumentException("CTM must consist of 6 double values!");
                }
                return new CTM(values[0], values[1], values[2], values[3], values[4], values[5]);
            } else {
                throw new IllegalArgumentException("CTM must be surrounded by square brackets!");
            }
        }

        private static void transferForeignObjects(Attributes atts, AreaTreeObject ato) {
            for (int i = 0, c = atts.getLength(); i < c; i++) {
                String ns = atts.getURI(i);
                if (ns.length() > 0) {
                    if ("http://www.w3.org/2000/xmlns/".equals(ns)) {
                        continue;
                    }
                    QName qname = new QName(ns, atts.getQName(i));
                    ato.setForeignAttribute(qname, atts.getValue(i));
                }
            }
        }

        /** {@inheritDoc} */
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (delegate != null) {
                delegate.characters(ch, start, length);
            } else if (!ignoreCharacters) {
                int maxLength = this.content.capacity() - this.content.position();
                if (maxLength < length) {
                    // allocate a larger buffer and transfer content
                    CharBuffer newContent
                            = CharBuffer.allocate(this.content.position() + length);
                    this.content.flip();
                    newContent.put(this.content);
                    this.content = newContent;
                }
                // make sure the full capacity is used
                this.content.limit(this.content.capacity());
                // add characters to the buffer
                this.content.put(ch, start, length);
                // decrease the limit, if necessary
                if (this.content.position() < this.content.limit()) {
                    this.content.limit(this.content.position());
                }
            }
        }
    }
}
