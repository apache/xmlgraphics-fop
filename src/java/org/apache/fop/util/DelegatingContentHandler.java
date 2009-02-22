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

package org.apache.fop.util;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * SAX 2 Event Handler which simply delegates all calls to another ContentHandler. Subclasses can
 * do additional processing. This class is the passive counterpart to XMLFilterImpl.
 * <p>
 * The ContentHandler is the only instance that is required. All others (DTDHandler,
 * EntityResolver, LexicalHandler and ErrorHandler) may be ignored.
 */
public class DelegatingContentHandler
        implements EntityResolver, DTDHandler, ContentHandler, LexicalHandler, ErrorHandler {

    private ContentHandler delegate;
    private EntityResolver entityResolver;
    private DTDHandler dtdHandler;
    private LexicalHandler lexicalHandler;
    private ErrorHandler errorHandler;

    /**
     * Main constructor.
     */
    public DelegatingContentHandler() {
        //nop
    }

    /**
     * Convenience constructor. If the given handler also implements any of the EntityResolver,
     * DTDHandler, LexicalHandler or ErrorHandler interfaces, these are set automatically.
     * @param handler the content handler to delegate to
     */
    public DelegatingContentHandler(ContentHandler handler) {
        setDelegateContentHandler(handler);
        if (handler instanceof EntityResolver) {
            setDelegateEntityResolver((EntityResolver)handler);
        }
        if (handler instanceof DTDHandler) {
            setDelegateDTDHandler((DTDHandler)handler);
        }
        if (handler instanceof LexicalHandler) {
            setDelegateLexicalHandler((LexicalHandler)handler);
        }
        if (handler instanceof ErrorHandler) {
            setDelegateErrorHandler((ErrorHandler)handler);
        }
    }

    /**
     * @return the delegate that all ContentHandler events are forwarded to
     */
    public ContentHandler getDelegateContentHandler() {
        return this.delegate;
    }

    /**
     * Sets the delegate ContentHandler that all events are forwarded to.
     * @param handler the delegate instance
     */
    public void setDelegateContentHandler(ContentHandler handler) {
        this.delegate = handler;
    }

    /**
     * Sets the delegate EntityResolver.
     * @param resolver the delegate instance
     */
    public void setDelegateEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    /**
     * Sets the delegate DTDHandler.
     * @param handler the delegate instance
     */
    public void setDelegateDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    /**
     * Sets the delegate LexicalHandler.
     * @param handler the delegate instance
     */
    public void setDelegateLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
     * Sets the delegate ErrorHandler.
     * @param handler the delegate instance
     */
    public void setDelegateErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    // ==== EntityResolver

    /** {@inheritDoc} */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (entityResolver != null) {
            return entityResolver.resolveEntity(publicId, systemId);
        } else {
            return null;
        }
    }

    // ==== DTDHandler

    /** {@inheritDoc} */
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        if (dtdHandler != null) {
            dtdHandler.notationDecl(name, publicId, systemId);
        }
    }

    /** {@inheritDoc} */
    public void unparsedEntityDecl(String name, String publicId, String systemId,
            String notationName) throws SAXException {
        if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    // ==== ContentHandler

    /** {@inheritDoc} */
    public void setDocumentLocator(Locator locator) {
        delegate.setDocumentLocator(locator);
    }

    /** {@inheritDoc} */
    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        delegate.endDocument();
    }

    /** {@inheritDoc} */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        delegate.startPrefixMapping(prefix, uri);
    }

    /** {@inheritDoc} */
    public void endPrefixMapping(String prefix) throws SAXException {
        delegate.endPrefixMapping(prefix);
    }

    /** {@inheritDoc} */
    public void startElement(String uri, String localName, String qName,
                Attributes atts) throws SAXException {
        delegate.startElement(uri, localName, qName, atts);
    }

    /** {@inheritDoc} */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        delegate.endElement(uri, localName, qName);
    }

    /** {@inheritDoc} */
    public void characters(char[] ch, int start, int length) throws SAXException {
        delegate.characters(ch, start, length);
    }

    /** {@inheritDoc} */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        delegate.ignorableWhitespace(ch, start, length);
    }

    /** {@inheritDoc} */
    public void processingInstruction(String target, String data) throws SAXException {
        delegate.processingInstruction(target, data);
    }

    /** {@inheritDoc} */
    public void skippedEntity(String name) throws SAXException {
        delegate.skippedEntity(name);
    }

    // ==== LexicalHandler

    /** {@inheritDoc} */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId, systemId);
        }

    }

    /** {@inheritDoc} */
    public void endDTD() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    /** {@inheritDoc} */
    public void startEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(name);
        }
    }

    /** {@inheritDoc} */
    public void endEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(name);
        }
    }

    /** {@inheritDoc} */
    public void startCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    /** {@inheritDoc} */
    public void endCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    /** {@inheritDoc} */
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.comment(ch, start, length);
        }
    }

    // ==== ErrorHandler

    /** {@inheritDoc} */
    public void warning(SAXParseException exception) throws SAXException {
        if (errorHandler != null) {
            errorHandler.warning(exception);
        }
    }

    /** {@inheritDoc} */
    public void error(SAXParseException exception) throws SAXException {
        if (errorHandler != null) {
            errorHandler.error(exception);
        }
    }

    /** {@inheritDoc} */
    public void fatalError(SAXParseException exception) throws SAXException {
        if (errorHandler != null) {
            errorHandler.fatalError(exception);
        }
    }

}
