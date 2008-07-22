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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.util.XMLizable;

import org.apache.fop.util.ColorUtil;

/**
 * IFPainter implementation that serializes the intermediate format to XML.
 */
public class IFSerializer extends AbstractXMLWritingIFPainter implements IFConstants {

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
    public void startDocument() throws IFException {
        try {
            handler.startDocument();
            handler.startPrefixMapping("", NAMESPACE);
            handler.startPrefixMapping(XLINK_PREFIX, XLINK_NAMESPACE);
            startElement(EL_DOCUMENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        try {
            startElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        try {
            endElement(EL_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            endElement(EL_DOCUMENT);
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
                atts.addAttribute(XML_NAMESPACE, "id", "xml:id", CDATA, id);
            }
            startElement(EL_PAGE_SEQUENCE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        try {
            endElement(EL_PAGE_SEQUENCE);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, Dimension size) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "index", "index", CDATA, Integer.toString(index));
            atts.addAttribute("", "name", "name", CDATA, name);
            atts.addAttribute("", "width", "width", CDATA, Integer.toString(size.width));
            atts.addAttribute("", "height", "height", CDATA, Integer.toString(size.height));
            startElement(EL_PAGE, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPage()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
        try {
            startElement(EL_PAGE_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
        try {
            endElement(EL_PAGE_HEADER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageContent() throws IFException {
        try {
            startElement(EL_PAGE_CONTENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            endElement(EL_PAGE_CONTENT);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
        try {
            startElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
        try {
            endElement(EL_PAGE_TRAILER);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageTrailer()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            endElement(EL_PAGE);
        } catch (SAXException e) {
            throw new IFException("SAX error in endPage()", e);
        }
    }

    /** {@inheritDoc} */
    public void startBox(AffineTransform transform, Dimension size, boolean clip)
            throws IFException {
        StringBuffer sb = new StringBuffer();
        toString(transform, sb);
        startBox(sb.toString(), size, clip);
    }

    /** {@inheritDoc} */
    public void startBox(AffineTransform[] transforms, Dimension size, boolean clip)
            throws IFException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0, c = transforms.length; i < c; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            toString(transforms[i], sb);
        }
        startBox(sb.toString(), size, clip);
    }

    private void startBox(String transform, Dimension size, boolean clip) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "transform", "transform", CDATA, transform);
            if (size != null) {
                atts.addAttribute("", "width", "width", CDATA, Integer.toString(size.width));
                atts.addAttribute("", "height", "height", CDATA, Integer.toString(size.height));
            }
            if (clip) {
                atts.addAttribute("", "clip", "clip", CDATA, "true");
            }
            startElement(EL_BOX, atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startBox()", e);
        }
    }

    /** {@inheritDoc} */
    public void endBox() throws IFException {
        try {
            endElement(EL_BOX);
        } catch (SAXException e) {
            throw new IFException("SAX error in endBox()", e);
        }
    }

    /** {@inheritDoc} */
    public void startImage(Rectangle rect) throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void endImage() throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void addTarget(String name, int x, int y) throws IFException {
        // TODO Auto-generated method stub

    }

    private static String toString(Paint paint) {
        if (paint instanceof Color) {
            return ColorUtil.colorToString((Color)paint);
        } else {
            throw new UnsupportedOperationException("Paint not supported: " + paint);
        }
    }

    /** {@inheritDoc} */
    public void drawRect(Rectangle rect, Paint fill, Color stroke) throws IFException {
        if (fill == null && stroke == null) {
            return;
        }
        try {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "x", "x", CDATA, Integer.toString(rect.x));
            atts.addAttribute("", "y", "y", CDATA, Integer.toString(rect.y));
            atts.addAttribute("", "width", "width", CDATA, Integer.toString(rect.width));
            atts.addAttribute("", "height", "height", CDATA, Integer.toString(rect.height));
            if (fill != null) {
                atts.addAttribute("", "fill", "fill", CDATA, toString(fill));
            }
            if (stroke != null) {
                atts.addAttribute("", "stroke", "sroke", CDATA, toString(stroke));
            }
            element("rect", atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in drawRect()", e);
        }
    }

    /** {@inheritDoc} */
    public void drawText(int x, int y, int[] dx, int[] dy, String text) throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "x", "x", CDATA, Integer.toString(x));
            atts.addAttribute("", "y", "y", CDATA, Integer.toString(y));
            if (dx != null) {
                atts.addAttribute("", "dx", "dx", CDATA, toString(dx));
            }
            if (dy != null) {
                atts.addAttribute("", "dy", "dy", CDATA, toString(dy));
            }
            startElement("text", atts);
            char[] chars = text.toCharArray();
            handler.characters(chars, 0, chars.length);
            endElement("text");
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
                atts.addAttribute("", "family", "family", CDATA, family);
            }
            if (style != null) {
                atts.addAttribute("", "style", "style", CDATA, style);
            }
            if (weight != null) {
                atts.addAttribute("", "weight", "weight", CDATA, weight.toString());
            }
            if (variant != null) {
                atts.addAttribute("", "variant", "variant", CDATA, variant);
            }
            if (size != null) {
                atts.addAttribute("", "size", "size", CDATA, size.toString());
            }
            if (color != null) {
                atts.addAttribute("", "color", "color", CDATA, toString(color));
            }
            element("font", atts);
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
                    "Don't know how to handle extension object: " + extension);
        }
    }

}
