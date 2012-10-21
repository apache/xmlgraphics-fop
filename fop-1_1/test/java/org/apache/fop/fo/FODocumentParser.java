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

package org.apache.fop.fo;

import java.io.File;
import java.io.InputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.EventListener;

/**
 * Parse an FO document and run the corresponding FO events through a given
 * {@link FOEventHandler} instance. That instance is created using the helper
 * {@link FOEventHandlerFactory}.
 *
 * <p>An instance of this class may not be used in multiple threads concurrently.<p>
 *
 * <p>An instance of this class may be used multiple times if the given
 * {@link FOEventHandler} implementation can be used multiple times.
 */
public final class FODocumentParser {

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private static final FopFactory FOP_FACTORY = FopFactory.newInstance(new File(".").toURI());

    private final FOEventHandlerFactory foEventHandlerFactory;

    private Fop fop;

    private Transformer transformer;

    private EventListener eventListener;

    /**
     * A factory to create custom instances of {@link FOEventHandler}.
     */
    public static interface FOEventHandlerFactory {

        /**
         * Creates a new {@code FOEventHandler} instance parameterized with the given FO user agent.
         *
         * @param foUserAgent an FO user agent
         * @return a new {@code FOEventHandler} instance
         */
        FOEventHandler newFOEventHandler(FOUserAgent foUserAgent);
    }

    private FODocumentParser(FOEventHandlerFactory foeEventHandlerFactory) {
        this.foEventHandlerFactory = foeEventHandlerFactory;
    }

    /**
     * Creates and returns a new FO document parser. The given factory will be used to
     * customize the handler that will receive FO events, using the
     * {@link FOUserAgent#setFOEventHandlerOverride(FOEventHandler)} method.
     *
     * @param foEventHandlerFactory the factory to be used to create {@code
     * FOEventHandler} instances
     * @return a new parser
     */
    public static FODocumentParser newInstance(FOEventHandlerFactory foEventHandlerFactory) {
        return new FODocumentParser(foEventHandlerFactory);
    }

    /**
     * Sets the event listener to be used if events occurs when parsing the document.
     *
     * @param eventListener an event listener
     */
    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Runs FOP on the given document.
     *
     * @param document XSL-FO document to parse
     * @throws FOPException if an error occurs when initializing FOP
     * @throws LoadingException if an error occurs when parsing the document
     */
    public void parse(InputStream document) throws FOPException, LoadingException {
        parse(document, createFOUserAgent());
    }

    /**
     * Runs FOP on the given document with the supplied {@link FOUserAgent}.
     *
     * @param document XSL-FO document to parse
     * @param foUserAgent The user agent
     * @throws FOPException if an error occurs when initializing FOP
     * @throws LoadingException if an error occurs when parsing the document
     */
    public void parse(InputStream document, FOUserAgent foUserAgent)
            throws FOPException, LoadingException {
        fop = FOP_FACTORY.newFop(foUserAgent);
        createTransformer();
        runTransformer(document);
    }

    /**
     * Creates a new {@link FOUserAgent}.
     * @return It
     */
    public FOUserAgent createFOUserAgent() {
        FOUserAgent userAgent = FOP_FACTORY.newFOUserAgent();
        FOEventHandler foEventHandler = foEventHandlerFactory.newFOEventHandler(userAgent);
        userAgent.setFOEventHandlerOverride(foEventHandler);
        if (eventListener != null) {
            userAgent.getEventBroadcaster().addEventListener(eventListener);
        }
        return userAgent;
    }

    private void createTransformer() {
        try {
            transformer = TRANSFORMER_FACTORY.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private void runTransformer(InputStream input) throws LoadingException, FOPException {
        Source source = new StreamSource(input);
        Result result = new SAXResult(fop.getDefaultHandler());
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            Throwable cause = e.getCause();
            throw new LoadingException(cause == null ? e : cause);
        }
    }
}
