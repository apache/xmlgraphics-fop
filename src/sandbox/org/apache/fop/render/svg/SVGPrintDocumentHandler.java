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

import java.awt.Dimension;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.render.intermediate.IFConstants;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.util.XMLUtil;

/**
 * {@link IFDocumentHandler} implementation that writes SVG Print.
 */
public class SVGPrintDocumentHandler extends AbstractSVGDocumentHandler {

    /**
     * Default constructor.
     */
    public SVGPrintDocumentHandler(IFContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return false;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_SVG_PRINT;
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        super.startDocument();
        try {
            handler.startDocument();
            handler.startPrefixMapping("", NAMESPACE);
            handler.startPrefixMapping(XLINK_PREFIX, XLINK_NAMESPACE);
            handler.startPrefixMapping("if", IFConstants.NAMESPACE);
            AttributesImpl atts = new AttributesImpl();
            XMLUtil.addAttribute(atts, "version", "1.2"); //SVG Print is SVG 1.2
            handler.startElement("svg", atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocument()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        try {
            handler.endElement("svg");
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
            handler.startElement("pageSet", atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        try {
            handler.endElement("pageSet");
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageSequence()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        try {
            AttributesImpl atts = new AttributesImpl();
            /*
            XMLUtil.addAttribute(atts, "index", Integer.toString(index));
            XMLUtil.addAttribute(atts, "name", name);
            */
            //NOTE: SVG Print doesn't support individual page sizes for each page
            atts.addAttribute(IFConstants.NAMESPACE, "width", "if:width",
                    CDATA, Integer.toString(size.width));
            atts.addAttribute(IFConstants.NAMESPACE, "height", "if:height",
                    CDATA, Integer.toString(size.height));
            atts.addAttribute(IFConstants.NAMESPACE, "viewBox", "if:viewBox", CDATA,
                    "0 0 " + Integer.toString(size.width) + " " + Integer.toString(size.height));
            handler.startElement("page", atts);
        } catch (SAXException e) {
            throw new IFException("SAX error in startPage()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPageHeader() throws IFException {
    }

    /** {@inheritDoc} */
    public IFPainter startPageContent() throws IFException {
        try {
            handler.startElement("g");
        } catch (SAXException e) {
            throw new IFException("SAX error in startPageContent()", e);
        }
        return new SVGPainter(this, handler);
    }

    /** {@inheritDoc} */
    public void endPageContent() throws IFException {
        try {
            handler.endElement("g");
        } catch (SAXException e) {
            throw new IFException("SAX error in endPageContent()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageTrailer() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPageTrailer() throws IFException {
    }

    /** {@inheritDoc} */
    public void endPage() throws IFException {
        try {
            handler.endElement("page");
        } catch (SAXException e) {
            throw new IFException("SAX error in endPage()", e);
        }
    }

}
