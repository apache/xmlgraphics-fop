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

import java.io.File;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

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
     * happens after setParams have been broadcast.
     *
     * @param downstreamHandler
     *            the handler passed in
     * @param xsltFile
     *            for transform
     * @throws FOPException
     *             for general errors
     */
    public TransformerNode(DefaultHandler downstreamHandler, File xsltFile) throws FOPException {
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory)transFact);
            StreamSource ss = new StreamSource(xsltFile);
            transformerHandler = saxTFactory.newTransformerHandler(ss);
            SAXResult saxResult = new SAXResult();
            saxResult.setHandler(downstreamHandler);
            transformerHandler.setResult(saxResult);
        } catch (TransformerConfigurationException t) {
            throw new FOPException(t);
        }
    }

    /**
     *
     * @param result
     *            of transform
     * @param xsltFile
     *            for transform
     * @throws FOPException
     *             for general errors
     */
    public TransformerNode(Result result, File xsltFile) throws FOPException {
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory)transFact);
            StreamSource ss = new StreamSource(xsltFile);
            transformerHandler = saxTFactory.newTransformerHandler(ss);
            transformerHandler.setResult(result);
        } catch (TransformerConfigurationException t) {
            throw new FOPException(t);
        }
    }

    /**
     * This is part of a two phase construction. Call this, then call
     * initResult.
     *
     * @param xsltFile
     *            for transform
     * @throws FOPException
     *             for general errors
     */
    public TransformerNode(Source xsltFile) throws FOPException {
        try {
            TransformerFactory transFact = TransformerFactory.newInstance();
            SAXTransformerFactory saxTFactory = ((SAXTransformerFactory)transFact);
            transformerHandler = saxTFactory.newTransformerHandler(xsltFile);
        } catch (TransformerConfigurationException t) {
            throw new FOPException(t);
        }
    }

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
        if (transformerHandler != null) {
            transformerHandler.setDocumentLocator(locator);
        }
    }

    /** {@inheritDoc} */
    public void startDocument() throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.startDocument();
        }
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.endDocument();
        }
    }

    /** {@inheritDoc} */
    public void processingInstruction(String target, String data) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.processingInstruction(target, data);
        }
    }

    /** {@inheritDoc} */
    public void startElement(String uri, String local, String raw, Attributes attrs)
            throws SAXException {
        AttributesImpl ai = new AttributesImpl(attrs);
        if (transformerHandler != null) {
            transformerHandler.startElement(uri, local, raw, ai);
        }
    }

    /** {@inheritDoc} */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.characters(ch, start, length);
        }
    }

    /** {@inheritDoc} */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.ignorableWhitespace(ch, start, length);
        }
    }

    /** {@inheritDoc} */
    public void endElement(String uri, String local, String raw) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.endElement(uri, local, raw);
        }
    }

    /** {@inheritDoc} */
    public void skippedEntity(String string) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.skippedEntity(string);
        }
    }

    /** {@inheritDoc} */
    public void startPrefixMapping(String string, String string1) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.startPrefixMapping(string, string1);
        }
    }

    /** {@inheritDoc} */
    public void endPrefixMapping(String string) throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.endPrefixMapping(string);
        }
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
        if (transformerHandler != null) {
            transformerHandler.startDTD(name, pid, lid);
        }
    }

    /**
     * End of DTD
     *
     * @throws SAXException
     *             - if parser fails
     */
    public void endDTD() throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.endDTD();
        }
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
        if (transformerHandler != null) {
            transformerHandler.startEntity(string);
        }
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
        if (transformerHandler != null) {
            transformerHandler.endEntity(string);
        }
    }

    /**
     * Start of CDATA section
     *
     * @throws SAXException
     *             - parser fails
     */
    public void startCDATA() throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.startCDATA();
        }
    }

    /**
     * endCDATA section
     *
     * @throws SAXException
     *             - if paser fails
     */
    public void endCDATA() throws SAXException {
        if (transformerHandler != null) {
            transformerHandler.endCDATA();
        }
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
        if (transformerHandler != null) {
            transformerHandler.comment(charArray, int1, int2);
        }
    }

    /******************** End of Lexical Handler ***********************/
}
