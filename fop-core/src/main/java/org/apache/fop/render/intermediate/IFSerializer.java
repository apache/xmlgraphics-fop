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
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.XMLizable;

import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.extensions.InternalElementMapping;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.DefaultRendererConfigurator;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.render.intermediate.IFRendererConfig.IFRendererConfigParser;
import org.apache.fop.render.intermediate.IFStructureTreeBuilder.IFStructureTreeElement;
import org.apache.fop.render.intermediate.extensions.AbstractAction;
import org.apache.fop.render.intermediate.extensions.Bookmark;
import org.apache.fop.render.intermediate.extensions.BookmarkTree;
import org.apache.fop.render.intermediate.extensions.DocumentNavigationExtensionConstants;
import org.apache.fop.render.intermediate.extensions.Link;
import org.apache.fop.render.intermediate.extensions.NamedDestination;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.DOM2SAX;
import org.apache.fop.util.LanguageTags;
import org.apache.fop.util.XMLConstants;
import org.apache.fop.util.XMLUtil;


/**
 * IFPainter implementation that serializes the intermediate format to XML.
 */
public class IFSerializer extends AbstractXMLWritingIFDocumentHandler
implements IFConstants, IFPainter, IFDocumentNavigationHandler {

    /**
     * Intermediate Format (IF) version, used to express an @version attribute
     * in the root element of the IF document, the initial value of which
     * is set to '2.0' to signify that something preceded it (but didn't
     * happen to be marked as such), and that this version is not necessarily
     * backwards compatible with the unmarked (&lt;2.0) version.
     */
    public static final String VERSION = "2.0";

    private IFDocumentHandler mimicHandler;
    private int pageSequenceIndex; // used for accessibility

    /** Holds the intermediate format state */
    private IFState state;

    private String currentID = "";

    private IFStructureTreeBuilder structureTreeBuilder;
    private int pageNumberEnded;

    public IFSerializer(IFContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    @Override
    protected String getMainNamespace() {
        return NAMESPACE;
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
        //Theoretically supported but disabled to improve performance when
        //rendering the IF to the final format later on
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        if (this.mimicHandler != null) {
            return getMimickedDocumentHandler().getConfigurator();
        } else {
            return new DefaultRendererConfigurator(getUserAgent(), new IFRendererConfigParser());
        }
    }

    /** {@inheritDoc} */
    @Override
    public IFDocumentNavigationHandler getDocumentNavigationHandler() {
        return this;
    }

    /**
     * Tells this serializer to mimic the given document handler (mostly applies to the font set
     * that is used during layout).
     * @param targetHandler the document handler to mimic
     */
    public void mimicDocumentHandler(IFDocumentHandler targetHandler) {
        this.mimicHandler = targetHandler;
    }

    /**
     * Returns the document handler that is being mimicked by this serializer.
     * @return the mimicked document handler or null if no such document handler has been set
     */
    public IFDocumentHandler getMimickedDocumentHandler() {
        return this.mimicHandler;
    }

    /** {@inheritDoc} */
    public FontInfo getFontInfo() {
        if (this.mimicHandler != null) {
            return this.mimicHandler.getFontInfo();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    public void setFontInfo(FontInfo fontInfo) {
        if (this.mimicHandler != null) {
            this.mimicHandler.setFontInfo(fontInfo);
        }
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        if (this.mimicHandler != null) {
            this.mimicHandler.setDefaultFontInfo(fontInfo);
        }
    }

    @Override
    public StructureTreeEventHandler getStructureTreeEventHandler() {
        if (structureTreeBuilder == null) {
            structureTreeBuilder = new IFStructureTreeBuilder();
        }
        return structureTreeBuilder;
    }

    /** {@inheritDoc} */
    @Override
    public void startDocument() throws IFException {
        super.startDocument();
        try {
            handler.startDocument();
            handler.startPrefixMapping("", NAMESPACE);
            handler.startPrefixMapping(XLINK_PREFIX, XLINK_NAMESPACE);
            handler.startPrefixMapping(DocumentNavigationExtensionConstants.PREFIX,
                    DocumentNavigationExtensionConstants.NAMESPACE);
            handler.startPrefixMapping(InternalElementMapping.STANDARD_PREFIX,
                    InternalElementMapping.URI);
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "version", VERSION);
            handler.startElement(EL_DOCUMENT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocument()", e);
        }
    }

    @Override
    public void setDocumentLocale(Locale locale) {
        AttributesImpl atts  = new AttributesImpl();
        atts.addAttribute(XML_NAMESPACE, "lang", "xml:lang", XMLUtil.CDATA,
                LanguageTags.toLanguageTag(locale));
        try {
            handler.startElement(EL_LOCALE, atts);
            handler.endElement(EL_LOCALE);
        } catch (SAXException e) {
            throw new RuntimeException("Unable to create the " + EL_LOCALE + " element.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void startDocumentHeader() throws IFException {
        try {
            handler.startElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endDocumentHeader() throws IFException {
        try {
            handler.endElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void startDocumentTrailer() throws IFException {
        try {
            handler.startElement(EL_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endDocumentTrailer() throws IFException {
        try {
            handler.endElement(EL_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endDocumentTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            handler.endElement(EL_DOCUMENT);
            handler.endDocument();
            finishDocumentNavigation();
        } catch (SAXException e) {
            throw new IFException("SAX error in endDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            if (id != null) {
                atts.addAttribute(XML_NAMESPACE, "id", "xml:id", XMLUtil.CDATA, id);
            }
            Locale lang = getContext().getLanguage();
            if (lang != null) {
                atts.addAttribute(XML_NAMESPACE, "lang", "xml:lang", XMLUtil.CDATA,
                        LanguageTags.toLanguageTag(lang));
            }
            XMLUtil.addAttribute(atts, XMLConstants.XML_SPACE, "preserve");
            addForeignAttributes(atts);
            handler.startElement(EL_PAGE_SEQUENCE, atts);
            if (this.getUserAgent().isAccessibilityEnabled()) {
                assert (structureTreeBuilder != null);
                structureTreeBuilder.replayEventsForPageSequence(handler, pageSequenceIndex++);
            }
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        try {

            handler.endElement(EL_PAGE_SEQUENCE);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
            throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "index", Integer.toString(index));
            addAttribute(atts, "name", name);
            if (pageMasterName != null) {
                //fox:external-document doesn't have a page-master
                addAttribute(atts, "page-master-name", pageMasterName);
            }
            addAttribute(atts, "width", Integer.toString(size.width));
            addAttribute(atts, "height", Integer.toString(size.height));
            addForeignAttributes(atts);
            getContext().setPageIndex(index);
            handler.startElement(EL_PAGE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPage()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void startPageHeader() throws IFException {
        try {
            handler.startElement(EL_PAGE_HEADER);
            if (this.getUserAgent().isAccessibilityEnabled()) {
                structureTreeBuilder.replayEventsForRetrievedMarkers(handler);
            }
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageHeader()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endPageHeader() throws IFException {
        try {
            handler.endElement(EL_PAGE_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        try {
            handler.startElement(EL_PAGE_CONTENT);
            this.state = IFState.create();
            return this;
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            this.state = null;
            currentID = "";
            handler.endElement(EL_PAGE_CONTENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void startPageTrailer() throws IFException {
        try {
            handler.startElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void endPageTrailer() throws IFException {
        try {
            commitNavigation();
            handler.endElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            handler.endElement(EL_PAGE);
            getContext().setPageIndex(-1);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPage()", e);
        }
        if (mimicHandler != null) {
            pageNumberEnded++;
            FOUserAgent userAgent = mimicHandler.getContext().getUserAgent();
            RendererEventProducer.Provider.get(userAgent.getEventBroadcaster()).endPage(this, pageNumberEnded);
        }
    }

    //---=== IFPainter ===---

    /** {@inheritDoc} */
    public void startViewport(AffineTransform transform, Dimension size, Rectangle clipRect)
            throws IFException {
        startViewport(IFUtil.toString(transform), size, clipRect);
    }

    /** {@inheritDoc} */
    public void startViewport(AffineTransform[] transforms, Dimension size, Rectangle clipRect)
            throws IFException {
        startViewport(IFUtil.toString(transforms), size, clipRect);
    }

    private void startViewport(String transform, Dimension size, Rectangle clipRect)
            throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            if (transform != null && transform.length() > 0) {
                addAttribute(atts, "transform", transform);
            }
            addAttribute(atts, "width", Integer.toString(size.width));
            addAttribute(atts, "height", Integer.toString(size.height));
            if (clipRect != null) {
                addAttribute(atts, "clip-rect", IFUtil.toString(clipRect));
            }
            if (getUserAgent().isAccessibilityEnabled() && getContext().getRegionType() != null) {
                addAttribute(atts, "region-type", getContext().getRegionType());
            }
            handler.startElement(EL_VIEWPORT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startViewport()", e);
        }
    }

    /** {@inheritDoc} */
    public void endViewport() throws IFException {
        try {
            handler.endElement(EL_VIEWPORT);
        } catch (SAXException e) {
            throw new IFException("SAX error in endViewport()", e);
        }
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform[] transforms, String layer) throws IFException {
        startGroup(IFUtil.toString(transforms), layer);
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform, String layer) throws IFException {
        startGroup(IFUtil.toString(transform), layer);
    }

    private void startGroup(String transform, String layer) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            if (transform != null && transform.length() > 0) {
                addAttribute(atts, "transform", transform);
            }
            if (layer != null && layer.length() > 0) {
                addAttribute(atts, "layer", layer);
            }
            handler.startElement(EL_GROUP, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startGroup()", e);
        }
    }

    /** {@inheritDoc} */
    public void endGroup() throws IFException {
        try {
            handler.endElement(EL_GROUP);
        } catch (SAXException e) {
            throw new IFException("SAX error in endGroup()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        try {
            addID();
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, XLINK_HREF, uri);
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            addForeignAttributes(atts);
            addStructureReference(atts);
            handler.element(EL_IMAGE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startGroup()", e);
        } finally {
            ImageSessionContext session = getUserAgent().getImageSessionContext();
            ImageManager imageManager = getUserAgent().getImageManager();
            imageManager.closeImage(uri, session);
        }
    }

    private void addForeignAttributes(AttributesImpl atts) throws SAXException {
        Map foreignAttributes = getContext().getForeignAttributes();
        if (!foreignAttributes.isEmpty()) {
            for (Object o : foreignAttributes.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                addAttribute(atts, (QName) entry.getKey(), entry.getValue().toString());
            }
        }
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect) throws IFException {
        try {
            addID();
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            addForeignAttributes(atts);
            addStructureReference(atts);
            handler.startElement(EL_IMAGE, atts);
            new DOM2SAX(handler).writeDocument(doc, true);
            handler.endElement(EL_IMAGE);
        } catch (SAXException e) {
            throw new IFException("SAX error in startGroup()", e);
        }
    }

    @Override
    public boolean supportsSoftHyphen() {
        return false;
    }

    private static String toString(Paint paint) {
        if (paint instanceof Color) {
            return ColorUtil.colorToString((Color)paint);
        } else {
            throw new UnsupportedOperationException("Paint not supported: " + paint);
        }
    }

    /** {@inheritDoc} */
    public void clipRect(Rectangle rect) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            handler.element(EL_CLIP_RECT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in clipRect()", e);
        }
    }

    /** {@inheritDoc} */
    public void clipBackground(Rectangle rect, BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            if (hasRoundedCorners(bpsBefore, bpsAfter, bpsStart, bpsEnd)) {

                if (bpsBefore != null) {
                    addAttribute(atts, "top", bpsBefore.toString());
                }
                if (bpsAfter != null) {
                    addAttribute(atts, "bottom", bpsAfter.toString());
                }
                if (bpsStart != null) {
                    addAttribute(atts, "left", bpsStart.toString());
                }
                if (bpsEnd != null) {
                    addAttribute(atts, "right", bpsEnd.toString());
                }
            }
            handler.element(EL_CLIP_RECT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in clipRect()", e);
        }
    }


    /** {@inheritDoc} */
    public void fillRect(Rectangle rect, Paint fill) throws IFException {
        if (fill == null) {
            return;
        }
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            addAttribute(atts, "fill", toString(fill));
            handler.element(EL_RECT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in fillRect()", e);
        }
    }

    //TODO create a class representing all borders should exist
    //with query methods like this
    private boolean hasRoundedCorners(BorderProps bpsBefore, BorderProps bpsAfter,
            BorderProps bpsStart, BorderProps bpsEnd) {
        boolean rtn = false;

        if (bpsBefore != null && bpsBefore.getRadiusStart() > 0
                && bpsStart != null && bpsStart.getRadiusStart() > 0) {
            rtn = true;
        }

        if (bpsBefore != null && bpsBefore.getRadiusEnd() > 0
                && bpsEnd != null && bpsEnd.getRadiusStart() > 0) {
            rtn = true;
        }

        if (bpsEnd != null && bpsEnd.getRadiusEnd() > 0
                && bpsAfter != null && bpsAfter.getRadiusEnd() > 0) {
            rtn = true;
        }

        if (bpsAfter != null && bpsAfter.getRadiusStart() > 0
                && bpsStart != null && bpsStart.getRadiusEnd() > 0) {
            rtn = true;
        }

        return rtn;
    }

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps top, BorderProps bottom,
            BorderProps left, BorderProps right, Color innerBackgroundColor) throws IFException {
        if (top == null && bottom == null && left == null && right == null) {
            return;
        }
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            if (top != null) {
                addAttribute(atts, "top", top.toString());
            }
            if (bottom != null) {
                addAttribute(atts, "bottom", bottom.toString());
            }
            if (left != null) {
                addAttribute(atts, "left", left.toString());
            }
            if (right != null) {
                addAttribute(atts, "right", right.toString());
            }

            if (innerBackgroundColor != null) {
                addAttribute(atts, "inner-background-color",
                        ColorUtil.colorToString(innerBackgroundColor));
            }

            handler.element(EL_BORDER_RECT, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in drawBorderRect()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawLine(Point start, Point end, int width, Color color, RuleStyle style)
            throws IFException {
        try {
            addID();
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x1", Integer.toString(start.x));
            addAttribute(atts, "y1", Integer.toString(start.y));
            addAttribute(atts, "x2", Integer.toString(end.x));
            addAttribute(atts, "y2", Integer.toString(end.y));
            addAttribute(atts, "stroke-width", Integer.toString(width));
            addAttribute(atts, "color", ColorUtil.colorToString(color));
            addAttribute(atts, "style", style.getName());
            handler.element(EL_LINE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in drawLine()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing,
                         int[][] dp, String text) throws IFException {
        drawText(x, y, letterSpacing, wordSpacing, dp, text, false);
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int letterSpacing, int wordSpacing,
            int[][] dp, String text, boolean nextIsSpace) throws IFException {
        try {
            addID();
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(x));
            addAttribute(atts, "y", Integer.toString(y));
            if (nextIsSpace) {
                addAttribute(atts, "next-is-space", "true");
            }
            if (letterSpacing != 0) {
                addAttribute(atts, "letter-spacing", Integer.toString(letterSpacing));
            }
            if (wordSpacing != 0) {
                addAttribute(atts, "word-spacing", Integer.toString(wordSpacing));
            }
            if (dp != null) {
                if (IFUtil.isDPIdentity(dp)) {
                    // don't add dx or dp attribute
                } else if (IFUtil.isDPOnlyDX(dp)) {
                    // add dx attribute only
                    int[] dx = IFUtil.convertDPToDX(dp);
                    addAttribute(atts, "dx", IFUtil.toString(dx));
                } else {
                    // add dp attribute only
                    addAttribute(atts, "dp", XMLUtil.encodePositionAdjustments(dp));
                }
            }
            addStructureReference(atts);
            if (getContext().isHyphenated()) {
                addAttribute(atts, "hyphenated", "true");
            }
            handler.startElement(EL_TEXT, atts);
            char[] chars = text.toCharArray();
            handler.characters(chars, 0, chars.length);
            handler.endElement(EL_TEXT);
        } catch (SAXException e) {
            throw new IFException("SAX error in setFont()", e);
        }
    }

    /** {@inheritDoc} */
    public void setFont(String family, String style, Integer weight, String variant, Integer size,
            Color color) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            boolean changed;
            if (family != null) {
                changed = !family.equals(state.getFontFamily());
                if (changed) {
                    state.setFontFamily(family);
                    addAttribute(atts, "family", family);
                }
            }
            if (style != null) {
                changed = !style.equals(state.getFontStyle());
                if (changed) {
                    state.setFontStyle(style);
                    addAttribute(atts, "style", style);
                }
            }
            if (weight != null) {
                changed = (weight != state.getFontWeight());
                if (changed) {
                    state.setFontWeight(weight);
                    addAttribute(atts, "weight", weight.toString());
                }
            }
            if (variant != null) {
                changed = !variant.equals(state.getFontVariant());
                if (changed) {
                    state.setFontVariant(variant);
                    addAttribute(atts, "variant", variant);
                }
            }
            if (size != null) {
                changed = (size != state.getFontSize());
                if (changed) {
                    state.setFontSize(size);
                    addAttribute(atts, "size", size.toString());
                }
            }
            if (color != null) {
                changed = !org.apache.xmlgraphics.java2d.color.ColorUtil.isSameColor(
                        color, state.getTextColor());
                if (changed) {
                    state.setTextColor(color);
                    addAttribute(atts, "color", toString(color));
                }
            }
            if (atts.getLength() > 0) {
                handler.element(EL_FONT, atts);
            }
        } catch (SAXException e) {
            throw new IFException("SAX error in setFont()", e);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof XMLizable) {
            try {
                ((XMLizable)extension).toSAX(this.handler);
            } catch (SAXException e) {
                throw new IFException("SAX error while handling extension object", e);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Extension must implement XMLizable: "
                            + extension + " (" + extension.getClass().getName() + ")");
        }
    }

    /**
     * @return a new rendering context
     * @throws IllegalStateException unless overridden
     */
    protected RenderingContext createRenderingContext() throws IllegalStateException {
        throw new IllegalStateException("Should never be called!");
    }

    private void addAttribute(AttributesImpl atts,
            org.apache.xmlgraphics.util.QName attribute, String value) throws SAXException {
        handler.startPrefixMapping(attribute.getPrefix(), attribute.getNamespaceURI());
        XMLUtil.addAttribute(atts, attribute, value);
    }

    private void addAttribute(AttributesImpl atts, String localName, String value) {
        XMLUtil.addAttribute(atts, localName, value);
    }

    private void addStructureReference(AttributesImpl atts) {
        IFStructureTreeElement structureTreeElement
                = (IFStructureTreeElement) getContext().getStructureTreeElement();
        if (structureTreeElement != null) {
            addStructRefAttribute(atts, structureTreeElement.getId());
        }
    }

    private void addStructRefAttribute(AttributesImpl atts, String id) {
        atts.addAttribute(InternalElementMapping.URI,
                InternalElementMapping.STRUCT_REF,
                InternalElementMapping.STANDARD_PREFIX + ":" + InternalElementMapping.STRUCT_REF,
                XMLConstants.CDATA,
                id);
    }

    private void addID() throws SAXException {
        String id = getContext().getID();
        if (!currentID.equals(id)) {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "name", id);
            handler.startElement(EL_ID, atts);
            handler.endElement(EL_ID);
            currentID = id;
        }
    }

    private Map incompleteActions = new java.util.HashMap();
    private List completeActions = new java.util.LinkedList();

    private void noteAction(AbstractAction action) {
        if (action == null) {
            throw new NullPointerException("action must not be null");
        }
        if (!action.isComplete()) {
            assert action.hasID();
            incompleteActions.put(action.getID(), action);
        }
    }

    /** {@inheritDoc} */
    public void renderNamedDestination(NamedDestination destination) throws IFException {
        noteAction(destination.getAction());

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "name", "name", XMLConstants.CDATA, destination.getName());
        try {
            handler.startElement(DocumentNavigationExtensionConstants.NAMED_DESTINATION, atts);
            serializeXMLizable(destination.getAction());
            handler.endElement(DocumentNavigationExtensionConstants.NAMED_DESTINATION);
        } catch (SAXException e) {
            throw new IFException("SAX error serializing named destination", e);
        }
    }

    /** {@inheritDoc} */
    public void renderBookmarkTree(BookmarkTree tree) throws IFException {
        AttributesImpl atts = new AttributesImpl();
        try {
            handler.startElement(DocumentNavigationExtensionConstants.BOOKMARK_TREE, atts);
            for (Object o : tree.getBookmarks()) {
                Bookmark b = (Bookmark) o;
                if (b.getAction() != null) {
                    serializeBookmark(b);
                }
            }
            handler.endElement(DocumentNavigationExtensionConstants.BOOKMARK_TREE);
        } catch (SAXException e) {
            throw new IFException("SAX error serializing bookmark tree", e);
        }
    }

    private void serializeBookmark(Bookmark bookmark) throws SAXException, IFException {
        noteAction(bookmark.getAction());

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "title", "title", XMLUtil.CDATA, bookmark.getTitle());
        atts.addAttribute("", "starting-state", "starting-state",
                XMLUtil.CDATA, bookmark.isShown() ? "show" : "hide");
        handler.startElement(DocumentNavigationExtensionConstants.BOOKMARK, atts);
        serializeXMLizable(bookmark.getAction());
        for (Object o : bookmark.getChildBookmarks()) {
            Bookmark b = (Bookmark) o;
            if (b.getAction() != null) {
                serializeBookmark(b);
            }
        }
        handler.endElement(DocumentNavigationExtensionConstants.BOOKMARK);
    }

    /** {@inheritDoc} */
    public void renderLink(Link link) throws IFException {
        noteAction(link.getAction());

        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "rect", "rect",
                XMLConstants.CDATA, IFUtil.toString(link.getTargetRect()));
        if (getUserAgent().isAccessibilityEnabled()) {
            IFStructureTreeElement structureTreeElement =
                    (IFStructureTreeElement) link.getAction().getStructureTreeElement();
            if (structureTreeElement != null) {
                addStructRefAttribute(atts, structureTreeElement.getId());
            }
        }
        try {
            handler.startElement(DocumentNavigationExtensionConstants.LINK, atts);
            serializeXMLizable(link.getAction());
            handler.endElement(DocumentNavigationExtensionConstants.LINK);
        } catch (SAXException e) {
            throw new IFException("SAX error serializing link", e);
        }
    }

    /** {@inheritDoc} */
    public void addResolvedAction(AbstractAction action) throws IFException {
        assert action.isComplete();
        assert action.hasID();
        AbstractAction noted = (AbstractAction)incompleteActions.remove(action.getID());
        if (noted != null) {
            completeActions.add(action);
        } else {
            //ignore as it was already complete when it was first used.
        }
    }

    public int getPageIndex() {
        return -1;
    }

    private void commitNavigation() throws IFException {
        Iterator iter = this.completeActions.iterator();
        while (iter.hasNext()) {
            AbstractAction action = (AbstractAction)iter.next();
            iter.remove();
            serializeXMLizable(action);
        }
        assert this.completeActions.size() == 0;
    }

    private void finishDocumentNavigation() {
        assert this.incompleteActions.size() == 0 : "Still holding incomplete actions!";
    }

    private void serializeXMLizable(XMLizable object) throws IFException {
        try {
            object.toSAX(handler);
        } catch (SAXException e) {
            throw new IFException("SAX error serializing object", e);
        }
    }

    /** {@inheritDoc} */
    public boolean isBackgroundRequired(BorderProps bpsTop, BorderProps bpsBottom,
            BorderProps bpsLeft, BorderProps bpsRight) {
        return true;
    }
}
