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

package org.apache.fop.render.svg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.render.intermediate.AbstractXMLWritingIFPainter;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.util.ColorUtil;

/**
 * Abstract base class for SVG Painter implementations.
 */
public abstract class AbstractSVGPainter extends AbstractXMLWritingIFPainter
            implements SVGConstants {

    /** Holds the intermediate format state */
    protected IFState state;

    private static final int MODE_NORMAL = 0;
    private static final int MODE_TEXT = 1;

    private int mode = MODE_NORMAL;

    /** {@inheritDoc} */
    protected String getMainNamespace() {
        return NAMESPACE;
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        try {
            startElement("defs");
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        try {
            endElement("defs");
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageContent() throws IFException {
        this.state = IFState.create();
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        assert this.state.pop() == null;
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
            establish(MODE_NORMAL);
            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "transform", "transform", CDATA, transform);
            /*
            if (size != null) {
                atts.addAttribute("", "width", "width", CDATA, Integer.toString(size.width));
                atts.addAttribute("", "height", "height", CDATA, Integer.toString(size.height));
            }
            if (clip) {
                atts.addAttribute("", "clip", "clip", CDATA, "true");
            }*/
            startElement("g", atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startBox()", e);
        }
    }

    /** {@inheritDoc} */
    public void endBox() throws IFException {
        try {
            establish(MODE_NORMAL);
            endElement("g");
        } catch (SAXException e) {
            throw new IFException("SAX error in endBox()", e);
        }
    }

    /** {@inheritDoc} */
    public void startImage(Rectangle rect) throws IFException {
        //establish(MODE_NORMAL);
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void drawImage(String uri, Rectangle rect) throws IFException {
        //establish(MODE_NORMAL);
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void endImage() throws IFException {
        // TODO Auto-generated method stub

    }

    /** {@inheritDoc} */
    public void addTarget(String name, int x, int y) throws IFException {
        //establish(MODE_NORMAL);
        // TODO Auto-generated method stub

    }

    private static String toString(Paint paint) {
        //TODO Paint serialization: Fine-tune and extend!
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
            establish(MODE_NORMAL);
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
            establish(MODE_TEXT);
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
        if (family != null) {
            state.setFontFamily(family);
        }
        if (style != null) {
            state.setFontStyle(style);
        }
        if (weight != null) {
            state.setFontWeight(weight.intValue());
        }
        if (variant != null) {
            state.setFontVariant(variant);
        }
        if (size != null) {
            state.setFontSize(size.intValue());
        }
        if (color != null) {
            state.setTextColor(color);
        }
    }

    private void leaveTextMode() throws SAXException {
        assert this.mode == MODE_TEXT;
        endElement("g");
        this.mode = MODE_NORMAL;
    }

    private void establish(int newMode) throws SAXException {
        switch (newMode) {
        case MODE_TEXT:
            enterTextMode();
            break;
        default:
            if (this.mode == MODE_TEXT) {
                leaveTextMode();
            }
        }
    }

    private void enterTextMode() throws SAXException {
        if (state.isFontChanged() && this.mode == MODE_TEXT) {
            leaveTextMode();
        }
        if (this.mode != MODE_TEXT) {
            startTextGroup();
            this.mode = MODE_TEXT;
        }
    }

    private void startTextGroup() throws SAXException {
        AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", "font-family", "font-family",
                CDATA, state.getFontFamily());
        atts.addAttribute("", "font-style", "font-style",
                CDATA, state.getFontStyle());
        atts.addAttribute("", "font-weight", "font-weight",
                CDATA, Integer.toString(state.getFontWeight()));
        atts.addAttribute("", "font-variant", "font-variant",
                CDATA, state.getFontVariant());
        atts.addAttribute("", "font-size", "font-size",
                CDATA, Integer.toString(state.getFontSize()));
        atts.addAttribute("", "fill", "fill",
                CDATA, toString(state.getTextColor()));
        startElement("g", atts);
        state.resetFontChanged();
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof Metadata) {
            Metadata meta = (Metadata)extension;
            try {
                establish(MODE_NORMAL);
                startElement("metadata");
                meta.toSAX(this.handler);
                endElement("metadata");
            } catch (SAXException e) {
                throw new IFException("SAX error while handling extension object", e);
            }
        } else {
            throw new UnsupportedOperationException(
                    "Don't know how to handle extension object: " + extension);
        }
    }
}
