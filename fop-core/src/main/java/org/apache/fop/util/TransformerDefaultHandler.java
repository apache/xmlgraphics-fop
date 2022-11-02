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

import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A DefaultHandler implementation that delegates all the method calls to a
 * {@link TransformerHandler} instance.
 */
public class TransformerDefaultHandler extends DefaultHandler2 {

    private TransformerHandler transformerHandler;

    /**
     * Creates a new instance delegating to the given TransformerHandler object.
     *
     * @param transformerHandler the object to which all the method calls will
     * be delegated
     */
    public TransformerDefaultHandler(TransformerHandler transformerHandler) {
        this.transformerHandler = transformerHandler;
    }

    /**
     * Returns the delegate TransformerHandler instance.
     *
     * @return the object to which all method calls are delegated
     */
    public TransformerHandler getTransformerHandler() {
        return transformerHandler;
    }

    /** {@inheritDoc} */
    public void setDocumentLocator(Locator locator) {
        transformerHandler.setDocumentLocator(locator);
    }

    /** {@inheritDoc} */
    public void startDocument() throws SAXException {
        transformerHandler.startDocument();
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        transformerHandler.endDocument();
    }

    /** {@inheritDoc} */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        transformerHandler.startPrefixMapping(prefix, uri);
    }

    /** {@inheritDoc} */
    public void endPrefixMapping(String string) throws SAXException {
        transformerHandler.endPrefixMapping(string);
    }

    /** {@inheritDoc} */
    public void startElement(String uri, String localName, String qName, Attributes attrs)
            throws SAXException {
        AttributesImpl ai = new AttributesImpl(attrs);
        transformerHandler.startElement(uri, localName, qName, ai);
    }

    /** {@inheritDoc} */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        transformerHandler.endElement(uri, localName, qName);
    }

    /** {@inheritDoc} */
    public void characters(char[] ch, int start, int length) throws SAXException {
        transformerHandler.characters(ch, start, length);
    }

    /** {@inheritDoc} */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        transformerHandler.ignorableWhitespace(ch, start, length);
    }

    /** {@inheritDoc} */
    public void processingInstruction(String target, String data) throws SAXException {
        transformerHandler.processingInstruction(target, data);
    }

    /** {@inheritDoc} */
    public void skippedEntity(String name) throws SAXException {
        transformerHandler.skippedEntity(name);
    }

    /** {@inheritDoc} */
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        transformerHandler.notationDecl(name, publicId, systemId);
    }

    /** {@inheritDoc} */
    public void unparsedEntityDecl(String name, String publicId, String systemId,
            String notationName) throws SAXException {
        transformerHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }

    /** {@inheritDoc} */
    public void startDTD(String name, String pid, String lid) throws SAXException {
        transformerHandler.startDTD(name, pid, lid);
    }

    /** {@inheritDoc} */
    public void endDTD() throws SAXException {
        transformerHandler.endDTD();
    }

    /** {@inheritDoc} */
    public void startEntity(String name) throws SAXException {
        transformerHandler.startEntity(name);
    }

    /** {@inheritDoc} */
    public void endEntity(String name) throws SAXException {
        transformerHandler.endEntity(name);
    }

    /** {@inheritDoc} */
    public void startCDATA() throws SAXException {
        transformerHandler.startCDATA();
    }

    /** {@inheritDoc} */
    public void endCDATA() throws SAXException {
        transformerHandler.endCDATA();
    }

    /** {@inheritDoc} */
    public void comment(char[] charArray, int start, int length) throws SAXException {
        transformerHandler.comment(charArray, start, length);
    }

}
