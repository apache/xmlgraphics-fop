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
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.Trait.Background;
import org.apache.fop.area.inline.AbstractTextArea;
import org.apache.fop.area.inline.Character;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.Image;
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
import org.apache.fop.image.FopImage;
import org.apache.fop.image.ImageFactory;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.ContentHandlerFactory;
import org.apache.fop.util.ContentHandlerFactoryRegistry;
import org.apache.fop.util.DefaultErrorListener;
import org.apache.fop.util.QName;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

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
        private StringBuffer content = new StringBuffer();

        private PageViewport currentPageViewport;
        private Stack areaStack = new Stack();
        private boolean firstFlow;
        private boolean pendingStartPageSequence;
        
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
            makers.put("inlineparent", new InlineParentMaker());
            makers.put("inlineblockparent", new InlineBlockParentMaker());
            makers.put("text", new TextMaker());
            makers.put("word", new WordMaker());
            makers.put("space", new SpaceMaker());
            makers.put("char", new CharMaker());
            makers.put("leader", new LeaderMaker());
            makers.put("viewport", new ViewportMaker());
            makers.put("image", new ImageMaker());
            makers.put("foreignObject", new ForeignObjectMaker());
        }

        private static Rectangle2D parseRect(String rect) {
            StringTokenizer tokenizer = new StringTokenizer(rect, " ");
            return new Rectangle2D.Double(
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken()));
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
        
        /** @see org.xml.sax.helpers.DefaultHandler */
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
                lastAttributes = attributes;
                boolean handled = true;
                if ("".equals(uri)) {
                    Maker maker = (Maker)makers.get(localName);
                    if (maker != null) {
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
        
        /** @see org.xml.sax.helpers.DefaultHandler */
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
                    }
                } else {
                    //log.debug("Ignoring " + localName + " in namespace: " + uri);
                }
                content.setLength(0); //Reset text buffer (see characters())
            }
        }
        
        // ============== Maker classes for the area tree objects =============
        
        private static interface Maker {
            void startElement(Attributes attributes) throws SAXException;
            void endElement();
        }
        
        private abstract class AbstractMaker implements Maker {

            public void startElement(Attributes attributes) throws SAXException {
                //nop
            }

            public void endElement() {
                //nop
            }
        }
        
        private class AreaTreeMaker extends AbstractMaker {
            //no overrides
        }
        
        private class PageSequenceMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                pendingStartPageSequence = true;
                //treeModel.startPageSequence(null); Done after title or on the first viewport
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
                treeModel.startPageSequence(line);
                pendingStartPageSequence = false;
            }
            
            
        }
        
        private class PageViewportMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                if (pendingStartPageSequence) {
                    treeModel.startPageSequence(null);
                    pendingStartPageSequence = false;
                }
                if (currentPageViewport != null) {
                    throw new IllegalStateException("currentPageViewport must be null");
                }
                Rectangle2D viewArea = parseRect(attributes.getValue("bounds"));
                int pageNumber = getAttributeAsInteger(attributes, "nr", -1);
                String key = attributes.getValue("key");
                String pageNumberString = attributes.getValue("formatted-nr");
                String pageMaster = attributes.getValue("simple-page-master-name");
                boolean blank = getAttributeAsBoolean(attributes, "blank", false);
                currentPageViewport = new PageViewport(viewArea, 
                        pageNumber, pageNumberString,
                        pageMaster, blank);
                transferForeignObjects(attributes, currentPageViewport);
                currentPageViewport.setKey(key);
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
                Rectangle2D viewArea = parseRect(attributes.getValue("rect"));
                rv = new RegionViewport(viewArea);
                transferForeignObjects(attributes, rv);
                rv.setClip(getAttributeAsBoolean(attributes, "clipped", false));
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
                int columnCount = getAttributeAsInteger(attributes, "columnCount", 1);
                int columnGap = getAttributeAsInteger(attributes, "columnGap", 0);
                RegionViewport rv = getCurrentRegionViewport();
                body = new BodyRegion(Constants.FO_REGION_BODY, 
                        regionName, rv, columnCount, columnGap);
                transferForeignObjects(attributes, body);
                body.setCTM(getAttributeAsCTM(attributes, "ctm"));
                setAreaAttributes(attributes, body);
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
                int ipd = getAttributeAsInteger(attributes, "ipd", 0);
                int columnCount = getAttributeAsInteger(attributes, "columnCount", 1);
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
                boolean isViewport = getAttributeAsBoolean(attributes, 
                        "is-viewport-area", false);
                Block block;
                if (isViewport) {
                    BlockViewport bv = new BlockViewport();
                    bv.setClip(getAttributeAsBoolean(attributes, "clipped", false));
                    bv.setCTM(getAttributeAsCTM(attributes, "ctm"));
                    if (bv.getPositioning() != BlockViewport.RELATIVE) {
                        bv.setXOffset(
                                getAttributeAsInteger(attributes, "left-position", 0));
                        bv.setYOffset(
                                getAttributeAsInteger(attributes, "top-position", 0));
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
                    block.setXOffset(getAttributeAsInteger(attributes, "left-offset", 0));
                }
                if (attributes.getValue("top-offset") != null) {
                    block.setYOffset(getAttributeAsInteger(attributes, "top-offset", 0));
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

        private class InlineParentMaker extends AbstractMaker {

            public void startElement(Attributes attributes) {
                InlineParent ip = new InlineParent();
                transferForeignObjects(attributes, ip);
                ip.setOffset(getAttributeAsInteger(attributes, "offset", 0));
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
                ibp.setOffset(getAttributeAsInteger(attributes, "offset", 0));
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
                text.setBaselineOffset(getAttributeAsInteger(attributes, "baseline", 0));
                text.setOffset(getAttributeAsInteger(attributes, "offset", 0));
                text.setTextLetterSpaceAdjust(getAttributeAsInteger(attributes, 
                        "tlsadjust", 0));
                text.setTextWordSpaceAdjust(getAttributeAsInteger(attributes, 
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

            private int[] toIntArray(String s) {
                if (s == null || s.length() == 0) {
                    return null;
                }
                StringTokenizer tokenizer = new StringTokenizer(s, " ");
                List values = new java.util.ArrayList();
                while (tokenizer.hasMoreTokens()) {
                    values.add(new Integer(tokenizer.nextToken()));
                }
                int[] res = new int[values.size()];
                for (int i = 0, c = res.length; i < c; i++) {
                    res[i] = ((Integer)values.get(i)).intValue();
                }
                return res;
            }
            
            public void endElement() {
                int offset = getAttributeAsInteger(lastAttributes, "offset", 0);
                int[] letterAdjust = toIntArray(lastAttributes.getValue("letter-adjust"));
                String txt = content.toString();
                WordArea word = new WordArea(txt, offset, letterAdjust);
                AbstractTextArea text = getCurrentText();
                word.setParentArea(text);
                text.addChildArea(word);
            }            
        }

        private class SpaceMaker extends AbstractMaker {

            public void endElement() {
                int offset = getAttributeAsInteger(lastAttributes, "offset", 0);
                String txt = content.toString();
                //TODO the isAdjustable parameter is currently not used/implemented
                if (txt.length() > 0) {
                    boolean adjustable = getAttributeAsBoolean(lastAttributes, "adj", true);
                    SpaceArea space = new SpaceArea(txt.charAt(0), offset, adjustable);
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
        }

        private class CharMaker extends AbstractMaker {

            public void endElement() {
                String txt = content.toString();
                Character ch = new Character(txt.charAt(0));
                transferForeignObjects(lastAttributes, ch);
                setAreaAttributes(lastAttributes, ch);
                setTraits(lastAttributes, ch, SUBSET_COMMON);
                setTraits(lastAttributes, ch, SUBSET_BOX);
                setTraits(lastAttributes, ch, SUBSET_COLOR);
                setTraits(lastAttributes, ch, SUBSET_FONT);
                ch.setOffset(getAttributeAsInteger(lastAttributes, "offset", 0));
                ch.setBaselineOffset(getAttributeAsInteger(lastAttributes, "baseline", 0));
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(ch);
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
                leader.setOffset(getAttributeAsInteger(attributes, "offset", 0));
                String ruleStyle = attributes.getValue("ruleStyle");
                if (ruleStyle != null) {
                    leader.setRuleStyle(ruleStyle);
                }
                leader.setRuleThickness(
                        getAttributeAsInteger(attributes, "ruleThickness", 0));
                Area parent = (Area)areaStack.peek();
                parent.addChildArea(leader);
            }
            
            public void endElement() {
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
                viewport.setContentPosition(getAttributeAsRectangle2D(attributes, "pos"));
                viewport.setClip(getAttributeAsBoolean(attributes, "clip", false));
                viewport.setOffset(getAttributeAsInteger(attributes, "offset", 0));
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

        // ====================================================================
        

        private void pushNewRegionReference(Attributes attributes, int side) {
            String regionName = attributes.getValue("name");
            RegionViewport rv = getCurrentRegionViewport();
            RegionReference reg = new RegionReference(side, 
                    regionName, rv);
            transferForeignObjects(attributes, reg);
            reg.setCTM(getAttributeAsCTM(attributes, "ctm"));
            setAreaAttributes(attributes, reg);
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
                log.warn("Don't know how to handle externally generated object: " + obj);
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

        private void setTraits(Attributes attributes, Area area, Object[] traitSubset) {
            for (int i = 0, c = traitSubset.length; i < c; i++) {
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
                    } else if (cl == Color.class) {
                        try {
                            area.addTrait(trait, ColorUtil.parseColorString(this.userAgent, value));
                        } catch (PropertyException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }
                    } else if (cl == Background.class) {
                        Background bkg = new Background();
                        try {
                            Color col = ColorUtil.parseColorString(
                                        this.userAgent, attributes.getValue("bkg-color"));
                            bkg.setColor(col);
                        } catch (PropertyException e) {
                            throw new IllegalArgumentException(e.getMessage());
                        }                            
                        String url = attributes.getValue("bkg-img");
                        if (url != null) {
                            bkg.setURL(url);
                            
                            ImageFactory fact = userAgent.getFactory().getImageFactory();
                            FopImage img = fact.getImage(url, userAgent);
                            if (img == null) {
                                log.error("Background image not available: " + url);
                            } else {
                                // load dimensions
                                if (!img.load(FopImage.DIMENSIONS)) {
                                    log.error("Cannot read background image dimensions: " 
                                            + url);
                                }
                            }
                            bkg.setFopImage(img);

                            String repeat = attributes.getValue("bkg-repeat");
                            if (repeat != null) {
                                bkg.setRepeat(repeat);
                            }
                            bkg.setHoriz(getAttributeAsInteger(attributes, 
                                    "bkg-horz-offset", 0));
                            bkg.setVertical(getAttributeAsInteger(attributes, 
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
                            int fontWeight = getAttributeAsInteger(
                                    attributes, "font-weight", Font.NORMAL);
                            area.addTrait(trait, 
                                    FontInfo.createFontKey(fontName, fontStyle, fontWeight));
                        }
                    }
                }
            }
        }
        
        private boolean getAttributeAsBoolean(Attributes attributes, String name, 
                boolean defaultValue) {
            String s = attributes.getValue(name);
            if (s == null) {
                return defaultValue;
            } else {
                return Boolean.valueOf(s).booleanValue();
            }
        }

        private int getAttributeAsInteger(Attributes attributes, String name, 
                int defaultValue) {
            String s = attributes.getValue(name);
            if (s == null) {
                return defaultValue;
            } else {
                return Integer.parseInt(s);
            }
        }

        private CTM getAttributeAsCTM(Attributes attributes, String name) {
            String s = attributes.getValue(name).trim();
            if (s.startsWith("[") && s.endsWith("]")) {
                s = s.substring(1, s.length() - 1);
                StringTokenizer tokenizer = new StringTokenizer(s, " ");
                double[] values = new double[] {
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken()),
                        Double.parseDouble(tokenizer.nextToken())};
                return new CTM(values[0], values[1], values[2], values[3], values[4], values[5]);
            } else {
                throw new IllegalArgumentException("CTM must be surrounded by square brackets");
            }
        }

        private Rectangle2D getAttributeAsRectangle2D(Attributes attributes, String name) {
            String s = attributes.getValue(name).trim();
            StringTokenizer tokenizer = new StringTokenizer(s, " ");
            double[] values = new double[] {
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken()),
                    Double.parseDouble(tokenizer.nextToken())};
            return new Rectangle2D.Double(values[0], values[1], values[2], values[3]);
        }

        private void transferForeignObjects(Attributes atts, AreaTreeObject ato) {
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
        
        /** @see org.xml.sax.ContentHandler#characters(char[], int, int) */
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (delegate != null) {
                delegate.characters(ch, start, length);
            } else {
                content.append(ch, start, length);
            }
        }

    }
    
}
