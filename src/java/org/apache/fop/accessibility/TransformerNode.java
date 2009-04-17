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

package org.apache.fop.accessibility;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;

/**
 * Used for accessibility to run required xslt transforms
 */
class TransformerNode extends DefaultHandler {

    private TransformerHandler transformerHandler;

    /**
     * This is part of a two phase construction. Call this, then call
     * initResult.
     *
     * @param xsltTemplates
     *            for transform
     * @throws FOPException
     *             for general errors
     */
    public TransformerNode(Templates xsltTemplates) throws FOPException {
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory)transFact);
            transformerHandler = saxTFactory.newTransformerHandler(xsltTemplates);
        } catch (TransformerConfigurationException t) {
            throw new FOPException(t);
        }
    }

    /**
     * Call this after calling constructor for xsltFile only above.
     *
     * @param result
     *            of transform
     */
    public void initResult(Result result) {
        transformerHandler.setResult(result);
    }

    /******************** start of ContentHandler ***************************/
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
    public void processingInstruction(String target, String data) throws SAXException {
        transformerHandler.processingInstruction(target, data);
    }

    /** {@inheritDoc} */
    public void startElement(String uri, String local, String raw, Attributes attrs)
            throws SAXException {
        AttributesImpl ai = new AttributesImpl(attrs);
        transformerHandler.startElement(uri, local, raw, ai);
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
    public void endElement(String uri, String local, String raw) throws SAXException {
        transformerHandler.endElement(uri, local, raw);
    }

    /** {@inheritDoc} */
    public void skippedEntity(String string) throws SAXException {
        transformerHandler.skippedEntity(string);
    }

    /** {@inheritDoc} */
    public void startPrefixMapping(String string, String string1) throws SAXException {
        transformerHandler.startPrefixMapping(string, string1);
    }

    /** {@inheritDoc} */
    public void endPrefixMapping(String string) throws SAXException {
        transformerHandler.endPrefixMapping(string);
    }

    /***************************** LexicalHandlerImpl **************************/
    /**
     * @param name
     *            - param1
     * @param pid
     *            - param2
     * @param lid
     *            - param3
     * @throws SAXException
     *             - if parser fails
     */
    public void startDTD(String name, String pid, String lid) throws SAXException {
        transformerHandler.startDTD(name, pid, lid);
    }

    /**
     * End of DTD
     *
     * @throws SAXException
     *             - if parser fails
     */
    public void endDTD() throws SAXException {
        transformerHandler.endDTD();
    }

    /**
     * startEnitity.
     *
     * @param string
     *            - param 1
     * @throws SAXException
     *             - if parser fails
     */
    public void startEntity(String string) throws SAXException {
        transformerHandler.startEntity(string);
    }

    /**
     * end Entity
     *
     * @param string
     *            - param 1
     * @throws SAXException
     *             - if paser fails
     */
    public void endEntity(String string) throws SAXException {
        transformerHandler.endEntity(string);
    }

    /**
     * Start of CDATA section
     *
     * @throws SAXException
     *             - parser fails
     */
    public void startCDATA() throws SAXException {
        transformerHandler.startCDATA();
    }

    /**
     * endCDATA section
     *
     * @throws SAXException
     *             - if paser fails
     */
    public void endCDATA() throws SAXException {
        transformerHandler.endCDATA();
    }

    /**
     *
     * @param charArray
     *            - the characters
     * @param int1
     *            - param 2
     * @param int2
     *            - param 3
     * @throws SAXException
     *             - if paser fails
     */
    public void comment(char[] charArray, int int1, int int2) throws SAXException {
        transformerHandler.comment(charArray, int1, int2);
    }

    /******************** End of Lexical Handler ***********************/
}
