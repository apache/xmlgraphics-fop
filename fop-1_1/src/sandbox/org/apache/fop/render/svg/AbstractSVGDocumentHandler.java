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

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.xmp.Metadata;

import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.AbstractXMLWritingIFDocumentHandler;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFState;
import org.apache.fop.render.java2d.Java2DUtil;

/**
 * Abstract base class for SVG Painter implementations.
 */
public abstract class AbstractSVGDocumentHandler extends AbstractXMLWritingIFDocumentHandler
            implements SVGConstants {

    /** logging instance */
    private static Log log = LogFactory.getLog(AbstractSVGDocumentHandler.class);

    /** Font configuration */
    protected FontInfo fontInfo;

    /** Holds the intermediate format state */
    protected IFState state;

    private static final int MODE_NORMAL = 0;
    private static final int MODE_TEXT = 1;

    private int mode = MODE_NORMAL;

    public AbstractSVGDocumentHandler(IFContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    protected String getMainNamespace() {
        return NAMESPACE;
    }

    /** {@inheritDoc} */
    public FontInfo getFontInfo() {
        return this.fontInfo;
    }

    /** {@inheritDoc} */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
    }

    /** {@inheritDoc} */
    public void setDefaultFontInfo(FontInfo fontInfo) {
        FontInfo fi = Java2DUtil.buildDefaultJava2DBasedFontInfo(fontInfo, getUserAgent());
        setFontInfo(fi);
    }

    /** {@inheritDoc} */
    public IFDocumentHandlerConfigurator getConfigurator() {
        return null; //No configurator, yet.
    }

    /** {@inheritDoc} */
    public void startDocumentHeader() throws IFException {
        try {
            handler.startElement("defs");
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        try {
            handler.endElement("defs");
        } catch (SAXException e) {
            throw new IFException("SAX error in startDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void handleExtensionObject(Object extension) throws IFException {
        if (extension instanceof Metadata) {
            Metadata meta = (Metadata)extension;
            try {
                handler.startElement("metadata");
                meta.toSAX(this.handler);
                handler.endElement("metadata");
            } catch (SAXException e) {
                throw new IFException("SAX error while handling extension object", e);
            }
        } else {
            log.debug("Don't know how to handle extension object. Ignoring: "
                    + extension + " (" + extension.getClass().getName() + ")");
        }
    }
}
