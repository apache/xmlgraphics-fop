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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;

/**
 * An extension of TransformerNode used to run 2nd transform after completion of first
 */
class TransformerNodeEndProcessing extends TransformerNode {

    private final ByteArrayOutputStream enrichedFOBuffer = new ByteArrayOutputStream();
    private DefaultHandler delegateHandler = null;
    private final FOUserAgent userAgent;

    /**
     * Do a transform, but perform special processing at the end for the access
     * stuff.
     *
     * @param xsltTemplates Transform to do.
     * @param fopHandler Used in the end processing
     * @param userAgent the userAgent
     * @throws FOPException
     *                      if transform fails
     */
    public TransformerNodeEndProcessing(Templates xsltTemplates, DefaultHandler fopHandler,
            FOUserAgent userAgent) throws FOPException {
        super(xsltTemplates);
        delegateHandler = fopHandler;
        this.userAgent = userAgent;
        Result res1 = new StreamResult(enrichedFOBuffer);
        super.initResult(res1);
    }

    /** {@inheritDoc} */
    public void endDocument() throws SAXException {
        super.endDocument();
        // do the second transform to struct
        try {
            //TODO this must be optimized, no buffering (ex. SAX-based tee-proxy)
            byte[] enrichedFO = enrichedFOBuffer.toByteArray();
            Transformer transformer = AccessibilityUtil.getReduceFOTreeTemplates().newTransformer();
            Source src = new StreamSource(new ByteArrayInputStream(enrichedFO));
            DOMResult res = new DOMResult();
            transformer.transform(src, res);
            userAgent.setStructureTree(new StructureTree(res.getNode()));

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            saxParserFactory.setValidating(false);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            InputStream in = new ByteArrayInputStream(enrichedFO);
            saxParser.parse(in, delegateHandler);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new SAXException(e);
        }

    }

}
