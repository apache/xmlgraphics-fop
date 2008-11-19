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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.commons.io.IOUtils;

import org.apache.fop.render.bitmap.MultiFileRenderingUtil;
import org.apache.fop.render.intermediate.DelegatingFragmentContentHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.util.GenerationHelperContentHandler;
import org.apache.fop.util.XMLUtil;

/**
 * {@code IFDocumentHandler} implementation that writes SVG 1.1.
 */
public class SVGDocumentHandler extends AbstractSVGDocumentHandler {

    /** Helper class for generating multiple files */
    private MultiFileRenderingUtil multiFileUtil;

    private StreamResult firstStream;
    private StreamResult currentStream;

    private Document reusedParts;

    /**
     * Default constructor.
     */
    public SVGDocumentHandler() {
        //nop
    }

    /** {@inheritDoc} */
    public boolean supportsPagesOutOfOrder() {
        return true;
    }

    /** {@inheritDoc} */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /** {@inheritDoc} */
    public void setResult(Result result) throws IFException {
        if (result instanceof StreamResult) {
            multiFileUtil = new MultiFileRenderingUtil(FILE_EXTENSION_SVG,
                    getUserAgent().getOutputFile());
            this.firstStream = (StreamResult)result;
        } else {
            throw new UnsupportedOperationException("Result is not supported: " + result);
        }
    }

    /** {@inheritDoc} */
    public void startDocument() throws IFException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setValidating(false);
        try {
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            this.reusedParts = builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IFException("Error while setting up a DOM for SVG generation", e);
        }

        try {
            TransformerHandler toDOMHandler = tFactory.newTransformerHandler();
            toDOMHandler.setResult(new DOMResult(this.reusedParts));
            this.handler = decorate(toDOMHandler);
            this.handler.startDocument();
        } catch (SAXException se) {
            throw new IFException("SAX error in startDocument()", se);
        } catch (TransformerConfigurationException e) {
            throw new IFException(
                    "Error while setting up a TransformerHandler for SVG generation", e);
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endDocumentHeader() throws IFException {
        super.endDocumentHeader();
        try {
            //Stop recording parts reused for each page
            this.handler.endDocument();
            this.handler = null;
        } catch (SAXException e) {
            throw new IFException("SAX error in endDocumentHeader()", e);
        }
    }

    /** {@inheritDoc} */
    public void startPageSequence(String id) throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void endPageSequence() throws IFException {
        //nop
    }

    /** {@inheritDoc} */
    public void startPage(int index, String name, String pageMasterName, Dimension size)
                throws IFException {
        OutputStream out;
        try {
            if (index == 0) {
                out = null;
            } else {
                out = this.multiFileUtil.createOutputStream(index);
                if (out == null) {
                    //TODO Convert to event
                    throw new IFException(
                            "No filename information available. Stopping after first page.", null);
                }
            }
        } catch (IOException ioe) {
            throw new IFException("I/O exception while setting up output file", ioe);
        }
        if (out == null) {
            this.handler = decorate(createContentHandler(this.firstStream));
        } else {
            this.currentStream = new StreamResult(out);
            this.handler = decorate(createContentHandler(this.currentStream));
        }
        if (false) {
            final ContentHandler originalHandler = this.handler;
            this.handler = decorate((ContentHandler)Proxy.newProxyInstance(
                    ContentHandler.class.getClassLoader(),
                    new Class[] {ContentHandler.class},
                    new InvocationHandler() {
                        public Object invoke(Object proxy, Method method, Object[] args)
                                throws Throwable {
                            String methodName = method.getName();
                            System.out.println(methodName + ":");
                            if (args != null) {
                                for (int i = 0; i < args.length; i++) {
                                    System.out.println("  " + args[i]);
                                }
                            }
                            return method.invoke(originalHandler, args);
                        }
                    }));
        }
        try {
            handler.startDocument();
            handler.startPrefixMapping("", NAMESPACE);
            handler.startPrefixMapping(XLINK_PREFIX, XLINK_NAMESPACE);
            AttributesImpl atts = new AttributesImpl();
            XMLUtil.addAttribute(atts, "version", "1.1"); //SVG 1.1
            /*
            XMLUtil.addAttribute(atts, "index", Integer.toString(index));
            XMLUtil.addAttribute(atts, "name", name);
            */
            XMLUtil.addAttribute(atts, "width", Float.toString(size.width / 1000f) + "pt");
            XMLUtil.addAttribute(atts, "height", Float.toString(size.height / 1000f) + "pt");
            XMLUtil.addAttribute(atts, "viewBox",
                    "0 0 " + Integer.toString(size.width) + " " + Integer.toString(size.height));
            handler.startElement("svg", atts);

            try {
                Transformer transformer = tFactory.newTransformer();
                Source src = new DOMSource(this.reusedParts.getDocumentElement());
                Result res = new SAXResult(new DelegatingFragmentContentHandler(this.handler));
                transformer.transform(src, res);
            } catch (TransformerConfigurationException tce) {
                throw new IFException("Error setting up a Transformer", tce);
            } catch (TransformerException te) {
                if (te.getCause() instanceof SAXException) {
                    throw (SAXException)te.getCause();
                } else {
                    throw new IFException("Error while serializing reused parts", te);
                }
            }
        } catch (SAXException e) {
            throw new IFException("SAX error in startPage()", e);
        }
    }

    private GenerationHelperContentHandler decorate(ContentHandler contentHandler) {
        return new GenerationHelperContentHandler(contentHandler, getMainNamespace());
    }

    private void closeCurrentStream() {
        if (this.currentStream != null) {
            IOUtils.closeQuietly(currentStream.getOutputStream());
            currentStream.setOutputStream(null);
            IOUtils.closeQuietly(currentStream.getWriter());
            currentStream.setWriter(null);
            this.currentStream = null;
        }
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
    public void endPage() throws IFException {
        try {
            handler.endElement("svg");
            this.handler.endDocument();
        } catch (SAXException e) {
            throw new IFException("SAX error in endPage()", e);
        }
        closeCurrentStream();
    }

}
