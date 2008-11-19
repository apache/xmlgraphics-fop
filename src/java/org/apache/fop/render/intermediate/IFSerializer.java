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
import java.util.Map;

import org.w3c.dom.Document;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.QName;
import org.apache.xmlgraphics.util.XMLizable;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.RenderingContext;
import org.apache.fop.traits.BorderProps;
import org.apache.fop.traits.RuleStyle;
import org.apache.fop.util.ColorUtil;
import org.apache.fop.util.DOM2SAX;
import org.apache.fop.util.XMLUtil;

/**
 * IFPainter implementation that serializes the intermediate format to XML.
 */
public class IFSerializer extends AbstractXMLWritingIFDocumentHandler implements IFConstants, IFPainter {

    private IFDocumentHandler mimicHandler;

    /**
     * Default constructor.
     */
    public IFSerializer() {
    }

    /** {@inheritDoc} */
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
            return new IFSerializerConfiguration(getUserAgent());
        }
    }

    public void mimicDocumentHandler(IFDocumentHandler targetHandler) {
        this.mimicHandler = targetHandler;
    }

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
        //nop, not used
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        //nop, not used
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        try {
            handler.startDocument();
            handler.startPrefixMapping("", NAMESPACE);
            handler.startPrefixMapping(XLINK_PREFIX, XLINK_NAMESPACE);
            handler.startElement(EL_DOCUMENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        try {
            handler.startElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        try {
            handler.endElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void startDocumentTrailer() throws IFException {
        try {
            handler.startElement(EL_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentTrailer()", e);
        }
    }

    /** {@inheritDoc} */
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
            handler.startElement(EL_PAGE_SEQUENCE, atts);
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
    public void startPage(int index, String name, String pageMasterName, Dimension size) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "index", Integer.toString(index));
            addAttribute(atts, "name", name);
            addAttribute(atts, "page-master-name", name);
            addAttribute(atts, "width", Integer.toString(size.width));
            addAttribute(atts, "height", Integer.toString(size.height));
            handler.startElement(EL_PAGE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPage()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        try {
            handler.startElement(EL_PAGE_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageHeader()", e);
        }
    }

    /** {@inheritDoc} */
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
            return this;
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            handler.endElement(EL_PAGE_CONTENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        try {
            handler.startElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        try {
            handler.endElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            handler.endElement(EL_PAGE);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPage()", e);
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
    public void startGroup(AffineTransform[] transforms) throws IFException {
        startGroup(IFUtil.toString(transforms));
    }

    /** {@inheritDoc} */
    public void startGroup(AffineTransform transform) throws IFException {
        startGroup(IFUtil.toString(transform));
    }

    private void startGroup(String transform) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            if (transform != null && transform.length() > 0) {
                addAttribute(atts, "transform", transform);
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
    public void drawImage(String uri, Rectangle rect, Map foreignAttributes) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, XLINK_HREF, uri);
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            if (foreignAttributes != null) {
                Iterator iter = foreignAttributes.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    addAttribute(atts, (QName)entry.getKey(), entry.getValue().toString());
                }
            }
            handler.element(EL_IMAGE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startGroup()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawImage(Document doc, Rectangle rect, Map foreignAttributes) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            if (foreignAttributes != null) {
                Iterator iter = foreignAttributes.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    addAttribute(atts, (QName)entry.getKey(), entry.getValue().toString());
                }
            }
            handler.startElement(EL_IMAGE, atts);
            new DOM2SAX(handler).writeDocument(doc, true);
            handler.endElement(EL_IMAGE);
        } catch (SAXException e) {
            throw new IFException("SAX error in startGroup()", e);
        }
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

    /** {@inheritDoc} */
    public void drawBorderRect(Rectangle rect, BorderProps before, BorderProps after,
            BorderProps start, BorderProps end) throws IFException {
        if (before == null && after == null && start == null && end == null) {
            return;
        }
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(rect.x));
            addAttribute(atts, "y", Integer.toString(rect.y));
            addAttribute(atts, "width", Integer.toString(rect.width));
            addAttribute(atts, "height", Integer.toString(rect.height));
            if (before != null) {
                addAttribute(atts, "before", before.toString());
            }
            if (after != null) {
                addAttribute(atts, "after", after.toString());
            }
            if (start != null) {
                addAttribute(atts, "start", start.toString());
            }
            if (end != null) {
                addAttribute(atts, "end", end.toString());
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
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x1", Integer.toString(start.x));
            addAttribute(atts, "y1", Integer.toString(start.y));
            addAttribute(atts, "x2", Integer.toString(end.x));
            addAttribute(atts, "y2", Integer.toString(end.y));
            addAttribute(atts, "stroke-width", Integer.toString(width));
            addAttribute(atts, "color", Integer.toString(width));
            addAttribute(atts, "style", style.getName());
            handler.element(EL_LINE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in drawLine()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            addAttribute(atts, "x", Integer.toString(x));
            addAttribute(atts, "y", Integer.toString(y));
            if (dx != null) {
                addAttribute(atts, "dx", IFUtil.toString(dx));
            }
            if (dy != null) {
                addAttribute(atts, "dy", IFUtil.toString(dy));
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
            if (family != null) {
                addAttribute(atts, "family", family);
            }
            if (style != null) {
                addAttribute(atts, "style", style);
            }
            if (weight != null) {
                addAttribute(atts, "weight", weight.toString());
            }
            if (variant != null) {
                addAttribute(atts, "variant", variant);
            }
            if (size != null) {
                addAttribute(atts, "size", size.toString());
            }
            if (color != null) {
                addAttribute(atts, "color", toString(color));
            }
            handler.element(EL_FONT, atts);
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
                    "Don't know how to handle extension object: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }

    /** {@inheritDoc} */
    protected RenderingContext createRenderingContext() {
        throw new IllegalStateException("Should never be called!");
    }

    private void addAttribute(AttributesImpl atts,
            org.apache.xmlgraphics.util.QName attribute, String value) {
        XMLUtil.addAttribute(atts, attribute, value);
    }

    private void addAttribute(AttributesImpl atts, String localName, String value) {
        XMLUtil.addAttribute(atts, localName, value);
    }

}
